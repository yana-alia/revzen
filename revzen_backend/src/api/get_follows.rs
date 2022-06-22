//! The api method for retrieving friends of a user.
//!
//! ## Post Request Fields
//!
//! | Key            | Type    | Value                                   |
//! |----------------|---------|-----------------------------------------|
//! | user_id        | integer | The google provided 'sub'/subject id.   |
//! | client_version | integer | The api version being used.             |
//!
//! ## Response:
//!
//! | Status          | Meaning                          |
//! |-----------------|----------------------------------|
//! | 200 - OK        | The user's friends are returned. |
//! | 404 - Not Found | No such account exists.          |
//!
//! In event of a 200 - OK the following json is returned
//! ```json
//! [
//!     "requests" :
//!         [
//!             {
//!                 "friendcode" : 123,
//!                 "username"   : "ollie"
//!             },
//!             {
//!                 "friendcode" : 234,
//!                 "username"   : "bob"
//!             },
//!         ],
//!     "requested" :
//!         [
//!             {
//!                 "friendcode" : 3423,
//!                 "username"   : "mike"
//!             },
//!         ],
//!     "follows" :
//!         [
//!             {
//!                 "friendcode" : 234,
//!                 "username"   : "alfie"
//!             },
//!             {
//!                 "friendcode" : 541,
//!                 "username"   : "dayana"
//!             },
//!             {
//!                 "friendcode" : 69,
//!                 "username"   : "miles"
//!             },
//!         ],
//!     "followers" :
//!         [
//!             {
//!                 "friendcode" : 69,
//!                 "username"   : "miles"
//!             },
//!         ]
//! ]
//! ```
//!
//! ## CURL Example:
//! ```bash
//! curl -X POST -F 'user_id=560' -F 'version=1' 'http://127.0.0.1:8000/api/get_follows'
//! ```

use crate::{api::Client, *};
use diesel::{dsl::exists, select};
use rocket::serde::{json::Json, Serialize};

#[derive(Serialize)]
#[serde(crate = "rocket::serde")]
pub struct FollowDetails {
    friendcode: FriendCode,
    username: String,
}

#[derive(Serialize)]
#[serde(crate = "rocket::serde")]
pub struct FollowResponse {
    requests: Vec<FollowDetails>,
    requested: Vec<FollowDetails>,
    following: Vec<FollowDetails>,
    followers: Vec<FollowDetails>,
}

fn map_to_details(tuples: Vec<(String, FriendCode)>) -> Vec<FollowDetails> {
    tuples
        .into_iter()
        .map(|(username, friendcode)| FollowDetails {
            friendcode,
            username,
        })
        .collect()
}

#[post("/get_follows", data = "<user_auth>")]
pub(crate) async fn api_get_follows(
    db: RevzenDB,
    user_auth: Form<Client>,
) -> Option<Json<FollowResponse>> {
    #[allow(unused_variables)]
    let Client {
        user,
        client_version,
    } = user_auth.into_inner();

    use crate::schema::{follows::dsl::*, users::dsl::*};

    if db
        .run(move |c| {
            matches!(
                select(exists(users.filter(id.eq(user)))).get_result::<bool>(c),
                Ok(true)
            )
        })
        .await
    {
        let requests_future = db.run(move |c| {
            users
                .inner_join(
                    follows.on(id
                        .eq(follower)
                        .and(followee.eq(user).and(accepted.eq(false)))),
                )
                .select((username, friendcode))
                .get_results::<(String, FriendCode)>(c)
        });
        let requested_future = db.run(move |c| {
            users
                .inner_join(
                    follows.on(id
                        .eq(followee)
                        .and(follower.eq(user).and(accepted.eq(false)))),
                )
                .select((username, friendcode))
                .get_results::<(String, FriendCode)>(c)
        });
        let follows_future = db.run(move |c| {
            users
                .inner_join(
                    follows.on(id
                        .eq(follower)
                        .and(followee.eq(user).and(accepted.eq(true)))),
                )
                .select((username, friendcode))
                .get_results::<(String, FriendCode)>(c)
        });
        let followers_future = db.run(move |c| {
            users
                .inner_join(
                    follows.on(id
                        .eq(followee)
                        .and(follower.eq(user).and(accepted.eq(true)))),
                )
                .select((username, friendcode))
                .get_results::<(String, FriendCode)>(c)
        });

        if let (Ok(requests), Ok(requested), Ok(following), Ok(followers)) = (
            requests_future.await,
            requested_future.await,
            follows_future.await,
            followers_future.await,
        ) {
            Some(Json(FollowResponse {
                requests: map_to_details(requests),
                requested: map_to_details(requested),
                following: map_to_details(following),
                followers: map_to_details(followers),
            }))
        } else {
            None
        }
    } else {
        None
    }
}
