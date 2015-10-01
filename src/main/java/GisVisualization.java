import com.vividsolutions.jts.geom.Geometry;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import models.GeometryModel;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Johannes on 10.09.2015.
 */

public class GisVisualization {

    private static int idCounter = 0;       //Static counter for IDs
    private AnchorPane group;         //Root node all canvases will be drawn to

    private int id;

    private Canvas canvas;
    private GraphicsContext graphicsContext;
    private GeometryModel geometryModel;
    private ArrayList<Circle> tooltips;

    public GisVisualization(final double canvasWidth,
                            final double canvasHeight,
                            final Geometry geometry,
                            final AnchorPane group) {
        this.id = idCounter;
        incrementCounter();
        this.group = group;
        this.canvas = new Canvas(canvasWidth, canvasHeight);
        this.graphicsContext = canvas.getGraphicsContext2D();
        this.geometryModel = GeometryModel.getModel(geometry, group);
        this.tooltips = new ArrayList<>();
    }

    public final GeometryModel getGeometryModel() {
        return this.geometryModel;
    }

    private static void incrementCounter() {
        GisVisualization.idCounter += 1;
    }

    public final AnchorPane getGroup() {
        return this.group;
    }

    public final GraphicsContext getGraphicsContext() {
        return this.graphicsContext;
    }

    public final void clearGraphicsContext() {
        this.canvas.getGraphicsContext2D().clearRect(0, 0, this.canvas.getWidth(), this.canvas.getHeight());
        this.setDisplayTooltips(false);
        tooltips.clear();
    }

    public final void reDraw() {
        this.clearGraphicsContext();
        ArrayList<Circle> partialTooltips = this.geometryModel.drawAndCreateToolTips(this.graphicsContext);
        tooltips.addAll(partialTooltips);
    }

    /**
     * Creates a polygon from the given points and draw it on the canvas.
     * Also creates tooltips for each point in the polygon.
     *
     * @param canvasWidth  Width of the canvas the GIS-visualization will drawn to.
     * @param canvasHeight Height of the canvas the GIS-visualization will drawn to.
     * @param geometry      The geometry object to visualize.
     * @param group         The group the polygon will be drawn at.
     * @return a GisVisualization object.
     */
    public static GisVisualization createVisualization(final double canvasWidth,
                                                       final double canvasHeight,
                                                       final Geometry geometry,
                                                       final AnchorPane group) {
        GisVisualization gisVis = new GisVisualization(canvasWidth, canvasHeight, geometry, group);
        gisVis.create2DShape(getRandomColor(1.0f));
        group.getChildren().add(gisVis.canvas);

        return gisVis;
    }


    /**
     * Redraws this GisVisualization object and tooltips to its given group.
     */
    public final void reAddCanvas() {
        group.getChildren().add(this.canvas);
    }

    public final void setDisplayTooltips(final boolean display) {
        //group.getChildren().remove(0, group.getChildren().size());
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
     * Creates a polygon using this GisVisualization object's graphicsContext.
     * @param color The color of the polygon
     */
    private void create2DShape(final Color color) {

        this.graphicsContext.setFill(color);
        this.graphicsContext.setStroke(color);

        //This GisVisualization object can contain any geometry
        //There check its instance and draw accordingly

        ArrayList<Circle> partialTooltips =
                geometryModel.drawAndCreateToolTips(this.graphicsContext);

        tooltips.addAll(partialTooltips);
    }

    public final void toggleVisibility() {
        this.canvas.setVisible(!canvas.isVisible());
        for (Circle c : this.tooltips) {
            c.setVisible(canvas.isVisible());
        }
    }

    private static final int RGB_PARAM = 255;

    /**
     * Returns a random rgb color with provided opacity.
     *
     * @param opacity transparent component, in range 0.0f-1.0f.
     * @return the color.
     */
    private static Color getRandomColor(final float opacity) {
        Random r = new Random();
        return Color.rgb(r.nextInt(RGB_PARAM),
                r.nextInt(RGB_PARAM),
                r.nextInt(RGB_PARAM),
                opacity);
    }


    /**
     * Get the ID for this GisVisualization object.
     *
     * @return The ID.
     */
    public final int getID() {
        return this.id;
    }

}
