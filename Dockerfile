FROM rust:latest
COPY ./ ./
RUN cd revzen_backend 
RUN cargo build --release
