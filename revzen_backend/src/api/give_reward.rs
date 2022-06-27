//! The API method for rewarding a user after completing several sessions
//!
//! ## Post Request Fields:
//!
//! | Key                | Type    | Value                                       |
//! |--------------------|---------|---------------------------------------------|
//! | gained_xp          | integer | gained experience                           |
//! | health_change      | integer | the health change for the pet               |
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
//!     "pet_type"  : 0,
//!     "health"    : 0,
//!     "xp"        : 0,
//!     "pet_change" : 2,
//! }
//! ```
//!
//! ## CURL Example:
//! ```bash
//! curl -X POST -F 'user_id=301' -F 'version=3' -F 'gained_xp=5' -F 'health_change=-1' 'http://127.0.0.1:8000/api/give_reward'
//! ```

use crate::{
    models::{Pet, User},
    *,
};
use diesel::{delete, insert_into, update};
use rocket::serde::{json::Json, Serialize};

#[derive(FromForm)]
pub(crate) struct GiveReward {
    #[field(name = uncased("user_id"))]
    user: UserID,

    #[field(name = uncased("version"), validate = eq(BACKEND_VERSION))]
    #[allow(dead_code)]
    api_version: AppVer,

    #[field(name = "gained_xp")]
    xp_change: i32,

    #[field(name = "health_change")]
    health_change: i32,
}

pub enum RewardStatus {
    NoChange = 0,
    SwitchedPet = 1,
    OnlyRock = 2,
}

#[derive(Serialize)]
#[serde(crate = "rocket::serde")]
pub struct Reward {
    pet_type: PetType,
    health: i32,
    xp: i32,
    pet_change: i32,
}

#[post("/give_reward", data = "<reward_data>")]
pub(crate) async fn api_give_reward(
    db: RevzenDB,
    reward_data: Form<GiveReward>,
) -> Option<Json<Reward>> {
    use crate::schema::{pets::dsl::*, users::dsl::*};

    let GiveReward {
        user,
        api_version: _,
        xp_change,
        health_change,
    } = reward_data.into_inner();

    if let Ok(user_data) = db.run(move |c| users.find(user).first::<User>(c)).await {
        if user_data.main_pet != PET_ROCK {
            Some(Json(
                db.run(move |c| {
                    let (curr_health, curr_xp) = insert_into(pets)
                        .values(&Pet {
                            user_id: user,
                            pet_type: user_data.main_pet,
                            health: health_change,
                            xp: xp_change,
                        })
                        .on_conflict((user_id, pet_type))
                        .do_update()
                        .set((health.eq(health + health_change), xp.eq(xp + xp_change)))
                        .returning((health, xp))
                        .get_result::<(i32, i32)>(c)
                        .expect("No database issues");

                    if curr_health <= MIN_HEALTH {
                        delete(pets.find((user, user_data.main_pet)))
                            .execute(c)
                            .expect("No database issues");

                        // get the next pet.
                        let all_pets = pets
                            .filter(user_id.eq(user))
                            .order(health.asc())
                            .get_results::<Pet>(c)
                            .expect("No database issues");

                        if let Some(healthiest_pet) = all_pets.get(0) {
                            update(users.find(user))
                                .set(main_pet.eq(healthiest_pet.pet_type))
                                .execute(c)
                                .expect("No database issues");

                            Reward {
                                pet_type: healthiest_pet.pet_type,
                                health: healthiest_pet.health,
                                xp: healthiest_pet.xp,
                                pet_change: RewardStatus::SwitchedPet as i32,
                            }
                        } else {
                            update(users.find(user))
                                .set(main_pet.eq(PET_ROCK))
                                .execute(c)
                                .expect("No database issues");

                            Reward {
                                pet_type: PET_ROCK,
                                health: MIN_HEALTH,
                                xp: 0,
                                pet_change: RewardStatus::OnlyRock as i32,
                            }
                        }
                    } else if curr_health > MAX_HEALTH {
                        update(pets.find((user, user_data.main_pet)))
                            .set(health.eq(MAX_HEALTH))
                            .execute(c)
                            .expect("No database issues");
                        Reward {
                            pet_type: user_data.main_pet,
                            health: MAX_HEALTH,
                            xp: curr_xp,
                            pet_change: RewardStatus::NoChange as i32,
                        }
                    } else {
                        Reward {
                            pet_type: user_data.main_pet,
                            health: curr_health,
                            xp: curr_xp,
                            pet_change: RewardStatus::NoChange as i32,
                        }
                    }
                })
                .await,
            ))
        } else {
            Some(Json(Reward {
                pet_type: PET_ROCK,
                health: MIN_HEALTH,
                xp: 0,
                pet_change: RewardStatus::NoChange as i32,
            }))
        }
    } else {
        None
    }
}
