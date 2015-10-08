package models;

import com.vividsolutions.jts.geom.*;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.util.ArrayList;

/**
 * Created by thea on 22/09/15.
 */
public class PolygonModel extends GeometryModel {

    public PolygonModel(final Geometry geometry, final AnchorPane group) {
        super(geometry, group);
    }

    public final ArrayList<Circle> drawAndCreateToolTips(final GraphicsContext graphicsContext) {
        ArrayList<Circle> tooltips = new ArrayList<>();

        Coordinate[] coordinates = this.getGeometry().getCoordinates();
        Coordinate[] origCoordinates = this.getOriginalGeometry().getCoordinates();
        double[] xCoordinates = new double[coordinates.length];
        double[] yCoordinates = new double[coordinates.length];
        for (int i = 0; i < coordinates.length; i++) {
            xCoordinates[i] = coordinates[i].x;
            yCoordinates[i] = coordinates[i].y;
            Circle tooltip = createToolTip(coordinates[i].x, coordinates[i].y,
                    origCoordinates[i].x, origCoordinates[i].y, Color.BLACK);
            tooltips.add(tooltip);
        }


        graphicsContext.fillPolygon(xCoordinates, yCoordinates, coordinates.length);

        drawOutLines(graphicsContext);

        return tooltips;
    }

    private void strokeCoordinateSequence(final CoordinateSequence cs, final GraphicsContext graphicsContext) {
        for (int i = 1; i < cs.size(); i++) {
                graphicsContext.strokeLine(cs.getCoordinate(i - 1).x, cs.getCoordinate(i - 1).y,
                        cs.getCoordinate(i).x, cs.getCoordinate(i).y);
            }
    }

    private void drawOutLines(final GraphicsContext graphicsContext) {
        Polygon polygon = (Polygon) getGeometry();

        graphicsContext.setStroke(Color.BLACK);

        if (polygon.getNumInteriorRing() > 0) {
            int interiorRings = polygon.getNumInteriorRing();

            for (int x = 0; x < interiorRings; x++) {
                LineString hole = polygon.getInteriorRingN(x);

                strokeCoordinateSequence(hole.getCoordinateSequence(), graphicsContext);
            }
        }

        LineString outer = polygon.getExteriorRing();

        strokeCoordinateSequence(outer.getCoordinateSequence(), graphicsContext);

    }

}
