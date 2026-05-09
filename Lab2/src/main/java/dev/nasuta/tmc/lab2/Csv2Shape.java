package dev.nasuta.tmc.lab2;

import java.io.File;

import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.data.simple.SimpleFeatureCollection;

// Check output files here: https://mapshaper.org/
public class Csv2Shape {
    public static void main(String[] args) throws Exception {
        InputMode mode = UiSelectors.askInputMode();
        if (mode == null) {
            System.out.println("No input mode selected");
            return;
        }

        File inputFile = UiSelectors.chooseInputFile(mode);
        if (inputFile == null) {
            System.out.println("No input file selected");
            return;
        }

        SimpleFeatureType featureType = switch (mode) {
            case LOCATIONS -> FeatureSchemas.locationType();
            case BUILDINGS -> FeatureSchemas.buildingType();
            case ROADS -> FeatureSchemas.roadType();
        };

        SimpleFeatureCollection features = switch (mode) {
            case LOCATIONS -> FeatureLoaders.loadLocationFeatures(inputFile, featureType);
            case BUILDINGS -> FeatureLoaders.loadBuildingFeatures(inputFile, featureType);
            case ROADS -> FeatureLoaders.loadRoadFeatures(inputFile, featureType);
        };

        System.out.println("Successfully read " + features.size() + " " + featureType.getTypeName() + " features from "
                + inputFile.getAbsolutePath());

        File exportFile = UiSelectors.chooseOutputShapeFile(inputFile);
        if (exportFile == null) {
            System.out.println("No export file selected");
            return;
        }

        ShapefileExporter.exportFeatures(features, featureType, exportFile);
        System.out.println("Exported features to " + exportFile.getAbsolutePath());
    }
}
