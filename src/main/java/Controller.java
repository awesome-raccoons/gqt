
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;


public class Controller {

    private Database currentDatabase = null;

    @FXML
    private TextArea wktTextArea;
    @FXML
    private TextArea dbTextArea;
    @FXML
    private ComboBox dbList;
    @FXML
    private ComboBox dbList2;
    @FXML
    private TextField dbName;
    @FXML
    private TextField dbUrl;
    @FXML
    private TextField dbUser;
    @FXML
    private TextField dbPassword;
    @FXML
    private AnchorPane upperPane;
    @FXML
    private VBox vboxLayers;
    @FXML
    private TextField zoomText;
    @FXML
    private Text zoomTextError;
    @FXML
    private Text positionX;
    @FXML
    private Text positionY;
    @FXML
    private TabPane tabPane;
    @FXML
    private Tab queryTab;
    @FXML
    private Tab databasesTab;
    @FXML
    private Button submit;
    @FXML
    private MenuItem fitVisibleMenuItem;
    @FXML
    private MenuItem fitSelectedMenuItem;
    @FXML
    private MenuItem fitAllMenuItem;
    @FXML
    private Button loadConfig;
    @FXML
    private Button saveConfig;
    


    /**
     * Stored all GisVisualizations.
     */
    private static List<KeyCode> heldDownKeys = new ArrayList<>();

    private DisplayController displayController;

    public final DisplayController createDisplayController(
            final BackgroundGrid backgroundGrid, final Stage stage) {
        this.displayController = new DisplayController(upperPane, zoomText,
                backgroundGrid, positionX,  positionY, zoomTextError, stage);
        return this.displayController;
    }


    public final void init() {
        fitVisibleMenuItem.setDisable(true);
        fitSelectedMenuItem.setDisable(true);
        fitAllMenuItem.setDisable(true);
    }

    public final AnchorPane getUpperPane() {
        return upperPane;
    }

    /**
     * Called when selecting a database in the dbList dropdownlist,
     * in the main tab.
     */
    public final void changeDatabase() {
        Database db = (Database) dbList.getSelectionModel().getSelectedItem();
        setDatabase(db);
        dbList2.getSelectionModel().select(getCurrentDB());
        this.dbName.setText(getCurrentDB().getName());
        this.dbUrl.setText(getCurrentDB().getUrl());
        this.dbUser.setText(getCurrentDB().getUser());
        this.dbPassword.setText(getCurrentDB().getPassword());
    }

    /**
     * Called when selecting a database in the dbList2 dropdownlist,
     * in the databases tab.
     */
    public final void changeDatabaseOther() {
        Database db = (Database) dbList2.getSelectionModel().getSelectedItem();
        setDatabase(db);
        dbList.getSelectionModel().select(getCurrentDB());
        this.dbName.setText(getCurrentDB().getName());
        this.dbUrl.setText(getCurrentDB().getUrl());
        this.dbUser.setText(getCurrentDB().getUser());
        this.dbPassword.setText(getCurrentDB().getPassword());
    }

    /**
     * Changes the current database to the parameter.
     * @param db The database to switch to.
     */
    public final void setDatabase(final Database db) {
        this.currentDatabase = db;
    }

    /**
     * Called when the Add Database button is clicked. Creates a new
     * database object from the four TextField objects in the databases tab.
     * Gives an alert if all fields are not filled.
     */
    public final void addDatabase() {
        String name = dbName.getText();
        String url = dbUrl.getText();
        String user = dbUser.getText();
        String password = dbPassword.getText();
        if(!name.isEmpty() && !url.isEmpty() && !user.isEmpty()) {
            if(password.isEmpty()){
                password = "";
            }
            Database db = new Database(name, url, user, password);
            setDatabase(db);
            dbList.getItems().add(db);
            dbList2.getItems().add(db);
            dbList.getSelectionModel().select(db);
            dbList2.getSelectionModel().select(db);
            dbName.clear();
            dbUrl.clear();
            dbUser.clear();
            dbPassword.clear();
        } else {
            String title = "Empty field error";
            String body = "All entry fields (except the password field) must be filled";
            Alerts alert = new Alerts(title, "", body);
            alert.show();
        }
    }

    /**
     * Loads a database stored in config.properties. Gives an alert if there is
     * no database stored in the file.
     */
    public final void loadConfig() {
        Database db = PropertyValues.Input();
        if (db != null) {
            dbList.getItems().add(db);
            dbList2.getItems().add(db);
            setDatabase(db);
            dbList.getSelectionModel().select(db);
            dbList2.getSelectionModel().select(db);
        } else {
            String title = "Failed to load properties";
            String body = "No database stored in config.properties";
            Alerts alert = new Alerts(title, "", body);
            alert.show();

        }


    }

