
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;

import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Johannes on 10.09.2015.
 */

public class Layer extends HBox {

    //TODO style buttons with better representation, generally improve layer visuals
    //TODO Make upper layer act as front layer
    //TODO Make layers in layerview draggable
    //TODO Todo enable selecting color
    //TODO Suggestion: make layer hbox selectable, when selected show tooltips.


    private final GisVisualization gisVis;     //The drawing model
    private final VBox parentContainer;        //Container where layers are put

    private int orderID;                //Defines the drawing order, highest value is drawn last
    private Button buttonUp;            //Button for moving layer up a layer
    private Button buttonDown;          //Button for moving layer down a layer
    private String name;                //Name of the layers
    private String WKTString;           //Original WKT string entered for this layer
    private TextArea textArea;          //The textarea callback used for showing this layer's WKT string
    private LayerSelectedProperty isSelected;

    private static ArrayList<Layer> layers = new ArrayList<>();

    public Layer(final GisVisualization gisVis, final VBox parentContainer, final String name,
                 final String WKTString, final TextArea textArea) {
        this.gisVis = gisVis;
        this.orderID = gisVis.getID();
        this.parentContainer = parentContainer;
        this.name = name;
        this.WKTString = WKTString;
        this.textArea = textArea;
        this.isSelected = new LayerSelectedProperty();
        this.setOnMouseClicked(mouseClickedHandler);
        this.setOnKeyReleased(keyReleasedHandler);
        createLayer();
    }

    public static ArrayList<Layer> getLayers() {
        return layers;
    }

    public final void deleteLayers() {
        this.parentContainer.getChildren().remove(0, this.parentContainer.getChildren().size());
        layers.clear();
    }

    private EventHandler<KeyEvent> keyReleasedHandler = new EventHandler<KeyEvent>() {
        @Override
        public void handle(final KeyEvent event) {
            if (event.getCode() == KeyCode.DOWN) {
                //Move selected layers down
                moveSelectedLayers(1);
            } else if (event.getCode() == KeyCode.UP) {
                //Move selected layers up
                moveSelectedLayers(-1);
            }
        }
    };

    /**
     * Deselects all layers, disables their tooltip and returns background color to normal.
     */
    public final void deselectAllLayers() {
        for (Layer l : layers) {
            l.isSelected.set(false);
            l.gisVis.setDisplayTooltips(false);
            l.toggleBackgroundColor(l.isSelected);
        }
    }

    private EventHandler<MouseEvent> mouseClickedHandler = new EventHandler<MouseEvent>() {
        @Override
        public void handle(final MouseEvent event) {
            //CTRL is pressed select additional, otherwise unselected previously selected
            boolean oldValue = isSelected.get();
            if (!Controller.isKeyHeldDown(KeyCode.CONTROL)) {
                deselectAllLayers();
            }

            //Toggle selection and display tooltips if it is selected
            isSelected.set(!oldValue);
            gisVis.setDisplayTooltips(isSelected.get());
            if (isSelected.get()) {
                showWKTString();
                requestFocus();
            }
            else
            {
                int numberOfSelected = 0;
                Layer selectedLayer = null;
                for (Layer l : layers)
                {
                    if (l.isSelected.get())
                    {
                        selectedLayer = l;
                        numberOfSelected++;
                    }
                }
                if (numberOfSelected == 1)
                {
                     selectedLayer.showWKTString();
                }
            }
            toggleBackgroundColor(isSelected);
        }
    };

    private void showWKTString()
    {
        textArea.clear();
        textArea.setText(WKTString);
    }

    private void toggleBackgroundColor(final BooleanProperty val) {
        backgroundProperty().bind(Bindings.when(val)
                .then(new Background(
                        new BackgroundFill(Color.CORNFLOWERBLUE,
                                CornerRadii.EMPTY, Insets.EMPTY)))
                .otherwise(new Background(
                        new BackgroundFill(Color.TRANSPARENT,
                                CornerRadii.EMPTY, Insets.EMPTY))));
    }

