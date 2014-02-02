package org.oozie;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Properties;

/**
 * @author keyki
 */
public class DoubleAction {

    private static final String OOZIE_ACTION_OUTPUT_PROPERTIES = "oozie.action.output.properties";
    private static final String OUTPUT_NUMBER = "outNumber";

    public static void main(String args[]) {
        try {
            File file = new File(System.getProperty(OOZIE_ACTION_OUTPUT_PROPERTIES));
            Properties props = new Properties();
            props.setProperty(OUTPUT_NUMBER, "" + Integer.valueOf(args[0]) * 2);
            OutputStream os = new FileOutputStream(file);
            props.store(os, "");
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
