# Used by heroku to launch the server

# Postgres variables
POSTGRES_TIMEOUT=10
POSTGRES_CONNS=5

# start the server
ROCKET_DATABASES="{revzen_db={url=\"$DATABASE_URL\", timeout=$POSTGRES_TIMEOUT, pool_size=$POSTGRES_CONNS}}" ROCKET_PORT=$PORT ROCKET_ADDRESS=0.0.0.0 ./target/release/revzen_backend