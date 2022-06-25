table! {
    follows (followee, follower) {
        followee -> Int8,
        follower -> Int8,
        accepted -> Bool,
    }
}

table! {
    histories (id) {
        id -> Int4,
        sub -> Int8,
        session_time -> Timestamp,
        plan_study_time -> Int4,
        plan_break_time -> Int4,
        study_time -> Int4,
        break_time -> Int4,
    }
}

table! {
    pets (user_id, pet_type) {
        user_id -> Int8,
        pet_type -> Int4,
        health -> Int4,
        xp -> Int4,
    }
}

table! {
    users (id) {
        friendcode -> Int4,
        id -> Int8,
        username -> Varchar,
        main_pet -> Int4,
    }
}

allow_tables_to_appear_in_same_query!(follows, histories, pets, users,);
