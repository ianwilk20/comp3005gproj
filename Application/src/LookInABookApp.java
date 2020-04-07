import javafx.util.Pair;

import javax.sound.midi.SysexMessage;
import java.awt.print.Book;
import java.math.BigDecimal;
import java.math.BigInteger;
import javafx.util.Pair;
import java.sql.*;
import java.util.*;
 
public class LookInABookApp {

    private static int userAccountNumber;
    private static Connection c = null;

    public static void main(String[] args) {
        String url = "jdbc:postgresql://localhost:5432/OnlineBookstore";
        String user = "postgres";
        String password = "Whale987654";

      try {
         Class.forName("org.postgresql.Driver");
         c = DriverManager
            .getConnection(url, user, password);
      } catch (Exception e) {
          e.printStackTrace();
          System.err.println(e.getClass().getName()+": "+e.getMessage());
          System.exit(0);
      }

        System.out.println("Connected to the PostgreSQL server successfully.\n\n");
        startLoop();
    }

    private static void startLoop(){
        boolean uoLoop = true;
        while(uoLoop){
            System.out.println("--------------------------------------------------");
            System.out.print("Would you like to login as a User or Owner?\n\nPress (1) for User \t\t Press (2) for Owner\n\n \t\t Selection: ");
            Scanner uInput = new Scanner(System.in);
            Integer role = uInput.nextInt();

            if (role == 1){ //User
                uoLoop = false;
                userLoginLoop();
            } else if (role == 2){ //Owner
                uoLoop = false;
                ownerLoginLoop();
            } else {
                System.out.println("Invalid selection");
                continue;
            }
        }
    }

    /**
     * Creates the user entry in table
     */
    private static void createUserAccount(){
        Scanner uInput = new Scanner(System.in);
        while (true){
            System.out.println("--------------------------------------------------");
            System.out.print("Please enter a username to create: ");
            String user_name = uInput.nextLine();

            System.out.println("--------------------------------------------------");
            System.out.print("Please enter an email to associate to the user_name: ");
            String email = uInput.nextLine();

            if (user_name.isEmpty() || user_name == null || email.isEmpty() || email == null){
                System.out.println("Invalid user_name or email please try again!");
                continue;
            } else {
                insertUserIntoDB(user_name, email);
                getUserWithEmail(email); //So we store the users account number
                System.out.println("--------------------------------------------------\n");
                //System.out.println("Please wait while we redirect you to login");
                return;
            }
        }
    }

    /**
     * Used to authenticate user at checkout
     */
    private static void userLoginLoop() {
        System.out.println("--------------------------------------------------");
        System.out.print("Would you like to Login or Create and Account?\n\nPress (1) to Login \t\t Press (2) to Create an Account\n\n \t\t Selection: ");
        Scanner uInput = new Scanner(System.in);
        Integer role = uInput.nextInt();
        if (role == 1){
            System.out.println("--------------------------------------------------");
            System.out.print("Please enter your email: ");
            uInput = new Scanner(System.in);
            String email = uInput.nextLine();
            Boolean userInDB = getUserWithEmail(email);
            if (userInDB){
                System.out.println("--------------------------------------------------");
                System.out.println("Authenticated");
                userLoop();
            }
            else {
                System.out.println("--------------------------------------------------");
                System.out.println("Username not found!");
                userLoginLoop();
            }
        }
        else if (role == 2){
            //Validate User first
            createUserAccount();

            //Get User Billing
            createUserBilling();

            //Get User Shipping
            createUserShipping();

            //return to login
            userLoginLoop();
        }
    }

    /**
     * Creates the users billing information
     */
    private static void createUserBilling(){
        Scanner uInput = new Scanner(System.in);
        while (true){
            System.out.println("| -------  User Billing Information ------- |");
            System.out.print("Enter your street address: ");
            String street = uInput.nextLine();
            System.out.print("Enter your city: ");
            String city = uInput.nextLine();
            System.out.print("Enter your postal code: ");
            String postal_code = uInput.nextLine();
            System.out.print("Enter your province: ");
            String province = uInput.nextLine();
            System.out.print("Enter your country: ");
            String country = uInput.nextLine();
            System.out.print("Enter your credit card number: ");
            String creditCard = uInput.nextLine();
            Long creditParsed = null;
            try {
                creditParsed = Long.parseLong(creditCard);
            } catch (NumberFormatException nfe){
                System.out.println("Invalid order number");
                createUserBilling();
            }

            if (street.isEmpty() || street == null || city.isEmpty() || city == null || postal_code.isEmpty() || postal_code == null ||
                province.isEmpty() || province == null || country.isEmpty() || country == null || creditParsed == null) {
                System.out.println("Invalid entry or entries please try again!");
                continue;
            } else {
                //int result = insertIntoAddressAndPostalAddress(street, city, postal_code, province, country);
                boolean result = insertUserBillingIntoDB(street, city, postal_code, creditParsed, province, country);
                System.out.println("--------------------------------------------------\n");
                if (result == false){
                    System.out.println("Please try again.");
                    createUserBilling();
                }
                return;
            }
        }

    }

    /**
     * Creates the users shipping information
     */
    private static void createUserShipping(){
        Scanner uInput = new Scanner(System.in);
        while (true) {
            System.out.println("| -------  User Shipping Information ------- |");
            System.out.print("Would you like to use your Billing information as your Shipping information?\n Yes (Y) or (N): ");
            String option = uInput.nextLine();
            if (option.equals("Y") || option.equals("y") || option.equals("Yes") || option.equals("yes")) {
                insertUserShippingFromBilling();
                return;
            } else {
                break;
            }
        }

         while (true){
             System.out.print("Enter your street address: ");
             String address = uInput.nextLine();
             System.out.print("Enter your city: ");
             String city = uInput.nextLine();
             System.out.print("Enter your postal code: ");
             String postal_code = uInput.nextLine();
             System.out.print("Enter your province: ");
             String province = uInput.nextLine();
             System.out.print("Enter your country: ");
             String country = uInput.nextLine();

             if (address.isEmpty() || address == null || city.isEmpty() || city == null || postal_code.isEmpty() || postal_code == null ||
                     province.isEmpty() || province == null || country.isEmpty() || country == null || !country.equals("Canada")) {
                 System.out.println("Invalid entry or entries please try again!");
                 continue;
             } else {
                 boolean result = insertUserShippingIntoDB(address, city, postal_code, province, country);
                 System.out.println("--------------------------------------------------\n");
                 if (result == false){
                     System.out.println("Please try again.");
                     createUserShipping();
                 }
                 System.out.println("Account Successfully Created! Please Login.\n");
                 return;
             }
         }
    }

    /**
     * The User Book Search/Order Lookup Loop
     */
    private static void userLoop() {
        Scanner uInput = new Scanner(System.in);
        System.out.println("--------------------------------------------------");
        System.out.println("What would you like to do");
        System.out.println(" (1) - Search for a book \t (2) - Track an existing order");
        System.out.print("\n\t \tSelection: ");
        Integer selection = uInput.nextInt();
        if (selection == 1){
            bookSearch();
        } else if (selection == 2){
            orderSearch();
        } else {
            userLoop();
        }
    }

