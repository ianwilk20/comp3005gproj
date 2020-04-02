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
            System.out.println("Please enter an email to associate to the user_name: ");
            String email = uInput.nextLine();

            if (user_name.isEmpty() || user_name == null || email.isEmpty() || email == null){
                System.out.println("Invalid user_name or email please try again!");
                continue;
            } else {
                insertUserIntoDB(user_name, email);
                System.out.println("--------------------------------------------------\n");
                //System.out.println("Please wait while we redirect you to login");
                return;
            }
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
            String address = uInput.nextLine();
            System.out.print("Enter your city: ");
            String city = uInput.nextLine();
            System.out.print("Enter your postal code: ");
            String postal_code = uInput.nextLine();
            System.out.print("Enter your province: ");
            String province = uInput.nextLine();
            System.out.print("Enter your country: ");
            String country = uInput.nextLine();
            uInput = new Scanner(System.in);
            System.out.print("Enter your credit card number: ");
            Integer creditCard = uInput.nextInt();


            if (address.isEmpty() || address == null || city.isEmpty() || city == null || postal_code.isEmpty() || postal_code == null ||
                province.isEmpty() || province == null || country.isEmpty() || country == null || creditCard == null) {
                System.out.println("Invalid entry or entire(s) please try again!");
                continue;
            } else {
                boolean result = insertUserBillingIntoDB(address, city, postal_code, province, country, creditCard);
                System.out.println("--------------------------------------------------\n");
                if (result == false){
                    System.out.println("Please try again.");
                    createUserBilling();
                }
                return;
            }
        }

    }

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
                     province.isEmpty() || province == null || country.isEmpty() || country == null) {
                 System.out.println("Invalid entry or entirie(s) please try again!");
                 continue;
             } else {
                 insertUserShippingIntoDB(address, city, postal_code, province, country);
                 System.out.println("--------------------------------------------------\n");
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
     * The User Book Search Loop
     */
    private static void userLoop() {
        Scanner uInput = new Scanner(System.in);
        System.out.println("\n--------------------------------------------------");
        System.out.println("How would you like to search for your book?");
        System.out.print(" (1) - Book Name \t (2) - Author Name \t (3) - ISBN \t (4) - Genre");
        Integer selection = uInput.nextInt();

        switch(selection){
            case 1:
                while (true){
                    System.out.print("Search by [Book Name]: ");
                    String book_name = uInput.nextLine();
                    String result = getBookByName(book_name);
                    if (result.equals("Not Found")){
                        System.out.println("The Book: '" + book_name + "' could not be found.");
                        System.out.print("Would you like to (1) Try Again \t (2) Return to Search: ");
                        Integer choice = uInput.nextInt();
                        if (choice == 1){
                            continue;
                        } else if (choice == 2){
                            userLoop();
                        }
                    } else {
                        System.out.println("-------------------------------");
                        System.out.println("/| We found the following books |\\");
                        System.out.println(result); //not gonna be this easy...
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
    private static String getBookByName(String book_name){
        String query = "SELECT * FROM Book_ISBN WHERE book_name LIKE ?";
        Statement stmt = null;
        try {
            PreparedStatement pstmt = c.prepareStatement(query);
            pstmt.setString(1, book_name);
            ResultSet rs = pstmt.executeQuery();
            rs.next();
            String book_found = rs.getString(book_name);
            return book_found;
//            if (count > 0) { return true; }
//            else { return false; }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Not Found";
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
            boolean results = pstmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    /**
     * Insert user's billing info into DB
     * @param street
     * @param city
     * @param postal_code
     * @param province
     * @param country
     * @param creditCard
     * @return
     */
    private static boolean insertUserBillingIntoDB(String street, String city, String postal_code, String province, String country, Integer creditCard) {
        String billingQuery = "INSERT into users_billing (account_no, postal_code, street, city, credit_card) values (?, ?, ?, ?, ?)";
        boolean results = false;
        try {
            PreparedStatement pstmt = c.prepareStatement(billingQuery);
            pstmt.setInt(1, userAccountNumber);
            pstmt.setString(2, postal_code);
            pstmt.setString(3, street);
            pstmt.setString(4, city);
            pstmt.setInt(5, creditCard);
            results = pstmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        results = insertIntoAddressAndPostalAddress(city, street, postal_code, province, country);
        return results;
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
        String billingQuery = "INSERT into users_shipping (account_no, postal_code, street, city) values (?, ?, ?, ?)";
        boolean results = false;
        try {
            PreparedStatement pstmt = c.prepareStatement(billingQuery);
            pstmt.setInt(1, userAccountNumber);
            pstmt.setString(2, postal_code);
            pstmt.setString(3, street);
            pstmt.setString(4, city);
            results = pstmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        results = insertIntoAddressAndPostalAddress(city, street, postal_code, province, country);
        return results;
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
    private static boolean insertIntoAddressAndPostalAddress(String street, String city, String postal_code, String province, String country){
        String addressQuery = "INSERT into address (postal_code, street, city) values (?, ?, ?)";
        String postalAddressQuery = "INSERT into postal_address (postal_code, province, country) values (?, ?, ?)";
        boolean results = false;
        try {
            PreparedStatement pstmt = c.prepareStatement(addressQuery);
            pstmt.setString(1, postal_code);
            pstmt.setString(2, street);
            pstmt.setString(3, city);
            results = pstmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try {
            PreparedStatement pstmt = c.prepareStatement(postalAddressQuery);
            pstmt.setString(1, postal_code);
            pstmt.setString(2, province);
            pstmt.setString(3, country);
            results = pstmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
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
            boolean results = pstmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}