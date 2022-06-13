//! Client login method of the API.
//!
//! When a client logs in, we make use of the [google sign in](https://developers.google.com/identity/sign-in/web/backend-auth) to get a sub (subject identifier) unique to a google account.
//!
//! Using this we can identify login the user and login.
//!
use rocket::serde::{json::Json, Serialize};

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

#[derive(Serialize)]
#[serde(crate = "rocket::serde")]
pub struct UserData {
    friendcode: i32,
    username: String,
}

#[post("/login", data = "<user_auth>")]
pub(crate) async fn api_login(db: RevzenDB, user_auth: Form<Client>) -> Option<Json<UserData>> {
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
        Ok(user_data) => Some(Json(UserData {
            friendcode: user_data.friendcode,
            username: user_data.username,
        })),
        Err(_) => None,
    }
}
