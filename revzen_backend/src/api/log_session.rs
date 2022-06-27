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
//! curl -X POST -F 'user_id=301' -F 'version=3' -F 'planned_study_time=3600' -F 'planned_break_time=60' -F 'study_time=3000' -F 'break_time=120' 'http://127.0.0.1:8000/api/log_session'
//! ```

use std::time::SystemTime;

use diesel::insert_into;

use crate::{
    models::{Session, User},
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
}

#[post("/log_session", data = "<session_data>")]
pub(crate) async fn api_log_session(db: RevzenDB, session_data: Form<LogSession>) -> Status {
    use crate::schema::users::dsl::*;

    let LogSession {
        user,
        api_version: _,
        plan_study_time,
        plan_break_time,
        study_time,
        break_time,
    } = session_data.into_inner();

    if db
        .run(move |c| users.find(user).first::<User>(c))
        .await
        .is_ok()
    {
        db.run(move |c| {
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
        .expect("No database errors");

        Status::Ok
    } else {
        Status::NotFound
    }
}
