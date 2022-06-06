//! The backend for the group 28 DRP project.
//!
//! This backend will perform the following functions:
//! - Provide user authentication information
//! - Store user data (revision time, goals (for feed) and current revision status)
#![doc(html_logo_url = "https://i.imgur.com/82uGv0e.png")]
#![doc(html_favicon_url = "https://i.imgur.com/82uGv0e.png")]
#[macro_use]
extern crate rocket;

#[get("/")]
fn index() -> &'static str {
    "Hello, this is the Group 28 DRP project backend!"
}

#[launch]
fn rocket() -> _ {
    rocket::build().mount("/", routes![index])
}

#[cfg(test)]
mod test {

    #[test]
    fn this_is_a_test() {
        assert!(true)
    }
}
