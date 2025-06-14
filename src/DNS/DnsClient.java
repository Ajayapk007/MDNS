package DNS;
import  java.net.*;
import java.util.Scanner;

public class DnsClient {
    private  DatagramSocket udpSocket;
    private  InetAddress  address;
    private byte[] buf = new byte[256];


    public  void initClient(){
            try{
            udpSocket = new DatagramSocket();
            address = InetAddress.getByName("localhost");
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
    }
    public void run(){
        try {
            System.out.println("i am hre");
//            String domainName = "www.google.com"; // hardcoded domain Name
            Scanner sc = new Scanner(System.in);
            String domainName = sc.nextLine(); // Dynamic domain Name
            byte[] requestByte = domainName.getBytes();
            DatagramPacket clientPacket = new DatagramPacket(
                    requestByte,
                    requestByte.length,
                    address,
                    5359
            );
               udpSocket.send(clientPacket);
            DatagramPacket responsePacket = new DatagramPacket(buf, buf.length);
            udpSocket.receive(responsePacket);
            String response = new String(responsePacket.getData(), 0, responsePacket.getLength());
            System.out.println(response);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        udpSocket.close();
    }
    public  static  void main(String[] args){
        System.out.println("Client started working");
        try{
            DnsClient dnsClientDomain = new DnsClient();
            dnsClientDomain.initClient();
            dnsClientDomain.run();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
