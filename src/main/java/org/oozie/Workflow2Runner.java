package org.oozie;

import org.apache.oozie.client.OozieClient;
import org.apache.oozie.client.OozieClientException;

import java.util.Properties;

import static org.oozie.OozieJobRunnerUtil.createConfiguration;
import static org.oozie.OozieJobRunnerUtil.run;

/**
 * @author keyki
 */
public class Workflow2Runner {
    public static void main(String args[]) throws OozieClientException, InterruptedException {
        Properties conf = createConfiguration();
        conf.setProperty(OozieClient.APP_PATH, "hdfs://localhost:9000/wf");
        conf.setProperty("number", "10");
        conf.setProperty("location", "/oz/seq");
        run(conf);
    }
}
