CREATE OR REPLACE FUNCTION fn_echo_function (
                v_string varchar
                , v_integer integer
                , v_double numeric
                , v_boolean boolean
                , v_instant date
                , v_timestamp timestamp without time zone
                -- , v_date date
                -- , v_time time
                , v_inputstream bytea
                )

RETURNS table(
                c_string varchar
                , c_integer integer
                , c_double numeric
                , c_boolean boolean
                , c_instant date
                , c_timestamp timestamp
                , c_inputstream bytea
                )

LANGUAGE plpgsql
AS $function$
    DECLARE
    BEGIN
        RETURN QUERY
            SELECT
                v_string as c_string,
                v_integer as c_integer,
                v_double as c_double,
                v_boolean as c_boolean,
                v_instant as c_instant,
                v_timestamp as c_timestamp,
                v_inputstream as c_inputstream;
    END;
$function$;
