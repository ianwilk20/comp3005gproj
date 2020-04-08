-- Triggers --

-- After a book is updated as sold, the db
-- automatically place orders for new books
create trigger order_books 
	after update of sold 
	on Book
	for each row
	execute procedure trigfun();