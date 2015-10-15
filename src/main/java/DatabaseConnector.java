


//import java.sql.*;
import org.postgresql.util.PSQLException;

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
 * Created by Eirik on 10/6/2015.
 */
public class DatabaseConnector {


        private String password;
        private String user;
        private String url;



        public DatabaseConnector(){}

        public static String[] executeQuery(String query){
            String[] results = new String[]{null,null};
            Connection pCon = null;
            int count = 0;
            Connection con = null;
            Statement st = null;
            Statement pSt = null;
            ResultSet rs = null;
            ResultSet pRs = null;
        try {

            Class.forName("com.mysql.jdbc.Driver");
            Class.forName("org.postgresql.Driver");


            //String query = "SELECT ST_AsText(ST_Envelope(ST_GeomFromText('POLYGON((5 0,7 10,0 15,10 15,15 25,20 15,30 15,22 10,25 0,15 5,5 0))')));;";
            String url = "jdbc:mysql://127.0.0.1:3306/sakila";
            String user = "root";
            String password = "dbpass";
            //String url = "jdbc:mysql://mysql.stud.ntnu.no/perchrib_raccoons";
            //String user = "perchrib";
            //String password = "123";
            con = DriverManager.getConnection(url, user, password);
            st = con.createStatement();
            rs = st.executeQuery(query);
            pCon = DriverManager.getConnection("jdbc:postgresql://127.0.0.1:5450/postgres", "postgres", "dbpass");
            pSt = pCon.createStatement();
            pRs = pSt.executeQuery(query);



            while (rs.next()) {
                System.out.print(rs.getString(1));
                results[count] = rs.getString(1);
                //System.out.print(": ");
                //System.out.println(rs.getString(2));
                count++;
            }
            while(pRs.next())
            {
                System.out.println(pRs.getString(1));
                results[count] = pRs.getString(1);
            }


        } catch (SQLException | ClassNotFoundException ex) {
            Logger lgr = Logger.getLogger(SQLOutput.class.getName());
            //lgr.log(Level.SEVERE, ex.getMessage(), ex);


            if(ex.toString().contains("PSQL")){
                String exception = "POSTGIS Error";
                results[count] = exception;
                return results;
            }
            else if(ex.toString().contains("MySQL")){
                String exception = "MYSQL error";
                results[count] = exception;
                return results;
            }
            else {
                lgr.log(Level.WARNING, ex.getMessage(), ex);
                System.out.println("test");
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
                if(pRs != null) {
                    pRs.close();
                }
                if(pSt != null) {
                    pSt.close();
                }
                if(pCon != null) {
                    pCon.close();
                }
            } catch (SQLException ex) {
                System.out.println("The error:");
                System.out.println(ex.getMessage());
                //Logger lgr = Logger.getLogger(SQLOutput.class.getName());

                if(SQLOutput.class.getName().contains("PSQL")){
                    String exception = "Postgis error";
                    results[count] = exception;
                    return results;
                }
                else if(ex.getMessage().contains("MYSQL")){
                    String exception = "MYSQL error";
                    results[count] = exception;
                    return results;
                }
                else {
                    //lgr.log(Level.WARNING, ex.getMessage(), ex);
                    System.out.println("test");
                }

            }
            return results;


    }

}


