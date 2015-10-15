/**
 * Created by Eirik on 10/15/2015.
 */
import javafx.scene.control.Alert;
public class Alerts {
    private String alertMsg;
    private String header;
    private String title;
    public Alerts(String msg, String header, String title){
        this.alertMsg = msg;
        this.header = header;
        this.title = title;
    }

    public final void show() {
        Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        String s = alertMsg;
        alert.setContentText(s);
        alert.show();
    }
}
