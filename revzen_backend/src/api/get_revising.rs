//! The API method for getting those currently revising (live)
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
//! | 200 - OK        | Successfully got revising users. |
//! | 404 - Not Found | No such account exists.          |
//!
//! ## CURL Example:
//! ```bash
//! curl -X POST -F 'user_id=301' -F 'version=4' 'http://127.0.0.1:8000/api/get_revising'
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

    let Client {
        user,
        client_version: _,
    } = user_auth.into_inner();

    if let Ok(following) = db
        .run(move |c| {
            users
                .inner_join(
                    follows.on(id
                        .eq(follower)
                        .and(followee.eq(user).and(accepted.eq(true)))),
                )
                .select(id)
                .get_results::<UserID>(c)
        })
        .await
    {
        let read_state = state.0.read().await;
        Some(Json(
            following
                .into_iter()
                .filter_map(|user_id| read_state.get(&user_id).map(UserDetails::clone))
                .collect(),
        ))
    } else {
        None
    }
}
