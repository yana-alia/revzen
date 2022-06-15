//! Client revision session history retrieval method of the API.
//!
//! Used for getting the most recent session's details and for displaying user's revision history.
//!
//! ## Post Request Fields
//! ## Post Request Fields
//!
//! | Key            | Type    | Value                                   |
//! |----------------|---------|-----------------------------------------|
//! | user_id        | integer | The google provided 'sub'/subject id.   |
//! | client_version | integer | The api version being used.             |
//!
//! ## Response:
//!
//! | Status          | Meaning                                         |
//! |-----------------|-------------------------------------------------|
//! | 200 - OK        | The user was successfully added, can now login. |
//! | 404 - Not Found | No such account exists.                         |
//!
//! 
//! ## CURL Example:
//! ```bash
//! curl -X POST -F 'user_id=301' -F 'version=1' 'http://127.0.0.1:8000/api/get_history'
//! ```
//! 
//! In event of a 200 - OK the following json is returned
//! ```json
//! [
//!     {
//!         "time": {"secs_since_epoch":1655317746,"nanos_since_epoch":717077000},
//!         "planned_study_time":3600,
//!         "planned_break_time":60,
//!         "study_time":3000,
//!         "break_time":120
//!     },
//!     {
//!         "time": {"secs_since_epoch":1655317761,"nanos_since_epoch":265490000},
//!         "planned_study_time":3600,
//!         "planned_break_time":60,
//!         "study_time":3000,
//!         "break_time":120
//!     },
//!     {
//!         "time": {"secs_since_epoch":1655317763,"nanos_since_epoch":70545000},
//!         "planned_study_time":3600,
//!         "planned_break_time":60,
//!         "study_time":3000,
//!         "break_time":120
//!     }
//! ]
//! ```

use std::time::SystemTime;

use diesel::{dsl::exists, select};
use rocket::serde::{json::Json, Serialize};

use crate::{
    models::History,
    *,
};

/// Used to identify a client (with version number for compatability check)
#[derive(FromForm)]
pub struct Client {
    #[field(name = uncased("user_id"))]
    user: UserID,

    #[field(name = uncased("version"), validate = eq(BACKEND_VERSION))]
    #[allow(dead_code)]
    client_version: AppVer,
}

#[derive(Serialize)]
#[serde(crate = "rocket::serde")]
pub struct StudySession {
    time: SystemTime,
    planned_study_time: i32,
    planned_break_time: i32,
    study_time: i32,
    break_time: i32,
}

#[post("/get_history", data = "<user_auth>")]
pub(crate) async fn api_get_history(
    db: RevzenDB,
    user_auth: Form<Client>,
) -> Option<Json<Vec<StudySession>>> {
    use crate::schema::{
        histories::dsl::*,
        users::dsl::{id as users_id, users},
    };

    db.run(move |c| {
        if let Ok(true) =
            select(exists(users.filter(users_id.eq(user_auth.user)))).get_result::<bool>(c)
        {
            if let Ok(hists) = histories
                .filter(sub.eq(user_auth.user))
                .order(session_time)
                .load::<History>(c)
            {
                Some(Json(hists
                    .into_iter()
                    .map(|entry| StudySession {
                        time: entry.session_time,
                        planned_study_time: entry.plan_study_time,
                        planned_break_time: entry.plan_break_time,
                        study_time: entry.study_time,
                        break_time: entry.break_time,
                    })
                    .collect::<Vec<_>>()))
            } else {
                None
            }
        } else {
            None
        }
    })
    .await
}
