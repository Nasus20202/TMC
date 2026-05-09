package dev.nasuta.tmc.lab2;

import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.data.DataUtilities;
import org.geotools.feature.SchemaException;

public final class FeatureSchemas {
    private static final String LOCATION_TYPE_NAME = "Location";
    private static final String LOCATION_TYPE_DEFINITION = "the_geom:Point:srid=4326,name:String,number:Integer";
    private static final String BUILDING_TYPE_NAME = "Building";
    private static final String BUILDING_TYPE_DEFINITION = "the_geom:Polygon:srid=4326,address:String";
    private static final String ROAD_TYPE_NAME = "Road";
    private static final String ROAD_TYPE_DEFINITION = "the_geom:LineString:srid=4326,address:String";

    private FeatureSchemas() {
    }

    public static SimpleFeatureType locationType() throws SchemaException {
        return DataUtilities.createType(LOCATION_TYPE_NAME, LOCATION_TYPE_DEFINITION);
    }

    public static SimpleFeatureType buildingType() throws SchemaException {
        return DataUtilities.createType(BUILDING_TYPE_NAME, BUILDING_TYPE_DEFINITION);
    }

    public static SimpleFeatureType roadType() throws SchemaException {
        return DataUtilities.createType(ROAD_TYPE_NAME, ROAD_TYPE_DEFINITION);
    }
}
