package org.oozie.exercise;

import org.apache.oozie.client.OozieClient;
import org.apache.oozie.client.OozieClientException;
import org.apache.oozie.client.WorkflowJob;

import java.util.Properties;

/**
 * User: keyki
 * Date: 1/20/14
 * Time: 2:44 PM
 */
public class OozieJob {

    public static void main(String args[]) throws OozieClientException, InterruptedException {
        OozieClient oozieClient = new OozieClient("http://localhost:8080/oozie/");
        Properties conf = oozieClient.createConfiguration();
        conf.setProperty(OozieClient.APP_PATH, "hdfs://localhost:9000/workflow.xml");
        conf.setProperty("nameNode", "hdfs://localhost:9000");
        conf.setProperty("inputDir", "/oozie/inputdir");
        conf.setProperty("outputDir", "/oozie/outputdir");
        String jobId = oozieClient.run(conf);
        System.out.println("Work flow submitted");
        while (oozieClient.getJobInfo(jobId).getStatus() == WorkflowJob.Status.RUNNING) {
            System.out.println("Workflow job running...");
            Thread.sleep(10 * 1000);
        }
        System.out.println("Workflow job completed");
        System.out.println(oozieClient.getJobInfo(jobId));
    }
}
