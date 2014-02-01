package org.hadoop.exercise;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.util.ReflectionUtils;

import java.io.IOException;

/**
 * @author keyki
 */
public class SequenceFileReader {
    public static void main(String[] args) throws IOException {
        String location = args[0];
        Configuration configuration = new Configuration();
        SequenceFile.Reader reader = new SequenceFile.Reader(configuration,
                SequenceFile.Reader.file(new Path(location)));
        IntWritable key = (IntWritable) ReflectionUtils.newInstance(reader.getKeyClass(), configuration);
        Text value = (Text) ReflectionUtils.newInstance(reader.getValueClass(), configuration);
        while (reader.next(key, value)) {
            System.out.println("Key: " + key + " value: " + value);
        }
        reader.close();
    }
}
