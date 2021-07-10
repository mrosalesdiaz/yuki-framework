-- List all functions defined in a schema passed as parameter
SELECT
    p.oid AS "id"
    , n.nspname AS "schemaName"
    , proname AS "functionName"
    , pa.hasMany AS "hasMany"
    , pg_catalog.pg_get_function_arguments(p.oid) AS "functionArguments"
    , r.typname AS "functionReturn"
FROM
    pg_catalog.pg_proc p
LEFT JOIN pg_catalog.pg_namespace n ON
    n.oid = p.pronamespace
LEFT JOIN pg_type r
  ON r.oid = p.prorettype
LEFT JOIN (
        SELECT
            count(p1.proname) > 1 AS hasMany
            , array_agg(p1."oid") fn_ids
        FROM
            pg_catalog.pg_proc p1
        WHERE
            p1.proname LIKE 'fn_%'
        GROUP BY
            p1.proname
    ) AS pa ON
    p."oid" = ANY(pa.fn_ids)
WHERE
    p.proname LIKE 'fn_%'
    AND n.nspname = ?;