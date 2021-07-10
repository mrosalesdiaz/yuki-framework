-- List all vies defined in a schema passed as parameter
SELECT
  table_schema
  , table_name
  , column_name
  , data_type
FROM
  information_schema.columns
WHERE
  table_schema = ?
  AND LOWER(table_name) LIKE 'v\_%';