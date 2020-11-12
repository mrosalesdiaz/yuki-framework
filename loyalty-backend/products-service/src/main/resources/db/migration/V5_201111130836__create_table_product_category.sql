create table product_category (
	id SERIAL PRIMARY KEY,
	name TEXT
);

ALTER TABLE product 
ADD CONSTRAINT product_fk_category 
FOREIGN KEY (category_id) 
REFERENCES product_category (id);