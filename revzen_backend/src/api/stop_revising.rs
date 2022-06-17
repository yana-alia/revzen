//! The API method for signalling the user has stopped revising
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
//! | Status            | Meaning                                         |
//! |-------------------|-------------------------------------------------|
//! | 200 - OK          | The user was successfully added, can now login. |
//! | 400 - Bad Request | No account of this user had started revising.   |
//!
//! ## CURL Example:
//! ```bash
//! curl -X POST -F 'user_id=29' -F 'version=1' 'http://127.0.0.1:8000/api/stop_relising'
//! ```

use rocket::State;

use crate::*;

/// Used to identify a client (with version number for compatability check)
#[derive(FromForm)]
pub struct Client {
    #[field(name = uncased("user_id"))]
    user: UserID,

    #[field(name = uncased("version"), validate = eq(BACKEND_VERSION))]
    #[allow(dead_code)]
    client_version: AppVer,
}

#[post("/stop_revising", data = "<user_auth>")]
pub(crate) async fn api_stop_revising(
    state: &State<StudyState>,
    user_auth: Form<Client>,
) -> Status {
    // since we only remove data, only need to check for user_auth data in the state
    let mut write_state = state.0.write().await;
    if write_state.remove(&user_auth.user).is_some() {
        Status::Ok
    } else {
        Status::BadRequest
    }
}
