use crate::schema::users;
#[derive(Identifiable, Queryable)]
pub struct User {
    pub friendcode: i32,
    pub id: i64,
    pub username: String,
}

#[derive(Insertable)]
#[table_name = "users"]
pub struct AddUser {
    pub id: i64,
    pub username: String,
}
