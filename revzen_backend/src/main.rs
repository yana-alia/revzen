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
//!
//! # Live updating
//! We can make use of [server sent events/streams](https://rocket.rs/v0.5-rc/guide/responses/#async-streams), as well as by continually polling the api per fixed time.
//!
//! This is required in order to get data on live friend requests/accepts and currently live revision sessions.
#![doc(html_logo_url = "https://i.imgur.com/82uGv0e.png")]
#![doc(html_favicon_url = "https://i.imgur.com/82uGv0e.png")]

#[macro_use]
extern crate rocket;
#[macro_use]
extern crate diesel;

mod models;
mod schema;

use diesel::{dsl::Find, insert_into, prelude::*, select};
use models::AddUser;
use rocket::{
    form::Form,
    http::{ContentType, Status},
    tokio::sync::RwLock,
    State,
};
use rocket_sync_db_pools::database;
use schema::users;
use std::{collections::HashMap, time::SystemTime};

use crate::models::User;

/// The revzen database type, which will hold the connection pool used by the application.
#[database("revzen_db")]
struct RevzenDB(diesel::PgConnection);

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

/// The version number, use to check the clients and backend are using compatible versions.
const BACKEND_VERSION: AppVer = 0;

type UserID = i64;
type AppVer = u32;

/// Used to identify a client (with version number for compatability check)
#[derive(FromForm)]
struct Client {
    #[field(name = uncased("user_id"))]
    user: UserID,

    #[field(name = uncased("version"), validate = eq(BACKEND_VERSION))]
    client_version: AppVer,
}

#[post("/login", data = "<user_auth>")]
async fn api_login(db: RevzenDB, user_auth: Form<Client>) -> Status {
    use crate::schema::users::dsl::*;
    
    todo!()
}

#[post("/create/<name>", data = "<user_auth>")]
async fn api_create_user(db: RevzenDB, name: &str, user_auth: Form<Client>) -> Status {
    println!(
        "/create: User: {} running version: {} created with name {}",
        user_auth.user, user_auth.client_version, name
    );
    let res = db
        .run(move |c| {
            insert_into(users::table)
                .values(&AddUser {
                    id: user_auth.user,
                    username: "hi",
                })
                .execute(c)
        })
        .await;
    println!("{:?}", res);
    Status::Ok
}

#[derive(FromForm)]
struct ClientRevise {
    #[field(name = uncased("user_id"))]
    user: UserID,

    #[field(name = uncased("version"), validate = eq(BACKEND_VERSION))]
    client_version: AppVer,

    #[field(name = uncased("rev_time"), validate = range(0..7200))]
    revision_time: u64,
}

struct RevisionStates {
    users_revising: HashMap<UserID, (SystemTime, u64)>,
    events: Vec<(UserID, u64, u64)>,
}

impl RevisionStates {
    fn new() -> Self {
        RevisionStates {
            users_revising: HashMap::new(),
            events: Vec::new(),
        }
    }

    fn start_revising(&mut self, user: UserID, expected: u64) {
        self.users_revising
            .insert(user, (SystemTime::now(), expected));
    }

    fn remove_revision(&mut self, user: UserID) {
        if let Some((time, exp)) = self.users_revising.remove(&user) {
            self.events
                .push((user, exp, time.elapsed().unwrap().as_secs() as u64))
        }
    }
}

#[post("/revise", data = "<user_data>")]
async fn api_user_revise(
    revstate: &State<RwLock<RevisionStates>>,
    user_data: Form<ClientRevise>,
) -> Status {
    println!(
        "/revise: User: {} running version: {} is going to revise for {} minutes",
        user_data.user, user_data.client_version, user_data.revision_time
    );
    revstate
        .write()
        .await
        .start_revising(user_data.user, user_data.revision_time);
    Status::Ok
}

#[post("/stop_revise", data = "<user_data>")]
async fn api_user_stop_revise(
    revstate: &State<RwLock<RevisionStates>>,
    user_data: Form<Client>,
) -> Status {
    println!(
        "/stop_revise: User: {} running version: {} has stopped revising",
        user_data.user, user_data.client_version,
    );
    revstate.write().await.remove_revision(user_data.user);
    Status::Ok
}

#[launch]
fn rocket() -> _ {
    rocket::build()
        .mount("/", routes![index, policy])
        .mount(
            "/api",
            routes![
                api_login,
                api_create_user,
                api_user_revise,
                api_user_stop_revise
            ],
        )
        .manage(RwLock::from(RevisionStates::new()))
        .attach(RevzenDB::fairing())
}

#[cfg(test)]
mod test {}
