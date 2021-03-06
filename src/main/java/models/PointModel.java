package models;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.util.ArrayList;

/**
 * Created by thea on 22/09/15.
 */
public class PointModel extends GeometryModel {

    private static final double POINT_SIZE = 5;
    private static final double ADJUSTMENT_FACTOR = 0.5;

    public PointModel(final Geometry geometry, final AnchorPane group) {
        super(geometry, group);
    }

    public final ArrayList<Circle> drawAndCreateToolTips(final GraphicsContext graphicsContext) {
        ArrayList<Circle> tooltips = new ArrayList<>();
        Point point = (Point) this.getGeometry();
        Coordinate[] origCoordinates = this.getOriginalGeometry().getCoordinates();
        graphicsContext.fillOval(
                point.getX() - POINT_SIZE * ADJUSTMENT_FACTOR,
                point.getY() - POINT_SIZE * ADJUSTMENT_FACTOR,
                POINT_SIZE, POINT_SIZE);
        Circle tooltip = createToolTip(point.getX(), point.getY(),
                origCoordinates[0].x, origCoordinates[0].y, Color.TRANSPARENT);
        tooltips.add(tooltip);
        return tooltips;
    }
}
