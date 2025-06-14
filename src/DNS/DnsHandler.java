package DNS;
import java.sql.SQLOutput;
import  java.util.*;
import java.net.*;
import java.io.*;
import java.util.logging.Logger;
import java.util.logging.Level;

public class DnsHandler implements Runnable{
  private DatagramPacket udpPacket;
  private DatagramSocket udpSocket;
  private Map<String, String> mapName;
  private String path ;
  private  static  final Logger logger = Logger.getLogger(DnsHandler.class.getName());

    public DnsHandler(DatagramPacket packet, DatagramSocket socket, Map<String, String> records, String path) {
        this.udpPacket = packet;
        this.udpSocket = socket;
        this.mapName = records;
        this.path = path;
    }

    public void   run(){
      try {
          // 1. Extract the query from udpPacket
          byte[] queryData = Arrays.copyOf(udpPacket.getData(), udpPacket.getLength());
          // 2. Parse the domain name from the query
          logger.info(  "Received raw data..");
          String receivedDomain = extractDomainName(queryData);
          logger.info("parsed Domain Name: " + receivedDomain);
          // 3. Use the mapName to look up the IP address
          // 4. Build a DNS response
          byte[] responseBytes = BuildResponse(queryData, receivedDomain);
          // 5. Send the response using udpSocket
          DatagramPacket responsePacket = new DatagramPacket(
                  responseBytes,
                  responseBytes.length,
                  udpPacket.getAddress(),
                  udpPacket.getPort()
          );
          udpSocket.send(responsePacket);
          logger.info("Response sent" );
//          logger.info(new Date() + " Response sent" );
      } catch (Exception  e) {
//          throw new RuntimeException(e);
          logger.log(Level.SEVERE, "Error processing DNS request", e);
      }

     }

 public  String extractDomainName(byte[] data){
      int position = 12;
      StringBuilder domainName = new StringBuilder();
      while(true){
          int labelLength = data[position] & 0xFF;
          if(labelLength == 0) break;
          position++;  // move past length byte
          for (int i = 0; i < labelLength; i++) {
              domainName.append((char)data[position + i]);
          }
          position += labelLength;

          domainName.append(".");
      }
      return domainName.substring(0,domainName.length() - 1);
 }

 public  byte[] BuildResponse( byte[] data, String domainName){
      String ip = mapName.get(domainName);
      byte[] response = new byte[512];
      System.arraycopy(data, 0,response,0,12);

     // Flags
     response[2] = (byte) 0x81;  // QR=1, RD=1
     response[3] = (byte) 0x80;  // RA=1

     // QDCOUNT = 1
     response[4] = 0x00;
     response[5] = 0x01;

     // ANCOUNT = 1 if IP found, else 0
     response[6] = 0x00;
     response[7] = (ip != null) ? (byte) 0x01 : 0x00;

     // NSCOUNT & ARCOUNT = 0
     response[8] = response[9] = 0x00;
     response[10] = response[11] = 0x00;

     // Copy Question Section
     int questionLen = getQuesLen(data);
     System.arraycopy(data,12,response,12,questionLen);
     if (ip == null) {
         try {
             InetAddress address = InetAddress.getByName(domainName);
             ip = address.getHostAddress();
             mapName.put(domainName, ip);
             response[6] = 0x00;
             response[7] = 0x01;
             try (FileWriter fw = new FileWriter(path, true);
                  BufferedWriter bw = new BufferedWriter(fw)) {
                 bw.write(domainName + "=" + ip);
                 bw.newLine();
             } catch (IOException ioEx) {
                 logger.warning("Failed to write to config file: " + ioEx.getMessage());
             }
         } catch (Exception e) {
             logger.warning("No IP found for domain name: " + domainName + " - " + e.getMessage());
             return Arrays.copyOf(response, 12 + questionLen);
         }
     }

     // Answer section starts here
     int pos = 12 + questionLen;

     // NAME (pointer to domain name in question) => 0xC00C
     response[pos++] = (byte) 0xC0;
     response[pos++] = 0x0C;

     // TYPE = A (1)
     response[pos++] = 0x00;
     response[pos++] = 0x01;

     // CLASS = IN (1)
     response[pos++] = 0x00;
     response[pos++] = 0x01;

     // TTL = 60 seconds
     response[pos++] = 0x00;
     response[pos++] = 0x00;
     response[pos++] = 0x00;
     response[pos++] = 0x3C;

     // RDLENGTH = 4
     response[pos++] = 0x00;
     response[pos++] = 0x04;

     // RDATA = IP in 4 bytes
     String[] parts = ip.split("\\.");
     for (String part : parts) {
         response[pos++] = (byte) Integer.parseInt(part);
     }

     return Arrays.copyOf(response, pos);
 }

 public  int getQuesLen(byte[]data){
      int pos = 12;
      while(data[pos] != 0){
            pos += (data[pos] & 0xFF) + 1;
      }
      return (pos + 1 + 4) - 12;
 }
}
