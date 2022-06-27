//! The API method for signalling the user is currently revising
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
//! | Status          | Meaning                        |
//! |-----------------|--------------------------------|
//! | 200 - OK        | Successfully started revising. |
//! | 404 - Not Found | No such account exists.        |
//!
//! ## CURL Example:
//! ```bash
//! curl -X POST -F 'user_id=29' -F 'version=3' 'http://127.0.0.1:8000/api/start_revising'
//! ```

use rocket::State;

use crate::{api::Client, models::User, *};

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
                UserDetails {
                    friendcode: user_data.friendcode,
                    username: user_data.username,
                    main_pet: user_data.main_pet,
                },
            );
            Status::Ok
        }
        Err(_) => Status::NotFound,
    }
}
