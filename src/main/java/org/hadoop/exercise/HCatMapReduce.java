package org.hadoop.exercise;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hcatalog.data.HCatRecord;
import org.apache.hcatalog.mapreduce.HCatInputFormat;

import java.io.IOException;

/**
 * @author Krisztian_Horvath
 */
public class HCatMapReduce {

    public static class HCatMapper extends Mapper<WritableComparable, HCatRecord, LongWritable, IntWritable> {
        private final static IntWritable ONE = new IntWritable(1);

        @Override
        protected void map(WritableComparable key, HCatRecord value, Context context) throws IOException, InterruptedException {
            context.write(new LongWritable((Long) value.get(4)), ONE);
        }
    }

    public static class HCatReducer extends Reducer<LongWritable, IntWritable, LongWritable, IntWritable> {
        private static final double LIMIT = 12.7;

        @Override
        protected void reduce(LongWritable key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {
            long price = key.get();
            if (price > LIMIT) {
                int sum = 0;
                while (values.iterator().hasNext()) {
                    sum++;
                }
                context.write(new LongWritable(price), new IntWritable(sum));
            }
        }
    }

    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();

        Job job = new Job(conf, "stock price high above 12.7");

        HCatInputFormat.setInput(job, "default", "nyse_stocks");
        job.setInputFormatClass(HCatInputFormat.class);

        job.setJarByClass(HCatMapReduce.class);
        job.setMapperClass(HCatMapper.class);
        job.setReducerClass(HCatReducer.class);

        job.setOutputKeyClass(LongWritable.class);
        job.setOutputValueClass(IntWritable.class);

        FileSystem fileSystem = FileSystem.get(conf);
        Path outputPath = new Path("stocks-out");
        fileSystem.delete(outputPath, true);
        FileOutputFormat.setOutputPath(job, outputPath);

        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}
