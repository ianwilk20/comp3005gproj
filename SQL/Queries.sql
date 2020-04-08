-- Queries --

-- @Purpose Get the book information for the books in a user's checkout
-- @Returns The full displayable book information for the user's checkout
select checkout.serial_no, isbn, book_name, author_name, genre, pub_name, no_pages, sales_price
from checkout natural join Book natural join Book_ISBN natural join Book_Author natural join Author natural join Publisher
where account_no = ?

-- @Purpose Insert a book's serial number into a user's checkout
-- @Returns n/a
insert into checkout(serial_no, account_no) values (?, ?)

-- @Purpose Get the user's email that matches the input string denoted by '?' (used in Java Prepared Statements)
-- @Returns The entire user table columns that matches that email
SELECT * FROM users WHERE email = ?

-- @Purpose Get all registered users for our application in the database
-- @Returns The registered users in our application in the database
SELECT * from users

-- @Purpose Get the book's with book name matching the user's criteria specified by '?' (used in Java Prepared Statements)
-- ensuring that the book isn't sold and the book isn't already in someones checkout
-- @Returns The books matching the criteria
SELECT distinct on (Book.isbn) isbn, serial_no, book_name, author_name, genre, pub_name, no_pages, sales_price
from Book natural join Book_ISBN natural join Book_Author natural join Author natural join Publisher
WHERE LOWER(book_name) LIKE LOWER(?) and Book.sold = false and Book.serial_no NOT IN (SELECT serial_no FROM checkout)

-- @Purpose Get the book's with book author matching the user's criteria specified by '?' (used in Java Prepared Statements)
-- ensuring that the book isn't sold and the book isn't already in someones checkout
-- @Returns The books matching the criteria
SELECT distinct on (Book.isbn) isbn, serial_no, book_name, author_name, genre, pub_name, no_pages, sales_price
from Book natural join Book_ISBN natural join Book_Author natural join Author natural join Publisher
WHERE LOWER(author_name) LIKE LOWER(?) and Book.sold = false and Book.serial_no NOT IN (SELECT serial_no FROM checkout)

-- @Purpose Get the book's with book ISBN matching the user's criteria specified by '?' (used in Java Prepared Statements)
-- ensuring that the book isn't sold and the book isn't already in someones checkout
-- @Returns The books matching the criteria
SELECT distinct on (Book.isbn) isbn, serial_no, book_name, author_name, genre, pub_name, no_pages, sales_price
FROM Book natural join Book_ISBN natural join Book_Author natural join Author natural join Publisher
WHERE isbn::text LIKE ? and Book.sold = false and Book.serial_no NOT IN (SELECT serial_no FROM checkout)

-- @Purpose Get the book's with book genre matching the user's criteria specified by '?' (used in Java Prepared Statements)
-- ensuring that the book isn't sold and the book isn't already in someones checkout
-- @Returns The books matching the criteria
SELECT distinct on (Book.isbn) isbn, serial_no, book_name, author_name, genre, pub_name, no_pages, sales_price
FROM Book natural join Book_ISBN natural join Book_Author natural join Author natural join Publisher
WHERE LOWER(genre) LIKE LOWER(?) and Book.sold = false and Book.serial_no NOT IN (SELECT serial_no FROM checkout)

-- @Purpose Get the book's with book publisher matching the user's criteria specified by '?' (used in Java Prepared Statements)
-- ensuring that the book isn't sold and the book isn't already in someones checkout
-- @Returns The books matching the criteria
SELECT distinct on (Book.isbn) isbn, serial_no, book_name, author_name, genre, pub_name, no_pages, sales_price
FROM Book natural join Book_ISBN natural join Book_Author natural join Author natural join Publisher
WHERE LOWER(pub_name) LIKE LOWER(?) and Book.sold = false and Book.serial_no NOT IN (SELECT serial_no FROM checkout)

-- @Purpose Insert a user into DB where the first '?' is the user_name and the second '?' is the email (used in Java Prepared Statements)
-- @Returns n/a
INSERT into users(account_no, user_name, email) values(default, ?, ?)

-- @Purpose Insert an address into the address table using user defined values from the three '?' (used in Java Prepared Statements)
-- @Returns n/a
INSERT into address (postal_code, street, city) values (?, ?, ?)

-- @Purpose Insert a postal_address into the postal_address table using user defined values from the three '?' (used in Java Prepared Statements)
-- @Returns n/a
INSERT into postal_address (postal_code, province, country) values (?, ?, ?)

-- @Purpose Insert a users shipping details based off of their billing details
-- @Returns n/a
INSERT into users_shipping(account_no, postal_code, street, city) 
select ?, users_billing.postal_code, users_billing.street, users_billing.city
FROM users_billing WHERE account_no = ?

-- @Purpose Gets an order's status for a specific user based on the order number
-- @Returns The order status associated with the order entered
SELECT order_status
FROM users NATURAL JOIN users_orders NATURAL JOIN orders
WHERE orders.order_no = ? AND users_orders.account_no = ?

