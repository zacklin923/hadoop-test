package org.oozie;

import org.apache.oozie.client.OozieClient;
import org.apache.oozie.client.OozieClientException;

import java.util.Properties;

import static org.oozie.OozieJobRunnerUtil.createConfiguration;
import static org.oozie.OozieJobRunnerUtil.run;

/**
 * @author keyki
 */
public class Workflow2PreRunner {
    public static void main(String args[]) throws OozieClientException, InterruptedException {
        Properties conf = createConfiguration();
        conf.setProperty(OozieClient.APP_PATH, "hdfs://localhost:9000/wfpre");
        conf.setProperty("number", "5");
        conf.setProperty("location", "/oz/seq");
        conf.setProperty("secondPath", "hdfs://localhost:9000/wf");
        run(conf);
    }
}
