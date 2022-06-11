use crate::schema::users;
#[derive(Identifiable, Queryable)]
pub struct User {
    pub friendcode: i64,
    pub id: i64,
    pub username: String,
}

#[derive(Insertable)]
#[table_name = "users"]
pub struct AddUser<'a> {
    pub id: i64,
    pub username: &'a str,
}
