//! # Revzen API
//!
//! This api provides functionality for:
//! - [Creating entries for new users](create_user)
//! - [Getting user friendcode and username data](login_user)
//! - Setting user's study status to [revising](start_revising) or [not](stop_revising)
//! - [retriving all revising users](get_revising)
//! - [Sending user study session history to the database](log_session)
//! - [Retrieving a user's study history](get_history)

use crate::{AppVer, FriendCode, UserID, BACKEND_VERSION};
use rocket::serde::Serialize;

/// Used to identify a client (with version number for compatability check)
#[derive(FromForm)]
pub struct Client {
    #[field(name = uncased("user_id"))]
    user: UserID,

    #[field(name = uncased("version"), validate = eq(BACKEND_VERSION))]
    #[allow(dead_code)]
    client_version: AppVer,
}

/// A basic holder struct for friendcodes and usernames that can be serialized to json.
#[derive(Serialize)]
#[serde(crate = "rocket::serde")]
pub struct FollowDetails {
    friendcode: FriendCode,
    username: String,
}

/// Mapping of (username, friendcode) tuples into [FollowDetails] structs
pub(self) fn map_to_details(tuples: Vec<(String, FriendCode)>) -> Vec<FollowDetails> {
    tuples
        .into_iter()
        .map(|(username, friendcode)| FollowDetails {
            friendcode,
            username,
        })
        .collect()
}

mod create_user;
mod get_follows;
mod get_history;
mod get_revising;
mod get_user;
mod log_session;
mod login_user;
mod manage_follows;
mod start_revising;
mod stop_revising;

/// Re-Export the api methods to be used by rocket
pub(crate) use self::{
    create_user::api_create_user, get_follows::api_get_follows, get_history::api_get_history,
    get_revising::api_get_revising, get_user::api_get_user, log_session::api_log_session,
    login_user::api_login, manage_follows::api_manage_friend, start_revising::api_start_revising,
    stop_revising::api_stop_revising,
};
