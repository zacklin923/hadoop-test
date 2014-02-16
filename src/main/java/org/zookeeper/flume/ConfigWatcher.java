package org.zookeeper.flume;

import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.Properties;

/**
 * @author keyki
 */
public class ConfigWatcher implements Watcher, AsyncCallback.StatCallback, Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigWatcher.class);
    private final ZooKeeper zooKeeper;
    private final String root;
    private final String flumeHome;
    private Process process;

    public ConfigWatcher(String hostPort, String root, String flumeHome) throws IOException {
        this.root = root;
        this.flumeHome = flumeHome;
        this.zooKeeper = new ZooKeeper(hostPort, 3000, this);
    }

    @Override
    public void run() {
        try {
            synchronized (this) {
                while (true) {
                    wait();
                }
            }
        } catch (InterruptedException e) {
            LOGGER.error("Error during run", e);
        }
    }

    @Override
    public void process(WatchedEvent event) {
        if (event.getType() == Event.EventType.None && event.getState() == Event.KeeperState.SyncConnected) {
            LOGGER.info("Connected to zookeeper");
            zooKeeper.exists(root, true, this, null);
        } else {
            String path = event.getPath();
            if (path != null) {
                LOGGER.info("Change happened on {}", path);
                zooKeeper.exists(path, true, this, null);
            }
        }
    }

    @Override
    public void processResult(int rc, String path, Object ctx, Stat stat) {
        LOGGER.info("Stat arrived for node {}", path);
        switch (KeeperException.Code.get(rc)) {
            case OK:
                runProcess(path);
                break;
            case NONODE:
            case SESSIONEXPIRED:
            case NOAUTH:
                break;
            default:
                zooKeeper.exists(path, true, this, null);
        }
    }

    private void runProcess(String path) {
        try {
            byte[] data = zooKeeper.getData(path, false, null);
            if (data != null) {
                killProcess();
                FlumeProperties properties = loadProperties(data);
                replaceSinkPath(properties, path);
                String absolutePath = writePropertiesToFile(properties);
                String exec = "flume-ng agent -n " + getAgentName(properties) + " -f " + absolutePath
                        + " -c " + flumeHome + "/conf";
                LOGGER.info("Running process {}", exec);
                process = Runtime.getRuntime().exec(exec);
            }
        } catch (KeeperException | InterruptedException | IOException e) {
            LOGGER.error("Error occurred during process run.", e);
        }
    }

    private String writePropertiesToFile(FlumeProperties properties) throws IOException {
        String fileName = root.substring(1).replaceAll("/", "-") + ".conf";
        try (FileOutputStream fileOutputStream = new FileOutputStream(fileName)) {
            fileOutputStream.write(properties.toString().getBytes());
        }
        String absolutePath = new File(fileName).getAbsolutePath();
        LOGGER.info("Properties saved to {}", absolutePath);
        return absolutePath;
    }

    private void replaceSinkPath(Properties properties, String path) {
        getSinkKey(properties).ifPresent(key -> {
            if (isHdfsSink(properties, key)) {
                String fsPath = properties.getProperty(key + ".hdfs.path");
                String newPath = fsPath.substring(0, fsPath.lastIndexOf('/')) + path;
                properties.setProperty(key + ".hdfs.path", newPath);
            }
        });
    }

    private boolean isHdfsSink(Properties properties, String sinkKey) {
        return "hdfs".equals(properties.getProperty(sinkKey + ".type"));
    }

    private Optional<String> getSinkKey(Properties properties) {
        return properties.keySet().stream().findAny().map(k -> {
            String agent = getAgentName(properties);
            String sink = properties.getProperty(agent + ".sinks");
            return agent + ".sinks." + sink;
        });
    }

    private String getAgentName(Properties properties) {
        String agent = "";
        Optional<String> optional = properties.keySet().stream().findAny().map(k -> {
            String key = String.valueOf(k);
            return key.substring(0, key.indexOf('.'));
        });
        if (optional.isPresent()) {
            agent = optional.get();
        }
        return agent;
    }

    private FlumeProperties loadProperties(byte[] data) {
        FlumeProperties properties = new FlumeProperties();
        try (ByteArrayInputStream stream = new ByteArrayInputStream(data)) {
            properties.load(stream);
        } catch (IOException e) {
            LOGGER.error("Error parsing the config file", e);
        }
        return properties;
    }

    private void killProcess() {
        if (process != null) {
            LOGGER.info("Killing process..");
            process.destroy();
            try {
                process.waitFor();
                LOGGER.info("Successfully killed the process.");
            } catch (InterruptedException e) {
                LOGGER.error("Error killing the process", e);
            }
        }
    }

    public static void main(String[] args) throws IOException {
        new ConfigWatcher("localhost:2181", "/companyA/flume", "/usr/local/apache-flume-1.5.0-SNAPSHOT-bin").run();
    }

}

