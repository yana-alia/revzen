//! API method for giving users a new pet. If they already have the pet, it is not added.
//!
//! If there currently have no pets, their main pet is switched to this new pet.
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
//! | 404 - Not Found             | No such account exists.             |
//! | 500 - Internal Server Error | An unexpected server error occured. |
//!
//! ## CURL Example:
//! ```bash
//! curl -X POST -F 'user_id=301' -F 'version=1' -F 'pet_type=1' 'http://127.0.0.1:8000/api/give_pet'
//! ```

use diesel::{insert_into, update};

use crate::{
    models::{Pet, User},
    *,
};

#[derive(FromForm)]
pub struct GivePet {
    #[field(name = uncased("user_id"))]
    user: UserID,

    #[field(name = uncased("version"), validate = eq(BACKEND_VERSION))]
    #[allow(dead_code)]
    client_version: AppVer,

    #[field(name = uncased("pet_type"), validate = range(1..=3))]
    pet_given_type: PetType,
}

#[post("/give_pet", data = "<user_pet>")]
pub(crate) async fn api_give_pet(db: RevzenDB, user_pet: Form<GivePet>) -> Status {
    use crate::schema::{pets::dsl::*, users::dsl::*};

    let GivePet {
        user,
        client_version: _,
        pet_given_type,
    } = user_pet.into_inner();

    if let Ok(user_data) = db.run(move |c| users.find(user).first::<User>(c)).await {
        if db
            .run(move |c| {
                insert_into(pets)
                    .values(&Pet {
                        user_id: user,
                        pet_type: pet_given_type,
                        health: INITIAL_HEALTH,
                        xp: 0,
                    })
                    .on_conflict_do_nothing()
                    .execute(c)
            })
            .await
            .is_ok()
        {
            if user_data.main_pet == PET_ROCK {
                if db
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
                    Status::InternalServerError
                }
            } else {
                Status::Ok
            }
        } else {
            Status::InternalServerError
        }
    } else {
        Status::NotFound
    }
}
