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
        controller.init();
        primaryStage.setTitle("Gis Query Tool");
        primaryStage.setMaximized(true);
        Scene scene = new Scene(root);
        // set zooming by Control+scroll to work from everywhere
        javafx.event.EventHandler<ScrollEvent> mouseScrollHandler =
                event -> controller.handleSceneScrollEvent(event);
        scene.setOnScroll(mouseScrollHandler);
        // enable keyboard shortcuts to work regardless of current focus
        javafx.event.EventHandler<KeyEvent> keyHandler =
                event -> controller.handleSceneKeyEvent(event);
        scene.setOnKeyPressed(keyHandler);

        primaryStage.setScene(scene);
        primaryStage.show();

        //Draw grid
        BackgroundGrid bg = new BackgroundGrid(primaryStage.getWidth(),
                primaryStage.getHeight(), controller.getUpperPane());
        bg.moveGrid(bg.DEFAULT_SPACING_X, bg.DEFAULT_SPACING_Y,0,0);
        controller.getUpperPane().getChildren().add(0, bg);
        controller.setBackgroundGrid(bg);

        primaryStage.widthProperty().addListener(bg);
        primaryStage.heightProperty().addListener(bg);

        //Create an initial empty layer
        controller.createEmptyLayer();
    }


    public static void main(final String[] args) {
        launch(args);
    }
}
