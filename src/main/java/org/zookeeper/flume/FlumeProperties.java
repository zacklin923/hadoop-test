package org.zookeeper.flume;

import java.util.Properties;

/**
 * @author keyki
 */
public class FlumeProperties extends Properties {

    @Override
    public synchronized String toString() {
        StringBuilder sb = new StringBuilder();
        entrySet().forEach(e -> sb.append(e.getKey()).append("=").append(e.getValue()).append("\n"));
        return sb.toString();
    }
}
