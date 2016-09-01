package org.maptalks.benchmark.geo.feature;

import org.junit.Before;
import org.junit.Test;
import org.maptalks.geojson.Feature;
import org.maptalks.geojson.FeatureCollection;

import java.util.ArrayList;
import java.util.List;

public class EncoderTest {

    private static FeatureCollection collection;

    @Before
    public void setup() throws Exception {
        List<Feature> features = new ArrayList<Feature>();
        for (int i = 0; i < 1000000; i++) {
            Feature feature = new Feature();
            features.add(feature);
        }
        collection = new FeatureCollection((Feature[]) features.toArray());
    }

    @Test
    public void encodeToSimple() throws Exception {
        Encoder.encodeToSimple(collection);
    }

    @Test
    public void encodeToGeoBuf() throws Exception {
        Encoder.encodeToGeoBuf(collection);
    }
}
