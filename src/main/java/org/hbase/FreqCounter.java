package org.hbase;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.FirstKeyOnlyFilter;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil;
import org.apache.hadoop.hbase.mapreduce.TableMapper;
import org.apache.hadoop.hbase.mapreduce.TableReducer;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapreduce.Job;

import java.io.IOException;

/**
 * User: keyki
 * <p/>
 * bug: ZeroCopyLiteralByteString cannot access its superclass com.google.protobuf.LiteralByteString
 * fix: HADOOP_CLASSPATH=/path/to/hbase-protocol.jar
 */
public class FreqCounter {

    public static class Mapper extends TableMapper<ImmutableBytesWritable, IntWritable> {

        private int numRecords = 0;
        private static final IntWritable ONE = new IntWritable(1);

        @Override
        public void map(ImmutableBytesWritable row, Result values, Context context) throws IOException {
            // extract userKey from the compositeKey (userId + counter)
            ImmutableBytesWritable userKey = new ImmutableBytesWritable(row.get(), 0, Bytes.SIZEOF_INT);
            try {
                context.write(userKey, ONE);
            } catch (InterruptedException e) {
                throw new IOException(e);
            }
            numRecords++;
            if ((numRecords % 10000) == 0) {
                context.setStatus("mapper processed " + numRecords + " records so far");
            }
        }
    }

    public static class Reducer extends TableReducer<ImmutableBytesWritable, IntWritable, ImmutableBytesWritable> {
        public void reduce(ImmutableBytesWritable key, Iterable<IntWritable> values, Context context)
                throws IOException, InterruptedException {
            int sum = 0;
            for (IntWritable val : values) {
                sum += val.get();
            }
            Put put = new Put(key.get());
            put.add(Bytes.toBytes("details"), Bytes.toBytes("total"), Bytes.toBytes(sum));
            System.out.println(String.format("stats :   key : %d,  count : %d", Bytes.toInt(key.get()), sum));
            context.write(key, put);
        }
    }

    public static void main(String[] args) throws Exception {
        Configuration conf = HBaseConfiguration.create();
        Job job = new Job(conf, "Hbase_FreqCounter");
        job.setJarByClass(FreqCounter.class);
        Scan scan = new Scan();
        scan.addColumn("details".getBytes(), null); // comma separated
        scan.setFilter(new FirstKeyOnlyFilter());
        TableMapReduceUtil.initTableMapperJob("access_logs", scan, Mapper.class, ImmutableBytesWritable.class,
                IntWritable.class, job);
        TableMapReduceUtil.initTableReducerJob("summary_user", Reducer.class, job);
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }

}
