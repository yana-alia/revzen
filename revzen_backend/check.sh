echo "Checking the repo will pass basic CI"
echo "TESTING..."
cargo test
echo "LINTING..."
cargo clippy
echo "FORMATTING..."
cargo fmt