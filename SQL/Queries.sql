-- Queries --

-- User Browses all Books --
select distinct ISBN, book_name, author_name, sales_price
from Book_ISBN natural join Book natural join Book_Author natural join Author
where Book.sold = false

-- Display selected book (actually a function) --
select book_name, author_name, sales_price, genre, pub_name, no_pages
from Publisher natural join Book_ISBN natural join Book natural join Book_Author natural join Author
where Book.serial_no = 1234