import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.util.Pair;

import java.util.Optional;

/**
 * Created by Johannes on 15.10.2015.
 */
public class BackgroundGrid extends Canvas {

    private static final float LINE_WIDTH = 0.1f;
    private static final int DIALOG_PADDING = 10;
    private static final Insets DIALOG_INSETS = new Insets(20, 150, 10, 10);

    public static final int DEFAULT_SPACING_X = 10;
    public static final int DEFAULT_SPACING_Y = 10;

    private double width;
    private double height;

    /**
     * How many pixels between each vertical line.
     */
    private int xScale;
    /**
     * How many pixels between each horizontal line-
     */
    private int yScale;
    /**
     * Allow zooming to override our x and y scale value.
     * If true, whenever the user zooms the scale will zoom with it,
     * and disregard the users preferred settings.
     */
    private boolean allowOverriding;

    private ContextMenu cm;
    /**
     * Parent node.
     * We need this to know where to show our popup context menu.
     */
    private AnchorPane parent;

    public BackgroundGrid(final double width,
                          final double height,
                          final AnchorPane parent) {
        super(width, height);
        this.width = width;
        this.height = height;
        this.parent = parent;
        this.allowOverriding = true;

        createContextMenu();
    }


    private void createContextMenu() {
        cm = new ContextMenu();
        MenuItem cmItem1 = new MenuItem("Configure scale");
        cmItem1.setOnAction(event -> scaleInputDialog(false));
        cm.getItems().add(cmItem1);
    }

    /**
     * Displays the context many at the given point.
     * @param x x position of popup.
     * @param y y position of popup.
     */
    public final void showContextMenu(final double x, final double y) {
        cm.show(parent, x, y);
    }

    public final void hideContextMenu() {
        cm.hide();
    }

    /**
     * Draws the scaled grid.
     * Will immediately return without doing anything if either argument is 0.
     * Make sure to call clear grid first, to remove any old grid drawing.
     * @param xSpacing  Amount of pixels between each horizontal line.
     * @param ySpacing  Amount of pixels between each vertical line.
     */
    public final void createGrid(final int xSpacing, final int ySpacing) {
        if (xSpacing == 0 || ySpacing == 0) {
            return;
        }

        GraphicsContext gc = this.getGraphicsContext2D();
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(LINE_WIDTH);

        //Draws horizontal lines
        for (int i = 0; i < height; i += ySpacing) {
            gc.strokeLine(0, i, width, i);
        }

        //Draws vertical lines
        for (int i = 0; i < width; i += xSpacing) {
            gc.strokeLine(i, 0, i, height);
        }
    }

    public final void scaleGrid(final int xSpacing, final int ySpacing) {
        if (allowOverriding) {
            clearGrid();
            createGrid(xSpacing, ySpacing);
        }
    }

    /**
     * Erases the previously drawn scaled grid.
     */
    public final void clearGrid() {
        this.getGraphicsContext2D().clearRect(0, 0, width, height);
    }

    /**
     * Creates a dialog where user can enter x and y scale.
     * Also given the option whether to let user zooming override chosen values
     * @param error Displays an error label if true. If the user entered any zero values.
     */
    public final void scaleInputDialog(final boolean error) {
        Dialog<Pair<Integer, Integer>> dialog = new Dialog<>();
        dialog.setTitle("Adjust grid scaling");

        // Create buttons.
        ButtonType loginButtonType = new ButtonType("Apply", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

        // Create the contents.
        // Hierarchy:
        //      GridPane
        //          GridPane
        //              Label   NumberTextField
        //              Label   NumberTextField
        //          Hbox
        //              Label   Checkbox
        //          Label (optional)
        GridPane pane = new GridPane();
        GridPane grid = new GridPane();
        grid.setHgap(DIALOG_PADDING);
        grid.setVgap(DIALOG_PADDING);
        grid.setPadding(DIALOG_INSETS);

        NumberTextField xScalingTextField = new NumberTextField(xScale);
        xScalingTextField.setPromptText("Pixels between each vertical line");
        NumberTextField yScalingTextField = new NumberTextField(yScale);
        yScalingTextField.setPromptText("Pixels between each horizontal line");
        CheckBox overrideCheckBox = new CheckBox();
        overrideCheckBox.setSelected(allowOverriding);

        grid.add(new Label("X scale:"), 0, 0);
        grid.add(xScalingTextField, 1, 0);
        grid.add(new Label("Y Scale:"), 0, 1);
        grid.add(yScalingTextField, 1, 1);

        HBox hb = new HBox();
        hb.getChildren().add(new Label("Allow zooming to override settings: "));
        hb.getChildren().add(overrideCheckBox);

        pane.add(grid, 0, 0);
        pane.add(hb, 0, 1);

        if (error) {
            Label errorMessage = new Label("* Values must be greater than zero!");
            errorMessage.setTextFill(Color.RED);
            pane.add(errorMessage, 0, 2);
        }

        dialog.getDialogPane().setContent(pane);

        // Request focus on the first input
        Platform.runLater(xScalingTextField::requestFocus);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType) {
                return new Pair<>(xScalingTextField.getValue(), yScalingTextField.getValue());
            }
            return null;
        });

        // Show dialog and wait for results
        Optional<Pair<Integer, Integer>> result = dialog.showAndWait();

        result.ifPresent(XYPair -> {
            if (XYPair.getKey() == 0 || XYPair.getValue() == 0) {
                scaleInputDialog(true);
            } else {
                xScale = XYPair.getKey();
                yScale = XYPair.getValue();
                scaleGrid(xScale, yScale);
                allowOverriding = overrideCheckBox.isSelected();
            }
        });
    }

}
