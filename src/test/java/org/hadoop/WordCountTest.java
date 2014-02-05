package org.hadoop;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mrunit.mapreduce.MapDriver;
import org.junit.Test;

/**
 * @author keyki
 */
public class WordCountTest {

    @Test
    public void testMapper() throws Exception {
        new MapDriver<Object, Text, Text, IntWritable>()
                .withMapper(new WordCount.WordCountMapper())
                .withInput(new LongWritable(1), new Text("cat cat dog"))
                .withOutput(new Text("cat"), new IntWritable(1))
                .withOutput(new Text("cat"), new IntWritable(1))
                .withOutput(new Text("dog"), new IntWritable(1))
                .runTest();
    }

}
