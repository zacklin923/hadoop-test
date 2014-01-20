package org.mahout.exercise.recommendations.wikipedia;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.mahout.cf.taste.hadoop.RecommendedItemsWritable;
import org.apache.mahout.cf.taste.hadoop.TasteHadoopUtils;
import org.apache.mahout.cf.taste.impl.recommender.ByValueRecommendedItemComparator;
import org.apache.mahout.cf.taste.impl.recommender.GenericRecommendedItem;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.math.VarLongWritable;
import org.apache.mahout.math.Vector;
import org.apache.mahout.math.VectorWritable;
import org.apache.mahout.math.map.OpenIntLongHashMap;

import java.io.IOException;
import java.util.*;

/**
 * Download wikipedia related link data from here:
 * http://users.on.net/%7Ehenry/pagerank/links-simple-sorted.zip
 *
 * @author Krisztian_Horvath
 */
public class AggregateAndRecommendReducer
        extends
        Reducer<VarLongWritable, VectorWritable, VarLongWritable, RecommendedItemsWritable> {

    private int recommendationsPerUser = 10;
    private OpenIntLongHashMap indexItemIDMap;
    static final String ITEMID_INDEX_PATH = "itemIDIndexPath";
    static final String NUM_RECOMMENDATIONS = "numRecommendations";
    static final int DEFAULT_NUM_RECOMMENDATIONS = 10;

    protected void setup(Context context) throws IOException {
        Configuration jobConf = context.getConfiguration();
        recommendationsPerUser = jobConf.getInt(NUM_RECOMMENDATIONS,
                DEFAULT_NUM_RECOMMENDATIONS);
        indexItemIDMap = TasteHadoopUtils.readIDIndexMap(
                jobConf.get(ITEMID_INDEX_PATH), jobConf);
    }

    public void reduce(VarLongWritable key, Iterable<VectorWritable> values,
                       Context context) throws IOException, InterruptedException {

        Vector recommendationVector = null;
        for (VectorWritable vectorWritable : values) {
            recommendationVector = recommendationVector == null ? vectorWritable
                    .get() : recommendationVector.plus(vectorWritable.get());
        }

        Queue<RecommendedItem> topItems = new PriorityQueue<RecommendedItem>(
                recommendationsPerUser + 1,
                Collections.reverseOrder(ByValueRecommendedItemComparator
                        .getInstance()));

        Iterator<Vector.Element> recommendationVectorIterator = recommendationVector
                .nonZeroes().iterator();
        while (recommendationVectorIterator.hasNext()) {
            Vector.Element element = recommendationVectorIterator.next();
            int index = element.index();
            float value = (float) element.get();
            if (topItems.size() < recommendationsPerUser) {
                topItems.add(new GenericRecommendedItem(indexItemIDMap
                        .get(index), value));
            } else if (value > topItems.peek().getValue()) {
                topItems.add(new GenericRecommendedItem(indexItemIDMap
                        .get(index), value));
                topItems.poll();
            }
        }

        List<RecommendedItem> recommendations = new ArrayList<RecommendedItem>(
                topItems.size());
        recommendations.addAll(topItems);
        Collections.sort(recommendations,
                ByValueRecommendedItemComparator.getInstance());
        context.write(key, new RecommendedItemsWritable(recommendations));
    }
}