




import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.sql.DriverManager;


import java.sql.SQLException;
import java.sql.SQLOutput;
/**
 * Created by Eirik on 10/6/2015.
 */
public final class DatabaseConnector {

        private DatabaseConnector() { }
    /**
     * Makes a connection and sends a query to the currently selected database.
     * @param query Query to be sent to the SQL server
     * @param db The current selected db, contains name, url, username, password.
     * @return Returns either a WKT string or an error message.
     */
        public static String executeQuery(final String query, final Database db) {
            String results = null;
            Connection pCon = null;
            Connection con = null;
            Statement st = null;
            Statement pSt = null;
            ResultSet rs = null;
            ResultSet pRs = null;
            String user = db.getUser();
            String url = db.getUrl();
            String password = db.getPassword();
        try {

            Class.forName("com.mysql.jdbc.Driver");
            Class.forName("org.postgresql.Driver");



            //Sample query and servers for testing

            //String query = "SELECT ST_AsText(ST_Envelope(ST_GeomFromText(
            // 'POLYGON((5 0,7 10,0 15,10 15,15 25,20 15,30 15,22 10,25 0,15 5,5 0))')));;";
            //url = "jdbc:mysql://127.0.0.1:3306/sakila";
            //user = "root";
            //password = "dbpass";

            //String url = "jdbc:mysql://mysql.stud.ntnu.no/perchrib_raccoons";
            //String user = "perchrib";
            //String password = "123";


            if (url.contains("mysql")) {
                con = DriverManager.getConnection(url, user, password);
                st = con.createStatement();
                rs = st.executeQuery(query);
                while (rs.next()) {
                    System.out.print(rs.getString(1));
                    results = rs.getString(1);

                }
            } else if (url.contains("postgresql")) {
                pCon = DriverManager.getConnection(url, user, password);
                pSt = pCon.createStatement();
                pRs = pSt.executeQuery(query);
                while (pRs.next()) {
                    System.out.println(pRs.getString(1));
                    results = pRs.getString(1);
                }
            } else {
                String exception = "Server URL not valid";
                results = exception;
            }
            if (rs != null) {
                rs.close();
            }
            if (st != null) {
                st.close();
            }
            if (con != null) {
                con.close();
            }
            if (pRs != null) {
                pRs.close();
            }
            if (pSt != null) {
                pSt.close();
            }
            if (pCon != null) {
                pCon.close();
            }


        } catch (SQLException | ClassNotFoundException ex) {


            if (ex.toString().contains("PSQL")) {
                String exception = "POSTGIS Error";
                results = exception;
                return results;
            } else if (ex.toString().contains("MySQL")) {
                String exception = "MYSQL error";
                results = exception;
                return results;
            } else if (ex.toString().contains("Access denied")) {
                String exception = "Wrong username or password";
                results = exception;
                return results;

            } else if (ex.toString().contains("Communications")) {
                String exception = "Wrong server address";
                results = exception;
                return results;
            }
        }
            try {
                if (rs != null) {
                    rs.close();
                }
                if (st != null) {
                    st.close();
                }
                if (con != null) {
                    con.close();
                }
                if (pRs != null) {
                    pRs.close();
                }
                if (pSt != null) {
                    pSt.close();
                }
                if (pCon != null) {
                    pCon.close();
                }
            } catch (SQLException ex) {
                System.out.println("The error:");
                System.out.println(ex.getMessage());
                Logger lgr = Logger.getLogger(SQLOutput.class.getName());

                if (SQLOutput.class.getName().contains("PSQL")) {
                    String exception = "Postgis error";
                    results = exception;
                    return results;
                } else if (ex.getMessage().contains("MYSQL")) {
                    String exception = "MYSQL error";
                    results = exception;
                    return results;
                } else {
                    lgr.log(Level.WARNING, ex.getMessage(), ex);
                }

            }
            if (results == null) {
                String exception = "Invalid Query";
                results = exception;
            }
            return results;


    }

}