-- @Purpose Gets an order's details for a specific user based on the order number
-- @Returns The order details associated with the order entered
SELECT order_no, serial_no, book_name, author_name, genre, no_pages, sales_price
FROM book natural join book_isbn natural join book_author natural join author natural join checkout natural join users_orders natural join orders
WHERE checkout.account_no = ? AND users_orders.account_no = ?


-- Owner Queries --

-- retrieves the number of owners with the given owner_id
-- used to verify if the owner exists in the db
SELECT count(owner_id) FROM Owners WHERE owner_id= ?

-- retrieves the serial_no of a book with the gien owner_id + serial_no
-- used to verify that the book exists and belongs to that owner before deleting it
select serial_no
from Inventory natural join Book natural join Book_ISBN natural join Book_Author natural join Author natural join Publisher
where owner_id = ? and serial_no = ?

-- deletes the book with the given serial_no
delete from Book where serial_no = ?

-- deletes the book with the given serial_no from 
-- the Inventory table of the owner with the given owner_id
delete from Inventory where serial_no = ? and owner_id = ?

-- retrieves information about the publisher with the given pub_name
-- who publish a book in the owner's inventory
select distinct bank_account, pub_name, email, street, city, province, country, postal_code, p_number
from Inventory natural join Book natural join Book_ISBN natural join Publisher natural join Pub_Address natural join Address natural join Postal_Address natural join Pub_Phone
where owner_id = ? and pub_name = ?

-- retrieves an owner's inventory given their owner_id
select serial_no, book_name, author_name, isbn, genre, no_pages, cost_price, sales_price, sold, percent_to_pub, pub_name
from Inventory natural join Book natural join Book_ISBN natural join Book_Author natural join Author natural join Publisher
where owner_id = ?
order by serial_no

-- retrieves an owner's sold inventory ordered by serial_no
-- given their owner_id
-- used for the Sales vs Expenditures report
select serial_no, book_name, author_name, isbn, genre, no_pages, cost_price, sales_price, sold, percent_to_pub, pub_name
from Inventory natural join Book natural join Book_ISBN natural join Book_Author natural join Author natural join Publisher
where owner_id = ? and sold = true
order by serial_no

-- retrieves an owner's unsold inventory ordered by serial_no
-- given their owner_id
-- used for the Sales vs Expenditures report
select serial_no, book_name, author_name, isbn, genre, no_pages, cost_price, sales_price, sold, percent_to_pub, pub_name
from Inventory natural join Book natural join Book_ISBN natural join Book_Author natural join Author natural join Publisher
where owner_id = ? and sold = false
order by serial_no

-- retrieves the total sales subtract the amount transfered to publishers
-- of an owner's inventory given their owner_id
-- used for the Sales vs Expenditures report
select round((sum(sales_price)-sum(percent_to_pub/100*sales_price)),2) as tot_sales
from Inventory natural join Book natural join Book_ISBN natural join Book_Author natural join Author natural join Publisher
where owner_id = ? and sold = true

-- retrieves the total expenditures
-- of an owner's inventory given their owner_id
-- used for the Sales vs Expenditures report
select sum(cost_price) as tot_expenditures
from Inventory natural join Book natural join Book_ISBN natural join Book_Author natural join Author natural join Publisher
where owner_id = ?

-- retrieves the total sales subtract the amount transfered to publishers per genre
-- of an owner's inventory given their owner_id
-- used for the Sales per genre report 
select genre, round((sum(sales_price)-sum(percent_to_pub/100*sales_price)),2) as tot_sales
from Inventory natural join Book natural join Book_ISBN natural join Book_Author natural join Author natural join Publisher
where owner_id = ? and sold = true
group by genre

-- retrieves an owner's sold inventory ordered by genre
-- given their owner_id
-- used for the Sales per genre report 
select serial_no, book_name, author_name, isbn, genre, no_pages, cost_price, sales_price, sold, percent_to_pub, pub_name
from Inventory natural join Book natural join Book_ISBN natural join Book_Author natural join Author natural join Publisher
where owner_id = ? and sold = true
order by genre

-- retrieves the total sales subtract the amount transfered to publishers per author
-- of an owner's inventory given their owner_id
-- used for the Sales per author report
select author_name, round((sum(sales_price)-sum(percent_to_pub/100*sales_price)),2) as tot_sales
from Inventory natural join Book natural join Book_ISBN natural join Book_Author natural join Author natural join Publisher
where owner_id = ? and sold = true
group by author_name

-- retrieves an owner's sold inventory ordered by author_name
-- given their owner_id
-- used for the Sales per author report 
select serial_no, book_name, author_name, isbn, genre, no_pages, cost_price, sales_price, sold, percent_to_pub, pub_name
from Inventory natural join Book natural join Book_ISBN natural join Book_Author natural join Author natural join Publisher
where owner_id = ? and sold = true
order by author_name