package dev.nasuta.tmc.lab2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;

public final class FeatureLoaders {
    private static final String DAT_BLOCK_END = "END";

    private FeatureLoaders() {
    }

    public static SimpleFeatureCollection loadLocationFeatures(File file, SimpleFeatureType featureType)
            throws IOException {
        var locationCollection = new DefaultFeatureCollection();
        var geometryFactory = JTSFactoryFinder.getGeometryFactory();
        var featureBuilder = new SimpleFeatureBuilder(featureType);

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                if (lineNumber++ == 0 || line.trim().isEmpty()) {
                    continue;
                }
                String[] parts = line.split(",");
                if (parts.length < 4) {
                    System.err.println("Invalid line: " + line);
                    continue;
                }

                try {
                    double lat = Double.parseDouble(parts[0].trim());
                    double lon = Double.parseDouble(parts[1].trim());
                    String city = parts[2].trim();
                    int number = Integer.parseInt(parts[3].trim());
                    Point point = geometryFactory.createPoint(new Coordinate(lon, lat));
                    featureBuilder.add(point);
                    featureBuilder.add(city);
                    featureBuilder.add(number);
                    locationCollection.add(buildFeature(featureBuilder));
                } catch (NumberFormatException e) {
                    System.err.println("Invalid line: " + line);
                }
            }
        }

        return locationCollection;
    }

    public static SimpleFeatureCollection loadBuildingFeatures(File file, SimpleFeatureType featureType)
            throws IOException {
        var geometryFactory = JTSFactoryFinder.getGeometryFactory();
        return loadDatFeatures(file, featureType, 4, "Polygon", geometryFactory::createPolygon);
    }

    public static SimpleFeatureCollection loadRoadFeatures(File file, SimpleFeatureType featureType)
            throws IOException {
        var geometryFactory = JTSFactoryFinder.getGeometryFactory();
        return loadDatFeatures(file, featureType, 2, "LineString", geometryFactory::createLineString);
    }

    private static SimpleFeatureCollection loadDatFeatures(
            File file,
            SimpleFeatureType featureType,
            int minCoordinates,
            String geometryName,
            Function<Coordinate[], Geometry> geometryBuilder) throws IOException {
        var collection = new DefaultFeatureCollection();
        var featureBuilder = new SimpleFeatureBuilder(featureType);

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            String address = null;
            List<Coordinate> coordinates = new ArrayList<>();
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }

                if (DAT_BLOCK_END.equalsIgnoreCase(line)) {
                    if (address != null && !coordinates.isEmpty()) {
                        SimpleFeature feature = buildDatFeature(
                                featureBuilder,
                                address,
                                coordinates,
                                minCoordinates,
                                geometryName,
                                geometryBuilder);
                        if (feature != null) {
                            collection.add(feature);
                        }
                    }
                    address = null;
                    coordinates = new ArrayList<>();
                    continue;
                }

                if (address == null) {
                    address = line;
                    continue;
                }

                String[] parts = line.split("\\s+");
                if (parts.length < 2) {
                    System.err.println("Invalid coordinate line: " + line);
                    continue;
                }

                try {
                    double lon = Double.parseDouble(parts[0]);
                    double lat = Double.parseDouble(parts[1]);
                    coordinates.add(new Coordinate(lon, lat));
                } catch (NumberFormatException e) {
                    System.err.println("Invalid coordinate line: " + line);
                }
            }
        }

        return collection;
    }

    private static SimpleFeature buildDatFeature(
            SimpleFeatureBuilder featureBuilder,
            String address,
            List<Coordinate> coordinates,
            int minCoordinates,
            String geometryName,
            Function<Coordinate[], Geometry> geometryBuilder) {
        if (coordinates.size() < minCoordinates) {
            System.err.println(geometryName + " needs at least " + minCoordinates + " coordinates for address: " + address);
            return null;
        }

        featureBuilder.add(geometryBuilder.apply(coordinates.toArray(new Coordinate[0])));
        featureBuilder.add(address);
        return buildFeature(featureBuilder);
    }

    private static SimpleFeature buildFeature(SimpleFeatureBuilder featureBuilder) {
        SimpleFeature feature = featureBuilder.buildFeature(null);
        featureBuilder.reset();
        return feature;
    }
}
