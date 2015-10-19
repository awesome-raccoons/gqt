import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import models.GeometryModel;
import models.ModelBoundaries;

import java.util.ArrayList;
import java.util.List;


public class Controller {

    @FXML
    private TextArea queryInput;
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
    private Button zoomToFitVisibleButton;
    @FXML
    private Button zoomToFitSelectedButton;
    @FXML
    private Button zoomToFitButton;


    /**
     * Stored all GisVisualizations.
     */
    private static List<KeyCode> heldDownKeys = new ArrayList<>();

    private DisplayController displayController;

    public final DisplayController createDisplayController(BackgroundGrid backgroundGrid, Stage stage) {
        this.displayController = new DisplayController(upperPane, zoomText,
                backgroundGrid, positionX,  positionY, zoomTextError, stage);
        return this.displayController;
    }


    public final void init() {
        zoomToFitVisibleButton.setDisable(true);
        zoomToFitSelectedButton.setDisable(true);
        zoomToFitButton.setDisable(true);
    }
    
    public final AnchorPane getUpperPane() {
        return upperPane;
    }
    @FXML
    public final void updateLayer() {
        WktParser wktParser = new WktParser(Layer.getSelectedLayer(), upperPane);
        boolean result = wktParser.parseWktString(queryInput.getText());
        if (result) {
            wktParser.updateLayerGeometries();
            displayController.rescaleAllGeometries();
        }
    }

    public final Button getZoomToFitSelectedButton() {
        return zoomToFitSelectedButton;
    }

    public final Button getSubmit() {
        return submit;
    }

    public final Button getZoomToFitButton() {
        return zoomToFitButton;
    }

    public final Button getZoomToFitVisibleButton() {
        return zoomToFitVisibleButton;
    }

    public final void createEmptyLayer() {
        Layer l = new Layer(null, vboxLayers, "Empty", "", queryInput, this);
        Layer.getLayers(false).add(l);
        l.addLayerToView();
        //To ensure the latest new layer will be selected.
        l.handleLayerMousePress();
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

    /**
     * Handler for shortcuts used when focus is on text area.
     * @param event key event
     */
    public final void wktAreaKeyPressed(final KeyEvent event) {
        if (event.isAltDown() && event.getCode() == KeyCode.ENTER) {
            updateLayer();
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

    /**
     * Handler to allow keyboard shortcuts regardless of current focus.
     * @param event
     */
    public final void handleSceneKeyEvent(final KeyEvent event) {
        if (event.isControlDown()) {
            if (event.getCode() == KeyCode.ENTER) {
                updateLayer();
            } else {
                switch (event.getText().toLowerCase()) {
                    case "n": // Ctrl+N - create new layer
                        createEmptyLayer();
                        break;
                    case "d": // Ctrl+D - delete layer
                        Layer.getAllSelectedLayers(false).forEach(Layer::deleteLayer);
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
