//! The backend for the group 28 DRP project.
//!
//! This backend will perform the following functions:
//! - Provide user authentication information
//! - Store user data (revision time, goals (for feed) and current revision status)
//! - Host a static website for monitoring (debug) and for downloading the android application.
//!
//! ## API
//! The api used to connect to the android application is encapsulated in the [api module](api).
//!
//! ## Database
//! We make use of Heroku Postgres (hobby plan) to host our data, and [diesel] for ORM, migrations.
#![doc(html_logo_url = "https://i.imgur.com/82uGv0e.png")]
#![doc(html_favicon_url = "https://i.imgur.com/82uGv0e.png")]

#[macro_use]
extern crate rocket;
#[macro_use]
extern crate diesel;

use diesel::{insert_into, prelude::*};
use models::AddUser;
use rocket::{
    form::Form,
    http::Status,
};

use rocket_sync_db_pools::database;
use schema::users;

mod api;
mod models;
mod schema;
mod static_pages;

use api::{create_user::api_create_user, login_user::api_login};
use static_pages::{index, page_not_found, policy};

/// User Identification type, common to all part of the api
type UserID = i64;

/// The version type, used to check the clients and backend are using compatible versions.
type AppVer = u32;
const BACKEND_VERSION: AppVer = 0;

/// The revzen database type, which will hold the connection pool used by the application.
#[database("revzen_db")]
struct RevzenDB(diesel::PgConnection);

#[launch]
fn rocket() -> _ {
    rocket::build()
        .mount("/", routes![index, policy])
        .mount("/api", routes![api_login, api_create_user,])
        .register("/", catchers![page_not_found])
        .attach(RevzenDB::fairing())
}
