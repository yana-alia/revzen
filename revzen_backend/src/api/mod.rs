//! # Revzen API
//!
//! This api provides functionality for:
//! - [Creating entries for new users](create_user)
//! - [Getting user friendcode and username data](login_user)
//! - Setting user's study status to [revising](start_revising) or [not](stop_revising)
//! - [retriving all revising users](get_revising)
//! - [Sending user study session history to the database](log_session)
//! - [Retrieving a user's study history](get_history)
//! - [Getting the user's current pet & its status](get_current_pet)
//! - [Getting the statuses of all a user's pets](get_pet_info)
//! - [Giving user's new pets](give_pet)

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

/// Mapping of (username, friendcode) tuples into [UserDetails] structs
pub(self) fn map_to_details(tuples: Vec<(String, FriendCode, PetType)>) -> Vec<UserDetails> {
    tuples
        .into_iter()
        .map(|(username, friendcode, pet_type)| UserDetails {
            friendcode,
            username,
            main_pet: pet_type,
        })
        .collect()
}

pub type PetType = i32;

pub const PET_ROCK: PetType = 0;
pub const PET_SHIBA: PetType = 1;

/// Maximum health allowed
pub const MAX_HEALTH: i32 = 3;

/// Lowest allowed health (at which point the pet dies)
pub const MIN_HEALTH: i32 = 0;

/// Initial health of a pet once gained.
pub const INITIAL_HEALTH: i32 = 2;

/// The default status when a user has no pets
const PET_ROCK_STATUS: PetStatus = PetStatus {
    pet_type: PET_ROCK,
    health: MIN_HEALTH,
    xp: 0,
};

/// The current main pet's status for a user.
#[derive(Serialize)]
#[serde(crate = "rocket::serde")]
pub struct PetStatus {
    pet_type: PetType,
    health: i32,
    xp: i32,
}

#[derive(FromForm)]
pub struct PetRequest {
    #[field(name = uncased("user_id"))]
    user: UserID,

    #[field(name = uncased("version"), validate = eq(BACKEND_VERSION))]
    #[allow(dead_code)]
    client_version: AppVer,

    #[field(name = uncased("pet_type"), validate = range(1..=3))]
    pet_given_type: PetType,
}

mod change_pet;
mod create_user;
mod get_current_pet;
mod get_follows;
mod get_history;
mod get_pet_info;
mod get_revising;
mod get_user;
mod give_pet;
mod give_reward;
mod log_session;
mod login_user;
mod manage_follows;
mod start_revising;
mod stop_revising;

/// Re-Export the api methods to be used by rocket
pub(crate) use self::{
    change_pet::api_change_pet, create_user::api_create_user, get_current_pet::api_get_current_pet,
    get_follows::api_get_follows, get_history::api_get_history, get_pet_info::api_get_pet_info,
    get_revising::api_get_revising, get_user::api_get_user, give_pet::api_give_pet,
    give_reward::api_give_reward, log_session::api_log_session, login_user::api_login,
    manage_follows::api_manage_friend, start_revising::api_start_revising,
    stop_revising::api_stop_revising,
};
