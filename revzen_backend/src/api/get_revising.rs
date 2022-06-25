//! The API method for getting those currently revising (live)
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
//! | Status          | Meaning                                         |
//! |-----------------|-------------------------------------------------|
//! | 200 - OK        | The user was successfully added, can now login. |
//! | 404 - Not Found | No such account exists.                         |
//!
//! ## CURL Example:
//! ```bash
//! curl -X POST -F 'user_id=301' -F 'version=1' 'http://127.0.0.1:8000/api/get_revising'
//! ```
//!
//! ## Json
//! In the event of a 200 - Ok the following kind of structure of json is returned
//! ```json
//! [
//!     {"friendcode":1,"username":"ollie"},
//!     {"friendcode":3,"username":"bob"}
//! ]
//! ```

use rocket::{serde::json::Json, State};

use crate::{
    api::{Client, UserDetails},
    *,
};

#[post("/get_revising", data = "<user_auth>")]
pub(crate) async fn api_get_revising(
    db: RevzenDB,
    state: &State<StudyState>,
    user_auth: Form<Client>,
) -> Option<Json<Vec<UserDetails>>> {
    use crate::schema::{follows::dsl::*, users::dsl::*};
    // again we assume the user is valid
    #[allow(unused_variables)]
    let Client {
        user,
        client_version,
    } = user_auth.into_inner();

    if let Ok(following) = db
        .run(move |c| {
            users
                .inner_join(
                    follows.on(id
                        .eq(follower)
                        .and(followee.eq(user).and(accepted.eq(true)))),
                )
                .select((id, username, friendcode, main_pet))
                .get_results::<(UserID, String, FriendCode, i32)>(c)
        })
        .await
    {
        let read_state = state.0.read().await;
        Some(Json(
            following
                .into_iter()
                .filter(|(user_id, _, _, _)| read_state.contains_key(user_id))
                .map(|(_, name, code, pet)| UserDetails {
                    friendcode: code,
                    username: name,
                    main_pet: pet.into(),
                })
                .collect(),
        ))
    } else {
        None
    }
}
