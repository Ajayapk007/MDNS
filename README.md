# 🧠 MDNS - Custom Multithreaded DNS Server

This is a simple Multithreaded DNS (MDNS) Server written in Java. It listens for DNS requests over UDP, maps domain names to IP addresses using a config file, and handles multiple clients concurrently using a thread pool.

---

📌 Project Summary

Built this project to understand how DNS works under the hood — parsing raw UDP packets, binary protocols, and concurrent networking in Java.

Language: Java  |  Protocol: UDP  |  Port: 5359 (configurable)

✨ Features
FeatureDescription🔁 Multi-threadedThread pool handles concurrent DNS queries🗂 Config-based recordsDomain → IP mappings in config.properties🌐 Fallback resolutionUses InetAddress.getByName() if record not found💾 Auto-cachingResolved domains are saved back to config📝 LoggingJava Logger for monitoring and debugging📦 Binary DNS parsingManual parsing of raw DNS packet bytes

---
### 📂 Project Structure
MDNS/
 ├── src/
 │    ├── Main.java              ← Entry point, starts the server
 │    └── DNS/
 │         ├── DnsServer.java    ← Listens on UDP, dispatches to thread pool
 │         ├── DnsHandler.java   ← Parses query, resolves domain, builds response
 │         └── ConfigLoader.java ← Loads & saves domain-IP mappings
 ├── config.properties           ← DNS records + server config
 └── MDNS.iml

🚀 How to Run
Prerequisites

Java 8+
IntelliJ IDEA (or any Java IDE)

Steps
bash# Clone the repo
git clone https://github.com/Ajayapk007/MDNS.git
cd MDNS

# Open in IntelliJ and run Main.java
# OR compile & run manually:
javac -d out src/Main.java src/DNS/*.java
java -cp out Main
Test it
bash# Using dig (Linux/Mac)
dig @127.0.0.1 -p 5359 www.google.com

# Using nslookup (Windows)
nslookup www.google.com 127.0.0.1

🌐 Networking Concepts Used
ConceptDetailsUDPDNS uses UDP — fast, connectionless, small packetsDatagramSocketJava class for sending/receiving UDP packetsDatagramPacketRepresents a single UDP packet (data + address + port)Port 53 / 5359Standard DNS port is 53; this server uses 5359 (configurable)DNS Packet FormatHeader → Question → Answer (binary, manually built)Thread PoolExecutorService with fixed thread count for concurrency

🤝 Connect

GitHub: Ajayapk007


⭐ If you found this useful, feel free to star the repo!
