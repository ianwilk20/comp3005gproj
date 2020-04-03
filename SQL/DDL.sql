-- DDL Statements (UPDATED) --

create table Publisher
(bank_account bigint check (bank_account > 0) not null unique,
 pub_name varchar(50) not null,
 email varchar(50) not null,
 primary key(bank_account)
);

create table Phone_Number
(p_number numeric(10, 0) not null unique,
 primary key(p_number)
);

create table Pub_Phone
(p_number numeric(10, 0) not null,
 bank_account bigint,
 primary key(p_number),
 foreign key(bank_account) references Publisher(bank_account) on delete cascade,
 foreign key(p_number) references Phone_Number(p_number) on delete cascade
);

create table Book_ISBN
(bank_account bigint not null,
 ISBN bigint check (ISBN > 0) not null unique,
 book_name varchar(100) not null,
 genre varchar(20),
 no_pages int check (no_pages >= 0) default 0,
 cost_price numeric(7,2) check (cost_price >= 0) default 0,
 sales_price numeric(7,2) check (sales_price >=0) default 0,
 percent_to_pub numeric(5,2) check (percent_to_pub >= 0) default 0,
 primary key (bank_account, ISBN),
 foreign key (bank_account) references Publisher(bank_account) on delete cascade
);

create table Book
(serial_no serial check (serial_no > 0) not null unique,
 ISBN bigint check (ISBN > 0) not null,
 sold boolean default false,
 primary key (serial_no),
 foreign key (ISBN) references Book_ISBN(ISBN) on delete cascade
);

create table Author
(author_id serial unique,
 author_name varchar(40) not null,
 primary key (author_id)
);

create table Book_Author
(author_id serial,
 serial_no serial,
 primary key (author_id, serial_no),
 foreign key (author_id) references Author(author_id) on delete cascade,
 foreign key (serial_no) references Book(serial_no) on delete cascade
);

create table Users
(account_no serial unique,
 user_name varchar(50) not null,
 email varchar(50) not null,
 primary key (account_no)
);

create table Checkout
(serial_no serial,
 account_no serial,
 primary key (serial_no),
 foreign key (serial_no) references Book(serial_no) on delete cascade,
 foreign key (account_no) references Users(account_no) on delete cascade
);

create table Owners
(owner_id serial unique,
 email varchar(50) not null,
 threshold int check (threshold >= 0),
 primary key (owner_id)
);

create table Inventory
(serial_no serial,
 owner_id serial,
 primary key (serial_no),
 foreign key (serial_no) references Book(serial_no) on delete cascade,
 foreign key (owner_id) references Owners(owner_id) on delete cascade
);

create table Owners_Phone
(p_number numeric(10, 0) not null,
 owner_id serial,
 primary key (p_number),
 foreign key (p_number) references Phone_Number(p_number) on delete cascade,
 foreign key (owner_id) references Owners(owner_id) on delete cascade
);

create table Orders
(order_no serial unique,
 serial_no serial,
 order_status varchar(30) not null,
 purchase_date timestamp,
 primary key (order_no, serial_no),
 foreign key (serial_no) references Book(serial_no) on delete cascade
);

create table Users_Book
(serial_no serial,
 account_no serial,
 primary key (serial_no),
 foreign key (serial_no) references Book(serial_no) on delete cascade,
 foreign key (account_no) references Users(account_no) on delete cascade
);

create table Users_Phone
(p_number numeric(10, 0) not null,
 account_no serial,
 primary key (p_number),
 foreign key (p_number) references Phone_Number(p_number) on delete cascade,
 foreign key (account_no) references Users(account_no) on delete cascade
);

create table Users_Orders
(order_no serial,
 account_no serial,
 primary key (order_no),
 foreign key (order_no) references Orders(order_no) on delete cascade,
 foreign key (account_no) references Users(account_no) on delete cascade
);

create table Postal_Address
(postal_code varchar(6) not null,
 province varchar(35) not null,
 country varchar(20) not null check (country='Canada'),
 primary key (postal_code)
);

create table Address
(postal_code varchar(6) not null,
 street varchar(20) not null,
 city varchar(50) not null,
 primary key (postal_code, street, city),
 foreign key (postal_code) references Postal_Address(postal_code)
);

create table Users_Shipping
(account_no serial,
 postal_code varchar(6) not null,
 street varchar(20) not null,
 city varchar(50) not null,
 primary key (account_no),
 foreign key (account_no) references Users(account_no) on delete cascade,
 foreign key (postal_code) references Postal_Address(postal_code) on delete cascade
);

create table Orders_Shipping
(order_no serial,
 postal_code varchar(6) not null,
 street varchar(20) not null,
 city varchar(50) not null,
 primary key (order_no),
 foreign key (order_no) references Orders(order_no) on delete cascade,
 foreign key (postal_code) references Postal_Address(postal_code) on delete cascade
);

create table Users_Billing
(account_no serial,
 postal_code varchar(6) not null,
 street varchar(20) not null,
 city varchar(50) not null,
 credit_card bigint not null,
 primary key (account_no),
 foreign key (account_no) references Users(account_no) on delete cascade,
 foreign key (postal_code) references Postal_Address(postal_code) on delete cascade
);

create table Orders_Billing
(order_no serial,
 postal_code varchar(6) not null,
 street varchar(20) not null,
 city varchar(50) not null,
 credit_card bigint not null,
 primary key (order_no),
 foreign key (order_no) references Orders(order_no) on delete cascade,
 foreign key (postal_code) references Postal_Address(postal_code) on delete cascade
);

create table Pub_Address
(bank_account bigint,
 postal_code varchar(6) not null,
 street varchar(20) not null,
 city varchar(50) not null,
 primary key (bank_account),
 foreign key (bank_account) references Publisher(bank_account) on delete cascade,
 foreign key (postal_code) references Postal_Address(postal_code) on delete cascade
);