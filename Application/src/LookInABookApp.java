import java.sql.*;
import java.util.*;
 
public class LookInABookApp {

    public static void main(String[] args) {
        //PostgreSqlExample.forName("com.example.jdbc.Driver");
        String url = "jdbc:postgresql://localhost:5432/OnlineBookstore";
        String user = "postgres";
        String password = "rootPass";

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
        System.out.println("In Owner Loop");
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
}