    /**
     * Saves the currently selected database to the config.properties file. Gives an
     * alert if no database is selected.
     */
    public final void saveConfig() {
        if (this.getCurrentDB() == null) {
            String title = "Failed to save properties";
            String body = "No database selected";
            Alerts alert = new Alerts(title, "", body);
            alert.show();
        } else {
            PropertyValues.Output(this.getCurrentDB());
        }

    }

    @FXML
    public final void updateLayer() {
        WktParser wktParser = new WktParser(Layer.getSelectedLayer(), upperPane);
        Layer.getSelectedLayer().setSQLQuery(dbTextArea.getText());
        boolean result = wktParser.parseWktString(wktTextArea.getText());
        if (result) {
            wktParser.updateLayerGeometries();
            displayController.rescaleAllGeometries();
        }
    }

    public final MenuItem getFitSelectedMenuItem() {
        return fitSelectedMenuItem;
    }

    public final Button getSubmit() {
        return submit;
    }

    public final MenuItem getFitAllMenuItem() {
        return fitAllMenuItem;
    }

    public final MenuItem getFitVisibleMenuItem() {
        return fitVisibleMenuItem;
    }

    public final void createEmptyLayer() {
        Layer l = new Layer(null, vboxLayers, "Empty", wktTextArea, dbTextArea, this);
        Layer.getLayers(false).add(l);
        l.addLayerToView();
        //To ensure the latest new layer will be selected.
        l.handleLayerMousePress(true);
        wktTextArea.requestFocus();

}


    /**
     * Gets boundaries for all layers and find best zoom and position for it.
     */
    public final void zoomToFitAll() {
        displayController.zoomToFitAll();
    }

    /**
     * Gets boundaries for all selected layers and find best zoom and position for it.
     */
    public final void zoomToFitSelected() {
        displayController.zoomToFitSelected();
    }

    /**
     * Gets boundaries for all visible layers and find best zoom and position for it.
     */
    public final void zoomToFitVisible() {
        displayController.zoomToFitVisible();
    }

    /**
     * reset view to default position centered around (0 0) with 100% zoom.
     */
    public final void resetView() {
        displayController.resetView();
    }

    /**
     * event handler for key press on upperPane.
     * @param event key event
     */
    public final void handleUpperPaneKeyPresses(final KeyEvent event) {
        switch (event.getText()) {
            case "+":
                displayController.zoomIn();
                break;
            case "-":
                displayController.zoomOut();
                break;
            case "*":
                zoomToFitVisible();
                break;
            case "/":
                resetView();
                break;
            default:
                break;
        }
    }

    /**
     * Event handler for mouse wheel.
     * Zooms in and out depending on direction of scrolling.
     * @param event scroll event
     */
    public final void mouseScrollEvent(final ScrollEvent event) {
        // scroll down
        if (event.getDeltaY() < 0) {
            displayController.zoomOut();
        } else { // scroll up
            displayController.zoomIn();
        }
        displayController.updateCoordinatesText(event.getSceneX(), event.getSceneY());
    }

    /**
     * Called when the mouse press the upperPane.
     * This pane will receive focus and save the coordinates of the press to be used for dragging.
     * @param event MouseEvent to react to.
     */
    public final void upperPaneMousePressed(final MouseEvent event) {
        displayController.upperPaneMousePressed(event);
    }

    /**
     * Called when upperPane is being dragged and drag it accordingly.
     * Also sets cursor to Cursor.Move.
     * @param event MouseEvent to react to.
     */
    public final void upperPaneMouseDragged(final MouseEvent event) {
        displayController.upperPaneMouseDragged(event);
    }

    /**
     * Called when mouse releases on upperPane. Makes sure cursor goes back to normal.
     */
    public final void upperPaneMouseReleased(final MouseEvent event) {
        displayController.upperPaneMouseReleased(event);
    }


    public final Database getCurrentDB() {
        return this.currentDatabase;
    }

    /**
     * Called when clicking the submit query button.
     */
    public final void submitQuery() {
        String qText = dbTextArea.getText();
        Database database = getCurrentDB();
        if (database != null) {
            try {
                String result = DatabaseConnector.executeQuery(qText, database);

                if (result.contains("POSTGIS Error")) {
                    String title = "SQL Error";
                    String header = "POSTGIS Error";

                    //Specify different errors later, for example self-intersection
                    String alertMsg = "Invalid geometry,wrong syntax or empty query";
                    Alerts alert = new Alerts(alertMsg, title, header);
                    alert.show();
                } else if (result.contains("MYSQL error")) {
                    String title = "SQL Error";
                    String header = "MYSQL Error";

                    //Specify different errors later, for example self intersection
                    String alertMsg = "Invalid geometry, wrong syntax or empty query";
                    Alerts alert = new Alerts(alertMsg, title, header);
                    alert.show();
                } else if (result.contains("URL not valid")) {
                    String title = "Server Error";
                    String header = " ";
                    String alertMsg = "Server URL not valid";
                    Alerts alert = new Alerts(alertMsg, title, header);
                    alert.show();
                } else if (result.contains("Wrong username")) {
                    String title = "Credential error";
                    String header = " ";
                    String alertMsg = "Wrong username or password";
                    Alerts alert = new Alerts(alertMsg, title, header);
                    alert.show();
                } else if (result.contains("Invalid Query")) {
                    String title = "Query Error";
                    String header = "";
                    String alertMsg = "Syntax error in query or invalid request";
                    Alerts alert = new Alerts(alertMsg, title, header);
                    alert.show();

                } else if (result.contains("Wrong server")) {
                    String title = "Server Error";
                    String header = "";
                    String alertMsg = "Server URL not valid";
                    Alerts alert = new Alerts(alertMsg, title, header);
                    alert.show();
                } else {
                    wktTextArea.setText(result);
                    updateLayer();
                }
            } catch (NullPointerException e) {
                Alerts alert = new Alerts("Query returned null", "Null error", "");
                alert.show();
            }
        } else {
            Alerts alert = new Alerts("No database selected", "DB Error", "");
            alert.show();
        }
    }


