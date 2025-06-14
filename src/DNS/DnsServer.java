package DNS;
import java.io.IOException;
import  java.net.*;
import  java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;
import java.util.logging.Level;


public class DnsServer {
    private  DatagramSocket udpSocket;
    private  byte[] buf = new byte[512];
    private final ExecutorService pool;
    private  boolean running;
    private  int port ;
    private static final Logger logger = Logger.getLogger(DnsServer.class.getName());
    private final ConfigLoader configLoader;

    public  DnsServer(ConfigLoader config){
        int noOfThread = config.getThreadPoolSize();
        int noOfport = config.getPort();
        this.port = noOfport;
        this.pool = Executors.newFixedThreadPool(noOfThread);
        this.configLoader = config;
    }

    public  void StartServer(){
        try {
            udpSocket = new DatagramSocket(port);
            logger.info("MDNS is started....");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Socket creation failed", e);
        }
        running  = true;
    }

    public void run()  {
//        long startTime = System.currentTimeMillis();
        while(running){
            try{
                DatagramPacket udpPacket = new DatagramPacket(buf, buf.length);
                udpSocket.receive(udpPacket);
                pool.submit(new DnsHandler(udpPacket, udpSocket, configLoader.getDnsRecords(),configLoader.getFilePath()));

            }
            catch (IOException ex){
                ex.printStackTrace();
            }
        }
        udpSocket.close();
    }

}