    /**
     * Creates a layer for this Layer object.
     */
    public final void createLayer() {

        CheckBox cb = new CheckBox();
        cb.setOnAction(event -> this.gisVis.toggleVisibility());

        cb.setSelected(true);

        //TODO style textfield with css so it looks like a label when not highlighted
        TextField tf = new TextField(this.name + " " + gisVis.getID());

        buttonUp = new Button("Up");
        buttonUp.setOnAction(event -> {
            moveSelectedLayers(1);
            this.orderID--;
            Layer.layers.get(this.orderID).setOrderID(this.orderID + 1);
            reorderLayers();
        });
        buttonDown = new Button("Down");
        buttonDown.setOnAction(event -> {
            this.orderID++;
            Layer.layers.get(this.orderID).setOrderID(this.orderID - 1);
            reorderLayers();
        });

        //TODO style up and down but

        VBox vb = new VBox();
        //vb.getChildren().add(buttonUp);
        //vb.getChildren().add(buttonDown);

        this.getChildren().add(cb);
        this.getChildren().add(tf);
        this.getChildren().add(vb);
    }

    public final int getOrderID() {
        return this.orderID;
    }

    public final void setOrderID(final int id) {
        this.orderID = id;
    }

    /**
     * Disable or enable the up button.
     *
     * @param b If True, disables button. Enables if false.
     */
    public final void setUpDisable(final boolean b) {
        buttonUp.setDisable(b);
    }

    /**
     * Disable or enable the down button.
     *
     * @param b If True, disables button. Enables if false.
     */
    public final void setDownDisable(final boolean b) {
        buttonDown.setDisable(b);
    }

    /**
     * Move all selected layers the amount of places up or down the list according to the offset.
     * @param offset    The number of places to move selected layers.
     *                  Offset > 0 moves it down the stack.
     *                  Offset < 0 moves it up the stack.
     */
    public final void moveSelectedLayers(final int offset) {
        //Keep track of all the layers that have been checked
        ArrayList<Layer> checkedLayers = new ArrayList<>();

        //Create a list of numbers from 0...n where n is the amount of layers
        //Is there an easier more convenient way to do this?
        ArrayList<Integer> iteratorList = new ArrayList<>();
        for (int i = 0; i < Layer.getLayers().size(); i++) {
            iteratorList.add(i);
        }

        if (offset > 0) {
            Collections.reverse(iteratorList);
        }
        for (int i : iteratorList) {
            Layer currentLayer = Layer.getLayers().get(i);

            if (!checkedLayers.contains(currentLayer)) {
                checkedLayers.add(currentLayer);

                if (currentLayer.isSelected.get()) {
                    Layer l = Layer.getLayers().remove(i);
                    int newPos = i + offset;

                    if (newPos >= Layer.getLayers().size()) {
                        Layer.getLayers().add(l);
                    } else if (newPos < 0) {
                        Layer.getLayers().add(0, l);
                    } else {
                        Layer.getLayers().add(newPos, l);
                    }
                }
            }

        }
        reorderLayers();
    }


    /**
     * Reorders the layers according to their position in the layers list.
     */
    public final void reorderLayers() {
        AnchorPane group = gisVis.getGroup();

        this.parentContainer.getChildren().remove(0, this.parentContainer.getChildren().size());

        group.getChildren().remove(0, group.getChildren().size());

        //Redraw all the layers, only make the toplayer have tooltips
        for (int i = layers.size() - 1; i >= 0; i--) {
            Layer hb = layers.get(i);
            hb.gisVis.reAddCanvas();
            hb.gisVis.setDisplayTooltips(hb.isSelected.get());
            hb.setUpDisable(false);
            hb.setDownDisable(false);
        }

        for (Layer layer : layers) {
            this.parentContainer.getChildren().add(layer);
        }

        layers.get(0).setUpDisable(true);
        layers.get(layers.size() - 1).setDownDisable(true);
    }

}
