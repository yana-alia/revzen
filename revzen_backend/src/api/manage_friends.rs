//! The api method for creating, cancelling, rejecting and accepting friend requests
//!
//! ## Post Request Fields:
//!
//! | Key            | Type    | Value                                                                    |
//! |----------------|---------|--------------------------------------------------------------------------|
//! | user_id        | integer | The google provided subject id.                                          |
//! | client_version | integer | The api version being used.                                              |
//! | friend_code    | integer | The friendcode of the user being requested/accepted/rejected/unfriended. |
//! | action         | string  | ("request"/"reject"/"accept"/"unfriend") The action to perform.          |
//!
//! ## Response:
//!
//! | Status          | Action   | Result                 | Meaning                                                 |
//! |-----------------|----------|------------------------|---------------------------------------------------------|
//! | Friends         | Request  | 403 - Forbidden        | Users Already Friends                                   |
//! | Friends         | Accept   | 409 - Conflict         | No friend request to accept.                            |
//! | Friends         | Reject   | 409 - Conflict         | No friend request to reject.                            |
//! | Friends         | Unfriend | 200 - Ok               | Users are no longer friends                             |
//! | Other Requested | Request  | 200 - Ok               | Users are now friends                                   |
//! | Other Requested | Accept   | 200 - Ok               | Users are now friends                                   |
//! | Other Requested | Reject   | 200 - Ok               | User request rejected                                   |
//! | Other Requested | Unfriend | 409 - Conflict         | No friendship to remove                                 |
//! | User Requested  | Request  | 403 - Forbidden        | Already have a request to the user                      |
//! | User Requested  | Accept   | 409 - Conflict         | No friend request to accept, waiting on the other user. |
//! | User Requested  | Reject   | 200 - Ok               | We have removed our friend request.                     |
//! | User Requested  | Unfriend | 409 - Conflict         | No friendship to remove.                                |
//! | None            | Request  | 200 - Ok               | Create an entry for the friend request                  |
//! | None            | Accept   | 409 - Conflict         | No friend request to accept, waiting on the other user. |
//! | None            | Reject   | 409 - Conflict         | No friend request to reject.                            |
//! | None            | Unfriend | 409 - Conflict         | No friendship to remove.                                |
//!
//! | Status            | Meaning                                                              |
//! |-------------------|----------------------------------------------------------------------|
//! | 410 - Gone        | The friendcode was not present/ friend being managed does not exist. |
//! | 404 - Not Found   | Could not find the user making the management request.               |
//! | 400 - Bad Request | Attempted to friend action yourself                                  |
//!
//! ## CURL Example:
//! ```bash
//! curl -X POST -F 'user_id=301' -F 'version=1' -F 'friend_code=2' -F 'action=request' 'http://127.0.0.1:8000/api/manage_friend'
//! ```

use diesel::{delete, update};

use crate::{
    api::check_execute,
    models::{Friend, User},
    *,
};

/// Enum for the allowed actions in a friendship.
#[derive(FromFormField, Debug)]
pub enum FriendAction {
    #[field(value = "request")]
    Request,

    #[field(value = "accept")]
    Accept,

    #[field(value = "reject")]
    Reject,

    #[field(value = "unfriend")]
    UnFriend,
}

#[derive(Clone, Copy)]
enum FriendStatus {
    Friends,
    UserARequested,
    UserBRequested,
}

#[derive(FromForm)]
pub(crate) struct ManageFriendship {
    #[field(name = uncased("user_id"))]
    #[allow(dead_code)]
    user: UserID,

    #[field(name = uncased("version"), validate = eq(BACKEND_VERSION))]
    #[allow(dead_code)]
    api_version: AppVer,

    #[field(name = uncased("friend_code"))]
    #[allow(dead_code)]
    friend_code: FriendCode,

    #[field(name = uncased("action"))]
    #[allow(dead_code)]
    action: FriendAction,
}

impl From<i32> for FriendStatus {
    fn from(n: i32) -> Self {
        match n {
            0 => FriendStatus::Friends,
            1 => FriendStatus::UserARequested,
            2 => FriendStatus::UserBRequested,
            _ => panic!("Invalid friend_status in database"),
        }
    }
}

