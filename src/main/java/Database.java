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
        this.name = name;

    }

    public String getUrl() {
        return url;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public String getName() {
        return name;
    }

    public String toString(){
        return this.name;
    }
}
