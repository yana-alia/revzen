//! The api method for creating, cancelling, rejecting and accepting friend requests.
//!
//! Note this particular method is low-quality code, and needs to be refactored.
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
//! | Status            | Meaning                                                              |
//! |-------------------|----------------------------------------------------------------------|
//! | 410 - Gone        | The friendcode was not present/ friend being managed does not exist. |
//! | 404 - Not Found   | Could not find the user making the management request.               |
//! | 400 - Bad Request | Attempted to friend action yourself                                  |
//! | 409 - Conflict    | Attempted an invalid action (e.g accept, but there is not request).  |
//!
//! ## CURL Example:
//! ```bash
//! curl -X POST -F 'user_id=301' -F 'version=3' -F 'friend_code=2' -F 'action=request' 'http://127.0.0.1:8000/api/manage_follows'
//! ```

use crate::{
    models::{Follow, User},
    *,
};
use diesel::{delete, insert_into, update};

/// Enum for the allowed actions in a friendship.
#[derive(FromFormField, Debug)]
pub enum FriendAction {
    /// Follower requests to follow
    #[field(value = "request")]
    Request,

    /// Followee accepts follower's request
    #[field(value = "accept")]
    Accept,

    /// Followee reject follower's request, or stop them following
    #[field(value = "reject")]
    Reject,

    /// Follower stops following
    #[field(value = "unfollow")]
    UnFollow,
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

/// This function is far more repetitive than I would like, couldnt think of a nice abstraction to take care of the reversed positions of key and the friend statuses
#[post("/manage_follows", data = "<manage_data>")]
pub(crate) async fn api_manage_friend(db: RevzenDB, manage_data: Form<ManageFriendship>) -> Status {
    // check the requesting user exists
    use crate::schema::{follows::dsl::*, users::dsl::*};

    #[allow(unused_variables)]
    let ManageFriendship {
        user,
        api_version,
        friend_code,
        action,
    } = manage_data.into_inner();

    let user_future = db.run(move |c| users.find(user).first::<User>(c));
    let friend_future = db.run(move |c| users.filter(friendcode.eq(friend_code)).first::<User>(c));

    match (user_future.await, friend_future.await) {
        (Ok(user), Ok(friend)) => {
            if user.id == friend.id {
                Status::BadRequest
            } else {
                db.run(move |c| {
                    if match action {
                        FriendAction::Request => insert_into(follows)
                            .values(&Follow {
                                followee: friend.id,
                                follower: user.id,
                                accepted: false,
                            })
                            .on_conflict_do_nothing()
                            .execute(c),
                        FriendAction::UnFollow => {
                            delete(follows.find((friend.id, user.id))).execute(c)
                        }
                        FriendAction::Accept => update(follows.find((user.id, friend.id)))
                            .set(accepted.eq(true))
                            .execute(c),
                        FriendAction::Reject => {
                            delete(follows.find((user.id, friend.id))).execute(c)
                        }
                    }
                    .is_ok()
                    {
                        Status::Ok
                    } else {
                        Status::Conflict
                    }
                })
                .await
            }
        }
        (Err(_), _) => Status::NotFound,
        (_, Err(_)) => Status::Gone,
    }
}
