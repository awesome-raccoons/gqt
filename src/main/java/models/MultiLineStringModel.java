package models;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiLineString;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Circle;

import java.util.ArrayList;

/**
 * Created by thea on 22/09/15.
 */
public class MultiLineStringModel extends GeometryModel {

    public MultiLineStringModel(Geometry geometry, AnchorPane group) {
        super(geometry, group);
    }

    public final ArrayList<Circle> drawAndCreateToolTips(GraphicsContext graphicsContext) {
        ArrayList<Circle> tooltips = new ArrayList<>();
        MultiLineString multiLineString = (MultiLineString) this.geometry;
        for (int i = 0; i < multiLineString.getNumGeometries(); i++) {
            LineStringModel lineStringModel =
                    new LineStringModel(multiLineString.getGeometryN(i), this.group);
            tooltips.addAll(lineStringModel.drawAndCreateToolTips(graphicsContext));
        }
        return tooltips;
    }
}
