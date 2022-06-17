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

use rocket::{
    serde::{json::Json, Serialize},
    State,
};

use crate::*;

/// Used to identify a client (with version number for compatability check)
#[derive(FromForm)]
pub struct Client {
    #[field(name = uncased("user_id"))]
    _user: UserID,

    #[field(name = uncased("version"), validate = eq(BACKEND_VERSION))]
    #[allow(dead_code)]
    client_version: AppVer,
}

#[derive(Serialize)]
#[serde(crate = "rocket::serde")]
pub struct LiveStudy {
    friendcode: i32,
    username: String,
}

#[post("/get_revising", data = "<_user_auth>")]
pub(crate) async fn api_get_revising(
    state: &State<StudyState>,
    _user_auth: Form<Client>,
) -> Option<Json<Vec<LiveStudy>>> {
    // again we assume the user is valid
    let read_state = state.0.read().await;
    Some(Json(
        read_state
            .iter()
            .map(|(_, (friendcode, username))| LiveStudy {
                friendcode: *friendcode,
                username: username.clone(),
            })
            .collect::<Vec<_>>(),
    ))
}
