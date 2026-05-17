package dev.nasuta.tmc.lab3;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.map.FeatureLayer;
import org.geotools.map.MapContent;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.swing.JMapFrame;
import org.geotools.styling.SLD;
import org.locationtech.jts.geom.*;
import org.opengis.feature.simple.SimpleFeatureType;

import java.net.URL;

public class OSMMapApp {
    private SimpleFeatureCollection points;
    private SimpleFeatureCollection ways;
    private SimpleFeatureCollection polygons;
    
    private SimpleFeatureBuilder pbPoint;
    private SimpleFeatureBuilder pbWay;
    private SimpleFeatureBuilder pbPolygon;
    
    private GeometryFactory gf = new GeometryFactory();
    private SimpleFeatureType typePoint;
    private SimpleFeatureType typeWay;
    private SimpleFeatureType typePolygon;
    
    private int id = 0;
    
    public OSMMapApp() {
        points = new DefaultFeatureCollection();
        ways = new DefaultFeatureCollection();
        polygons = new DefaultFeatureCollection();
        
        typePoint = createType("Location", Point.class);
        typeWay = createType("Lamana", LineString.class);
        typePolygon = createType("Budynek", Polygon.class);
        
        pbPoint = new SimpleFeatureBuilder(typePoint);
        pbWay = new SimpleFeatureBuilder(typeWay);
        pbPolygon = new SimpleFeatureBuilder(typePolygon);
    }
    
    private SimpleFeatureType createType(String name, Class<?> geom) {
        SimpleFeatureTypeBuilder b = new SimpleFeatureTypeBuilder();
        b.setName(name);
        b.setCRS(DefaultGeographicCRS.WGS84);
        b.add("geom", geom);
        return b.buildFeatureType();
    }
    
    public void fetch(String left, String bottom, String right, String top) throws Exception {
        String url = "https://api.openstreetmap.org/api/0.6/map?bbox=" + left + "," + bottom + "," + right + "," + top;
        URL u = new URL(url);
        OSMParser parser = new OSMParser();
        parser.parse(u.openStream());
        
        for (NodeData n : parser.getNodes()) {
            Point p = gf.createPoint(new Coordinate(n.lon(), n.lat()));
            pbPoint.add(p);
            ((DefaultFeatureCollection) points).add(pbPoint.buildFeature(String.valueOf(id++)));
            pbPoint.reset();
        }
        
        for (WayData w : parser.getWays()) {
            processWay(w, parser.getNodesMap());
        }
    }
    
    private void processWay(WayData w, java.util.Map<Long, NodeData> nodes) {
        boolean isBuilding = "yes".equals(w.tags().get("building"));
        
        Coordinate[] coords = new Coordinate[w.nodeRefs().size()];
        for (int i = 0; i < w.nodeRefs().size(); i++) {
            NodeData n = nodes.get(w.nodeRefs().get(i));
            if (n != null) coords[i] = new Coordinate(n.lon(), n.lat());
        }
        
        if (isBuilding && coords.length >= 4) {
            coords = closeRing(coords);
            try {
                LinearRing ring = gf.createLinearRing(coords);
                Polygon poly = gf.createPolygon(ring, null);
                pbPolygon.add(poly);
                ((DefaultFeatureCollection) polygons).add(pbPolygon.buildFeature(String.valueOf(id++)));
                pbPolygon.reset();
            } catch (Exception ignored) {}
        } else if (coords.length >= 2) {
            try {
                LineString ls = gf.createLineString(coords);
                pbWay.add(ls);
                ((DefaultFeatureCollection) ways).add(pbWay.buildFeature(String.valueOf(id++)));
                pbWay.reset();
            } catch (Exception ignored) {}
        }
    }
    
    private Coordinate[] closeRing(Coordinate[] c) {
        if (!c[0].equals2D(c[c.length - 1])) {
            return java.util.Arrays.copyOf(c, c.length + 1);
        }
        return c;
    }
    
    public void show() {
        MapContent map = new MapContent();
        map.setTitle("OSM Map");
        
        if (points.size() > 0) map.addLayer(new FeatureLayer(points, SLD.createPointStyle("Circle", java.awt.Color.RED, java.awt.Color.YELLOW, 0.8f, 3)));
        if (ways.size() > 0) map.addLayer(new FeatureLayer(ways, SLD.createLineStyle(java.awt.Color.BLUE, 2)));
        if (polygons.size() > 0) map.addLayer(new FeatureLayer(polygons, SLD.createPolygonStyle(java.awt.Color.BLACK, java.awt.Color.GREEN, 0.5f)));
        
        JMapFrame.showMap(map);
    }
    
    public static void main(String[] args) throws Exception {
        OSMMapApp app = new OSMMapApp();
        app.fetch("18.635", "54.345", "18.65", "54.36");
        app.show();
    }
}