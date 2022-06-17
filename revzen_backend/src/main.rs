//! The backend for the group 28 DRP project.
//!
//! This backend will perform the following functions:
//! - Provide user authentication information
//! - Store user data (revision time, goals (for feed) and current revision status)
//! - Host a static website for monitoring (debug) and for downloading the android application.
//!
//! ## API
//! The api used to connect to the android application is encapsulated in the api module.
//!
//! ## Database
//! We make use of Heroku Postgres (hobby plan) to host our data, and [diesel] for ORM, migrations.
#![doc(html_logo_url = "https://i.imgur.com/CgmGXqI.png")]
#![doc(html_favicon_url = "https://i.imgur.com/CgmGXqI.png")]

#[macro_use]
extern crate rocket;
#[macro_use]
extern crate diesel;

use std::collections::HashMap;

use diesel::{insert_into, prelude::*};
use models::AddUser;
use rocket::{form::Form, http::Status, tokio::sync::RwLock};

use rocket_sync_db_pools::database;
use schema::users;

mod api;
mod models;
mod pages;
mod schema;

use api::{
    create_user::api_create_user, get_history::api_get_history, log_session::api_log_session,
    login_user::api_login, start_revising::api_start_revising
};
use pages::{index, internal_error, page_not_found, policy};

/// User Identification type, common to all part of the api
type UserID = i64;

/// The version type, used to check the clients and backend are using compatible versions.
///
/// When an incorrect version is used, the client will get a 422 - Unprocessable Entity
/// Status returned (and hence knows to inform the user they must update their application).
type AppVer = u32;
const BACKEND_VERSION: AppVer = 1;

/// Managing live user's revising
struct StudyState (RwLock<HashMap<UserID, (i32, String)>>);

/// The revzen database type, which will hold the connection pool used by the application.
#[database("revzen_db")]
struct RevzenDB(diesel::PgConnection);

#[launch]
fn rocket() -> _ {
    rocket::build()
        .mount("/", routes![index, policy])
        .mount(
            "/api",
            routes![api_login, api_create_user, api_log_session, api_get_history, api_start_revising],
        )
        .register("/", catchers![page_not_found, internal_error])
        .attach(RevzenDB::fairing())
        .manage(StudyState(RwLock::from(HashMap::new())))
}
