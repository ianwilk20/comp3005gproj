import java.sql.*;
import java.util.*;
 
public class LookInABookApp {

    public static void main(String[] args) {
        //PostgreSqlExample.forName("com.example.jdbc.Driver");
        String url = "jdbc:postgresql://localhost:5432/OnlineBookStore";
        String user = "postgres";
        String password = "Whale987654";

        Connection c = null;

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
        startLoop(c);
    }

    private static void startLoop(Connection c){
        boolean uoLoop = true;
        while(uoLoop){
            System.out.println("--------------------------------------------------");
            System.out.print("Would you like to login as a User or Owner?\n\nPress (1) for User \t\t Press (2) for Owner\n\n \t\t Selection: ");
            Scanner uInput = new Scanner(System.in);
            Integer role = uInput.nextInt();

            if (role == 1){ //User
                uoLoop = false;
                userLoginLoop(c);
            } else if (role == 2){ //Owner
                uoLoop = false;
                ownerLoginLoop(c);
            } else {
                System.out.println("Invalid selection");
                continue;
            }
        }
    }

    private static void userLoginLoop(Connection c) {
        System.out.println("--------------------------------------------------");
        System.out.print("Would you like to Login or Create and Account?\n\nPress (1) to Login \t\t Press (2) to Create an Account\n\n \t\t Selection: ");
        Scanner uInput = new Scanner(System.in);
        Integer role = uInput.nextInt();
        if (role == 1){
            System.out.println("--------------------------------------------------");
            System.out.print("Please enter your username: ");
            uInput = new Scanner(System.in);
            String user_name = uInput.nextLine();
            Boolean userInDB = getUserWithUsername(c, user_name);
            if (userInDB){
                System.out.println("--------------------------------------------------");
                System.out.println("Authenticated");
                userLoop(c);
            }
            else {
                System.out.println("--------------------------------------------------");
                System.out.println("Username not found!");
                userLoginLoop(c);
            }
        }
        else if (role == 2){
            while (true){
                System.out.println("--------------------------------------------------");
                System.out.print("Please enter a username to create: ");
                uInput = new Scanner(System.in);
                String user_name = uInput.nextLine();
                System.out.println("--------------------------------------------------");
                System.out.println("Please enter an email to associate to the user_name: ");
                String email = uInput.nextLine();
                if (user_name.isEmpty() || user_name == null || email.isEmpty() || email == null){
                    System.out.println("Invalid user_name or email please try again!");
                    continue;
                } else {
                    addUserToDB(c, user_name, email);
                    System.out.println("--------------------------------------------------\n");
                    System.out.println("Please wait while we redirect you to login");
                    userLoginLoop(c);
                }
            }
        }
    }

    private static void ownerLoginLoop(Connection c) {
        System.out.println("--------------------------------------------------");
        System.out.print("Would you like to Login or Create an Account?\n\nPress (1) to Login \t\t Press (2) to Create an Account\n\n \t\t Selection: ");
        Scanner uInput = new Scanner(System.in);
        Integer role = uInput.nextInt();
        if (role == 1){
            System.out.println("--------------------------------------------------");
            System.out.print("Please enter your owner ID: ");
            uInput = new Scanner(System.in);
            Integer owner_id = uInput.nextInt();
            Boolean ownerInDB = getOwnerWithOwnerID(c, owner_id);
            if (ownerInDB){
                System.out.println("--------------------------------------------------");
                System.out.println("Authenticated");
                ownerLoop(c, owner_id);
            }
            else {
                System.out.println("--------------------------------------------------");
                System.out.println("Owner ID not found!");
                ownerLoginLoop(c);
            }
        }
        else if (role == 2){
            while (true){
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
                if (threshold == null || threshold<=0 || email.isEmpty() || email == null || p_number<=0){
                    System.out.println("Invalid email or phone number or threshold please try again!");
                    continue;
                } else {
                    int owner_id = addOwnerToDB(c, email, threshold, p_number);
                    System.out.println("--------------------------------------------------\n");
                    System.out.println("Please wait while we redirect you to login, your owner ID is: " + owner_id);
                    ownerLoginLoop(c);
                }
            }
        }
    }

