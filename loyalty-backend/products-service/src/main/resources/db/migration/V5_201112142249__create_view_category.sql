CREATE OR replace VIEW v_categories AS
SELECT
    id
    , name
    , active
FROM
    product_category;