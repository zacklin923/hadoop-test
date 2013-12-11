package org.hadoop.exercise;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.map.TokenCounterMapper;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.reduce.IntSumReducer;
import org.springframework.data.hadoop.mapreduce.JobRunner;

/**
 * @author Krisztian_Horvath
 */
public class RemoteJob {

    public static void main(String[] args) throws Exception {

        Configuration conf = new Configuration();
        conf.set("fs.defaultFS", "hdfs://localhost:8020");
        conf.set("mapred.job.tracker", "localhost:8021");
        conf.set("yarn.resourcemanager.address", "localhost:8032");

        Job job = new Job(conf, "word count1");
        job.setJarByClass(RemoteJob.class);
        job.setMapperClass(TokenCounterMapper.class);
        job.setReducerClass(IntSumReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);

        FileInputFormat.addInputPath(job, new Path("wc"));
        FileOutputFormat.setOutputPath(job, new Path("wc-out"));

        JobRunner runner = new JobRunner();
        runner.setJob(job);
        runner.setVerbose(true);
        runner.call();
    }
}
