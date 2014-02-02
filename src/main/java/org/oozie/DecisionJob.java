package org.oozie;

import org.apache.oozie.client.OozieClient;
import org.apache.oozie.client.OozieClientException;

import java.util.Properties;

/**
 * @author keyki
 */
public class DecisionJob {
    public static void main(String args[]) throws OozieClientException, InterruptedException {
        OozieClient oozieClient = new OozieClient("http://localhost:8080/oozie/");

        Properties conf = oozieClient.createConfiguration();
        conf.setProperty(OozieClient.APP_PATH, "hdfs://localhost:9000/wf");
        conf.setProperty(OozieClient.USER_NAME, "root");
        conf.setProperty("nameNode", "hdfs://localhost:9000");
        conf.setProperty("jobTracker", "localhost:8032");
        conf.setProperty("number", "5");
        conf.setProperty("location", "/oz/seq");

        oozieClient.run(conf);
    }
}
