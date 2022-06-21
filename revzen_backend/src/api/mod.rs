//! # Revzen API
//!
//! This api provides functionality for:
//! - [Creating entries for new users](create_user)
//! - [Getting user friendcode and username data](login_user)
//! - Setting user's study status to [revising](start_revising) or [not](stop_revising)
//! - [retriving all revising users](get_revising)
//! - [Sending user study session history to the database](log_session)
//! - [Retrieving a user's study history](get_history)

use rocket::http::Status;

use crate::{AppVer, UserID, BACKEND_VERSION};

/// Used to identify a client (with version number for compatability check)
#[derive(FromForm)]
pub struct Client {
    #[field(name = uncased("user_id"))]
    user: UserID,

    #[field(name = uncased("version"), validate = eq(BACKEND_VERSION))]
    #[allow(dead_code)]
    client_version: AppVer,
}

/// If there is  an executable database task, return 200 - Ok on success, otherwise an internal server error.
fn check_execute<S, E>(res: Result<S, E>) -> Status {
    match res {
        Ok(_) => Status::Ok,
        Err(_) => Status::InternalServerError,
    }
}

pub mod create_user;
pub mod get_history;
pub mod get_revising;
pub mod log_session;
pub mod login_user;
pub mod manage_friends;
pub mod start_revising;
pub mod stop_revising;
