//! # Revzen API
//!
//! This api provides functionality for:
//! - [Creating entries for new users](create_user)
//! - [Getting user friendcode and username data](login_user)
//! - Setting user's study status to [revising](start_revising) or [not](stop_revising)
//! - [retriving all revising users](get_revising)
//! - [Sending user study session history to the database](log_session)
//! - [Retrieving a user's study history](get_history)

use std::collections::HashMap;

use crate::{AppVer, FriendCode, UserID, BACKEND_VERSION};
use rocket::{serde::Serialize, tokio::sync::RwLock};

/// Managing live user's revising
pub struct StudyState(RwLock<HashMap<UserID, UserDetails>>);

impl StudyState {
    pub fn new() -> Self {
        StudyState(RwLock::from(HashMap::new()))
    }
}

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
#[derive(Serialize, Clone)]
#[serde(crate = "rocket::serde")]
pub struct UserDetails {
    friendcode: FriendCode,
    username: String,
    main_pet: PetType,
}

/// Mapping of (username, friendcode) tuples into [FollowDetails] structs
pub(self) fn map_to_details(tuples: Vec<(String, FriendCode, i32)>) -> Vec<UserDetails> {
    tuples
        .into_iter()
        .map(|(username, friendcode, pet_type)| UserDetails {
            friendcode,
            username,
            main_pet: pet_type.into(),
        })
        .collect()
}

#[derive(Serialize, Clone, PartialEq, Eq, Hash)]
#[serde(crate = "rocket::serde")]
pub(self) enum PetType {
    Rock,
    Shiba,
    Husky,
    Calico,
}

impl From<i32> for PetType {
    fn from(t: i32) -> Self {
        match t {
            0 => PetType::Rock,
            1 => PetType::Shiba,
            2 => PetType::Husky,
            3 => PetType::Calico,
            t => panic!("Invalid pet type used: {}", t),
        }
    }
}

mod create_user;
mod get_follows;
mod get_history;
mod get_pet_info;
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
    get_pet_info::api_get_pet_info, get_revising::api_get_revising, get_user::api_get_user,
    log_session::api_log_session, login_user::api_login, manage_follows::api_manage_friend,
    start_revising::api_start_revising, stop_revising::api_stop_revising,
};