    public final void queryAreaKeyPressed(final KeyEvent event) {
        if (event.isAltDown() && event.getCode() == KeyCode.ENTER) {

            submitQuery();
        }
    }

    public final void onAnyKeyPressed(final KeyEvent event) {
        if (!heldDownKeys.contains(event.getCode())) {
            heldDownKeys.add(event.getCode());
        }
    }

    /**
     * Handler to allow zooming by Control+MouseWheel regardless of current focus.
     * @param event
     */
    public final void handleSceneScrollEvent(final ScrollEvent event) {
        if (event.isControlDown()) {
            mouseScrollEvent(event);
        }
    }

    public final void moveLayersDown() {
        ArrayList<Layer> selectedLayers = Layer.getAllSelectedLayers(false);
        if (selectedLayers.size() != 0) {
            selectedLayers.get(0).moveSelectedLayers(1);
        }
    }

    public final void moveLayersUp() {
        ArrayList<Layer> selectedLayers = Layer.getAllSelectedLayers(false);
        if (selectedLayers.size() != 0) {
            selectedLayers.get(0).moveSelectedLayers(-1);
        }
    }

    public final void deleteSelectedLayers() {
        Layer.getAllSelectedLayers(false).forEach(Layer::deleteLayer);
        ArrayList<Layer> layers = Layer.getLayers(false);
        if (layers.size() > 0) {
            Layer layer = layers.get(layers.size() - 1);
            layer.handleLayerMousePress(true);
        }

    }

    public final void selectAllLayers() {
        Layer.selectAllLayers();
    }
    /**
     * Handler to allow keyboard shortcuts regardless of current focus.
     * @param event
     */
    public final void handleSceneKeyEvent(final KeyEvent event) {
        if (event.isControlDown()) {
            if (event.getCode() == KeyCode.ENTER) {
                // Ctrl + Enter -> update
                updateLayer();
            } else if (event.getCode() == KeyCode.DOWN) {
                // Ctrl + Down Arrow -> Move selected layers down
                moveLayersDown();
            } else if (event.getCode() == KeyCode.UP) {
                // Ctrl + Up Arrow -> Move selected layers up
                moveLayersUp();
            } else {
                switch (event.getText().toLowerCase()) {
                    case "n": // Ctrl+N - create new layer
                        createEmptyLayer();
                        break;
                    case "d": // Ctrl+D - delete layer
                        deleteSelectedLayers();
                        break;
                    case "l": // Ctrl+L - fit all
                        zoomToFitAll();
                        break;
                    case "s": // Ctrl+S - fit selected
                        zoomToFitSelected();
                        break;
                    case "w": // Ctrl+W - fit visible
                        zoomToFitVisible();
                        break;
                    case "a":
                        selectAllLayers();
                        break;
                    default:
                        break;
                }
            }
        } else if (event.isAltDown()) {
            switch (event.getText().toLowerCase()) {
                case "q": // Alt + Q - Query tab
                    tabPane.getSelectionModel().select(queryTab);
                    break;
                case "d": // Alt + D - Databases tab
                    tabPane.getSelectionModel().select(databasesTab);
                    break;
                default:
                    break;
            }
        }
    }



    /**
     * Handler for moving mouse on upperPane which updates coordinates displayed.
     * @param event mouse event
     */
    public final void upperPaneMouseMoved(final MouseEvent event) {
        displayController.updateCoordinatesText(event.getSceneX(), event.getSceneY());
    }

    /**
     * Handler for applying exact zoom factor received from user.
     * Zoom factor is read from zoomText TextField
     * @param event key event
     */
    public final void zoomTextKeyPressed(final KeyEvent event) {
        displayController.zoomTextKeyPressed(event);
    }


    public final void onAnyKeyReleased(final KeyEvent event) {
        heldDownKeys.remove(event.getCode());
    }

    public static boolean isKeyHeldDown(final KeyCode code) {
        return heldDownKeys.contains(code);
    }


}