    private static void userLoop(Connection c) {
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
                    String result = getBookByName(c, book_name);
                    if (result.equals("Not Found")){
                        System.out.println("The Book: '" + book_name + "' could not be found.");
                        System.out.print("Would you like to (1) Try Again \t (2) Return to Search: ");
                        Integer choice = uInput.nextInt();
                        if (choice == 1){
                            continue;
                        } else if (choice == 2){
                            userLoop(c);
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
                userLoop(c);
        }
    }

    private static void ownerLoop(Connection c, int owner_id){
        //FORMATTING
        ArrayList<ArrayList<String>> inventory = getInventory(c, owner_id);
        System.out.println("\n--------------------------------------------------");
        System.out.println("-Serial Number \t Book Name \t Author \t ISBN \t Genre \t " +
                "Number of Pages \t Cost Price \t Sales Price \t Sold \t " +
                "Percent to Publisher \t Publisher-");
        for(int i = 0; i < inventory.size(); ++i){
            System.out.print("-");
            for(int j = 0; j < inventory.get(i).size(); ++j){
                System.out.print(inventory.get(i).get(j) + " \t ");
            }
            System.out.print("-\n");
        }

        Scanner uInput = new Scanner(System.in);
        System.out.println("\n--------------------------------------------------");
        System.out.print(" (1) - Add a Book to your Collection \n (2) - Remove a Book from your Collection \n" +
                " (3) - View Publisher Details \n (4) - Generate a Report\n\n \t\t Selection: ");
        Integer selection = uInput.nextInt();

        switch(selection){
            case 1:
                //add a book
                break;
            case 2:
                //remove a book
                System.out.print("Remove Book with [Serial Number]: ");
                int serial_no = uInput.nextInt();
                boolean exists = bookExists(c, owner_id, serial_no);
                if(exists) {
                    deleteBookBySerialNo(c, serial_no, owner_id);
                }
                else{
                    System.out.println("Your Inventory did not contain a book with " +
                            serial_no + " as its serial number");
                }
                ownerLoop(c, owner_id);
            case 3:
                //publisher
                System.out.print("Get Details about [Publisher Name]: ");
                uInput = new Scanner(System.in);
                String pub_name = uInput.nextLine();
                ArrayList<ArrayList<String>> pubDetails = getPublisherByName(c, owner_id, pub_name);
                if(pubDetails.size()>0) {
                    System.out.println("\n--------------------------------------------------");
                    System.out.println("-Publisher Name \t Email Address \t Bank Account Number \t Address \t Phone Number(s)-");
//                book.add(rs.getString("pub_name"));
//                book.add(rs.getString("email"));
//                book.add(rs.getString("bank_account"));
//                book.add(rs.getString("street"));
//                book.add(rs.getString("city"));
//                book.add(rs.getString("province"));
//                book.add(rs.getString("country"));
//                book.add(rs.getString("postal_code"));
//                book.add(rs.getString("p_number"));
                    for (int i = 0; i < pubDetails.size(); ++i) {
                        if (i > 0) {
                            System.out.print(pubDetails.get(i).get(8) + " \t ");
                        }
                        else {
                            System.out.print("-");
                            for (int j = 0; j < pubDetails.get(i).size(); ++j) {
                                System.out.print(pubDetails.get(i).get(j) + " \t ");
                            }
                        }
                    }
                    System.out.print("-\n");
                }
                else{
                    System.out.println("None of the books in your Inventory are published by " + pub_name);
                }
                ownerLoop(c, owner_id);
            case 4:
                //reports
                System.out.println("Which report would you like to view?");
                System.out.print(" (1) - Sales vs Expenditures \t (2) - Sales per Genre \t (3) - Sales per Author\n\n \t\t Selection:");
                Integer sel = uInput.nextInt();
                switch(sel){
                    case 1:
                        break;
                    case 2:
                        break;
                    case 3:
                        break;
                    default:
                        System.out.println("Invalid selction, please try again");
                        ownerLoop(c, owner_id);
                }
                break;
            default:
                System.out.println("Invalid selction, please try again");
                ownerLoop(c, owner_id);
        }
    }

    /**
     * Delete Book by Serial_no
     * @param c
     * @param serial_no
     * @param owner_id
     */
    private static void deleteBookBySerialNo(Connection c, int serial_no, int owner_id) {
        String bookTable = "delete from Book where serial_no = ?";
        String inventoryTable = "delete from Inventory where serial_no = ? and owner_id = ?";
        try {
            PreparedStatement pstmt = c.prepareStatement(bookTable);
            pstmt.setInt(1, serial_no);
            pstmt.executeUpdate();

            PreparedStatement prstmt = c.prepareStatement(inventoryTable);
            prstmt.setInt(1, serial_no);
            prstmt.setInt(2, owner_id);
            prstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Add User To DB
     * @param c
     * @param user_name
     * @param email
     */
    private static void addUserToDB(Connection c, String user_name, String email) {
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
     * Add Owner To DB
     * @param c
     * @param email
     * @param threshold
     * @param p_number
     * @return owner_id
     */
    private static int addOwnerToDB(Connection c, String email, Integer threshold, Long p_number) {
        String ownersTable = "INSERT into Owners values(default, ?, ?)";
        String phoneNumberTable = "insert into Phone_Number values(?)";
        String ownersPhoneTable = "insert into Owners_Phone\n" +
                "select p_number, owner_id\n" +
                "from Phone_Number, Owners\n" +
                "where p_number = ? and owner_id = ?";
        try {
            PreparedStatement pstmt = c.prepareStatement(ownersTable);
            pstmt.setString(1, email);
            pstmt.setInt(2, threshold);
            pstmt.executeUpdate();
            int owner_id = getOwnerWithEmail(c, email);

            PreparedStatement prstmt = c.prepareStatement(phoneNumberTable);
            prstmt.setLong(1, p_number);
            prstmt.executeUpdate();

            PreparedStatement prestmt = c.prepareStatement(ownersPhoneTable);
            prestmt.setLong(1, p_number);
            prestmt.setInt(2, owner_id);
            prestmt.executeUpdate();

            return owner_id;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /* Get Owner with Specified email from DB
     * @param c
     * @param email
     * @return owner_id
     */
    private static int getOwnerWithEmail(Connection c, String email){
        String query = "SELECT owner_id FROM Owners WHERE email= ?";
        try {
            PreparedStatement pstmt = c.prepareStatement(query);
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            rs.next();
            return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Get User with Specified user_name from DB
     * @param c
     * @param user_name
     * @return
     */
    private static boolean getUserWithUsername(Connection c, String user_name){
        String query = "SELECT count(user_name) FROM users WHERE user_name= ?";
        Statement stmt = null;
        try {
            PreparedStatement pstmt = c.prepareStatement(query);
            pstmt.setString(1, user_name);
            ResultSet rs = pstmt.executeQuery();
            rs.next();
            int count = rs.getInt(1);
            if (count > 0) { return true; }
            else { return false; }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

     /* Get Owner with Specified owner_id from DB
     * @param c
     * @param owner_id
     * @return true/false
     */
    private static boolean getOwnerWithOwnerID(Connection c, Integer owner_id){
        String query = "SELECT count(owner_id) FROM Owners WHERE owner_id= ?";
        try {
            PreparedStatement pstmt = c.prepareStatement(query);
            pstmt.setInt(1, owner_id);
            ResultSet rs = pstmt.executeQuery();
            rs.next();
            int count = rs.getInt(1);
            if (count > 0) { return true; }
            else { return false; }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Get Users From DB
     * @param c
     * @return
     */
    private String getUsers(Connection c) {
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

    private static Boolean bookExists(Connection c, int owner_id, int serial_no){
        String query = "select serial_no " +
                "from Inventory natural join Book natural join Book_ISBN natural join Book_Author natural join Author natural join Publisher " +
                "where owner_id = ? and serial_no = ?";
        try {
            PreparedStatement pstmt = c.prepareStatement(query);
            pstmt.setInt(1, owner_id);
            pstmt.setInt(2, serial_no);
            ResultSet rs = pstmt.executeQuery();
            String book_found = null;
            while(rs.next()) {
                book_found = rs.getString("serial_no");
            }
            if(book_found != null) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Get Book By Name
     * @param c
     * @param book_name
     * @return
     */
    private static String getBookByName(Connection c, String book_name){
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
     * Get Publisher By Name
     * @param c
     * @param owner_id
     * @param pub_name
     * @return
     */
    private static ArrayList getPublisherByName(Connection c, int owner_id, String pub_name){
        String query = "select distinct bank_account, pub_name, email, street, city, province, country, postal_code, p_number " +
                "from Inventory natural join Book natural join Book_ISBN natural join Publisher natural join Pub_Address natural join Address natural join Postal_Address natural join Pub_Phone " +
                "where owner_id = ? and pub_name = ?";
        try {
            PreparedStatement pstmt = c.prepareStatement(query);
            pstmt.setInt(1, owner_id);
            pstmt.setString(2, pub_name);
            ResultSet rs = pstmt.executeQuery();
            ArrayList<ArrayList<String>> results = new ArrayList<>();
            while(rs.next()){
                ArrayList<String> pub = new ArrayList<>(9);
                pub.add(rs.getString("pub_name"));
                pub.add(rs.getString("email"));
                pub.add(rs.getString("bank_account"));
                pub.add(rs.getString("street"));
                pub.add(rs.getString("city"));
                pub.add(rs.getString("province"));
                pub.add(rs.getString("country"));
                pub.add(rs.getString("postal_code"));
                pub.add(rs.getString("p_number"));
                results.add(pub);
            }
            return results;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Get Owners Inventory
     * @param c
     * @param owner_id
     * @return
     */
    private static ArrayList getInventory(Connection c, int owner_id){
        String query = "select serial_no, book_name, author_name, isbn, genre, no_pages, cost_price, sales_price, sold, percent_to_pub, pub_name " +
                "from Inventory natural join Book natural join Book_ISBN natural join Book_Author natural join Author natural join Publisher " +
                "where owner_id = ? " +
                "order by serial_no";
        try {
            PreparedStatement pstmt = c.prepareStatement(query);
            pstmt.setInt(1, owner_id);
            ResultSet rs = pstmt.executeQuery();
            ArrayList<ArrayList<String>> results = new ArrayList<>();
            while(rs.next()){
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
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}