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

use rocket::http::ContentType;
#[macro_use]
extern crate rocket;

/// Basic landing page containing a link to the app to download.
#[get("/")]
fn index() -> (ContentType, &'static str) {
    (ContentType::HTML, include_str!("revzen.html"))
}

#[launch]
fn rocket() -> _ {
    rocket::build().mount("/", routes![index])
}

#[cfg(test)]
mod test {}