    /**
     * Directs User to the Book Search
     */
    private static void bookSearch(){
        Scanner uInput = new Scanner(System.in);
        System.out.println("--------------------------------------------------");
        System.out.println("How would you like to search for your book?");
        System.out.println("(1) - Book Name \t (2) - Author Name \t (3) - ISBN \t (4) - Genre \t (5) - Publisher");
        System.out.print("\n\t \tSelection: ");
        Integer selection = uInput.nextInt();
        uInput = new Scanner(System.in);
        switch(selection){
            case 1:
                while (true){
                    uInput = new Scanner(System.in);
                    System.out.print("Search by [Book Name]: ");
                    String book_name = uInput.nextLine();
                    ArrayList<BookDetails> returnedBooks = getBooksByName(book_name);
                    if (returnedBooks != null && returnedBooks.size() > 0){
                        System.out.println("-------------------------------");
                        printBookDetails(returnedBooks);
                        checkoutOrSearchLoop(returnedBooks);
                    } else {
                        System.out.println("The Book: '" + book_name + "' could not be found.");
                        System.out.print("Would you like to (1) Try Again \t (2) Return to Search: ");
                        Integer choice = uInput.nextInt();
                        if (choice == 1){
                            continue;
                        } else if (choice == 2){
                            bookSearch();
                        }
                    }
                }
            case 2:
                while (true){
                    uInput = new Scanner(System.in);
                    System.out.print("Search by [Author Name]: ");
                    String author_name = uInput.nextLine();
                    ArrayList<BookDetails> returnedBooks = getBooksByAuthorName(author_name);
                    if (returnedBooks != null && returnedBooks.size() > 0){
                        System.out.println("-------------------------------");
                        printBookDetails(returnedBooks);
                        checkoutOrSearchLoop(returnedBooks);
                    } else {
                        System.out.println("The author: '" + author_name + "' could not be found.");
                        System.out.print("Would you like to (1) Try Again \t (2) Return to Search: ");
                        Integer choice = uInput.nextInt();
                        if (choice == 1){
                            continue;
                        } else if (choice == 2){
                            bookSearch();
                        }
                    }
                }
            case 3:
                while (true){
                    uInput = new Scanner(System.in);
                    System.out.print("Search by [ISBN]: ");
                    String ISBN = uInput.nextLine();
                    ArrayList<BookDetails> returnedBooks = getBooksByISBN(ISBN);
                    if (returnedBooks != null && returnedBooks.size() > 0){
                        System.out.println("-------------------------------");
                        printBookDetails(returnedBooks);
                        checkoutOrSearchLoop(returnedBooks);
                    } else {
                        System.out.println("The ISBN: '" + ISBN + "' could not be found.");
                        System.out.println("Would you like to (1) Try Again \t (2) Return to Search ");
                        System.out.print("\n\t \tSelection: ");
                        Integer choice = uInput.nextInt();
                        if (choice == 1){
                            continue;
                        } else if (choice == 2){
                            bookSearch();
                        }
                    }
                }
            case 4:
                while (true){
                    uInput = new Scanner(System.in);
                    System.out.print("Search by [Genre]: ");
                    String genre = uInput.nextLine();
                    ArrayList<BookDetails> returnedBooks = getBooksByGenre(genre);
                    if (returnedBooks != null && returnedBooks.size() > 0){
                        System.out.println("-------------------------------");
                        printBookDetails(returnedBooks);
                        checkoutOrSearchLoop(returnedBooks);
                    } else {
                        System.out.println("The genre: '" + genre + "' could not be found or no books could be found with that genre");
                        System.out.print("Would you like to (1) Try Again \t (2) Return to Search ");
                        System.out.print("\n\t \tSelection: ");
                        Integer choice = uInput.nextInt();
                        if (choice == 1){

                        } else if (choice == 2){
                            bookSearch();
                        }
                    }
                }
            case 5:
                while (true){
                    uInput = new Scanner(System.in);
                    System.out.print("Search by [Publisher]: ");
                    String pub = uInput.nextLine();
                    ArrayList<BookDetails> returnedBooks = getBooksByPublisher(pub);
                    if (returnedBooks != null && returnedBooks.size() > 0){
                        System.out.println("-------------------------------");
                        printBookDetails(returnedBooks);
                        checkoutOrSearchLoop(returnedBooks);
                    } else {
                        System.out.println("The publisher with name: '" + pub + "' could not be found or no publishers exist");
                        System.out.print("Would you like to (1) Try Again \t (2) Return to Search ");
                        System.out.print("\n\t \tSelection: ");
                        Integer choice = uInput.nextInt();
                        if (choice == 1){

                        } else if (choice == 2){
                            bookSearch();
                        }
                    }
                }
            default:
                System.out.println("Invalid selection, please try again");
                userLoop();
        }
    }

    /**
     * Directs User to Add Book to Checkout or to go to bookSearch()
     * @param selectedBooks
     */
    private static void checkoutOrSearchLoop(ArrayList<BookDetails> selectedBooks) {
        Scanner uInput = new Scanner(System.in);
        System.out.println("--------------------------------------------------");
        System.out.println("What would you like to do?");
        System.out.println("(1) - Add a Book to your Cart \t (2) - Return to Main Search");
        int choice = uInput.nextInt();
        while (true){
            if (choice == 1){
                System.out.println("Enter the serial number of the book to add to cart");
                int serial_no = uInput.nextInt();
                boolean found = false;
                for (BookDetails bd : selectedBooks){
                    if (bd.serial_no == serial_no){
                        insertBookToCheckout(bd.serial_no);
                        found = true;
                        break;
                    }
                }
                if (found == false) {
                    System.out.println("Make sure you entered the correct serial number from the list of results and try again.");
                    continue;
                } else {
                    newSearchOrPlaceOrder();
                }
            } else if (choice == 2){
                bookSearch();
            } else { checkoutOrSearchLoop(selectedBooks); }
        }
    }

    /**
     * Menu to link back to search or to place an order
     */
    private static void newSearchOrPlaceOrder() {
        Scanner uInput = new Scanner(System.in);
        System.out.println("--------------------------------------------------");
        System.out.println("Your Cart"); //print cart
        ArrayList<BookDetails> booksInCart = getUsersCart();
        if (booksInCart != null && booksInCart.size() > 0){
            printBookDetailsForCart(booksInCart);
        }
        System.out.println("What would you like to do?");
        System.out.println("(1) - New Search \t (2) - Place Order");
        int choice = uInput.nextInt();
        while (true){
            if (choice == 1){
                bookSearch();
            } else if (choice == 2){
                checkoutLoop();
            } else {
                newSearchOrPlaceOrder();
            }
        }
    }

    /**
     * Place order loop
     */
    private static void checkoutLoop() {
        Scanner uInput = new Scanner(System.in);
        System.out.println("--------------------------------------------------");
        System.out.println("Would you like to make your order billing and shipping the same as your account billing and shipping?");
        System.out.println(" (1) - Yes \t (2) - No");
        System.out.print("Your Selection: ");
        int choice = uInput.nextInt();
        while (true){
            if (choice == 1){
                insertUserOrder();
                copyUserAddressesToOrderInformation();
                System.out.println("| -- Order Created -- |");
                ArrayList<OrderDetails> returnedOrdersCreated = getOrderDetailsFromCreatedOrder();
                if (returnedOrdersCreated != null && returnedOrdersCreated.size() > 0){
                    printOrderDetails(returnedOrdersCreated);
                } else {
                    System.out.println("Error when retrieving order details ");
                }
                deleteCheckoutItems();
            } else if (choice == 2) {
                Billing_Address orderBilling = getBillingForOrder();
                Shipping_Address orderShipping = getShippingForOrder();
                insertUserOrder();
                insertOrderShippingAndBilling(orderBilling, orderShipping);
                System.out.println("| -- Order Created -- |");
                System.out.println("Order Number - Serial Number - Book Name - Book Author - Genre - Number of Pages - Sales Price");
                ArrayList<OrderDetails> returnedOrdersCreated = getOrderDetailsFromCreatedOrder();
                if (returnedOrdersCreated != null && returnedOrdersCreated.size() > 0){
                    for (OrderDetails order: returnedOrdersCreated){
                        order.toString();
                    }
                } else {
                    System.out.println("Error when retrieving order details ");
                }
                deleteCheckoutItems();
            } else {
                checkoutLoop();
            }
        }
    }

    /**
     * Get Billing Information for Order
     * @return a Billing_Address Object
     */
    private static Billing_Address getBillingForOrder() {
        Scanner uInput = new Scanner(System.in);
        while (true){
            System.out.println("| -------  Order Billing Information ------- |");
            System.out.print("Enter your street address: ");
            String street = uInput.nextLine();
            System.out.print("Enter your city: ");
            String city = uInput.nextLine();
            System.out.print("Enter your postal code: ");
            String postal_code = uInput.nextLine();
            System.out.print("Enter your province: ");
            String province = uInput.nextLine();
            System.out.print("Enter your country: ");
            String country = uInput.nextLine();
            System.out.print("Enter your credit card number: ");

            String creditCard = uInput.nextLine();
            Long creditParsed = null;
            try {
                creditParsed = Long.parseLong(creditCard);
            } catch (NumberFormatException nfe){
                System.out.println("Invalid order number");
                getBillingForOrder();
            }


            if (street.isEmpty() || street == null || city.isEmpty() || city == null || postal_code.isEmpty() || postal_code == null ||
                    province.isEmpty() || province == null || country.isEmpty() || country == null || creditParsed == null) {
                System.out.println("Invalid entry or entries please try again!");
                continue;
            } else {
                return new Billing_Address(postal_code, street, city, province, country, creditParsed);
            }
        }
    }

