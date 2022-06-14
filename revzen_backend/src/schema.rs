table! {
    histories (id) {
        id -> Int4,
        sub -> Int8,
        session_time -> Nullable<Timestamp>,
        plan_study_time -> Nullable<Int4>,
        plan_break_time -> Nullable<Int4>,
        study_time -> Nullable<Int4>,
        break_time -> Nullable<Int4>,
    }
}

table! {
    users (id) {
        friendcode -> Int4,
        id -> Int8,
        username -> Varchar,
    }
}

allow_tables_to_appear_in_same_query!(histories, users,);
