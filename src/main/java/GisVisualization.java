import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import models.GeometryModel;

import java.util.ArrayList;

/**
 * Created by Johannes on 10.09.2015.
 */

public class GisVisualization {

    private static int idCounter = 0;       //Static counter for IDs
    private static AnchorPane group;        //Root node all canvases will be drawn to

    private static final int CANVAS_WIDTH = 5000;
    private static final int CANVAS_HEIGHT = 5000;
    private static final float OPACITY_PARAM = 0.7f;
    private static Canvas canvas;
    private static GraphicsContext graphicsContext;

    private int id;
    private Color color;
    private ArrayList<GeometryModel> geometryModelList;
    private ArrayList<Circle> tooltips;
    private ArrayList<Circle> originalTooltips;

    private static ArrayList<Color> colors = new ArrayList<>();

    /**
     * /**
     * Creates a geometry from the given points and draw it on the canvas.
     * Also creates tooltips for each point in the geometry.
     *
     * @param geometry      The geometry object to visualize.
     * @param group         The group the polygon will be drawn to.
     */
    public GisVisualization(final Geometry geometry,
                            final AnchorPane group) {
        this.id = idCounter;
        this.geometryModelList = new ArrayList<>();
        incrementCounter();
        createCanvas(group);
        this.geometryModelList.add(GeometryModel.getModel(geometry, group));
        this.tooltips = new ArrayList<>();
        initColors();
        this.color = getColor(this.id);
    }

    public GisVisualization(final AnchorPane group) {
        this.id = idCounter;
        this.geometryModelList = new ArrayList<>();
        incrementCounter();
        createCanvas(group);
        this.tooltips = new ArrayList<>();
        initColors();
        this.color = getColor(this.id);
    }

    private static void initColors() {
        colors.add(Color.RED);
        colors.add(Color.ORANGE);
        colors.add(Color.YELLOW);
        colors.add(Color.GREEN);
        colors.add(Color.BLUE);
        colors.add(Color.INDIGO);
        colors.add(Color.VIOLET);
    }

    public final ArrayList<GeometryModel> getGeometryModelList() {
        return this.geometryModelList;
    }

    public final void clearGeometryModelList() {
        geometryModelList.clear();
    }

    /**
     * Creates a canvas, graphicsContext and fixes all setup required for drawing geometries.
     * This will only happen the first time a layer is setup,
     * and will be ignored on all subsequent layer creations.
     * @param group The parent container in the drawing window.
     */
    private static void createCanvas(final AnchorPane group) {
        if (canvas == null) {
            canvas = new Canvas(CANVAS_WIDTH, CANVAS_HEIGHT);
            graphicsContext = canvas.getGraphicsContext2D();
            group.getChildren().add(canvas);
            GisVisualization.group = group;
        }
    }

    public final void setDisplayTooltips(final boolean display) {
        if (display) {
            for (Circle c : tooltips) {
                group.getChildren().add(c);
            }
        } else {
            for (Circle c : tooltips) {
                group.getChildren().remove(c);
            }
        }
    }

    /**
     * Reset the canvas.
     * Removes all elements in the plot view. Including tooltips and canvas.
     * Then adds the canvas again, and clears its contents.
     */
    public static void reset() {
        if (group != null) {
            group.getChildren().clear();
            group.getChildren().add(GisVisualization.getCanvas());
            graphicsContext.clearRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
        }
    }

    /**
     * Creates a geometry using the geometryModel class to delegate the drawing according to type.
     * Also creates tooltips and copies them in an original tooltip list
     */
    public final void create2DShapeAndTooltips() {
        tooltips.clear();
        graphicsContext.setFill(this.color);
        graphicsContext.setStroke(this.color);

        for (GeometryModel gm : geometryModelList) {
            tooltips.addAll(gm.drawAndCreateToolTips(graphicsContext));
        }
        originalTooltips = cloneList(tooltips);
    }

    /**
     * creates a geometry, but does not handle anything about its tooltips.
     */
    public final void redraw2DShape() {
        graphicsContext.setFill(this.color);
        graphicsContext.setStroke(this.color);

        for (GeometryModel gm : geometryModelList) {
            gm.drawAndCreateToolTips(graphicsContext);
        }
    }

    /**
     * Moves the tooltips according to current coordinates!
     * Tooltips are always equivalent to coordinates
     * Moved tooltips are added to the scene
     */
    public final void moveTooltips() {
        Coordinate[] coord;
        for (GeometryModel gm : geometryModelList) {
            coord = gm.getGeometry().getCoordinates();
            for (int i = 0; i < coord.length; i++) {
                tooltips.get(i).setCenterX(coord[i].x);
                tooltips.get(i).setCenterY(coord[i].y);
            }
        }
    }

    /**
     * Returns the next layer color with provided opacity.
     *
     * @return the color.
     */
    private static Color getColor(final int id) {
        String colorString = colors.get(id % colors.size()).toString();
        return Color.web(colorString, OPACITY_PARAM);
}

    /**
     * Get the ID for this GisVisualization object.
     *
     * @return The ID.
     */
    public final int getID() {
        return this.id;
    }

    /**
     * Get the canvas used to draw all geometries.
     * This is the same for all layers.
     * @return  The canvas.
     */
    public static Canvas getCanvas() {
        return canvas;
    }

    public static AnchorPane getGroup() {
        return group;
    }

    public final ArrayList<Circle> getTooltips() {
        return this.tooltips;
    }

    private static void incrementCounter() {
        GisVisualization.idCounter += 1;
    }

    public static ArrayList<Circle> cloneList(final ArrayList<Circle> circles) {
       ArrayList<Circle> clonedList = new ArrayList<Circle>(circles.size());
        for (Circle c : circles) {
            clonedList.add(new Circle(c.getCenterX(), c.getCenterY(), c.getRadius(), c.getFill()));
        }
        return clonedList;
    }

    public final void addGeometry(final Geometry geometry) {
        geometryModelList.add(GeometryModel.getModel(geometry, group));
    }
}
