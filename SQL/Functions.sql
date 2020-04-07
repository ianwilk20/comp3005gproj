-- Functions --

-- USER --

CREATE OR REPLACE FUNCTION insert_users_billing (IN u_acc_no BIGINT, IN u_postal_co VARCHAR, IN u_street VARCHAR, IN u_city VARCHAR, 
									  IN u_credit_card BIGINT, IN u_prov VARCHAR, IN u_country VARCHAR) RETURNS void AS $$
BEGIN
	IF NOT EXISTS (SELECT * FROM postal_address 
                   WHERE postal_code = u_postal_co)
	THEN			   
    	INSERT INTO postal_address (postal_code, province, country)
   		VALUES (u_postal_co, u_prov, u_country);	
	END IF;	
	
	IF NOT EXISTS (SELECT * FROM address 
                   WHERE postal_code = u_postal_co
				   AND street = u_street
				   AND city = u_city)
	THEN
		INSERT INTO address (postal_code, street, city)
		VALUES (u_postal_co, u_street, u_city);
	END	IF;
		
	IF NOT EXISTS (SELECT * FROM users_billing
                   WHERE account_no = u_acc_no
				   AND postal_code = u_postal_co
				   AND street = u_street
				   AND city = u_city
				   AND credit_card = u_credit_card)	
	THEN			   
		INSERT INTO users_billing (account_no, postal_code, street, city, credit_card)
		VALUES (u_acc_no, u_postal_co, u_street, u_city, u_credit_card);
	END IF;	
END;
$$ LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION insert_users_shipping (IN u_acc_no BIGINT, IN u_postal_co VARCHAR, IN u_street VARCHAR, IN u_city VARCHAR, 
												  IN u_prov VARCHAR, IN u_country VARCHAR) RETURNS void AS $$
BEGIN
	IF NOT EXISTS (SELECT * FROM postal_address 
                   WHERE postal_code = u_postal_co)
	THEN			   
    	INSERT INTO postal_address (postal_code, province, country)
   		VALUES (u_postal_co, u_prov, u_country);	
	END IF;	
	
	IF NOT EXISTS (SELECT * FROM address 
                   WHERE postal_code = u_postal_co
				   AND street = u_street
				   AND city = u_city)
	THEN
		INSERT INTO address (postal_code, street, city)
		VALUES (u_postal_co, u_street, u_city);
	END	IF;
		
	IF NOT EXISTS (SELECT * FROM users_shipping
                   WHERE account_no = u_acc_no
				   AND postal_code = u_postal_co
				   AND street = u_street
				   AND city = u_city)	
	THEN			   
		INSERT INTO users_shipping (account_no, postal_code, street, city)
		VALUES (u_acc_no, u_postal_co, u_street, u_city);
	END IF;	
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION insert_order (IN u_account_no BIGINT) RETURNS void AS $$
BEGIN
	
	INSERT INTO orders(serial_no, order_status, purchase_date)
	SELECT checkout.serial_no, 'Order Created', CURRENT_TIMESTAMP
	FROM checkout
	where checkout.account_no = u_account_no;
	
	
	INSERT INTO users_orders(order_no, account_no)
	SELECT orders.order_no, u_account_no
	FROM orders, checkout
	WHERE orders.serial_no = checkout.serial_no and account_no = u_account_no;
	
	
	UPDATE book
	SET sold = true
	WHERE book.serial_no IN (SELECT serial_no
					   		FROM users_orders, orders
						   	WHERE users_orders.order_no = orders.order_no and users_orders.account_no = u_account_no);
							

END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION copy_user_addresses_to_order (IN u_account_no BIGINT) RETURNS void AS $$
BEGIN

INSERT INTO orders_billing(order_no, postal_code, street, city, credit_card)
SELECT order_no, postal_code, street, city, credit_card 
FROM users_orders NATURAL JOIN checkout NATURAL JOIN users_billing natural join orders
WHERE users_orders.account_no = checkout.account_no AND users_orders.account_no = u_account_no;

INSERT INTO orders_shipping(order_no, postal_code, street, city)
SELECT order_no, postal_code, street, city
FROM users_orders NATURAL JOIN checkout NATURAL JOIN users_shipping natural join orders
WHERE users_orders.account_no = checkout.account_no AND users_orders.account_no = u_account_no;
							   

END;
$$ LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION delete_checkout (IN u_account_no BIGINT) RETURNS void AS $$
BEGIN

DELETE FROM checkout 
	WHERE serial_no IN (SELECT serial_no
					   	FROM users_orders, orders
						WHERE users_orders.order_no = orders.order_no and users_orders.account_no = u_account_no);

END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION insert_order_shipping (IN u_account_no BIGINT, IN u_postal_co VARCHAR, IN u_street VARCHAR, IN u_city VARCHAR, 
												  IN u_prov VARCHAR, IN u_country VARCHAR) RETURNS void AS $$

