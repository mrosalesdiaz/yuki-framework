-- --
-- Create table to store category 
-- --
CREATE TABLE product_category (
    id SERIAL PRIMARY KEY
    , name TEXT
    , active boolean
);

-- Adding product foreign key
ALTER TABLE product ADD CONSTRAINT product_fk_category FOREIGN KEY (category_id) REFERENCES product_category (id);