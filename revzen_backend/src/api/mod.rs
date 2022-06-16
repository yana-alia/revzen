//! # Revzen API
//!
//! This api provides functionality for:
//! - [Creating entries for new users](create_user)
//! - [Getting user friendcode and username data](login_user)

pub mod create_user;
pub mod get_history;
pub mod get_revising;
pub mod log_session;
pub mod login_user;
pub mod start_revising;
pub mod stop_revising;
pub mod friend_accept;
pub mod friend_request;
pub mod get_live_friends;
