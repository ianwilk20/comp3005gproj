import java.sql.*;
import java.util.*;
 
public class LookInABookApp {

    public static void main(String[] args) {
        //PostgreSqlExample.forName("com.example.jdbc.Driver");
        String url = "jdbc:postgresql://localhost:5432/OnlineBookstore";
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
            System.out.println("Would you like to login as a User or Owner?\n\nPress (1) for User \t\t Press (2) for Owner\n\n \t\t Selection: ");
            Scanner uInput = new Scanner(System.in);
            Integer role = uInput.nextInt();

            if (role == 1){ //User
                uoLoop = false;
                userLoop(c);
            } else if (role == 2){ //Owner
                uoLoop = false;
                ownerLoop(c);
            } else {
                System.out.println("Invalid selection");
                continue;
            }
        }
    }

    private static void userLoop(Connection c) {
        System.out.println("Would you like to Login or Create and Account?\n\nPress (1) to Login \t\t Press (2) to Create an Account\n\n \t\t Selection: ");
        Scanner uInput = new Scanner(System.in);
        Integer role = uInput.nextInt();
        if (role == 1){
            System.out.println("Please enter your username: ");
            uInput = new Scanner(System.in);
            String user_name = uInput.nextLine();
            Boolean userInDB = getUserWithUsername(c, user_name);
            if (userInDB){ System.out.println("Authenticated"); }
            else {
                System.out.println("Username not found!");
                userLoop(c);
            }
        }
        else if (role == 2){
            System.out.println("Please enter a username to create: ");
            uInput = new Scanner(System.in);
            String user_name = uInput.nextLine();
            //addUserToDB;
        }
    }

    private static void ownerLoop(Connection c) {
        System.out.println("In Owner Loop");
    }

    private static boolean getUserWithUsername(Connection c, String username){
        String query = "SELECT count(user_name) FROM users WHERE user_name='" + username + "'";
        Statement stmt = null;
        try {
            stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery(query);
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
     **/
    private String getUser(Connection c) {
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
}