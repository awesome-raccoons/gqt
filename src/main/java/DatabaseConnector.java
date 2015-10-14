import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.sql.DriverManager;

import java.lang.Class;
import java.sql.SQLException;
import java.sql.SQLOutput;


/**
 * Created by per on 14/10/15.
 */
public class DatabaseConnector {
    private String password;
    private String user;
    private String url;


    public DatabaseConnector(){}

    public static String[] executeQuery(String query){
        String[] results = new String[]{null,null};
        Connection pCon = null;

        Connection con = null;
        Statement st = null;
        Statement pSt = null;
        ResultSet rs = null;
        ResultSet pRs = null;
        try {

            Class.forName("com.mysql.jdbc.Driver");
            //Class.forName("org.postgresql.Driver");


            //query = "SELECT ST_AsText(ST_Envelope(ST_GeomFromText('POLYGON((5 0,7 10,0 15,10 15,15 25,20 15,30 15,22 10,25 0,15 5,5 0))')));;";
            String url = "jdbc:mysql://mysql.stud.ntnu.no/perchrib_raccoons";
            String user = "perchrib";
            String password = "123";
            con = DriverManager.getConnection(url, user, password);
            System.out.println("Connected");
            st = con.createStatement();
            rs = st.executeQuery(query);
            //pCon = DriverManager.getConnection("jdbc:postgresql://127.0.0.1:5450/postgres","postgres", "dbpass");
            //pSt = pCon.createStatement();
            //pRs = pSt.executeQuery(query);


            int count = 0;
            while (rs.next()) {
                System.out.print(rs.getString(1));
                results[count] = rs.getString(1);
                //System.out.print(": ");
                //System.out.println(rs.getString(2));
                count++;
            }
            /*
            while(pRs.next())
            {
                System.out.println(pRs.getString(1));
                results[count] = pRs.getString(1);
            }*/


        } catch (SQLException | ClassNotFoundException ex) {
            Logger lgr = Logger.getLogger(SQLOutput.class.getName());
            lgr.log(Level.SEVERE, ex.getMessage(), ex);
            System.out.println('d');

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
            /*
            if(pRs != null) {
                pRs.close();
            }
            if(pSt != null) {
                pSt.close();
            }
            if(pCon != null) {
                pCon.close();
            }*/
        } catch (SQLException ex) {
            Logger lgr = Logger.getLogger(SQLOutput.class.getName());
            lgr.log(Level.WARNING, ex.getMessage(), ex);
        }
        return results;


    }

}
