-- DDL Statements --

create table Publisher
(bank_account serial,
 pub_name varchar(50) not null,
 email varchar(50) not null,
 primary key(bank_account),
 unique(bank_account)
);

create table Phone_Number
(number varchar(15) not null,
 primary key(number),
 unique (number)
);

create table Pub_phone
(number varchar(15) not null,
 bank_account serial,
 primary key(number),
 foreign key(bank_account) references Publisher(bank_account) on delete cascade,
 foreign key(number) references Phone_Number(number) on delete cascade
);

create table Book
(serial_no serial,
 ISBN serial,
 sold boolean default false,
 primary key (serial_no),
 unique(serial_no),
 unique (ISBN)
);

create table Book_ISBN
(bank_account serial,
 ISBN serial,
 book_name varchar(100) not null,
 genre varchar(20),
 no_pages int check (no_pages >= 0) default 0,
 cost_price numeric(5,2) check (cost_price >= 0) default 0,
 sales_price numeric(5,2) check (sales_price >=0) default 0,
 percent_to_pub numeric(5,2) check (percent_to_pub >= 0) default 0,
 primary key (bank_account, ISBN),
 unique(ISBN),
 foreign key (ISBN) references Book(ISBN) on delete cascade,
 foreign key (bank_account) references Publisher(bank_account) on delete cascade
);

create table Author
(author_id serial,
 author_name varchar(40) not null,
 primary key (author_id),
 unique (author_id)
);

create table Book_author
(author_id serial,
 serial_no serial,
 primary key (author_id, serial_no),
 foreign key (author_id) references Author(author_id) on delete cascade,
 foreign key (serial_no) references Book(serial_no) on delete cascade
);

create table Users
(account_no serial,
 user_name varchar(50) not null,
 email varchar(50) not null,
 primary key (account_no),
 unique (account_no)
);

create table Checkout
(serial_no serial,
 account_no serial,
 primary key (serial_no),
 foreign key (serial_no) references Book(serial_no) on delete cascade,
 foreign key (account_no) references Users(account_no) on delete cascade
);

create table Owners
(owner_id serial,
 email varchar(50) not null,
 threshold int check (threshold >= 0),
 primary key (owner_id),
 unique (owner_id)
);

create table Inventory
(serial_no serial,
 owner_id serial,
 primary key (serial_no),
 foreign key (serial_no) references Book(serial_no) on delete cascade,
 foreign key (owner_id) references Owners(owner_id) on delete cascade
);

create table Owners_book
(serial_no serial,
 owner_id serial,
 primary key (serial_no),
 foreign key (serial_no) references Book(serial_no) on delete cascade,
 foreign key (owner_id) references Owners(owner_id) on delete cascade
);

create table Owners_Phone
(number varchar(15) not null,
 owner_id serial,
 primary key (number),
 foreign key (number) references Phone_Number(number) on delete cascade,
 foreign key (owner_id) references Owners(owner_id) on delete cascade
);

create table Orders
(order_no serial,
 serial_no serial,
 order_status varchar(30) not null,
 purchase_date timestamp,
 primary key (order_no, serial_no),
 unique (order_no),
 foreign key (serial_no) references Book(serial_no) on delete cascade
);

create table Users_book
(serial_no serial,
 account_no serial,
 primary key (serial_no),
 foreign key (serial_no) references Book(serial_no) on delete cascade,
 foreign key (account_no) references Users(account_no) on delete cascade
);

create table Users_Phone
(number varchar(15) not null,
 account_no serial,
 primary key (number),
 foreign key (number) references Phone_Number(number) on delete cascade,
 foreign key (account_no) references Users(account_no) on delete cascade
);

create table Users_orders
(order_no serial,
 account_no serial,
 primary key (order_no),
 foreign key (order_no) references Orders(order_no) on delete cascade,
 foreign key (account_no) references Users(account_no) on delete cascade
);

create table Address
(postal_code varchar(6) not null,
 street varchar(20) not null,
 city varchar(50) not null,
 primary key (postal_code, street, city)
);

create table Postal_address
(postal_code varchar(6) not null,
 province varchar(35) not null,
 country varchar(20) not null check (country='Canada'),
 primary key (postal_code),
 foreign key (postal_code) references Address(postal_code) on delete cascade
);

create table Owners_orders
(order_no serial,
 owner_id serial,
 primary key (order_no),
 foreign key (order_no) references Orders(order_no) on delete cascade,
 foreign key (owner_id) references Owners(owner_id) on delete cascade
);

create table Users_Shipping
(account_no serial,
 postal_code varchar(6) not null,
 street varchar(20) not null,
 city varchar(50) not null,
 primary key (account_no),
 foreign key (account_no) references Users(account_no) on delete cascade,
 foreign key (postal_code) references Address(postal_code) on delete cascade,
 foreign key (street) references Address(street) on delete cascade,
 foreign key (city) references Address(city) on delete cascade
);

create table Orders_Shipping
(order_no serial,
 postal_code varchar(6) not null,
 street varchar(20) not null,
 city varchar(50) not null,
 primary key (order_no),
 foreign key (order_no) references Orders(order_no) on delete cascade,
 foreign key (postal_code) references Address(postal_code) on delete cascade,
 foreign key (street) references Address(street) on delete cascade,
 foreign key (city) references Address(city) on delete cascade
);

create table Users_Billing
(account_no serial,
 postal_code varchar(6) not null,
 street varchar(20) not null,
 city varchar(50) not null,
 credit_card bigint not null,
 primary key (account_no),
 foreign key (account_no) references Users(account_no) on delete cascade,
 foreign key (postal_code) references Address(postal_code) on delete cascade,
 foreign key (street) references Address(street) on delete cascade,
 foreign key (city) references Address(city) on delete cascade
);

create table Orders_Billing
(order_no serial,
 postal_code varchar(6) not null,
 street varchar(20) not null,
 city varchar(50) not null,
 credit_card bigint not null,
 primary key (order_no),
 foreign key (order_no) references Orders(order_no) on delete cascade,
 foreign key (postal_code) references Address(postal_code) on delete cascade,
 foreign key (street) references Address(street) on delete cascade,
 foreign key (city) references Address(city) on delete cascade
);

create table Pub_Address
(bank_account serial,
 postal_code varchar(6) not null,
 street varchar(20) not null,
 city varchar(50) not null,
 primary key (bank_account),
 foreign key (bank_account) references Publisher(bank_account) on delete cascade,
 foreign key (postal_code) references Address(postal_code) on delete cascade,
 foreign key (street) references Address(street) on delete cascade,
 foreign key (city) references Address(city) on delete cascade
);