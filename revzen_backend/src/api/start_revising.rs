//! The API method for signalling the user is currently revising
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
//! curl -X POST -F 'user_id=29' -F 'version=1' 'http://127.0.0.1:8000/api/start_revising'
//! ```

use rocket::State;

use crate::{models::User, *};

/// Used to identify a client (with version number for compatability check)
#[derive(FromForm)]
pub struct Client {
    #[field(name = uncased("user_id"))]
    user: UserID,

    #[field(name = uncased("version"), validate = eq(BACKEND_VERSION))]
    #[allow(dead_code)]
    client_version: AppVer,
}

#[post("/start_revising", data = "<user_auth>")]
pub(crate) async fn api_start_revising(
    state: &State<StudyState>,
    db: RevzenDB,
    user_auth: Form<Client>,
) -> Status {
    use crate::schema::users::dsl::*;

    match db
        .run(move |c| {
            users
                .filter(id.eq(user_auth.user))
                .find(id)
                .first::<User>(c)
        })
        .await
    {
        Ok(user_data) => {
            let mut write_state = state.0.write().await;
            write_state.insert(
                user_data.id,
                (user_data.friendcode, user_data.username.clone()),
            );
            Status::Ok
        }
        Err(_) => Status::NotFound,
    }
}
