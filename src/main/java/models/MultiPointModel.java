package models;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.Point;
import javafx.scene.Group;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.shape.Circle;

import java.util.ArrayList;

/**
 * Created by thea on 22/09/15.
 */
public class MultiPointModel extends GeometryModel {
    public MultiPointModel(final Geometry geometry, final Group group) {
        super(geometry, group);
    }

    public final ArrayList<Circle> drawAndCreateToolTips(final GraphicsContext graphicsContext) {
        ArrayList<Circle> tooltips = new ArrayList<>();
        MultiPoint multiPoint = (MultiPoint) this.geometry;
        for (int i = 0; i < multiPoint.getNumGeometries(); i++) {
            PointModel pointModel = new PointModel(multiPoint.getGeometryN(i), this.group);
            tooltips.addAll(pointModel.drawAndCreateToolTips(graphicsContext));
        }
        return tooltips;
    }
}
