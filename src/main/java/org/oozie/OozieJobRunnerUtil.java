package org.oozie;

import org.apache.oozie.client.OozieClient;
import org.apache.oozie.client.OozieClientException;

import java.util.Properties;

/**
 * @author keyki
 */
public final class OozieJobRunnerUtil {

    private static final OozieClient oozieClient = new OozieClient("http://localhost:8080/oozie/");

    private OozieJobRunnerUtil() {
        throw new IllegalStateException();
    }

    public static Properties createConfiguration() {
        Properties config = oozieClient.createConfiguration();
        config.setProperty("nameNode", "hdfs://localhost:9000");
        config.setProperty("jobTracker", "localhost:8032");
        config.setProperty("user.name", "root");
        return config;
    }

    public static String run(Properties config) {
        try {
            return oozieClient.run(config);
        } catch (OozieClientException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static void kill(String id) {
        try {
            oozieClient.kill(id);
        } catch (OozieClientException e) {
            e.printStackTrace();
        }
    }

}
