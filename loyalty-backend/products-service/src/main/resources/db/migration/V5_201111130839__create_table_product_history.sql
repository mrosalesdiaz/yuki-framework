create table product_history(
	id serial primary key,
	product_id bigint,
	actions text,
	details text,
	creation_date TIMESTAMP,
	fOREIGN KEY(product_id) references product(id)
	);