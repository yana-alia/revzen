//! # Revzen API
//!
//! This api provides functionality for:
//! - [Creating entries for new users](create_user)
//! - [Getting user friendcode and username data](login_user)
//! - Setting user's study status to [revising](start_revising) or [not](stop_revising)
//! - [retriving all revising users](get_revising)
//! - [Sending user study session history to the database](log_session)
//! - [Retrieving a user's study history](get_history)

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

pub mod create_user;
pub mod friend_accept;
pub mod friend_request;
pub mod get_history;
pub mod get_live_friends;
pub mod get_revising;
pub mod log_session;
pub mod login_user;
pub mod start_revising;
pub mod stop_revising;
