use rocket::{http::ContentType, Request};

/// Basic landing page containing a link to the app to download.
#[get("/")]
pub fn index() -> (ContentType, &'static str) {
    (ContentType::HTML, include_str!("revzen.html"))
}

/// Privacy policy and Terms of Service are required by google oauth2. Hence a
/// page contains the required policy.
#[get("/policy")]
pub fn policy() -> (ContentType, &'static str) {
    (ContentType::HTML, include_str!("policy.html"))
}

#[catch(404)]
pub fn page_not_found(req: &Request) -> (ContentType, String) {
    (
        ContentType::HTML,
        format!(include_str!("handler_404.html"), req.uri().path()),
    )
}
