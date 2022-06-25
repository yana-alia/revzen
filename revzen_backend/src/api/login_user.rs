//! Client login method of the API.
//!
//! When a client logs in, we make use of the [google sign in](https://developers.google.com/identity/sign-in/web/backend-auth) to get a sub (subject identifier) unique to a google account.
//!
//! Using this we can identify login the user and login.
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
//! In event of a 200 - OK the following json is returned
//! ```json
//! {
//!     "friendcode": <friend code: integer>,
//!     "username": <user's username: string>
//! }
//! ```
//!
//! ## CURL Example:
//! ```bash
//! curl -X POST -F 'user_id=29' -F 'version=1' 'http://127.0.0.1:8000/api/login'
//! ```

use crate::{api::Client, models::User, *};
use rocket::serde::json::Json;

#[post("/login", data = "<user_auth>")]
pub(crate) async fn api_login(db: RevzenDB, user_auth: Form<Client>) -> Option<Json<UserDetails>> {
    use crate::schema::users::dsl::*;

    match db
        .run(move |c| users.find(user_auth.user).first::<User>(c))
        .await
    {
        Ok(user_data) => Some(Json(UserDetails {
            friendcode: user_data.friendcode,
            username: user_data.username,
            main_pet: user_data.main_pet.into(),
        })),
        Err(_) => None,
    }
}
