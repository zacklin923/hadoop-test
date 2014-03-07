package org.hadoop;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.security.UserGroupInformation;
import org.kitesdk.morphline.api.Command;
import org.kitesdk.morphline.api.MorphlineContext;
import org.kitesdk.morphline.api.Record;
import org.kitesdk.morphline.base.Fields;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.security.PrivilegedAction;

public class MapperCleaner {

    private static final String MORPHLINE_FILE = "morphlineFile";
    private static final String MORPHLINE_ID = "morphlineId";

    private static class Cleaner extends Mapper<Object, Text, Text, NullWritable> {
        private static final Logger LOGGER = LoggerFactory.getLogger(Cleaner.class);
        private final Text word = new Text();
        private final Record record = new Record();
        private Command morphline;

        @Override
        protected void setup(Context context) throws IOException, InterruptedException {
            File morphLineFile = new File(context.getConfiguration().get(MORPHLINE_FILE));
            String morphLineId = context.getConfiguration().get(MORPHLINE_ID);
            RecordEmitter recordEmitter = new RecordEmitter(context, word);
            MorphlineContext morphlineContext = new MorphlineContext.Builder().build();
            morphline = new org.kitesdk.morphline.base.Compiler()
                    .compile(morphLineFile, morphLineId, morphlineContext, recordEmitter);
        }

        public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
            record.put(Fields.ATTACHMENT_BODY, new ByteArrayInputStream(value.toString().getBytes()));
            if (!morphline.process(record)) {
                LOGGER.info("Morphline failed to process record: {}", record);
            }
            record.removeAll(Fields.ATTACHMENT_BODY);
        }

    }

    private static final class RecordEmitter implements Command {
        private static final Logger LOGGER = LoggerFactory.getLogger(RecordEmitter.class);
        private final Mapper.Context context;
        private final Text word;

        private RecordEmitter(Mapper.Context context, Text word) {
            this.context = context;
            this.word = word;
        }

        @Override
        public void notify(Record notification) {
        }

        @Override
        public Command getParent() {
            return null;
        }

        @Override
        public boolean process(Record record) {
            word.set(record.toString());
            try {
                context.write(word, null);
            } catch (Exception e) {
                LOGGER.warn("Cannot write record to context", e);
            }
            return true;
        }

    }

    public static void main(String[] args) throws Exception {
        UserGroupInformation.createRemoteUser("keyki").doAs(
                new PrivilegedAction() {
                    @Override
                    public Object run() {
                        try {
                            Configuration conf = new Configuration();
                            conf.set(MORPHLINE_FILE, args[2]);
                            conf.set(MORPHLINE_ID, args[3]);
                            Job job = new Job(conf, "data cleaner");
                            job.setJarByClass(MapperCleaner.class);
                            job.setMapperClass(Cleaner.class);
                            job.setNumReduceTasks(0);
                            job.setOutputKeyClass(Text.class);
                            job.setOutputValueClass(NullWritable.class);
                            FileInputFormat.addInputPath(job, new Path(args[0]));
                            FileOutputFormat.setOutputPath(job, new Path(args[1]));
                            System.exit(job.waitForCompletion(true) ? 0 : 1);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return null;
                    }
                });
    }
}
