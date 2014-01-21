package org.oozie.exercise;

import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.util.Tool;

/**
 * User: keyki
 * Date: 1/21/14
 * Time: 4:07 PM
 */
public class TestJob extends Configured implements Tool {

    @Override
    public int run(String[] strings) throws Exception {
        System.out.println("Called with args: " + strings);
        return 0;
    }

}
