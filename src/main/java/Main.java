import javafx.application.Application;
import javafx.event.EventType;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.stage.Stage;

import java.beans.EventHandler;


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

    }


    public static void main(final String[] args) {
        launch(args);
    }
}
