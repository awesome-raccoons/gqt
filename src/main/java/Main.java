
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.io.WKTReader;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class Main extends Application {

    public static Stage stage;

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        Main.stage = primaryStage;
        primaryStage.setTitle("Gis Query Tool");
        primaryStage.setMaximized(true);
        primaryStage.setScene(new Scene(root, 1080, 600));
        primaryStage.show();

    }


    public static void main(String[] args) {
        launch(args);
    }
}
