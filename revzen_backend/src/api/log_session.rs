//! The api request for adding a revision session to a user's history
//!
//! ## Post Request Fields:
//!
//! | Key                | Type    | Value                                       |
//! |--------------------|---------|---------------------------------------------|
//! | user_id            | integer | The google provided 'sub'/subject id.       |
//! | client_version     | integer | The api version being used.                 |
//! | planned_study_time | integer | Length in seconds of the planned study time |
//! | planned_break_time | integer | Length in seconds of the planned break time |
//! | study_time         | integer | Length in seconds of the study time         |
//! | break_time         | integer | Length in seconds of the break time         |
//! | xp                 | integer | gained experience                           |
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
//!     "pet_type" : 0,
//!     "health"   : 0,
//!     "xp"       : 0
//! }
//! ```
//!
//! ## CURL Example:
//! ```bash
//! curl -X POST -F 'user_id=301' -F 'version=1' -F 'planned_study_time=3600' -F 'planned_break_time=60' -F 'study_time=3000' -F 'break_time=120' -F 'gained_xp=5' -F 'health_change=-1' 'http://127.0.0.1:8000/api/log_session'
//! ```

use std::time::SystemTime;

use diesel::{delete, insert_into, update};
use rocket::serde::json::Json;

use crate::{
    api::PET_ROCK_STATUS,
    models::{Pet, Session, User},
    schema::histories,
    *,
};

#[derive(FromForm)]
pub(crate) struct LogSession {
    #[field(name = uncased("user_id"))]
    user: UserID,

    #[field(name = uncased("version"), validate = eq(BACKEND_VERSION))]
    #[allow(dead_code)]
    api_version: AppVer,

    #[field(name = "planned_study_time")]
    plan_study_time: i32,

    #[field(name = "planned_break_time")]
    plan_break_time: i32,

    #[field(name = "study_time")]
    study_time: i32,

    #[field(name = "break_time")]
    break_time: i32,

    #[field(name = "gained_xp")]
    xp_change: i32,

    #[field(name = "health_change")]
    health_change: i32,
}

#[post("/log_session", data = "<session_data>")]
pub(crate) async fn api_log_session(
    db: RevzenDB,
    session_data: Form<LogSession>,
) -> Option<Json<PetStatus>> {
    use crate::schema::{pets::dsl::*, users::dsl::*};

    let LogSession {
        user,
        api_version: _,
        plan_study_time,
        plan_break_time,
        study_time,
        break_time,
        xp_change,
        health_change,
    } = session_data.into_inner();

    match db.run(move |c| users.find(user).first::<User>(c)).await {
        Ok(user_data) => {
            if db
                .run(move |c| {
                    insert_into(histories::table)
                        .values(&Session {
                            sub: user,
                            session_time: SystemTime::now(),
                            plan_study_time,
                            plan_break_time,
                            study_time,
                            break_time,
                        })
                        .execute(c)
                })
                .await
                .is_ok()
            {
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

                                update(users.find(user))
                                    .set(
                                        main_pet.eq(all_pets
                                            .get(0)
                                            .map(|healthiest_pet| healthiest_pet.pet_type)
                                            .unwrap_or_else(|| PET_ROCK)),
                                    )
                                    .execute(c)
                                    .expect("No database issues");

                                PET_ROCK_STATUS
                            } else if curr_health > 5 {
                                update(pets.find((user, user_data.main_pet)))
                                    .set(health.eq(5))
                                    .execute(c)
                                    .expect("No database issues");
                                PetStatus {
                                    pet_type: user_data.main_pet,
                                    health: MAX_HEALTH,
                                    xp: curr_xp,
                                }
                            } else {
                                PetStatus {
                                    pet_type: user_data.main_pet,
                                    health: curr_health,
                                    xp: curr_xp,
                                }
                            }
                        })
                        .await,
                    ))
                } else {
                    Some(Json(PET_ROCK_STATUS))
                }
            } else {
                None
            }
        }
        Err(_) => None,
    }
}
