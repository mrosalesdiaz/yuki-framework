-- --
-- Create table to store product text entries
-- --
CREATE TABLE product_text( id serial PRIMARY KEY
, product_id bigint
, KEY text
, content text
, CURRENT boolean
, creation_date TIMESTAMP
, FOREIGN KEY(product_id) REFERENCES product(id) );