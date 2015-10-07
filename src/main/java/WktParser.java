import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.io.WKTReader;
import javafx.scene.control.Alert;
import javafx.scene.layout.AnchorPane;
import org.geotools.geometry.jts.JTSFactoryFinder;

import java.util.ArrayList;

/**
 * Created by Johannes on 07.10.2015.
 */
public class WktParser {

    private static WKTReader wktReader;
    private Layer layer;
    private String inputString;
    private String upperMostGeometryType;
    private ArrayList<Geometry> geometries;
    private AnchorPane group;

    public WktParser(final Layer layer, final AnchorPane group) {
        this.layer = layer;
        this.geometries = new ArrayList<>();
        this.group = group;
        initializeWktReader();
    }

    private void initializeWktReader() {
        if (wktReader == null) {
            //Create a WKT parser for reading WKT input
            GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
            wktReader = new WKTReader(geometryFactory);
        }
    }

    public final void printAllFoundGeometries() {
        System.out.println("Total size: " + geometries.size());
        for (Geometry g : geometries) {
            System.out.println("is valid: " + g.isValid());
            System.out.println(g.toString());
        }

    }


    /**
     * Creates and saves geometry. Calls methods to create new layer and visualization.
     * @param poly Well Known Text from user input
     */
    public final boolean parseWktString(final String poly) {
        if (poly.equals("")) {
            showWKTParseErrorMessage();
            return false;
        } else {
            try {
                Geometry geom = wktReader.read(poly);
                this.inputString = poly;
                this.upperMostGeometryType = geom.getGeometryType();
                extractAllGeometries(geom);
                //rescaleAllGeometries();
            } catch (com.vividsolutions.jts.io.ParseException e) {
                showWKTParseErrorMessage();
                return false;
            }

            return true;
        }
    }

    public final void extractAllGeometries(final Geometry geom) {
        if (geom instanceof GeometryCollection) {
            for (int i = 0; i < geom.getNumGeometries(); i++) {
                refineGeometryClass(geom.getGeometryN(i));
            }
        } else {
            refineGeometryClass(geom);
        }
    }

    /**
     * Delegates the task of creating the layer for this geometry. Whether it is a plain WKT object,
     * or a composite such as a MultiPolygon.
     * @param geometry geometry to consider.
     */
    private void refineGeometryClass(final Geometry geometry) {
        if (geometry instanceof GeometryCollection) {
            extractAllGeometries(geometry);
        } else {
            createLayersFromMultiples(geometry);
            //this.geometries.add(geometry);
        }
    }

    /**
     * Assumes the given geometry is of a multiple type, and creates a layer for each.
     * @param geometry geometry to consider.
     */
    private void createLayersFromMultiples(final Geometry geometry) {
        for (int i = 0; i < geometry.getNumGeometries(); i++) {
            this.geometries.add(geometry.getGeometryN(i));
        }
    }

    /**
     * Creates a layer for the given geometry.
     * @param geometry geometry to draw to a layer.
     */
    public final void updateLayerGeometries() {
        if (layer != null) {
            if (layer.getGisVis() == null) {
                    layer.setGisVis(new GisVisualization(group));
            } else {
                layer.getGisVis().clearGeometryModelList();
            }
            for (Geometry geometry : geometries) {
                layer.getGisVis().addGeometry(geometry);
            }
            layer.setWKTString(inputString);
            layer.setName(upperMostGeometryType);

            layer.reorderLayers();
        }
    }


    /**
     * Displays an alert dialog when trying to draw an invalid WKT string.
     */
    public final void showWKTParseErrorMessage() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error parsing WKT");
        alert.setHeaderText("Invalid WKT");
        String s = "The WKT string entered is of unknown geometry type ";
        alert.setContentText(s);
        alert.show();
    }



}