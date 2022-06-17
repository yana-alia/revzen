//! Models used to extract from and insert into the database.
use std::time::SystemTime;

use crate::schema::{histories, users};

/// User struct representing a record in the users table
#[derive(Identifiable, Queryable)]
pub struct User {
    pub friendcode: i32,
    pub id: i64,
    pub username: String,
}

/// Data required to insert the user (friendcode is a serial - automatically
/// generated number incremented for each new record)
#[derive(Insertable)]
#[table_name = "users"]
pub struct AddUser {
    pub id: i64,
    pub username: String,
}

/// Data required to add a user session to the histories table
#[derive(Insertable, Queryable)]
#[table_name = "histories"]
pub struct Session {
    pub sub: i64,
    pub session_time: SystemTime,
    pub plan_study_time: i32,
    pub plan_break_time: i32,
    pub study_time: i32,
    pub break_time: i32,
}

#[derive(Identifiable, Queryable)]
#[table_name = "histories"]
pub struct History {
    pub id: i32,
    pub sub: i64,
    pub session_time: SystemTime,
    pub plan_study_time: i32,
    pub plan_break_time: i32,
    pub study_time: i32,
    pub break_time: i32,
}
