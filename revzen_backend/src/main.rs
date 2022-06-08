//! The backend for the group 28 DRP project.
//!
//! This backend will perform the following functions:
//! - Provide user authentication information
//! - Store user data (revision time, goals (for feed) and current revision status)
//!
//! # Login
//! We will use google as our login service, to get a unique identifier for each
//! user (for use in database).
//!
//! This is explained [here](https://developers.google.com/identity/sign-in/web/backend-auth)
//!
//! # API
//! The main functions of the api are:
//! - Get user's revision history
//! - Get user's friend list
//! - Get if user is online
//! - Get new feed items from friends
//! - Post new revision sessions successes/failures
//! - Post new friend requests and friend accepts
//! - Post when revising, when revising ends.
//!
//! # Live updating
//! We can make use of [server sent events/streams](https://rocket.rs/v0.5-rc/guide/responses/#async-streams), as well as by continually polling the api per fixed time.
//!
//! This is required in order to get data on live friend requests/accepts and currently live revision sessions.
#![doc(html_logo_url = "https://i.imgur.com/82uGv0e.png")]
#![doc(html_favicon_url = "https://i.imgur.com/82uGv0e.png")]

use std::collections::HashMap;

use rocket::{http::{ContentType, Status}, tokio::sync::RwLock, State};
#[macro_use]
extern crate rocket;

/// Basic landing page containing a link to the app to download.
#[get("/")]
fn index() -> (ContentType, &'static str) {
    (ContentType::HTML, include_str!("static_pages/revzen.html"))
}

/// Privacy policy and Terms of Service are required by google oauth2. Hence a
/// page contains these
#[get("/policy")]
fn policy() -> (ContentType, &'static str) {
    (ContentType::HTML, include_str!("static_pages/policy.html"))
}

#[get("/revising")]
async fn revising(state: &State<RwLock<RevZenState>>) -> (ContentType, String) {
    (ContentType::Text, format!("{:?}",state.read().await.users))
}


type UserID = u128;

/// Holds the current state of the webserver. Currently the map of users.
struct RevZenState {
    users: HashMap<UserID, bool>
}

impl RevZenState {
    fn new() -> Self {
        RevZenState { users: HashMap::new() }
    }

    fn user_revise(&mut self, user: UserID) {
        self.users.insert(user, true);
    }

    fn user_no_revise(&mut self, user: UserID) {
        self.users.insert(user, false);
    }
}

#[get("/login/<user>")]
async fn api_login(state: &State<RwLock<RevZenState>>, user: UserID) -> Status {
    state.write().await.user_no_revise(user);
    Status::Ok
}

#[get("/revise/<user>")]
async fn api_start_revision(state: &State<RwLock<RevZenState>>, user: UserID) -> Status {
    state.write().await.user_revise(user);
    Status::Ok
}

#[get("/end_revise/<user>")]
async fn api_end_revision(state: &State<RwLock<RevZenState>>, user: UserID) -> Status {
    state.write().await.user_no_revise(user);
    Status::Ok
}

#[launch]
fn rocket() -> _ {
    rocket::build()
        .mount("/", routes![index, policy, revising])
        .mount("/api", routes![api_login, api_start_revision, api_end_revision])
        .manage(RwLock::from(RevZenState::new()))
}

#[cfg(test)]
mod test {}
