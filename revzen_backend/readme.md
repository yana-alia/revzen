# Rocket Backend for Revzen

## Framework - Rocket
A backend web framework for rust.

When determining what backend to use, there were several options (e.g Flask, ASP.net, NestJS etc), however rocket was particularly attractive for the following reasons:
- Its Rust (highly safe, eliminates many data races in a concurrent applications such as this, cargo brilliance, high performance)
- Request handlers are type safe
- Low amount of boilerplate code
- Good documentation

We also considered using actix (an actor based framework) which has higher performance, and potential scalability. However it's documentation is not as good, and is more arduous to code than rocket.

## Database - Postgres
Hosted on heroku as a free add-on, provides plenty of features as well as convenient deployment, and ORM + Migrations tools within rust (diesel).

Current setup is explained below:
### Heroku
Using the free Hobby plan for postgres on heroku. Connection details can bbe found inside the settings tab for the postgres addon (under credentials).


### pgAdmin4
![pgadmin4 dashboard](https://i.imgur.com/XbOPwu8.png)
Used to monitor the database, with all views, connections and the contents (tables).

In the image above you can see bothg the pgAdmin connection, as well as a pool of 5 connections used by a running backend.

### Diesel
Diesel manages ORM (object relation management - converting between rust structs and database tables & actions) as well as migrations (updating and changing tables with `up.sql` and `down.sql`).

The diesel command line tool can be installed with rust, as we are using posgres we only need the posgress cli tools.
```bash
# ubuntu to get libpq
sudo apt-get install -y libpq-dev 

# install with only the posgres features
cargo install diesel_cli --no-default-features --features postgres
```

In order to have the diesel cli interact with our heroku database we must use a `.env` file in the crate root with the connection url:
```bash
# e.g for locally interacting, note this changes and can be accessed in the postgres addons settings in 'credentials'
echo 'DATABASE_URL=postgres://xehjgjemxkoevv:56bc4268ea9ec1907ef226cd9bfe46a6490fb87491a3431c5c693fdffd681c2f@ec2-34-231-221-151.compute-1.amazonaws.com:5432/d9j2nr4kjidcog' > .env
```

We can then connect to the database and run our migrations
```bash
# for the help menu
diesel migration help

# when migrations have already been applied, redo can be run to re-apply
diesel migration redo

# to create a new table, we crate a new migration, then can edit the up.sql and down.sql files
diesel migration generate new_table_name
```

### Rocket Integration
A synchronous database connection pool is used by rocket by our handlers in the form of a gurard (seen as a parameter to the handler function)

These are provided by the `rocket_sync_db_pools` crate. Note that this also has diesel and the macros required for simple connection.

When the server runs, it attempts to find a url top connect to. Unlike the diesel CLI, this is passed to rocket via a separate environment variable. It is possible to set this, however as the same crate will be deployed on different heroku apps (with different database urls) we use heroku to launch with the environment variable.
```bash
# We set environment variables for rocket to run with
ROCKET_DATABASES="{revzen_db={url=\"$DATABASE_URL\", timout=20, pool_size=1}}" ROCKET_PORT=$PORT ROCKET_ADDRESS=0.0.0.0 ./target/release/revzen_backend
```
Note that the pool size is very important. Heroku's postgres hobby plan (which we are using) only allows 20 connections, so with pgAdmin on, and diesel cli use, you may get timeouts when out of connections.

## Utility
The basic `util.sh` bash script can be used for checking the backend passes test, lint checks as well as running the server locally (with database connection) for testing.

```bash
./util.sh -h

'This is the basic backend utility
-h  Display this help text!
-r  Run the server locally (debug) connecting to the database as specified by the variables in util.sh
-c  Check the repo will past test, lint and format locally before running the gitlab pipeline
-d  Build, then open the documentation in the browser.'
```
