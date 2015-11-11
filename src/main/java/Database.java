/**
 * Created by Eirik on 10/16/2015.
 */
public class Database {
    private String url;
    private String user;
    private String password;
    private String name;
    public Database(final String name, final String url, final String user, final String password) {
        this.url = url;
        this.user = user;
        this.password = password;
        this.name = name;

    }

    public final String getUrl() {
        return this.url;
    }

    public final String getUser() {
        return this.user;
    }

    public final String getPassword() {
        return this.password;
    }

    public final String getName() {
        return this.name;
    }

    public final String toString() {
        return this.name;
    }
 }
