BEGIN;

-- CREATE FUNCTION "fn_get_products( text )" -------------------
CREATE OR REPLACE FUNCTION fn_category_create_new( name text)
 RETURNS setof v_categories
 LANGUAGE plpgsql
AS $function$
	 DECLARE 
        generated_id bigint;
	 BEGIN
        insert into product_category (id, name) values (DEFAULT,name) RETURNING id INTO generated_id;
	 
        return query select * from v_categories where id = generated_id;
	END; 
	$function$;
-- -------------------------------------------------------------
COMMIT;

COMMENT ON FUNCTION fn_category_create_new ( text ) IS 'Returns Roman Numeral';