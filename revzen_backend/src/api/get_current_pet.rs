//! An api method to get the current pet status for the user.
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
//! | Status            | Meaning                             |
//! |-------------------|-------------------------------------|
//! | 200 - OK          | Session was successfully added      |
//! | 404 - Not Found   | No such user was found.             |
//!
//! In event of a 200 - OK the following json is returned
//! ```json
//! {
//!     "pet_type" : 0,
//!     "health"   : 0,
//!     "xp"       : 0
//! }
//! ```
//!
//! ## CURL Example:
//! ```bash
//! curl -X POST -F 'user_id=29' -F 'version=1' 'http://127.0.0.1:8000/api/get_current_pet'
//! ```

use rocket::serde::json::Json;

use crate::{
    models::{Pet, User},
    *,
};

use super::PET_ROCK_STATUS;

#[post("/get_current_pet", data = "<user_auth>")]
pub(crate) async fn api_get_current_pet(
    db: RevzenDB,
    user_auth: Form<Client>,
) -> Option<Json<PetStatus>> {
    use crate::schema::{pets::dsl::pets, users::dsl::users};

    let Client {
        user,
        client_version: _,
    } = user_auth.into_inner();

    match db.run(move |c| users.find(user).first::<User>(c)).await {
        Ok(user_data) => {
            if user_data.main_pet == PET_ROCK {
                Some(Json(PET_ROCK_STATUS))
            } else {
                match db
                    .run(move |c| pets.find((user, user_data.main_pet)).first::<Pet>(c))
                    .await
                {
                    Ok(Pet {
                        user_id: _,
                        pet_type,
                        health,
                        xp,
                    }) => Some(Json(PetStatus {
                        pet_type,
                        health,
                        xp,
                    })),
                    Err(_) => None,
                }
            }
        }
        Err(_) => None,
    }
}
