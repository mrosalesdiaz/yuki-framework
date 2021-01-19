-- --
-- Create function to create new category
-- --
CREATE OR REPLACE FUNCTION fn_category_create_new( name text) 
RETURNS 
    setof v_categories LANGUAGE plpgsql 
AS $function$ 
DECLARE 
    generated_id bigint;
BEGIN
    
    INSERT INTO product_category (id, name, active) 
    VALUES (DEFAULT, name, true) RETURNING id
    INTO generated_id;

    RETURN query SELECT * FROM v_categories WHERE id = generated_id;
END;
$function$;

COMMENT ON FUNCTION fn_category_create_new (text) IS 'Returns Roman Numeral';