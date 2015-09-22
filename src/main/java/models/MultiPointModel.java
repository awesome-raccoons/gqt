package models;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPoint;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Circle;

import java.util.ArrayList;

/**
 * Created by thea on 22/09/15.
 */
public class MultiPointModel extends GeometryModel {
    public MultiPointModel(final Geometry geometry, final AnchorPane group) {
        super(geometry, group);
    }

    public final ArrayList<Circle> drawAndCreateToolTips(final GraphicsContext graphicsContext) {
        ArrayList<Circle> tooltips = new ArrayList<>();
        MultiPoint multiPoint = (MultiPoint) this.getGeometry();
        for (int i = 0; i < multiPoint.getNumGeometries(); i++) {
            PointModel pointModel = new PointModel(multiPoint.getGeometryN(i), this.getGroup());
            tooltips.addAll(pointModel.drawAndCreateToolTips(graphicsContext));
        }
        return tooltips;
    }
}