DECLARE 	i record;
BEGIN
	IF NOT EXISTS (SELECT * FROM postal_address 
                   WHERE postal_code = u_postal_co)
	THEN			   
    	INSERT INTO postal_address (postal_code, province, country)
   		VALUES (u_postal_co, u_prov, u_country);	
	END IF;	
	
	IF NOT EXISTS (SELECT * FROM address 
                   WHERE postal_code = u_postal_co
				   AND street = u_street
				   AND city = u_city)
	THEN
		INSERT INTO address (postal_code, street, city)
		VALUES (u_postal_co, u_street, u_city);
	END	IF;
					  
	FOR i IN SELECT order_no
			  FROM checkout natural join orders
			  WHERE checkout.account_no = u_account_no and checkout.serial_no = orders.serial_no
	LOOP
				   
		INSERT INTO orders_shipping (order_no, postal_code, street, city)
		VALUES (i.order_no, u_postal_co, u_street, u_city);
	
	END LOOP;
	
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION insert_order_billing (IN u_account_no BIGINT, IN u_postal_co VARCHAR, IN u_street VARCHAR, IN u_city VARCHAR, 
												  IN u_prov VARCHAR, IN u_country VARCHAR, IN u_credit_card BIGINT) RETURNS void AS $$

DECLARE 	i record;
BEGIN
	IF NOT EXISTS (SELECT * FROM postal_address 
                   WHERE postal_code = u_postal_co)
	THEN			   
    	INSERT INTO postal_address (postal_code, province, country)
   		VALUES (u_postal_co, u_prov, u_country);	
	END IF;	
	
	IF NOT EXISTS (SELECT * FROM address 
                   WHERE postal_code = u_postal_co
				   AND street = u_street
				   AND city = u_city)
	THEN
		INSERT INTO address (postal_code, street, city)
		VALUES (u_postal_co, u_street, u_city);
	END	IF;
					  
	FOR i IN SELECT order_no
			  FROM checkout natural join orders
			  WHERE checkout.account_no = u_account_no and checkout.serial_no = orders.serial_no
	LOOP
				   
		INSERT INTO orders_billing (order_no, postal_code, street, city, credit_card)
		VALUES (i.order_no, u_postal_co, u_street, u_city, u_credit_card);
	
	END LOOP;
	
END;
$$ LANGUAGE plpgsql;

-- OWNER --


CREATE OR REPLACE FUNCTION addOwnerToDB (IN u_email VARCHAR, IN u_threshold INT, IN u_p_number NUMERIC) RETURNS integer AS $$
BEGIN			   
	INSERT INTO Owners
	VALUES (default, u_email, u_threshold);	
	
	IF NOT EXISTS (SELECT * FROM Phone_Number 
                   WHERE p_number = u_p_number)
	THEN
		INSERT INTO Phone_Number
		VALUES (u_p_number);
	END	IF;
					   
	INSERT INTO Owners_Phone
	SELECT p_number, owner_id
    FROM Phone_Number, Owners
    WHERE p_number = u_p_number AND email = u_email;
	
	RETURN(SELECT owner_id FROM Owners WHERE email = u_email);
END;
$$ LANGUAGE plpgsql;

create or replace function addAuthorToDB (in u_author_name varchar, in u_serial_no numeric) returns void as $$
begin			   	
	if not exists (select * from Author 
                   where author_name = u_author_name)
	then
		insert into Author
		values (default, u_author_name);
	end if;
					   
	insert into Book_Author
	select author_id, u_serial_no
    from Author
    where author_name = u_author_name;
	
end;
$$ language plpgsql;

create or replace function addBookToDB (in u_serial_no numeric, in u_book_name varchar, in owner_id int) returns void as $$
begin			   	
						   
	insert into Book
	select u_serial_no, Book_ISBN.ISBN, false
	from Book_ISBN
	where Book_ISBN.book_name = u_book_name;

	insert into Inventory
	values(u_serial_no, owner_id);
	
end;
$$ language plpgsql;

create or replace function addBook_ISBNToDB (in u_ISBN numeric, in u_book_name varchar, in u_genre varchar,
											in u_no_pages int, in u_cost_price real, in u_sales_price real,
											in u_percent_to_pub real, in u_pub_name varchar) returns void as $$
begin			   	
	insert into Book_ISBN
	select Publisher.bank_account, u_ISBN, u_book_name, u_genre, u_no_pages, u_cost_price, u_sales_price, u_percent_to_pub
	from Publisher
	where Publisher.pub_name = u_pub_name;
		
end;
$$ language plpgsql;

create or replace function addPublisherToDB (in u_bank_account numeric, in u_pub_name varchar, in u_email varchar, in u_p_number numeric,
											 in u_postal_code varchar, in u_prov varchar, in u_country varchar, 
											 in u_street varchar, in u_city varchar) returns void as $$
begin			   	
	insert into Publisher
	values(u_bank_account, u_pub_name, u_email);
	
	insert into Phone_Number
	values(u_p_number);

	insert into Pub_Phone
	values(u_p_number, u_bank_account);
	
	insert into Postal_Address
	values(u_postal_code, u_prov, u_country);

	insert into Address
	values(u_postal_code, u_street, u_city);

	insert into Pub_Address
	values(u_bank_account, u_postal_code, u_street, u_city);
		
end;
$$ language plpgsql;