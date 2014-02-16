package org.zookeeper;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * User: keyki
 *
 * example params: localhost:2181 /testNode alma.txt ipconfig
 */
public class Executor implements Watcher, Runnable, DataMonitor.DataMonitorListener {

    private DataMonitor dataMonitor;
    private ZooKeeper zooKeeper;
    private String fileName;
    private String exec[];
    private Process child;

    public Executor(String hostPort, String zNode, String fileName, String exec[]) throws KeeperException, IOException {
        this.fileName = fileName;
        this.exec = exec;
        this.zooKeeper = new ZooKeeper(hostPort, 3000, this);
        this.dataMonitor = new DataMonitor(zooKeeper, zNode, this);
    }

    @Override
    public void process(WatchedEvent watchedEvent) {
        dataMonitor.process(watchedEvent);
    }

    @Override
    public void run() {
        try {
            synchronized (this) {
                while (!dataMonitor.isDead()) {
                    wait();
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void closing(int rc) {
        synchronized (this) {
            notifyAll();
        }
    }

    public static class StreamWriter extends Thread {
        private OutputStream outputStream;
        private InputStream inputStream;

        public StreamWriter(InputStream inputStream, OutputStream outputStream) {
            this.inputStream = inputStream;
            this.outputStream = outputStream;
            start();
        }

        public void run() {
            byte b[] = new byte[80];
            int rc;
            try {
                while ((rc = inputStream.read(b)) > 0) {
                    outputStream.write(b, 0, rc);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void exists(byte[] data) {
        if (data == null) {
            if (child != null) {
                killChild();
            }
            child = null;
        } else {
            if (child != null) {
                killChild();
            }
            try {
                FileOutputStream fos = new FileOutputStream(fileName);
                fos.write(data);
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                System.out.println("Starting child");
                child = Runtime.getRuntime().exec(exec);
                new StreamWriter(child.getInputStream(), System.out);
                new StreamWriter(child.getErrorStream(), System.out);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void killChild() {
        System.out.println("Stopping child");
        child.destroy();
        try {
            child.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        if (args.length < 4) {
            System.err.println("USAGE: Executor host:Port zNode fileName program [args ...]");
            System.exit(2);
        }
        String hostPort = args[0];
        String zNode = args[1];
        String fileName = args[2];
        String exec[] = new String[args.length - 3];
        System.arraycopy(args, 3, exec, 0, exec.length);
        try {
            new Executor(hostPort, zNode, fileName, exec).run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