/// This function is far more repetitive than I would like, couldnt think of a nice abstraction to take care of the reversed positions of key and the friend statuses
#[post("/manage_friend", data = "<manage_data>")]
pub(crate) async fn api_manage_friend(db: RevzenDB, manage_data: Form<ManageFriendship>) -> Status {
    // check the requesting user exists
    use crate::schema::{friends::dsl::*, users::dsl::*};

    #[allow(unused_variables)]
    let ManageFriendship {
        user,
        api_version,
        friend_code,
        action,
    } = manage_data.into_inner();

    let user_future = db.run(move |c| users.find(user).first::<User>(c));
    let friend_future = db.run(move |c| users.filter(friendcode.eq(friend_code)).first::<User>(c));

    match user_future.await {
        Ok(user) => {
            match friend_future.await {
                Ok(friend) => {
                    if user.id == friend.id {
                        Status::BadRequest
                    } else {
                        // can now use friend status
                        let user_a_future =
                            db.run(move |c| friends.find((user.id, friend.id)).first::<Friend>(c));

                        let user_b_future =
                            db.run(move |c| friends.find((friend.id, user.id)).first::<Friend>(c));

                        match (user_a_future.await, user_b_future.await) {
                            (Ok(friendship), _) => {
                                //(user_a: friend, user_b: user)
                                println!("{:?},{:?}", friendship.friend_status, action);
                                match (friendship.friend_status.into(), action) {
                                    (FriendStatus::Friends, FriendAction::Request) => {
                                        Status::Forbidden
                                    }
                                    (
                                        FriendStatus::Friends,
                                        FriendAction::Accept | FriendAction::Reject,
                                    ) => Status::Conflict,
                                    (FriendStatus::Friends, FriendAction::UnFriend) => {
                                        check_execute(
                                            db.run(move |c| {
                                                delete(friends.find((user.id, friend.id)))
                                                    .execute(c)
                                            })
                                            .await,
                                        )
                                    }
                                    (
                                        FriendStatus::UserARequested,
                                        FriendAction::Request | FriendAction::Accept,
                                    ) => check_execute(
                                        db.run(move |c| {
                                            update(friends.find((user.id, friend.id)))
                                                .set(friend_status.eq(FriendStatus::Friends as i32))
                                                .execute(c)
                                        })
                                        .await,
                                    ),
                                    (FriendStatus::UserARequested, FriendAction::Reject) => {
                                        check_execute(
                                            db.run(move |c| {
                                                delete(friends.find((user.id, friend.id)))
                                                    .execute(c)
                                            })
                                            .await,
                                        )
                                    }
                                    (FriendStatus::UserARequested, FriendAction::UnFriend) => {
                                        Status::Conflict
                                    }
                                    (FriendStatus::UserBRequested, FriendAction::Request) => {
                                        Status::Forbidden
                                    }
                                    (
                                        FriendStatus::UserBRequested,
                                        FriendAction::Accept | FriendAction::UnFriend,
                                    ) => Status::Conflict,
                                    (FriendStatus::UserBRequested, FriendAction::Reject) => {
                                        check_execute(
                                            db.run(move |c| {
                                                delete(friends.find((user.id, friend.id)))
                                                    .execute(c)
                                            })
                                            .await,
                                        )
                                    }
                                }
                            }
                            (_, Ok(friendship)) => {
                                //(user_a: user, user_b: friend)
                                match (friendship.friend_status.into(), action) {
                                    (FriendStatus::Friends, FriendAction::Request) => {
                                        Status::Forbidden
                                    }
                                    (
                                        FriendStatus::Friends,
                                        FriendAction::Accept | FriendAction::Reject,
                                    ) => Status::Conflict,
                                    (FriendStatus::Friends, FriendAction::UnFriend) => {
                                        check_execute(
                                            db.run(move |c| {
                                                delete(friends.find((friend.id, user.id)))
                                                    .execute(c)
                                            })
                                            .await,
                                        )
                                    }
                                    (
                                        FriendStatus::UserBRequested,
                                        FriendAction::Request | FriendAction::Accept,
                                    ) => check_execute(
                                        db.run(move |c| {
                                            update(friends.find((friend.id, user.id)))
                                                .set(friend_status.eq(FriendStatus::Friends as i32))
                                                .execute(c)
                                        })
                                        .await,
                                    ),
                                    (FriendStatus::UserBRequested, FriendAction::Reject) => {
                                        check_execute(
                                            db.run(move |c| {
                                                delete(friends.find((friend.id, user.id)))
                                                    .execute(c)
                                            })
                                            .await,
                                        )
                                    }
                                    (FriendStatus::UserBRequested, FriendAction::UnFriend) => {
                                        Status::Conflict
                                    }
                                    (FriendStatus::UserARequested, FriendAction::Request) => {
                                        Status::Forbidden
                                    }
                                    (
                                        FriendStatus::UserARequested,
                                        FriendAction::Accept | FriendAction::UnFriend,
                                    ) => Status::Conflict,
                                    (FriendStatus::UserARequested, FriendAction::Reject) => {
                                        check_execute(
                                            db.run(move |c| {
                                                delete(friends.find((friend.id, user.id)))
                                                    .execute(c)
                                            })
                                            .await,
                                        )
                                    }
                                }
                            }
                            _ => {
                                // there is no friend struct in the database
                                match action {
                                    FriendAction::Request => {
                                        // create new friend request
                                        check_execute(
                                            db.run(move |c| {
                                                insert_into(friends)
                                                    .values(&Friend {
                                                        user_a: user.id,
                                                        user_b: friend.id,
                                                        friend_status: FriendStatus::UserARequested
                                                            as i32,
                                                    })
                                                    .execute(c)
                                            })
                                            .await,
                                        )
                                    }
                                    _ => Status::Conflict,
                                }
                            }
                        }
                    }
                }
                Err(_) => Status::Gone,
            }
        }
        Err(_) => Status::NotFound,
    }
}
