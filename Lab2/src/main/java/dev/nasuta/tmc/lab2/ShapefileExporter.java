package dev.nasuta.tmc.lab2;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.geotools.api.data.SimpleFeatureStore;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.referencing.crs.DefaultGeographicCRS;

public final class ShapefileExporter {
    private ShapefileExporter() {
    }

    public static void exportFeatures(SimpleFeatureCollection features, SimpleFeatureType featureType, File file)
            throws IOException {
        ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();

        Map<String, Serializable> params = new HashMap<>();
        params.put("url", file.toURI().toURL());
        params.put("create spatial index", Boolean.TRUE);

        ShapefileDataStore newDataStore = (ShapefileDataStore) dataStoreFactory.createNewDataStore(params);
        newDataStore.createSchema(featureType);
        newDataStore.forceSchemaCRS(DefaultGeographicCRS.WGS84);

        DefaultTransaction transaction = new DefaultTransaction("create");
        String typeName = newDataStore.getTypeNames()[0];
        var featureSource = newDataStore.getFeatureSource(typeName);

        if (featureSource instanceof SimpleFeatureStore featureStore) {
            featureStore.setTransaction(transaction);
            try {
                featureStore.addFeatures(features);
                transaction.commit();
            } catch (Exception e) {
                System.err.println("Failed to write features to " + file.getAbsolutePath() + ": " + e.getMessage());
                transaction.rollback();
            } finally {
                transaction.close();
            }
        } else {
            System.err.println("Feature source does not support read/write access");
        }
    }
}
