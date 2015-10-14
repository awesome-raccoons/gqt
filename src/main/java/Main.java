import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.ScrollEvent;
import javafx.stage.Stage;


public class Main extends Application {

    @Override
    public final void start(final Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("sample.fxml"));

        Parent root = loader.load();
        Controller controller = loader.<Controller>getController();
        controller.setStage(primaryStage);
        primaryStage.setTitle("Gis Query Tool");
        primaryStage.setMaximized(true);
        Scene scene = new Scene(root);
        // set zooming by Control+scroll to work from everywhere
        javafx.event.EventHandler<ScrollEvent> mouseScrollHandler =
                event -> controller.handleSceneScrollEvent(event);
        scene.setOnScroll(mouseScrollHandler);
        // set key combinations with Control to work from everywhere
        javafx.event.EventHandler<KeyEvent> keyHandler =
                event -> controller.handleSceneKeyEvent(event);
        scene.setOnKeyPressed(keyHandler);

        primaryStage.setScene(scene);
        primaryStage.show();
        DatabaseConnector dbConnector = new DatabaseConnector();
        String query = "SELECT AsText(Envelope(GeomFromText('POLYGON((5 0,7 10,0 15,10 15,15 25,20 15,30 15,22 10,25 0,15 5,5 0))')));;";
        dbConnector.executeQuery(query);

    }


    public static void main(final String[] args) {
        launch(args);
    }
}
