package org.hadoop;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;

import java.io.IOException;

/**
 * @author keyki
 */
public class SequenceFileWriter {
    public static void main(String[] args) throws IOException {
        String location = args[0];
        Configuration configuration = new Configuration();

        Path path = new Path(location);
        FileSystem fileSystem = path.getFileSystem(configuration);
        if (fileSystem.exists(path)) {
            fileSystem.delete(path, true);
        }
        fileSystem.create(path);

        IntWritable key = new IntWritable();
        Text value = new Text("alma");
        SequenceFile.Writer writer = SequenceFile.createWriter(configuration,
                SequenceFile.Writer.file(path),
                SequenceFile.Writer.keyClass(IntWritable.class),
                SequenceFile.Writer.valueClass(Text.class));
        for (int i = 0; i < 3001; i++) {
            key.set(i);
            writer.append(key, value);
        }
        writer.close();
    }
}
