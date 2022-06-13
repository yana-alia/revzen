//! Models used to extract from and insert into the database.
use crate::schema::users;

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
