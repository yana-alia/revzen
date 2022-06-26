//! The api request required for creating new users.
//!
//! ## Post Request Fields:
//!
//! | Key            | Type    | Value                                   |
//! |----------------|---------|-----------------------------------------|
//! | user_name      | string  | User's chosen username (e.g 'ollie123') |
//! | user_id        | integer | The google provided 'sub'/subject id.   |
//! | client_version | integer | The api version being used.             |
//!
//! ## Response:
//!
//! | Status         | Meaning                                         |
//! |----------------|-------------------------------------------------|
//! | 200 - OK       | The user was successfully added, can now login. |
//! | 409 - Conflict | This google account has already added.          |
//!
//! ## CURL Example:
//! ```bash
//! curl -X POST -F 'user_id=301' -F 'version=1' -F 'user_name=ollie' 'http://127.0.0.1:8000/api/create'
//! ```
use diesel::insert_into;
use crate::{
    api::PET_SHIBA,
    models::{AddUser, Pet},
    *,
};

#[derive(FromForm)]
pub(crate) struct CreateClient {
    #[field(name = uncased("user_name"))]
    username: String,

    #[field(name = uncased("user_id"))]
    user: UserID,

    #[field(name = uncased("version"), validate = eq(BACKEND_VERSION))]
    #[allow(dead_code)]
    api_version: AppVer,
}

#[post("/create", data = "<client_data>")]
pub(crate) async fn api_create_user(db: RevzenDB, client_data: Form<CreateClient>) -> Status {
    use crate::schema::{pets::dsl::*, users::dsl::*};
    match db
        .run::<_, QueryResult<usize>>(move |c| {
            insert_into(users)
                .values(&AddUser {
                    id: client_data.user,
                    username: client_data.username.clone(),
                    main_pet: PET_SHIBA,
                })
                .execute(c)?;

            insert_into(pets)
                .values(&Pet {
                    user_id: client_data.user,
                    pet_type: PET_SHIBA,
                    health: 1,
                    xp: 0,
                })
                .execute(c)
        })
        .await
    {
        Ok(_) => Status::Ok,
        Err(_) => Status::Conflict,
    }
}
