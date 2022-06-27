//! The api method for retrieving friends of a user.
//!
//! ## Post Request Fields:
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
//!                 "username"   : "ollie",
//!                 "main_pet"   : 0
//!             },
//!             {
//!                 "friendcode" : 234,
//!                 "username"   : "bob",
//!                 "main_pet"   : 0
//!             },
//!         ],
//!     "requested" :
//!         [
//!             {
//!                 "friendcode" : 3423,
//!                 "username"   : "mike",
//!                 "main_pet"   : 0
//!             },
//!         ],
//!     "follows" :
//!         [
//!             {
//!                 "friendcode" : 234,
//!                 "username"   : "alfie",
//!                 "main_pet"   : 0
//!             },
//!             {
//!                 "friendcode" : 541,
//!                 "username"   : "dayana",
//!                 "main_pet"   : 0
//!             },
//!             {
//!                 "friendcode" : 69,
//!                 "username"   : "miles",
//!                 "main_pet"   : 0
//!             },
//!         ],
//!     "followers" :
//!         [
//!             {
//!                 "friendcode" : 69,
//!                 "username"   : "miles",
//!                 "main_pet"   : 0
//!             },
//!         ]
//! ]
//! ```
//!
//! ## CURL Example:
//! ```bash
//! curl -X POST -F 'user_id=560' -F 'version=3' 'http://127.0.0.1:8000/api/get_follows'
//! ```

use crate::{
    api::{map_to_details, Client, UserDetails},
    *,
};
use diesel::{dsl::exists, select};
use rocket::serde::{json::Json, Serialize};

#[derive(Serialize)]
#[serde(crate = "rocket::serde")]
pub struct FollowResponse {
    requests: Vec<UserDetails>,
    requested: Vec<UserDetails>,
    following: Vec<UserDetails>,
    followers: Vec<UserDetails>,
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
                .select((username, friendcode, main_pet))
                .get_results::<(String, FriendCode, i32)>(c)
        });
        let requested_future = db.run(move |c| {
            users
                .inner_join(
                    follows.on(id
                        .eq(followee)
                        .and(follower.eq(user).and(accepted.eq(false)))),
                )
                .select((username, friendcode, main_pet))
                .get_results::<(String, FriendCode, i32)>(c)
        });
        let follows_future = db.run(move |c| {
            users
                .inner_join(
                    follows.on(id
                        .eq(follower)
                        .and(followee.eq(user).and(accepted.eq(true)))),
                )
                .select((username, friendcode, main_pet))
                .get_results::<(String, FriendCode, i32)>(c)
        });
        let followers_future = db.run(move |c| {
            users
                .inner_join(
                    follows.on(id
                        .eq(followee)
                        .and(follower.eq(user).and(accepted.eq(true)))),
                )
                .select((username, friendcode, main_pet))
                .get_results::<(String, FriendCode, i32)>(c)
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
