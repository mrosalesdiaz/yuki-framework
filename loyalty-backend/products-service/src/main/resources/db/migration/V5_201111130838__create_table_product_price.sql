	create table product_price(
	id serial primary key,
	product_id bigint,
	price money,
	start_date TIMESTAMP,
	end_date TIMESTAMP,
	current boolean,
	fOREIGN KEY(product_id) references product(id)
	);