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
//! ## CURL Example:
//! ```bash
//! curl -X POST -F 'user_id=301' -F 'version=1' -F 'planned_study_time=3600' -F 'planned_break_time=60' -F 'study_time=3000' -F 'break_time=120' 'http://127.0.0.1:8000/api/log_session'
//! ```

use std::time::SystemTime;

use diesel::{dsl::exists, select};

use crate::{models::Session, schema::histories, *};

#[derive(FromForm)]
pub(crate) struct LogSession {
    #[field(name = uncased("user_id"))]
    user: UserID,

    #[field(name = uncased("version"), validate = eq(BACKEND_VERSION))]
    #[allow(dead_code)]
    api_version: AppVer,

    #[field(name = "planned_study_time")]
    planned_study_time: i32,

    #[field(name = "planned_break_time")]
    planned_break_time: i32,

    #[field(name = "study_time")]
    study_time: i32,

    #[field(name = "break_time")]
    break_time: i32,
}

#[post("/log_session", data = "<session_data>")]
pub(crate) async fn api_log_session(db: RevzenDB, session_data: Form<LogSession>) -> Status {
    use crate::schema::users::dsl::*;

    db.run(move |c| {
        if let Ok(user) =
            select(exists(users.filter(id.eq(session_data.user)))).get_result::<bool>(c)
        {
            if user {
                if insert_into(histories::table)
                    .values(&Session {
                        sub: session_data.user,
                        session_time: SystemTime::now(),
                        plan_study_time: session_data.planned_study_time,
                        plan_break_time: session_data.planned_break_time,
                        study_time: session_data.study_time,
                        break_time: session_data.break_time,
                    })
                    .execute(c)
                    .is_ok()
                {
                    Status::Ok
                } else {
                    Status::InternalServerError
                }
            } else {
                Status::NotFound
            }
        } else {
            // Database has failed to respond
            Status::InternalServerError
        }
    })
    .await
}
