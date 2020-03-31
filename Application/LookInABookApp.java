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
        System.out.println("Connected to the PostgreSQL server successfully.\n\n");
        System.out.print("Enter a course to find prerequisites for: ");
        Scanner uInput = new Scanner(System.in);
        String course = uInput.nextLine();
        System.out.println("");
        Statement statement = c.createStatement();
        HashMap<String, String> preReqs = new HashMap<String, String>();
        Boolean morePreReqsExist = true;
        while (morePreReqsExist){
          try{
            ResultSet qResult = statement.executeQuery("select prereq.prereq_id" + 
                                                       " from course inner join prereq" + 
                                                       " on course.course_id = prereq.course_id" +
                                                       " where course.course_id = '" + course + "'");

            if (!qResult.next()){
              System.out.println("There is no prerequisite for " + course); 
              morePreReqsExist = false;
            } else {
                if (preReqs.containsKey(course)) {
                  morePreReqsExist = false;
                } else {
                  preReqs.put(course, qResult.getString("prereq_id"));
                  System.out.println("The prerequisite for " + course + " is: " + qResult.getString("prereq_id"));  
                  course = qResult.getString("prereq_id");
                }
              qResult.close();
            }
            
          } catch (SQLException sqlE) {
            sqlE.printStackTrace();
            System.err.println(sqlE.getClass().getName()+": "+sqlE.getMessage());
            System.exit(0);
          }
        }

      } catch (Exception e) {
         e.printStackTrace();
         System.err.println(e.getClass().getName()+": "+e.getMessage());
         System.exit(0);
      }
      System.out.println("\nThank you for using our database prerequiste querying system");
    }
}