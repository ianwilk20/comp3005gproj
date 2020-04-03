-- Functions --

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



