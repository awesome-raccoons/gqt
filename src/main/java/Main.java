import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class Main extends Application {

    private Stage stage;

    @Override
    public final void start(final Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("sample.fxml"));

        Parent root = loader.load();
        Controller controller = loader.<Controller>getController();
        controller.setStage(primaryStage);

        this.stage = primaryStage;
        primaryStage.setTitle("Gis Query Tool");
        primaryStage.setMaximized(true);
        primaryStage.setScene(new Scene(root));
        primaryStage.show();

    }

    public final Stage getStage() {
        return this.stage;
    }

    public static void main(final String[] args) {
        launch(args);
    }
}
