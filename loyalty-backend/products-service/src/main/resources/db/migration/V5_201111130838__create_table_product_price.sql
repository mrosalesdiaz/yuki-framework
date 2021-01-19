-- --
-- Create table to store historic of prices
-- --
CREATE TABLE product_price( id serial PRIMARY KEY
, product_id bigint
, price money
, start_date TIMESTAMP
, end_date TIMESTAMP
, CURRENT boolean
, FOREIGN KEY(product_id) REFERENCES product(id) );