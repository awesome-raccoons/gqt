import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class Main extends Application {

    private static Stage stage;

    @Override
    public final void start(final Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        Main.stage = primaryStage;
        primaryStage.setTitle("Gis Query Tool");
        primaryStage.setMaximized(true);
        primaryStage.setScene(new Scene(root));
        primaryStage.show();

    }

    public static Stage getStage() {
        return stage;
    }

    public static void main(final String[] args) {
        launch(args);
    }
}
