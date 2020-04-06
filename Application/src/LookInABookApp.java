import javax.sound.midi.SysexMessage;
import java.awt.print.Book;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.*;
import java.util.*;
 
public class LookInABookApp {

    private static int userAccountNumber;
    private static Connection c = null;

    public static void main(String[] args) {
        String url = "jdbc:postgresql://localhost:5432/OnlineBookstore";
        String user = "postgres";
        String password = "rootPass";

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
                Long.parseLong(creditCard);
            } catch (NumberFormatException nfe){
                System.out.println("Invalid order number");
                createUserBilling();
            }


            if (street.isEmpty() || street == null || city.isEmpty() || city == null || postal_code.isEmpty() || postal_code == null ||
                province.isEmpty() || province == null || country.isEmpty() || country == null || creditParsed == null) {
                System.out.println("Invalid entry or entire(s) please try again!");
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
                 System.out.println("Invalid entry or entirie(s) please try again!");
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
     * Used to authenticate owner at login
     */
    private static void ownerLoginLoop() {
        System.out.println("In Owner Loop");
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
        System.out.print(" (1) - Entire Collection \t (2) - Book Name \t (3) - Author Name \t (4) - ISBN \t (5) - Genre");
        Integer selection = uInput.nextInt();

        switch(selection){
            case 1:
                while (true){
                    System.out.print("Search by [Book Name]: ");
                    String book_name = uInput.nextLine();
                    ArrayList<BookDetails> returnedBooks = getBooksByName(book_name);
                    if (returnedBooks != null && returnedBooks.size() >= 0){
                        System.out.println("-------------------------------");
                        System.out.println("/| We found the following books |\\");
                        System.out.println("Serial Number - ISBN - Book Name - Book Author - Genre - Number of Pages - Sales Price");
                        for (BookDetails bd: returnedBooks){
                            System.out.println( bd.serial_no + " " + bd.ISBN + " " + bd.book_name + " " + bd.author_name + " " + bd.genre + " " + bd.no_pages + " " + bd.sales_price);
                        }
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
                break;
            case 3:
                break;
            case 4:
                break;
            default:
                System.out.println("Invalid selction, please try again");
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
                    if (bd.serial_no.equals(serial_no)){
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
            System.out.println("Serial Number - ISBN - Book Name - Book Author - Genre - Number of Pages - Sales Price");
            float total = 0;
            for (BookDetails bd: booksInCart){
                total += bd.sales_price;
                System.out.println( bd.serial_no + " " + bd.ISBN + " " + bd.book_name + " " + bd.author_name + " " + bd.genre + " " + bd.no_pages + " " + bd.sales_price);
            }
            System.out.println("\n Your Total = $" + total);
            System.out.println("--------------------------------------------------");
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
                Long.parseLong(creditCard);
            } catch (NumberFormatException nfe){
                System.out.println("Invalid order number");
                getBillingForOrder();
            }


            if (street.isEmpty() || street == null || city.isEmpty() || city == null || postal_code.isEmpty() || postal_code == null ||
                    province.isEmpty() || province == null || country.isEmpty() || country == null || creditParsed == null) {
                System.out.println("Invalid entry or entire(s) please try again!");
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
                System.out.println("Invalid entry or entire(s) please try again!");
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
        String query = "select checkout.serial_no, isbn, book_name, author_name, genre, no_pages, sales_price " +
                       "from checkout natural join Book natural join Book_ISBN natural join Book_Author natural join Author " +
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
                String genre = rs.getString("genre");
                int no_pages = rs.getInt("no_pages");
                float price = rs.getFloat("sales_price");
                bd = new BookDetails(serial_no, isbn, name, genre, no_pages, price);
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
            Long.parseLong(order_no);
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

    /**
     * Get Book By Name
     * @param book_name
     * @return
     */
    private static ArrayList<BookDetails> getBooksByName(String book_name){
        String query = "SELECT distinct on (Book.isbn) isbn, serial_no, book_name, author_name, genre, no_pages, sales_price " +
                       "from Book natural join Book_ISBN natural join Book_Author natural join Author " +
                       "WHERE book_name LIKE ? and Book.sold = false";
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
             String genre = rs.getString("genre");
             int no_pages = rs.getInt("no_pages");
             float price = rs.getFloat("sales_price");
             bd = new BookDetails(serial_no, isbn, name, genre, no_pages, price);
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
            int results = pstmt.executeUpdate();
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
    private static int insertIntoAddressAndPostalAddress(String street, String city, String postal_code, String province, String country){
        String addressQuery = "INSERT into address (postal_code, street, city) values (?, ?, ?)";
        String postalAddressQuery = "INSERT into postal_address (postal_code, province, country) values (?, ?, ?)";
        int results = 0;
        try {
            PreparedStatement pstmt = c.prepareStatement(postalAddressQuery);
            pstmt.setString(1, postal_code);
            pstmt.setString(2, province);
            pstmt.setString(3, country);
            results = pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            createUserBilling();
        }

        try {
            PreparedStatement pstmt = c.prepareStatement(addressQuery);
            pstmt.setString(1, postal_code);
            pstmt.setString(2, street);
            pstmt.setString(3, city);
            results = pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            createUserBilling();
        }
        return results;
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
            int results = pstmt.executeUpdate();
            return;
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
            int results = pstmt.executeUpdate();
            return;
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
            int results = pstmt.executeUpdate();
            return;
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
            int results = pstmt.executeUpdate();
            return;
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
}