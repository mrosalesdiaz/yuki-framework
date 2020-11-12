create table product_text(
	id serial primary key,
	product_id bigint,
	key text,
	content text,
	current boolean,
	creation_date TIMESTAMP,
	fOREIGN KEY(product_id) references product(id)
);