package org.flume;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;
import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.EventDeliveryException;
import org.apache.flume.PollableSource;
import org.apache.flume.conf.Configurable;
import org.apache.flume.event.EventBuilder;
import org.apache.flume.source.AbstractSource;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author keyki
 */
public class HazelcastQueueSource extends AbstractSource implements Configurable, PollableSource {

    private BlockingQueue<String> distributedQueue;
    private HazelcastInstance hazelcastClient;
    private String queueName;
    private String serverIP;
    private String userName;
    private String userPwd;

    @Override
    public void configure(Context context) {
        queueName = context.getString("queueName");
        serverIP = context.getString("servers");
        userName = context.getString("user");
        userPwd = context.getString("password");
    }

    @Override
    public synchronized void start() {
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.getGroupConfig().setName(userName).setPassword(userPwd);
        clientConfig.addAddress(serverIP);
        hazelcastClient = HazelcastClient.newHazelcastClient(clientConfig);
        distributedQueue = hazelcastClient.getQueue(queueName);
    }

    @Override
    public synchronized void stop() {
        hazelcastClient.shutdown();
    }

    @Override
    public Status process() throws EventDeliveryException {
        Status status = Status.READY;
        try {
            String msg = distributedQueue.poll(1000, TimeUnit.MILLISECONDS);
            if (msg == null) {
                return Status.BACKOFF;
            }
            Event event = EventBuilder.withBody(msg.getBytes());
            getChannelProcessor().processEvent(event);
        } catch (InterruptedException e) {
            status = Status.BACKOFF;
        }
        return status;
    }
}
