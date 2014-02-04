package org.hbase;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;

import java.util.Random;

/**
 * User: keyki
 * <p/>
 * hbase shell
 * create 'access_logs', 'details'
 * create 'summary_user', {NAME=>'details', VERSIONS=>1}
 */
public class Generator {
    public static void main(String[] args) throws Exception {
        String[] pages = {"/", "/a.html", "/b.html", "/c.html"};
        Configuration hbaseConfig = HBaseConfiguration.create();
        HTable htable = new HTable(hbaseConfig, "access_logs");
        htable.setAutoFlush(false, false);
        htable.setWriteBufferSize(1024 * 1024 * 12);
        int totalRecords = 100000;
        int maxID = totalRecords / 1000;
        Random rand = new Random();
        System.out.println("importing " + totalRecords + " records ....");
        for (int i = 0; i < totalRecords; i++) {
            int userID = rand.nextInt(maxID) + 1;
            byte[] rowkey = Bytes.add(Bytes.toBytes(userID), Bytes.toBytes(i));
            String randomPage = pages[rand.nextInt(pages.length)];
            Put put = new Put(rowkey);
            put.add(Bytes.toBytes("details"), Bytes.toBytes("page"), Bytes.toBytes(randomPage));
            htable.put(put);
        }
        htable.flushCommits();
        htable.close();
        System.out.println("done");
    }
}
