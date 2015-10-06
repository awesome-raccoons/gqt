package models;

import com.sun.javafx.sg.prism.NGShape;
import com.vividsolutions.jts.geom.*;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.opengis.geometry.Boundary;
import org.opengis.geometry.BoundingBox;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import java.util.ArrayList;

/**
 * Created by thea on 22/09/15.
 */
public abstract class GeometryModel {

    private final Geometry geometry;
    private final Geometry originalGeometry;
    private final AnchorPane group;
    public final ModelBoundaries modelBoundaries;

    private static final double TOOLTIP_SIZE = 2.5;

    public GeometryModel(final Geometry geometry, final AnchorPane group) {
        this.geometry = geometry;
        this.group = group;
        this.originalGeometry = (Geometry) geometry.clone();
        this.modelBoundaries = new ModelBoundaries();
        this.modelBoundaries.includeGeometry(this.geometry);
    }

    public final Geometry getGeometry() {
        return this.geometry;
    }

    public final AnchorPane getGroup() {
        return this.group;
    }

    public final Geometry getOriginalGeometry() {
        return this.originalGeometry;
    }

    /**
     * Recalculates new coordinates based on original coordinates, scale and current offset.
     * @param scale multiplies original coordinates to fit current zoom level
     * @param offsetX moves geometry on X axis
     * @param offsetY moves geometry on Y axis
     */
    public final void transformGeometry(double scale, double offsetX, double offsetY) {
        Coordinate[] coord = this.geometry.getCoordinates();
        Coordinate[] coordOrig = this.originalGeometry.getCoordinates();
        for (int j = 0; j < coordOrig.length; j++) {
            coord[j].x = (coordOrig[j].x ) * scale + offsetX;
            coord[j].y = (coordOrig[j].y ) * scale + offsetY;
        }
        this.modelBoundaries.update(this.geometry);
    }

    /**
     * Changes geometry coordinates to move it on X and Y axis.
     * @param offsetX moves geometry on X axis
     * @param offsetY moves geometry on X axis
     */
    public final void moveGeometry(double offsetX, double offsetY) {
        Coordinate[] coord = this.geometry.getCoordinates();
        for (int j = 0; j < coord.length; j++) {
            coord[j].x += offsetX;
            coord[j].y += offsetY;
        }
        this.modelBoundaries.update(this.geometry);
    }

    public static final GeometryModel getModel(final Geometry geometry, final AnchorPane group) {
        if (geometry instanceof Polygon) {
            return new PolygonModel(geometry, group);
        } else if (geometry instanceof Point) {
            return new PointModel(geometry, group);
        } else if (geometry instanceof LineString) {
            return new LineStringModel(geometry, group);
        } else if (geometry instanceof MultiPoint) {
            return new MultiPointModel(geometry, group);
        } else if (geometry instanceof MultiPolygon) {
            return new MultiPolygonModel(geometry, group);
        } else if (geometry instanceof MultiLineString) {
            return new MultiLineStringModel(geometry, group);
        }
        return null;
    }

    public final Circle createToolTip(final double x, final double y, final Paint color) {
        Circle circle = new Circle(x, y, TOOLTIP_SIZE, color);
        Tooltip tooltip = new Tooltip(x + ", " + y);
        Tooltip.install(circle, tooltip);
        //this.group.getChildren().add(circle);
        return circle;
    }


    /**
     * Draws a geometry-object with to the screen.
     * @param graphicsContext The graphics to which the object should be drawn.
     * @return A list of tooltips.
     */
    public abstract ArrayList<Circle> drawAndCreateToolTips(final GraphicsContext graphicsContext);
}

