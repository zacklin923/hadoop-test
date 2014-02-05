package org.flume;

import org.apache.flume.EventDeliveryException;
import org.apache.flume.api.RpcClient;
import org.apache.flume.api.RpcClientFactory;
import org.apache.flume.event.EventBuilder;

import java.nio.charset.Charset;
import java.util.stream.IntStream;

/**
 * @author keyki
 */
public class FlumeEventGenerator {

    private RpcClient client;

    public FlumeEventGenerator(String hostname, int port) {
        this.client = RpcClientFactory.getDefaultInstance(hostname, port);
    }

    public boolean sendData(String data) {
        boolean success = false;
        try {
            client.append(EventBuilder.withBody(data, Charset.forName("UTF-8")));
            success = true;
        } catch (EventDeliveryException e) {
            e.printStackTrace();
        }
        return success;
    }

    public static void main(String args[]) {
        FlumeEventGenerator flumeEventGenerator = new FlumeEventGenerator("keyki.hu", 60000);
        IntStream.range(1, 10).forEach(i -> flumeEventGenerator.sendData("Event " + i));
    }

}
