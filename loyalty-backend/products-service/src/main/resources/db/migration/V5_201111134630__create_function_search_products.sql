-- BEGIN;

-- -- CREATE FUNCTION "fn_get_products( text )" -------------------
-- CREATE OR REPLACE FUNCTION fn_product_search_index( title text, keywords text )
--  RETURNS setof v_product_index
--  LANGUAGE plpgsql
-- AS $function$
-- 	 DECLARE BEGIN
-- 	 return query select * from v_product_index;
-- 	END; 
-- 	$function$;
-- -- -------------------------------------------------------------
-- COMMIT;

-- COMMENT ON FUNCTION fn_product_search_index ( text, text ) IS 'Returns Roman Numeral';

