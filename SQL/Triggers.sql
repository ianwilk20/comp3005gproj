-- Triggers --

create trigger order_books 
	after update of sold 
	on Book
	for each row
	execute procedure trigfun();