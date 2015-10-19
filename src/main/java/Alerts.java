/**
 * Created by Eirik on 10/15/2015.
 */
import javafx.scene.control.Alert;
public class Alerts {
    private String alertMsg;
    private String header;
    private String title;
    public Alerts(final String msg, final String header, final String title) {
        this.alertMsg = msg;
        this.header = header;
        this.title = title;
    }

    /**
     * Displays the error message.
     */
    public final void show() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        String s = alertMsg;
        alert.setContentText(s);
        alert.show();
    }
}
