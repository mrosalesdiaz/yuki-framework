create table product (
	id SERIAL PRIMARY KEY,
	title VARCHAR(50),
	description TEXT,
	category VARCHAR(50),
	price DECIMAL(5,2)
);