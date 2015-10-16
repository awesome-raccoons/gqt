/**
 * Created by Eirik on 10/16/2015.
 */
public class Database {
    private String url;
    private String user;
    private String password;
    private String name;
    public Database(String name, String url,String user,String password)
    {
        this.url = url;
        this.user = user;
        this.password = password;

    }
}
