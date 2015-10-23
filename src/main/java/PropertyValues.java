import java.io.*;
import java.util.Properties;

/**
 * Created by Eirik on 10/23/2015.
 */
public class PropertyValues {

    public static Database Input() {
        Properties prop = new Properties();
        InputStream input = null;
        Database db = null;
        try {

            input = new FileInputStream("config.properties");
            prop.load(input);
            String name = prop.getProperty("name");
            String url = prop.getProperty("url");
            String user = prop.getProperty("user");
            String password = prop.getProperty("password");
            db = new Database(name,url,user,password);




        } catch (IOException ex) {
            String title = "File not found";
            String header = "";
            String body = "Did not find the configuration file. It should be called config.properties";
            Alerts alert = new Alerts(title,header,body);
            alert.show();
        } finally {
            return db;
        }
    }

    public static void Output(Database db) {
        Properties prop = new Properties();
        OutputStream out = null;

        try{
            out = new FileOutputStream("config.properties");
            prop.setProperty("name",db.getName());
            prop.setProperty("url",db.getUrl());
            prop.setProperty("user",db.getUser());
            prop.setProperty("password",db.getPassword());

            prop.store(out, null);

        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }
}
