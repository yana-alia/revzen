//! API method for changing the current pet.
//!
//! ## Post Request Fields:
//!
//! | Key            | Type    | Value                                   |
//! |----------------|---------|-----------------------------------------|
//! | user_id        | integer | The google provided 'sub'/subject id.   |
//! | client_version | integer | The api version being used.             |
//! | new_pet        | integer | The new pet type being given.           |
//!
//! ## Response:
//!
//! | Status                      | Meaning                             |
//! |-----------------------------|-------------------------------------|
//! | 200 - OK                    | The pet was successfully given.     |
//! | 409 - Conflict              | Cannot select the requested pet.    |
//! | 404 - Not Found             | No such account exists.             |
//! | 500 - Internal Server Error | An unexpected server error occured. |
//!
//! ## CURL Example:
//! ```bash
//! curl -X POST -F 'user_id=301' -F 'version=4' -F 'pet_type=1' 'http://127.0.0.1:8000/api/change_pet'
//! ```

use diesel::update;

use crate::{models::Pet, *};

#[post("/change_pet", data = "<user_pet>")]
pub(crate) async fn api_change_pet(db: RevzenDB, user_pet: Form<PetRequest>) -> Status {
    use crate::schema::{pets::dsl::*, users::dsl::*};

    let PetRequest {
        user,
        client_version: _,
        pet_given_type,
    } = user_pet.into_inner();

    if let Ok(all_pets) = db
        .run(move |c| pets.filter(user_id.eq(user)).get_results::<Pet>(c))
        .await
    {
        for pet in all_pets {
            if pet.pet_type == pet_given_type {
                return if db
                    .run(move |c| {
                        update(users.find(user))
                            .set(main_pet.eq(pet_given_type))
                            .execute(c)
                    })
                    .await
                    .is_ok()
                {
                    Status::Ok
                } else {
                    Status::NotFound
                };
            }
        }
        Status::Conflict
    } else {
        Status::InternalServerError
    }
}