    /**
     * Get Shipping Information for Order
     * @return a Shipping_Address Object
     */
    private static Shipping_Address getShippingForOrder() {
        Scanner uInput = new Scanner(System.in);
        while (true){
            System.out.println("| -------  Order Billing Information ------- |");
            System.out.print("Enter your street address: ");
            String street = uInput.nextLine();
            System.out.print("Enter your city: ");
            String city = uInput.nextLine();
            System.out.print("Enter your postal code: ");
            String postal_code = uInput.nextLine();
            System.out.print("Enter your province: ");
            String province = uInput.nextLine();
            System.out.print("Enter your country: ");
            String country = uInput.nextLine();

            if (street.isEmpty() || street == null || city.isEmpty() || city == null || postal_code.isEmpty() || postal_code == null ||
                    province.isEmpty() || province == null || country.isEmpty() || country == null) {
                System.out.println("Invalid entry or entries please try again!");
                continue;
            } else {
                return new Shipping_Address(postal_code, street, city, province, country);
            }
        }
    }

    /* ----------- DB FUNCTIONS -----------*/

    /**
     * Get Books from the users Cart
     * @return
     */
    private static ArrayList<BookDetails> getUsersCart() {
        String query = "select checkout.serial_no, isbn, book_name, author_name, genre, pub_name, no_pages, sales_price " +
                       "from checkout natural join Book natural join Book_ISBN natural join Book_Author natural join Author natural join Publisher " +
                       "where account_no = ?";

        Statement stmt = null;
        ArrayList<BookDetails> booksReturned = new ArrayList<BookDetails>();
        try {
            PreparedStatement pstmt = c.prepareStatement(query);
            pstmt.setLong(1, userAccountNumber);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()){
                BookDetails bd;
                Long serial_no  = rs.getLong("serial_no");
                Long isbn = rs.getLong("isbn");
                String name = rs.getString("book_name");
                String author = rs.getString("author_name");
                String genre = rs.getString("genre");
                String publisher = rs.getString("pub_name");
                int no_pages = rs.getInt("no_pages");
                float price = rs.getFloat("sales_price");
                bd = new BookDetails(serial_no, isbn, name, author, genre, publisher, no_pages, price);
                booksReturned.add(bd);
            }
            return booksReturned;

        } catch (SQLException e) {
            e.printStackTrace();
            booksReturned = null;
        }
        return booksReturned;
    }

    /**
     * Add Book to checkout
     * @param serial_no
     */
    private static void insertBookToCheckout(Long serial_no) {
        String query = "insert into checkout(serial_no, account_no) values (?, ?)";
        Statement stmt = null;
        try {
            PreparedStatement pstmt = c.prepareStatement(query);
            pstmt.setLong(1, serial_no);
            pstmt.setLong(2, userAccountNumber);
            int results = pstmt.executeUpdate();
            if (results == 1) {
                System.out.println("Successfully added the book with serial number: " + serial_no + " to your cart!");
            } else {
                System.out.println("Error! Couldn't add the book to your cart.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error in DB! Couldn't add the book to your cart.");
        }
    }

    /**
     * Look-up an order by @Order_no
     */
    private static void orderSearch(){
        Scanner uInput = new Scanner(System.in);
        System.out.println("--------------------------------------------------");
        System.out.println("Please enter your order number: ");
        String order_no = uInput.nextLine();
        Long order_number;
        try {
            order_number = Long.parseLong(order_no);
        } catch (NumberFormatException nfe){
            System.out.println("Invalid order number");
            orderSearch();
        }

        String order_status = getOrderDetailsFromOrder(order_no);
        if (order_status.equals("Not Found")){
            System.out.println("--------------------------------------------------");
            System.out.println("We could not find the order you're looking for.");
        } else {
            System.out.println("--------------------------------------------------");
            System.out.println("Order: " + order_no + " Status: " + order_status);
        }
        userLoop();

    }

    /**
     * Get User with Specified user_name from DB
     * @param email
     * @return
     */
    private static boolean getUserWithEmail(String email){
        String query = "SELECT * FROM users WHERE email = ?";
        Statement stmt = null;
        try {
            PreparedStatement pstmt = c.prepareStatement(query);
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            rs.next();
            Integer account_no = rs.getInt("account_no");
            if (account_no != null && account_no >= 0){
                userAccountNumber = account_no;
                return true;
            }
            else { return false; }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Get Users From DB
     * @return
     */
    private String getUsers() {
        String query = "SELECT * from users";
        Statement stmt = null;
        try {
            stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            rs.next();
            String acc_no = rs.getString("account_no");
            String users = rs.getString("user_name");
            String email = rs.getString("email");
            return acc_no + " | " + users + " | " + email;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "";
    }

    /* Book Searching Functions */

    /**
     * Get Book By Name
     * @param book_name
     * @return
     */
    private static ArrayList<BookDetails> getBooksByName(String book_name){
        String query = "SELECT distinct on (Book.isbn) isbn, serial_no, book_name, author_name, genre, pub_name, no_pages, sales_price " +
                       "from Book natural join Book_ISBN natural join Book_Author natural join Author natural join Publisher " +
                       "WHERE LOWER(book_name) LIKE LOWER(?) and Book.sold = false";
        Statement stmt = null;
        ArrayList<BookDetails> booksReturned = new ArrayList<BookDetails>();
        try {
            PreparedStatement pstmt = c.prepareStatement(query);
            pstmt.setString(1, "%" + book_name + "%");
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()){
             BookDetails bd;
             Long serial_no  = rs.getLong("serial_no");
             Long isbn = rs.getLong("isbn");
             String name = rs.getString("book_name");
             String author = rs.getString("author_name");
             String genre = rs.getString("genre");
             String publisher = rs.getString("pub_name");
             int no_pages = rs.getInt("no_pages");
             float price = rs.getFloat("sales_price");
             bd = new BookDetails(serial_no, isbn, name, author, genre, publisher, no_pages, price);
             booksReturned.add(bd);
            }
            return booksReturned;

        } catch (SQLException e) {
            e.printStackTrace();
            booksReturned = null;
        }
        return booksReturned;
    }

    /**
     * Get Book By Author Name
     * @param author_name
     * @return
     */
    private static ArrayList<BookDetails> getBooksByAuthorName(String author_name){
        String query = "SELECT distinct on (Book.isbn) isbn, serial_no, book_name, author_name, genre, pub_name, no_pages, sales_price " +
                        "from Book natural join Book_ISBN natural join Book_Author natural join Author natural join Publisher " +
                        "WHERE LOWER(author_name) LIKE LOWER(?) and Book.sold = false";
        Statement stmt = null;
        ArrayList<BookDetails> booksReturned = new ArrayList<BookDetails>();
        try {
            PreparedStatement pstmt = c.prepareStatement(query);
            pstmt.setString(1, "%" + author_name + "%");
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()){
                BookDetails bd;
                Long serial_no  = rs.getLong("serial_no");
                Long isbn = rs.getLong("isbn");
                String name = rs.getString("book_name");
                String author = rs.getString("author_name");
                String genre = rs.getString("genre");
                String publisher = rs.getString("pub_name");
                int no_pages = rs.getInt("no_pages");
                float price = rs.getFloat("sales_price");
                bd = new BookDetails(serial_no, isbn, name, author, genre, publisher, no_pages, price);
                booksReturned.add(bd);
            }
            return booksReturned;

        } catch (SQLException e) {
            e.printStackTrace();
            booksReturned = null;
        }
        return booksReturned;
    }

    /**
     * Get Book By ISBN
     * @param isbn
     * @return
     */
    private static ArrayList<BookDetails> getBooksByISBN(String isbn){
        String query = "SELECT distinct on (Book.isbn) isbn, serial_no, book_name, author_name, genre, pub_name, no_pages, sales_price " +
                       "FROM Book natural join Book_ISBN natural join Book_Author natural join Author natural join Publisher " +
                       "WHERE isbn::text LIKE ? and Book.sold = false";
        Statement stmt = null;
        ArrayList<BookDetails> booksReturned = new ArrayList<BookDetails>();
        try {
            PreparedStatement pstmt = c.prepareStatement(query);
            pstmt.setString(1, "%" + isbn + "%");
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()){
                BookDetails bd;
                Long serial_no  = rs.getLong("serial_no");
                Long s_isbn = rs.getLong("isbn");
                String name = rs.getString("book_name");
                String author = rs.getString("author_name");
                String genre = rs.getString("genre");
                String publisher = rs.getString("pub_name");
                int no_pages = rs.getInt("no_pages");
                float price = rs.getFloat("sales_price");
                bd = new BookDetails(serial_no, s_isbn, name, author, genre, publisher, no_pages, price);
                booksReturned.add(bd);
            }
            return booksReturned;

        } catch (SQLException e) {
            e.printStackTrace();
            booksReturned = null;
        }
        return booksReturned;
    }

    /**
     * Get Book By Genre
     * @param s_genre
     * @return
     */
    private static ArrayList<BookDetails> getBooksByGenre(String s_genre){
        String query = "SELECT distinct on (Book.isbn) isbn, serial_no, book_name, author_name, genre, pub_name, no_pages, sales_price " +
                "FROM Book natural join Book_ISBN natural join Book_Author natural join Author natural join Publisher " +
                "WHERE LOWER(genre) LIKE LOWER(?) and Book.sold = false";
        Statement stmt = null;
        ArrayList<BookDetails> booksReturned = new ArrayList<BookDetails>();
        try {
            PreparedStatement pstmt = c.prepareStatement(query);
            pstmt.setString(1, "%" + s_genre + "%");
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()){
                BookDetails bd;
                Long serial_no  = rs.getLong("serial_no");
                Long s_isbn = rs.getLong("isbn");
                String name = rs.getString("book_name");
                String author = rs.getString("author_name");
                String genre = rs.getString("genre");
                String publisher = rs.getString("pub_name");
                int no_pages = rs.getInt("no_pages");
                float price = rs.getFloat("sales_price");
                bd = new BookDetails(serial_no, s_isbn, name, author, genre, publisher, no_pages, price);
                booksReturned.add(bd);
            }
            return booksReturned;

        } catch (SQLException e) {
            e.printStackTrace();
            booksReturned = null;
        }
        return booksReturned;
    }

    /**
     * Get Book By Publisher
     * @param s_pub
     * @return
     */
    private static ArrayList<BookDetails> getBooksByPublisher(String s_pub){
        String query = "SELECT distinct on (Book.isbn) isbn, serial_no, book_name, author_name, genre, pub_name, no_pages, sales_price " +
                "FROM Book natural join Book_ISBN natural join Book_Author natural join Author natural join Publisher " +
                "WHERE LOWER(pub_name) LIKE LOWER(?) and Book.sold = false";
        Statement stmt = null;
        ArrayList<BookDetails> booksReturned = new ArrayList<BookDetails>();
        try {
            PreparedStatement pstmt = c.prepareStatement(query);
            pstmt.setString(1, "%" + s_pub + "%");
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()){
                BookDetails bd;
                Long serial_no  = rs.getLong("serial_no");
                Long s_isbn = rs.getLong("isbn");
                String name = rs.getString("book_name");
                String author = rs.getString("author_name");
                String genre = rs.getString("genre");
                String publisher = rs.getString("pub_name");
                int no_pages = rs.getInt("no_pages");
                float price = rs.getFloat("sales_price");
                bd = new BookDetails(serial_no, s_isbn, name, author, genre, publisher, no_pages, price);
                booksReturned.add(bd);
            }
            return booksReturned;

        } catch (SQLException e) {
            e.printStackTrace();
            booksReturned = null;
        }
        return booksReturned;
    }




    /**
     * Add User To DB
     * @param user_name
     * @param email
     */
    private static void insertUserIntoDB(String user_name, String email) {
        String query = "INSERT into users values(default, ?, ?)";
        try {
            PreparedStatement pstmt = c.prepareStatement(query);
            pstmt.setString(1, user_name);
            pstmt.setString(2, email);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    /**
     * Insert user's billing info into DB
     * @param street
     * @param city
     * @param postal_code
     * @param creditCard
     * @return
     */
    private static boolean insertUserBillingIntoDB(String street, String city, String postal_code, Long creditCard, String province, String country) {
        String billingQuery = "SELECT \"insert_users_billing\" (?, ?, ?, ?, ?, ?, ?)";
        ResultSet results = null;
        String u_AN = Integer.toString(userAccountNumber);
        try {
            PreparedStatement pstmt = c.prepareStatement(billingQuery);
            pstmt.setLong(1, Long.parseLong(u_AN));
            pstmt.setString(2, postal_code);
            pstmt.setString(3, street);
            pstmt.setString(4, city);
            pstmt.setLong(5, creditCard);
            pstmt.setString(6, province);
            pstmt.setString(7, country);
            results = pstmt.executeQuery();
            if (results != null) {
                 return true;
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Insert user's shipping info into DB
     * @param street
     * @param city
     * @param postal_code
     * @param province
     * @param country
     * @return
     */
    private static boolean insertUserShippingIntoDB(String street, String city, String postal_code, String province, String country) {
        String shippingQuery = "SELECT \"insert_users_shipping\" (?, ?, ?, ?, ?, ?)";
        ResultSet results = null;
        String u_AN = Integer.toString(userAccountNumber);
        try {
            PreparedStatement pstmt = c.prepareStatement(shippingQuery);
            pstmt.setLong(1, Long.parseLong(u_AN));
            pstmt.setString(2, postal_code);
            pstmt.setString(3, street);
            pstmt.setString(4, city);
            pstmt.setString(5, province);
            pstmt.setString(6, country);
            results = pstmt.executeQuery();
            if (results != null) {
                return true;
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Insert into address and subsequently the postal_address table
     * @param street
     * @param city
     * @param postal_code
     * @param province
     * @param country
     * @return
     */
    private static void insertIntoAddressAndPostalAddress(String street, String city, String postal_code, String province, String country){
        String addressQuery = "INSERT into address (postal_code, street, city) values (?, ?, ?)";
        String postalAddressQuery = "INSERT into postal_address (postal_code, province, country) values (?, ?, ?)";
        try {
            PreparedStatement pstmt = c.prepareStatement(postalAddressQuery);
            pstmt.setString(1, postal_code);
            pstmt.setString(2, province);
            pstmt.setString(3, country);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            createUserBilling();
        }

        try {
            PreparedStatement pstmt = c.prepareStatement(addressQuery);
            pstmt.setString(1, postal_code);
            pstmt.setString(2, street);
            pstmt.setString(3, city);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            createUserBilling();
        }
    }

    /**
     * Copy the Billing information into the Shipping for the user
     */
    private static void insertUserShippingFromBilling() {
        String query = "INSERT into users_shipping(account_no, postal_code, street, city) select ?, users_billing.postal_code, users_billing.street, users_billing.city" +
                       " FROM users_billing WHERE account_no = ?";
        try {
            PreparedStatement pstmt = c.prepareStatement(query);
            pstmt.setInt(1, userAccountNumber);
            pstmt.setInt(2, userAccountNumber);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Failed to insert information from Users_Billing to Users_Shipping");
            e.printStackTrace();
            createUserShipping();

        }
    }

    /**
     * Used to track an order number
     * @param order_no
     * @return
     */
    private static String getOrderDetailsFromOrder(String order_no) {
        String query = "SELECT order_status " +
                       "FROM users NATURAL JOIN users_orders NATURAL JOIN orders " +
                       "WHERE orders.order_no = ? AND users_orders.account_no = ?";
        try {
            PreparedStatement pstmt = c.prepareStatement(query);
            pstmt.setLong(1, Long.parseLong(order_no));
            pstmt.setInt(2, userAccountNumber);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()){
                String status = rs.getString("order_status");
                return status;
            }
        } catch (SQLException e) {
            System.out.println("Error when finding that order with order number:" + order_no);
            e.printStackTrace();
        }
        return "Not Found";
    }

    /**
     * Create the user's order from checkout information
     */
    private static void insertUserOrder() {
        String query = "SELECT \"insert_order\" (?)";
        try {
            PreparedStatement pstmt = c.prepareStatement(query);
            pstmt.setInt(1, userAccountNumber);
            pstmt.executeQuery();
        } catch (SQLException e) {
            System.out.println("Failed to create the order");
            e.printStackTrace();
            checkoutLoop();
        }
    }

    /**
     * Copy a user's shipping/billing to their order(s)
     */
    private static void copyUserAddressesToOrderInformation() {
        String query = "SELECT \"copy_user_addresses_to_order\" (?)";
        try {
            PreparedStatement pstmt = c.prepareStatement(query);
            pstmt.setInt(1, userAccountNumber);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Failed to copy the user's billing and shipping to their order(s) with UAN: " + userAccountNumber);
            e.printStackTrace();
            checkoutLoop();

        }
    }

    /**
     * Delete a user's checkout after an order and an orders shipping/billing is entered
     */
    private static void deleteCheckoutItems() {
        String query = "SELECT \"delete_checkout\" (?)";
        try {
            PreparedStatement pstmt = c.prepareStatement(query);
            pstmt.setInt(1, userAccountNumber);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Failed to delete the user's checkout with UAN: " + userAccountNumber);
            e.printStackTrace();
            checkoutLoop();

        }
    }

    /**
     * Get Order Full Details From the Created Order
     * @return
     */
    private static ArrayList<OrderDetails> getOrderDetailsFromCreatedOrder() {
        String query = "SELECT order_no, serial_no, book_name, author_name, genre, no_pages, sales_price" +
                       "FROM book natural join book_ISBN natural join book_author natural join author natural join checkout natural join users_orders natural join orders " +
                       "WHERE checkout.account_no = ? AND users_orders.account_no = ?";
        try {
            PreparedStatement pstmt = c.prepareStatement(query);
            pstmt.setInt(1, userAccountNumber);
            ResultSet rs = pstmt.executeQuery();
            ArrayList<OrderDetails> orders = new ArrayList<OrderDetails>();
            if (rs.next()){
                Long order_no = rs.getLong("order_no");
                Long serial_no = rs.getLong("serial_no");
                String b_name = rs.getString("book_name");
                String a_name = rs.getString("author_name");
                String genre = rs.getString("genre");
                int no_pages = rs.getInt("no_pages");
                float price = rs.getFloat("sales_price");
                orders.add(new OrderDetails(order_no, serial_no, b_name, a_name, genre, no_pages, price));
            }
            return orders;
        } catch (SQLException e) {
            System.out.println("Error when finding that orders for user account number: " + userAccountNumber);
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Insert the information for the Order Shipping and Billing
     * @param orderBilling
     * @param orderShipping
     * @return
     */
    private static boolean insertOrderShippingAndBilling(Billing_Address orderBilling, Shipping_Address orderShipping) {
        String shipping_query = "select insert_order_shipping (?, ?, ?, ?, ?, ?)";
        String billing_query = "select insert_order_billing (?, ?, ?, ?, ?, ?, ?)";
        ResultSet results = null;
        try {
            PreparedStatement pstmt = c.prepareStatement(shipping_query);
            pstmt.setInt(1, userAccountNumber);
            pstmt.setString(2, orderShipping.postal_code);
            pstmt.setString(3, orderShipping.street);
            pstmt.setString(4, orderShipping.city);
            pstmt.setString(5, orderShipping.province);
            pstmt.setString(6, orderShipping.country);
            results = pstmt.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Insert into order shipping failed");
            return false;
        }

        try {
            PreparedStatement pstmt = c.prepareStatement(billing_query);
            pstmt.setInt(1, userAccountNumber);
            pstmt.setString(2, orderBilling.postal_code);
            pstmt.setString(3, orderBilling.street);
            pstmt.setString(4, orderBilling.city);
            pstmt.setString(5, orderBilling.province);
            pstmt.setString(6, orderBilling.country);
            pstmt.setLong(7, orderBilling.credit_card);
            results = pstmt.executeQuery();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Insert into order billing failed");
            return false;
        }
    }

    private static void ownerLoginLoop() {
        System.out.println("--------------------------------------------------");
        System.out.print("Would you like to Login or Create an Account?\n\nPress (1) to Login \t\t Press (2) to Create an Account\n\n \t\t Selection: ");
        Scanner uInput = new Scanner(System.in);
        Integer role = uInput.nextInt();
        if (role == 1) {
            System.out.println("--------------------------------------------------");
            System.out.print("Please enter your owner ID: ");
            uInput = new Scanner(System.in);
            Integer owner_id = uInput.nextInt();
            Boolean ownerInDB = getOwnerWithOwnerID(owner_id);
            if (ownerInDB) {
                System.out.println("--------------------------------------------------");
                System.out.println("Authenticated");
                ownerLoop(owner_id);
            } else {
                System.out.println("--------------------------------------------------");
                System.out.println("Owner ID not found!");
                ownerLoginLoop();
            }
        } else if (role == 2) {
            while (true) {
                System.out.println("--------------------------------------------------");
                System.out.println("Please enter an email to associate with your account: ");
                uInput = new Scanner(System.in);
                String email = uInput.nextLine();
                System.out.println("--------------------------------------------------");
                System.out.print("Please enter a phone number [0123456789] to associate with your account: ");
                uInput = new Scanner(System.in);
                long p_number = uInput.nextLong();
                System.out.println("--------------------------------------------------");
                System.out.print("Please enter a threshold to keep your collection stalked at: ");
                uInput = new Scanner(System.in);
                Integer threshold = uInput.nextInt();
                if (threshold == null || threshold <= 0 || email.isEmpty() || email == null || p_number <= 0) {
                    System.out.println("Invalid email or phone number or threshold please try again!");
                    continue;
                } else {
                    int owner_id = addOwnerToDB(email, threshold, p_number);
                    System.out.println("--------------------------------------------------\n");
                    System.out.println("Please wait while we redirect you to login, your owner ID is: " + owner_id);
                    ownerLoginLoop();
                }
            }
        }
    }

    private static void ownerLoop(int owner_id) {
        ArrayList<ArrayList<String>> inventory = getInventory(owner_id);
        System.out.println("\n--------------------------------------------------");
        printInventory(inventory);

        Scanner uInput = new Scanner(System.in);
        System.out.println("\n--------------------------------------------------");
        System.out.print(" (1) - Add a Book to your Collection \n (2) - Remove a Book from your Collection \n" +
                " (3) - View Publisher Details \n (4) - Generate a Report\n\n \t\t Selection: ");
        Integer selection = uInput.nextInt();

        switch (selection) {
            case 1:
                //add a book
                int serial_no, no_pages;
                Long ISBN, bank_account, p_number;
                String author_name, book_name, genre, pub_name, email, street, city, province, country, postal_code;
                float cost_price, sales_price, percent_to_pub;

                System.out.print("Add Book with [Serial Number]: ");
                uInput = new Scanner(System.in);
                serial_no = uInput.nextInt();
                System.out.print("What is the Author's name? ");
                uInput = new Scanner(System.in);
                author_name = uInput.nextLine();

                System.out.print("Does another copy exist? (1) - yes (2) - no ");
                uInput = new Scanner(System.in);
                int response = uInput.nextInt();
                switch (response){
                    case 1:
                        System.out.print("What is the book's name? ");
                        uInput = new Scanner(System.in);
                        book_name = uInput.nextLine();
                        //insert Book + Inventory
                        addBookToDB(serial_no, book_name, owner_id);
                        break;
                    case 2:
                        System.out.println("Please enter all the book's information below.");
                        System.out.print("ISBN: ");
                        uInput = new Scanner(System.in);
                        ISBN = uInput.nextLong();
                        System.out.print("Book Name: ");
                        uInput = new Scanner(System.in);
                        book_name = uInput.nextLine();
                        System.out.print("Genre ");
                        uInput = new Scanner(System.in);
                        genre = uInput.nextLine();
                        System.out.print("Number of Pages: ");
                        uInput = new Scanner(System.in);
                        no_pages = uInput.nextInt();
                        System.out.print("Cost Price: ");
                        uInput = new Scanner(System.in);
                        cost_price = uInput.nextFloat();
                        System.out.print("Sales Price: ");
                        uInput = new Scanner(System.in);
                        sales_price = uInput.nextFloat();
                        System.out.print("Percent to Publisher: ");
                        uInput = new Scanner(System.in);
                        percent_to_pub = uInput.nextFloat();

                        System.out.print("Is the Publisher already in the Database? (1) - yes (2) - no ");
                        uInput = new Scanner(System.in);
                        response = uInput.nextInt();
                        switch (response){
                            case 1:
                                System.out.print("What is the Publisher's name? ");
                                uInput = new Scanner(System.in);
                                pub_name = uInput.nextLine();
                                //insert Book_ISBN - calls Book + Inventory fn
                                addBook_ISBNToDB (ISBN, book_name, genre, no_pages, cost_price, sales_price, percent_to_pub, pub_name);
                                addBookToDB(serial_no, book_name, owner_id);
                                break;
                            case 2:
                                System.out.println("Please enter all the Publisher's information below.");
                                System.out.print("Publisher Name: ");
                                uInput = new Scanner(System.in);
                                pub_name = uInput.nextLine();
                                System.out.print("Email: ");
                                uInput = new Scanner(System.in);
                                email = uInput.nextLine();
                                System.out.print("Bank Account Number: ");
                                uInput = new Scanner(System.in);
                                bank_account = uInput.nextLong();
                                System.out.print("Phone Number: [0123456789]");
                                uInput = new Scanner(System.in);
                                p_number = uInput.nextLong();
                                System.out.println("Please enter all the Publisher's Address' information below.");
                                System.out.print("Street: ");
                                uInput = new Scanner(System.in);
                                street = uInput.nextLine();
                                System.out.print("City: ");
                                uInput = new Scanner(System.in);
                                city = uInput.nextLine();
                                System.out.print("Postal Code: [A1B2C3]");
                                uInput = new Scanner(System.in);
                                postal_code = uInput.nextLine();
                                System.out.print("Province: ");
                                uInput = new Scanner(System.in);
                                province = uInput.nextLine();
                                System.out.print("Country: [Canada]");
                                uInput = new Scanner(System.in);
                                country = uInput.nextLine();
                                //insert Publisher + Phone_Number + Pub_Phone + Postal_Address + Address + Pub_Address - calls Book_ISBN
                                addPublisherToDB(bank_account, pub_name, email, p_number, postal_code, province, country, street, city);
                                addBook_ISBNToDB (ISBN, book_name, genre, no_pages, cost_price, sales_price, percent_to_pub, pub_name);
                                addBookToDB(serial_no, book_name, owner_id);
                                break;
                        }
                }
                //insert Author(if you can) + Book_Author
                addAuthorToDB(author_name, serial_no);
                ownerLoop(owner_id);
            case 2:
                //remove a book
                System.out.print("Remove Book with [Serial Number]: ");
                serial_no = uInput.nextInt();
                boolean exists = bookExists(owner_id, serial_no);
                if (exists) {
                    deleteBookBySerialNo(serial_no, owner_id);
                } else {
                    System.out.println("Your Inventory did not contain a book with " +
                            serial_no + " as its serial number");
                }
                ownerLoop(owner_id);
            case 3:
                //publisher
                System.out.print("Get Details about [Publisher Name]: ");
                uInput = new Scanner(System.in);
                pub_name = uInput.nextLine();
                ArrayList<ArrayList<String>> pubDetails = getPublisherByName(owner_id, pub_name);
                if (pubDetails.size() > 0) {
                    System.out.println("\n--------------------------------------------------");
                    System.out.printf("-%35s%50s%15s%90s%15s-\n", "Publisher Name", "Email Address", "Bank Account", "Address", "Phone #");
                    for (int i = 0; i < pubDetails.size(); ++i) {
                        if (i > 0) {
                            System.out.printf(" %15s", pubDetails.get(i).get(4));

                        } else {
                            System.out.print("-");
                            for (int j = 0; j < pubDetails.get(i).size(); ++j) {
                                if (j == 0) {
                                    System.out.printf("%35s", pubDetails.get(i).get(j));
                                } else if (j == 1) {
                                    System.out.printf("%50s", pubDetails.get(i).get(j));
                                } else if (j == 2 || j == 4) {
                                    System.out.printf("%15s", pubDetails.get(i).get(j));
                                } else if (j == 3) {
                                    System.out.printf("%90s", pubDetails.get(i).get(j));
                                }
                            }
                        }
                    }
                    System.out.print("-\n");
                } else {
                    System.out.println("None of the books in your Inventory are published by " + pub_name);
                }
                ownerLoop(owner_id);
            case 4:
                //reports
                System.out.println("Which report would you like to view?");
                System.out.print(" (1) - Sales vs Expenditures \t (2) - Sales per Genre \t (3) - Sales per Author\n\n \t\t Selection:");
                Integer sel = uInput.nextInt();
                switch (sel) {
                    case 1:
                        //sales vs expenditures
                        Pair<Pair<Float, Float>, Pair<ArrayList<ArrayList<String>>, ArrayList<ArrayList<String>>>> rv1 =
                                generateSvsEReport(owner_id);
                        String tot_sales = "$" + rv1.getKey().getKey();
                        String tot_expenditures = "$" + rv1.getKey().getValue();
                        ArrayList<ArrayList<String>> sold = rv1.getValue().getKey();
                        ArrayList<ArrayList<String>> notSold = rv1.getValue().getValue();
                        System.out.println("\n--------------------------------------------------");
                        System.out.printf("-%20s%15s-\n", "Total Sales:", tot_sales);
                        System.out.printf("-%20s%15s-\n\n", "Total Expenditures:", tot_expenditures);
                        System.out.println("Sold:");
                        printInventory(sold);
                        System.out.println("Not Sold:");
                        printInventory(notSold);
                        ownerLoop(owner_id);
                    case 2:
                        //sales per genre
                        Pair<ArrayList<Pair<String, Float>>, ArrayList<ArrayList<String>>> rv2 = generateSalesPerGenreReport(owner_id);
                        ArrayList<Pair<String, Float>> tot_genres = rv2.getKey();
                        ArrayList<ArrayList<String>> genres = rv2.getValue();
                        System.out.println("\n--------------------------------------------------");
                        System.out.printf("-%15s%15s-\n", "Genre", "Total Sales");
                        for (int i = 0; i < tot_genres.size(); ++i) {
                            System.out.printf("-%15s%15s-\n", tot_genres.get(i).getKey(), "$" + tot_genres.get(i).getValue());
                        }
                        System.out.println("Sold:");
                        printInventory(genres);
                        ownerLoop(owner_id);
                    case 3:
                        //sales per author
                        Pair<ArrayList<Pair<String, Float>>, ArrayList<ArrayList<String>>> rv3 = generateSalesPerAuthorReport(owner_id);
                        ArrayList<Pair<String, Float>> tot_authors = rv3.getKey();
                        ArrayList<ArrayList<String>> authors = rv3.getValue();
                        System.out.println("\n--------------------------------------------------");
                        System.out.printf("-%20s%15s-\n", "Author", "Total Sales");
                        for (int i = 0; i < tot_authors.size(); ++i) {
                            System.out.printf("-%20s%15s-\n", tot_authors.get(i).getKey(), "$" + tot_authors.get(i).getValue());
                        }
                        System.out.println("Sold:");
                        printInventory(authors);
                        ownerLoop(owner_id);
                    default:
                        System.out.println("Invalid selction, please try again");
                        ownerLoop(owner_id);
                }
                break;
            default:
                System.out.println("Invalid selction, please try again");
                ownerLoop(owner_id);
        }
    }

    private static boolean getOwnerWithOwnerID(Integer owner_id) {
        String query = "SELECT count(owner_id) FROM Owners WHERE owner_id= ?";
        try {
            PreparedStatement pstmt = c.prepareStatement(query);
            pstmt.setInt(1, owner_id);
            ResultSet rs = pstmt.executeQuery();
            rs.next();
            int count = rs.getInt(1);
            if (count > 0) {
                return true;
            } else {
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private static int addOwnerToDB(String email, Integer threshold, Long p_number) {
        int owner_id = -1;
        try(CallableStatement addOwnerToDB = c.prepareCall("{ ? = call addOwnerToDB(?, ?, ?)}")){
            addOwnerToDB.registerOutParameter(1, Types.INTEGER);
            addOwnerToDB.setString(2, email);
            addOwnerToDB.setInt(3, threshold);
            addOwnerToDB.setLong(4, p_number);
            boolean b = addOwnerToDB.execute();
            owner_id = addOwnerToDB.getInt(1);
        }catch (SQLException e) {
            e.printStackTrace();
        }
        return owner_id;
    }

    private static void addBookToDB (long serial_no, String book_name, int owner_id){
        try(CallableStatement addBookToDB = c.prepareCall("{call addBookToDB(?, ?, ?)}")){
            addBookToDB.setLong(1, serial_no);
            addBookToDB.setString(2, book_name);
            addBookToDB.setInt(3, owner_id);
            addBookToDB.execute();
        }catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void addBook_ISBNToDB (long ISBN, String book_name, String genre,
                                          int no_pages, float cost_price, float sales_price,
                                          float percent_to_pub, String pub_name){
        try(CallableStatement addBook_ISBNToDB = c.prepareCall("{call addBook_ISBNToDB(?, ?, ?, ?, ?, ?, ?, ?)}")){
            addBook_ISBNToDB.setLong(1, ISBN);
            addBook_ISBNToDB.setString(2, book_name);
            addBook_ISBNToDB.setString(3, genre);
            addBook_ISBNToDB.setInt(4, no_pages);
            addBook_ISBNToDB.setFloat(5, cost_price);
            addBook_ISBNToDB.setFloat(6, sales_price);
            addBook_ISBNToDB.setFloat(7, percent_to_pub);
            addBook_ISBNToDB.setString(8, pub_name);
            addBook_ISBNToDB.execute();
        }catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void addPublisherToDB (long bank_account, String pub_name, String email, long p_number,
                                          String postal_code, String province, String country, String street, String city){
        try(CallableStatement addPublisherToDB = c.prepareCall("{call addPublisherToDB(?, ?, ?, ?, ?, ?, ?, ?, ?)}")){
            addPublisherToDB.setLong(1, bank_account);
            addPublisherToDB.setString(2, pub_name);
            addPublisherToDB.setString(3, email);
            addPublisherToDB.setLong(4, p_number);
            addPublisherToDB.setString(5, postal_code);
            addPublisherToDB.setString(6, province);
            addPublisherToDB.setString(7, country);
            addPublisherToDB.setString(8, street);
            addPublisherToDB.setString(9, city);
            addPublisherToDB.execute();
        }catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void addAuthorToDB (String author_name, long serial_no){
        try(CallableStatement addAuthorToDB = c.prepareCall("{call addAuthorToDB(?, ?)}")){
            addAuthorToDB.setString(1, author_name);
            addAuthorToDB.setLong(2, serial_no);
            addAuthorToDB.execute();
        }catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static Boolean bookExists(int owner_id, long serial_no) {
        String query = "select serial_no " +
                "from Inventory natural join Book natural join Book_ISBN natural join Book_Author natural join Author natural join Publisher " +
                "where owner_id = ? and serial_no = ?";
        try {
            PreparedStatement pstmt = c.prepareStatement(query);
            pstmt.setInt(1, owner_id);
            pstmt.setLong(2, serial_no);
            ResultSet rs = pstmt.executeQuery();
            String book_found = null;
            while (rs.next()) {
                book_found = rs.getString("serial_no");
            }
            if (book_found != null) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private static void deleteBookBySerialNo(long serial_no, int owner_id) {
        String bookTable = "delete from Book where serial_no = ?";
        String inventoryTable = "delete from Inventory where serial_no = ? and owner_id = ?";
        try {
            PreparedStatement pstmt = c.prepareStatement(bookTable);
            pstmt.setLong(1, serial_no);
            pstmt.executeUpdate();

            PreparedStatement prstmt = c.prepareStatement(inventoryTable);
            prstmt.setLong(1, serial_no);
            prstmt.setInt(2, owner_id);
            prstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static ArrayList getPublisherByName(int owner_id, String pub_name) {
        String query = "select distinct bank_account, pub_name, email, street, city, province, country, postal_code, p_number " +
                "from Inventory natural join Book natural join Book_ISBN natural join Publisher natural join Pub_Address natural join Address natural join Postal_Address natural join Pub_Phone " +
                "where owner_id = ? and pub_name = ?";
        try {
            PreparedStatement pstmt = c.prepareStatement(query);
            pstmt.setInt(1, owner_id);
            pstmt.setString(2, pub_name);
            ResultSet rs = pstmt.executeQuery();
            ArrayList<ArrayList<String>> results = new ArrayList<>();
            while (rs.next()) {
                ArrayList<String> pub = new ArrayList<>(9);
                pub.add(rs.getString("pub_name"));
                pub.add(rs.getString("email"));
                pub.add(rs.getString("bank_account"));
                String address = rs.getString("street") + " " + rs.getString("city") +  " " +
                        rs.getString("province") +  " " + rs.getString("country") +  " " + rs.getString("postal_code");
                pub.add(address);
                pub.add(rs.getString("p_number"));
                results.add(pub);
            }
            return results;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static ArrayList getInventory(int owner_id) {
        String query = "select serial_no, book_name, author_name, isbn, genre, no_pages, cost_price, sales_price, sold, percent_to_pub, pub_name " +
                "from Inventory natural join Book natural join Book_ISBN natural join Book_Author natural join Author natural join Publisher " +
                "where owner_id = ? " +
                "order by serial_no";
        try {
            PreparedStatement pstmt = c.prepareStatement(query);
            pstmt.setInt(1, owner_id);
            ResultSet rs = pstmt.executeQuery();
            return getInventoryList(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Pair<Pair<Float, Float>, Pair<ArrayList<ArrayList<String>>, ArrayList<ArrayList<String>>>> generateSvsEReport(int owner_id){
        String soldQuery = "select serial_no, book_name, author_name, isbn, genre, no_pages, cost_price, sales_price, sold, percent_to_pub, pub_name " +
                "from Inventory natural join Book natural join Book_ISBN natural join Book_Author natural join Author natural join Publisher " +
                "where owner_id = ? and sold = true " +
                "order by serial_no";
        String notSoldQuery = "select serial_no, book_name, author_name, isbn, genre, no_pages, cost_price, sales_price, sold, percent_to_pub, pub_name " +
                "from Inventory natural join Book natural join Book_ISBN natural join Book_Author natural join Author natural join Publisher " +
                "where owner_id = ? and sold = false " +
                "order by serial_no";
        String tot_salesQuery = "select round((sum(sales_price)-sum(percent_to_pub/100*sales_price)),2) as tot_sales " +
                "from Inventory natural join Book natural join Book_ISBN natural join Book_Author natural join Author natural join Publisher " +
                "where owner_id = ? and sold = true";
        String tot_expendituresQuery = "select sum(cost_price) as tot_expenditures " +
                "from Inventory natural join Book natural join Book_ISBN natural join Book_Author natural join Author natural join Publisher " +
                "where owner_id = ?";

        float tot_sales = 0;
        float tot_expenditures = 0;
        ArrayList<ArrayList<String>> sold = new ArrayList<>();
        ArrayList<ArrayList<String>> notSold = new ArrayList<>();

        try {
            PreparedStatement pstmt1 = c.prepareStatement(soldQuery);
            pstmt1.setInt(1, owner_id);
            ResultSet rs1 = pstmt1.executeQuery();
            sold = getInventoryList(rs1);

            PreparedStatement pstmt2 = c.prepareStatement(notSoldQuery);
            pstmt2.setInt(1, owner_id);
            ResultSet rs2 = pstmt2.executeQuery();
            notSold = getInventoryList(rs2);

            PreparedStatement pstmt3 = c.prepareStatement(tot_salesQuery);
            pstmt3.setInt(1, owner_id);
            ResultSet rs3 = pstmt3.executeQuery();
            rs3.next();
            tot_sales = rs3.getFloat(1);

            PreparedStatement pstmt4 = c.prepareStatement(tot_expendituresQuery);
            pstmt4.setInt(1, owner_id);
            ResultSet rs4 = pstmt4.executeQuery();
            rs4.next();
            tot_expenditures = rs4.getFloat(1);

            Pair<Float, Float> floatPair = new Pair<Float, Float>(tot_sales, tot_expenditures);
            Pair<ArrayList<ArrayList<String>>, ArrayList<ArrayList<String>>> arrayPair =
                    new Pair<ArrayList<ArrayList<String>>, ArrayList<ArrayList<String>>>(sold, notSold);
            return new Pair<Pair<Float, Float>, Pair<ArrayList<ArrayList<String>>, ArrayList<ArrayList<String>>>>(floatPair, arrayPair);

        }catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Pair<ArrayList<Pair<String, Float>>, ArrayList<ArrayList<String>>> generateSalesPerGenreReport(int owner_id){
        String tot_GenreQuery = "select genre, round((sum(sales_price)-sum(percent_to_pub/100*sales_price)),2) as tot_sales " +
                "from Inventory natural join Book natural join Book_ISBN natural join Book_Author natural join Author natural join Publisher " +
                "where owner_id = ? and sold = true " +
                "group by genre";
        String genresQuery = "select serial_no, book_name, author_name, isbn, genre, no_pages, cost_price, sales_price, sold, percent_to_pub, pub_name " +
                "from Inventory natural join Book natural join Book_ISBN natural join Book_Author natural join Author natural join Publisher " +
                "where owner_id = ? and sold = true " +
                "order by genre";

        ArrayList<Pair<String, Float>> tot_genres = new ArrayList<Pair<String, Float>>();
        ArrayList<ArrayList<String>> genres = new ArrayList<>();

        try {
            PreparedStatement pstmt1 = c.prepareStatement(tot_GenreQuery);
            pstmt1.setInt(1, owner_id);
            ResultSet rs1 = pstmt1.executeQuery();
            while (rs1.next()) {
                Pair<String, Float> p = new Pair<String, Float>(rs1.getString("genre"), rs1.getFloat("tot_sales"));
                tot_genres.add(p);
            }

            PreparedStatement pstmt2 = c.prepareStatement(genresQuery);
            pstmt2.setInt(1, owner_id);
            ResultSet rs2 = pstmt2.executeQuery();
            genres = getInventoryList(rs2);

            return new Pair<ArrayList<Pair<String, Float>>, ArrayList<ArrayList<String>>>(tot_genres, genres);

        }catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Pair<ArrayList<Pair<String, Float>>, ArrayList<ArrayList<String>>> generateSalesPerAuthorReport(int owner_id){
        String tot_AuthorsQuery = "select author_name, round((sum(sales_price)-sum(percent_to_pub/100*sales_price)),2) as tot_sales " +
                "from Inventory natural join Book natural join Book_ISBN natural join Book_Author natural join Author natural join Publisher " +
                "where owner_id = ? and sold = true " +
                "group by author_name";
        String authorsQuery = "select serial_no, book_name, author_name, isbn, genre, no_pages, cost_price, sales_price, sold, percent_to_pub, pub_name " +
                "from Inventory natural join Book natural join Book_ISBN natural join Book_Author natural join Author natural join Publisher " +
                "where owner_id = ? and sold = true " +
                "order by author_name";

        ArrayList<Pair<String, Float>> tot_authors = new ArrayList<Pair<String, Float>>();
        ArrayList<ArrayList<String>> authors = new ArrayList<>();

        try {
            PreparedStatement pstmt1 = c.prepareStatement(tot_AuthorsQuery);
            pstmt1.setInt(1, owner_id);
            ResultSet rs1 = pstmt1.executeQuery();
            while (rs1.next()) {
                Pair<String, Float> p = new Pair<String, Float>(rs1.getString("author_name"), rs1.getFloat("tot_sales"));
                tot_authors.add(p);
            }

            PreparedStatement pstmt2 = c.prepareStatement(authorsQuery);
            pstmt2.setInt(1, owner_id);
            ResultSet rs2 = pstmt2.executeQuery();
            authors = getInventoryList(rs2);

            return new Pair<ArrayList<Pair<String, Float>>, ArrayList<ArrayList<String>>>(tot_authors, authors);

        }catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static ArrayList<ArrayList<String>> getInventoryList(ResultSet rs) {
        ArrayList<ArrayList<String>> results = new ArrayList<>();
        try {
            while (rs.next()) {
                ArrayList<String> book = new ArrayList<>(11);
                book.add(rs.getString("serial_no"));
                book.add(rs.getString("book_name"));
                book.add(rs.getString("author_name"));
                book.add(rs.getString("isbn"));
                book.add(rs.getString("genre"));
                book.add(rs.getString("no_pages"));
                book.add(rs.getString("cost_price"));
                book.add(rs.getString("sales_price"));
                book.add(rs.getString("sold"));
                book.add(rs.getString("percent_to_pub"));
                book.add(rs.getString("pub_name"));
                results.add(book);
            }
            return results;
        }catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void printInventory(ArrayList<ArrayList<String>> inventory) {
        System.out.printf("-%20s%50s%20s%20s%15s%7s%7s%7s%7s%7s%35s-\n", "Serial Number",
                "Book Name", "Author", "ISBN", "Genre", "Pages", "Cost", "Price", "Sold", "% Pub", "Publisher");
        for (int i = 0; i < inventory.size(); ++i) {
            System.out.print("-");
            for (int j = 0; j < inventory.get(i).size(); ++j) {
                if (j == 0 || j == 2 || j == 3) {
                    System.out.printf("%20s", inventory.get(i).get(j));
                } else if (j == 1) {
                    System.out.printf("%50s", inventory.get(i).get(j));
                } else if(j == 10){
                    System.out.printf("%35s", inventory.get(i).get(j));
                }
                else if (j == 4) {
                    System.out.printf("%15s", inventory.get(i).get(j));
                } else if (j >= 5 && j <= 9) {
                    System.out.printf("%7s", inventory.get(i).get(j));
                }
            }
            System.out.print("-\n");
        }
    }

    private static void printBookDetails(ArrayList<BookDetails> book){
        System.out.printf("-%20s%50s%20s%20s%15s%7s%7s%35s-\n", "Serial Number",
                "Book Name", "Author", "ISBN", "Genre", "Pages", "Price", "Publisher");
        for (int i = 0; i < book.size(); ++i) {
            System.out.print("-");
            System.out.printf("%20s", book.get(i).serial_no + "");
            System.out.printf("%50s", book.get(i).book_name + "");
            System.out.printf("%20s", book.get(i).author_name + "");
            System.out.printf("%20s", book.get(i).ISBN + "");
            System.out.printf("%15s", book.get(i).genre + "");
            System.out.printf("%7s", book.get(i).no_pages + "");
            System.out.printf("%7s", book.get(i).sales_price + "");
            System.out.printf("%35s", book.get(i).publisher + "");
            System.out.print("-\n");
        }
    }

    private static void printBookDetailsForCart(ArrayList<BookDetails> book){
        System.out.printf("-%20s%50s%20s%20s%15s%7s%7s%35s-\n", "Serial Number",
                "Book Name", "Author", "ISBN", "Genre", "Pages", "Price", "Publisher");
        float total = 0;
        for (int i = 0; i < book.size(); ++i) {
            total += book.get(i).sales_price;
            System.out.print("-");
            System.out.printf("%20s", book.get(i).serial_no + "");
            System.out.printf("%50s", book.get(i).book_name + "");
            System.out.printf("%20s", book.get(i).author_name + "");
            System.out.printf("%20s", book.get(i).ISBN + "");
            System.out.printf("%15s", book.get(i).genre + "");
            System.out.printf("%7s", book.get(i).no_pages + "");
            System.out.printf("%7s", book.get(i).sales_price + "");
            System.out.printf("%35s", book.get(i).publisher + "");
            System.out.print("-\n");
        }
        System.out.println("\n Your Total = $" + total);
    }

    private static void printOrderDetails(ArrayList<OrderDetails> order){
        System.out.printf("-%20s%20s%50s%20s%15s%7s%7s-\n", "Order Number", "Serial Number",
                "Book Name", "Author", "Genre", "Pages", "Price");
        for (int i = 0; i < order.size(); ++i) {
            System.out.print("-");
            System.out.printf("%20s", order.get(i).order_no + "");
            System.out.printf("%20s", order.get(i).serial_no + "");
            System.out.printf("%50s", order.get(i).book_name + "");
            System.out.printf("%20s", order.get(i).author_name + "");
            System.out.printf("%15s", order.get(i).genre + "");
            System.out.printf("%7s", order.get(i).no_pages + "");
            System.out.printf("%7s", order.get(i).sales_price + "");
            System.out.print("-\n");
        }
    }
}