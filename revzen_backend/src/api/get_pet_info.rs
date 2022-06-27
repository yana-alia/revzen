//! The API method for retrieving pet information
//!
//! Used for getting pet data for pet selection.
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
//! | Status          | Meaning                          |
//! |-----------------|----------------------------------|
//! | 200 - OK        | Successfully retrieved pet info. |
//! | 404 - Not Found | No such account exists.          |
//!
//! ## CURL Example:
//! ```bash
//! curl -X POST -F 'user_id=301' -F 'version=4' 'http://127.0.0.1:8000/api/get_pet_info'
//! ```
//!
//! ## Json
//! In the event of a 200 - Ok the following kind of structure of json is returned
//! ```json
//! {
//!     "main_pet" : "Rock",
//!     "all_pets":{
//!         "Husky": {
//!             "health" : 3,
//!             "xp" : 20
//!         },
//!         "Shiba": {
//!             "health" : 4,
//!             "xp" : 100
//!         }
//!     }
//! }
//! ```

use crate::{
    api::{Client, PetType},
    models::{Pet, User},
    *,
};
use rocket::serde::{json::Json, Serialize};
use std::collections::HashMap;

#[derive(Serialize, Clone)]
#[serde(crate = "rocket::serde")]
pub struct PetInfo {
    health: i32,
    xp: i32,
}

#[derive(Serialize, Clone)]
#[serde(crate = "rocket::serde")]
pub struct PetResponse {
    main_pet: PetType,
    all_pets: HashMap<PetType, PetInfo>,
}

#[post("/get_pet_info", data = "<user_auth>")]
pub(crate) async fn api_get_pet_info(
    db: RevzenDB,
    user_auth: Form<Client>,
) -> Option<Json<PetResponse>> {
    use crate::schema::{pets::dsl::*, users::dsl::*};

    let Client {
        user,
        client_version: _,
    } = user_auth.into_inner();

    let user_future = db.run(move |c| users.find(user).first::<User>(c));
    let pets_future = db.run(move |c| pets.filter(user_id.eq(user)).get_results::<Pet>(c));

    match (user_future.await, pets_future.await) {
        (Ok(user_data), Ok(all_pets)) => Some(Json(PetResponse {
            main_pet: user_data.main_pet,
            all_pets: all_pets
                .into_iter()
                .map(|pet| {
                    (
                        pet.pet_type,
                        PetInfo {
                            health: pet.health,
                            xp: pet.xp,
                        },
                    )
                })
                .collect::<HashMap<PetType, PetInfo>>(),
        })),
        _ => None,
    }
}
