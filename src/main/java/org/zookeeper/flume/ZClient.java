package org.zookeeper.flume;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author keyki
 */
public class ZClient implements Watcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZClient.class);
    private final ZooKeeper zooKeeper;
    private final String zNodeRoot;

    public ZClient(String hostPort, String zNodeRoot) throws IOException {
        this.zNodeRoot = zNodeRoot;
        this.zooKeeper = new ZooKeeper(hostPort, 3000, this);
    }

    @Override
    public void process(WatchedEvent event) {
        if (event.getType() == Event.EventType.None && event.getState() == Event.KeeperState.SyncConnected) {
            LOGGER.info("Connected to zookeeper");
        }
    }

    public void upload(FlumeProperties properties, String path) throws KeeperException, InterruptedException {
        String fullPath = zNodeRoot + path;
        createNodeIfAbsent(zNodeRoot);
        createNodeIfAbsent(fullPath);
        zooKeeper.setData(fullPath, properties.toString().getBytes(), -1);
        LOGGER.info("The data has been uploaded to {}", fullPath);
    }

    private void createNodeIfAbsent(String path) throws KeeperException, InterruptedException {
        Stat node = zooKeeper.exists(path, null);
        if (node == null) {
            String result = zooKeeper.create(path, "".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            LOGGER.info("Node created: {}", result);
        }
    }

    public static void main(String[] args) throws IOException, KeeperException, InterruptedException {
        ZClient zClient = new ZClient("localhost:2181", "/companyA");
        FlumeProperties properties = new FlumeProperties();
        properties.load(zClient.getClass().getResourceAsStream("/websocket.conf"));
        zClient.upload(properties, "/flume");
    }
}
