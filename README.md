# 🌐 MDNS — Multi-Threaded DNS Server in Java

A custom **Multi-Threaded DNS Server** built from scratch in Java. It listens for DNS queries over UDP, resolves domain names using a local config file or Java's built-in resolver, handles concurrent requests using a thread pool, and caches resolved records for future use.

---

## 📌 Project Summary

> Built this project to understand how DNS works under the hood — parsing raw UDP packets, binary protocols, and concurrent networking in Java.

**Language:** Java &nbsp;|&nbsp; **Protocol:** UDP &nbsp;|&nbsp; **Port:** 5359 (configurable)

---

## ✨ Features

| Feature | Description |
|---|---|
| 🔁 Multi-threaded | Thread pool handles concurrent DNS queries |
| 🗂 Config-based records | Domain → IP mappings in `config.properties` |
| 🌐 Fallback resolution | Uses `InetAddress.getByName()` if record not found |
| 💾 Auto-caching | Resolved domains are saved back to config |
| 📝 Logging | Java `Logger` for monitoring and debugging |
| 📦 Binary DNS parsing | Manual parsing of raw DNS packet bytes |

---

## 📂 Project Structure

```
MDNS/
 ├── src/
 │    ├── Main.java              ← Entry point, starts the server
 │    └── DNS/
 │         ├── DnsServer.java    ← Listens on UDP, dispatches to thread pool
 │         ├── DnsHandler.java   ← Parses query, resolves domain, builds response
 │         └── ConfigLoader.java ← Loads & saves domain-IP mappings
 ├── config.properties           ← DNS records + server config
 └── MDNS.iml
```

---

## ⚙️ How It Works — Request Flow

```
┌────────────────────────────────────────────────────────────┐
│                        DNS Client                          │
│              (browser / nslookup / dig)                    │
└──────────────────────┬─────────────────────────────────────┘
                       │  UDP DNS Query Packet
                       ▼
┌────────────────────────────────────────────────────────────┐
│                  DnsServer.java                            │
│   DatagramSocket.receive(packet)  ←  Port 5359            │
│   pool.submit(new DnsHandler(packet, socket))              │
└──────────────────────┬─────────────────────────────────────┘
                       │  Submits to Thread Pool
                       ▼
┌────────────────────────────────────────────────────────────┐
│                  DnsHandler.java  (Runnable)               │
│                                                            │
│  1. Extract raw bytes from packet                          │
│  2. Parse domain name (label encoding: 3www6google3com0)   │
│  3. Look up in ConfigLoader                                │
│                                                            │
│         ┌──────────────────────────┐                       │
│         │    Found in config?      │                       │
│         └────────┬─────────────────┘                       │
│              YES │           NO                            │
│                  │           │                             │
│                  │           ▼                             │
│                  │   InetAddress.getByName(domain)         │
│                  │   → Get IP from real DNS                │
│                  │   → Save to config.properties           │
│                  │           │                             │
│                  └─────┬─────┘                             │
│                        ▼                                   │
│             Build DNS Response Packet                      │
│       (Header + Question + Answer sections)                │
└──────────────────────┬─────────────────────────────────────┘
                       │  UDP Response Packet
                       ▼
┌────────────────────────────────────────────────────────────┐
│                     DNS Client                             │
│                  Receives IP Address                       │
└────────────────────────────────────────────────────────────┘
```

---

## 📊 Data Flow Diagram (DFD)

### Level 0 — Context Diagram

```
                     DNS Query (UDP)
  ┌──────────┐  ──────────────────────►  ┌──────────────────────┐
  │  Client  │                           │   MDNS DNS Server    │
  └──────────┘  ◄──────────────────────  └──────────────────────┘
                     IP Response (UDP)
```

---

### Level 1 — Internal Data Flow

```
                         ┌────────────────┐
                         │   DNS Client   │
                         └───────┬────────┘
                                 │ UDP Packet
                                 ▼
                    ┌────────────────────────┐
                    │   1. Receive & Queue   │  ← DnsServer.java
                    │   (DatagramSocket)     │
                    └────────────┬───────────┘
                                 │ Raw bytes
                                 ▼
                    ┌────────────────────────┐
                    │  2. Parse DNS Query    │  ← DnsHandler.java
                    │  (Extract domain name) │
                    └────────────┬───────────┘
                                 │ domain string
                                 ▼
                    ┌────────────────────────┐
                    │  3. Lookup in Config   │  ← ConfigLoader.java
                    └──────┬─────────────────┘
                           │                │
                    FOUND  │                │ NOT FOUND
                           │                ▼
                           │   ┌────────────────────────┐
                           │   │ 4. Fallback Resolution │
                           │   │  InetAddress.getByName │
                           │   └──────────┬─────────────┘
                           │              │ IP + cache write
                           │              ▼
                           │   ┌────────────────────────┐
                           │   │  5. Save to Config     │  ─── config.properties
                           │   └──────────┬─────────────┘
                           │              │
                           └──────┬───────┘
                                  │ IP Address
                                  ▼
                    ┌────────────────────────┐
                    │  6. Build DNS Response │
                    │  (Header + Answer)     │
                    └────────────┬───────────┘
                                 │ UDP Response
                                 ▼
                         ┌───────────────┐
                         │  DNS Client   │
                         └───────────────┘
```

---

## 🧩 Component Breakdown

### `DnsServer.java` — Core Server
Listens on a UDP port and dispatches requests to a thread pool.

```java
DatagramSocket socket = new DatagramSocket(port);
ExecutorService pool = Executors.newFixedThreadPool(threads);

while (true) {
    socket.receive(packet);              // Block until query arrives
    pool.submit(new DnsHandler(packet, socket));  // Hand off to worker
}
```

**Why UDP?** DNS uses UDP because it's fast, connectionless, and DNS packets are small.

---

### `DnsHandler.java` — Request Processor
Implements `Runnable`. Runs in the thread pool.

**DNS domain name encoding:**
```
"www.google.com"  →  3www6google3com0
                      ↑    ↑       ↑
                    label  label  terminator (0x00)
```

**Response packet header flags set manually:**
```
response[2] = (byte) 0x81   // QR=1 (response), RD=1 (recursion desired)
response[3] = (byte) 0x80   // RA=1 (recursion available)
```

---

### `ConfigLoader.java` — Config & Cache
Loads domain→IP records using Java `Properties`.

```properties
# config.properties
port=5359
threads=10
www.google.com=8.8.8.8
api.example.com=192.168.1.10
```

Newly resolved domains are appended automatically — acting as a **persistent DNS cache**.

---

## 🔄 Thread Pool — Concurrency Model

```
Incoming Requests:                Thread Pool (10 workers):

  Client A ──►  ┌─────────────────────────────────────┐
  Client B ──►  │  Thread 1 → DnsHandler(A)            │
  Client C ──►  │  Thread 2 → DnsHandler(B)            │
  Client D ──►  │  Thread 3 → DnsHandler(C)            │
                │  Thread 4 → DnsHandler(D)            │
                │  ...                                 │
                └─────────────────────────────────────┘

Without thread pool: Clients queue up → high latency
With thread pool:    Requests processed in parallel → low latency
```

---

## 📝 Logging

```java
Logger logger = Logger.getLogger(DnsServer.class.getName());

logger.info("Query received: www.google.com");
logger.warning("Domain not found in config, falling back...");
logger.severe("Socket error occurred");
```

Useful for monitoring traffic, debugging resolution failures, and observing cache hits.

---

## 🚀 How to Run

### Prerequisites
- Java 8+
- IntelliJ IDEA (or any Java IDE)

### Steps

```bash
# Clone the repo
git clone https://github.com/Ajayapk007/MDNS.git
cd MDNS

# Open in IntelliJ and run Main.java
# OR compile & run manually:
javac -d out src/Main.java src/DNS/*.java
java -cp out Main
```

### Test it

```bash
# Using dig (Linux/Mac)
dig @127.0.0.1 -p 5359 www.google.com

# Using nslookup (Windows)
nslookup www.google.com 127.0.0.1
```

---

## 🌐 Networking Concepts Used

| Concept | Details |
|---|---|
| **UDP** | DNS uses UDP — fast, connectionless, small packets |
| **DatagramSocket** | Java class for sending/receiving UDP packets |
| **DatagramPacket** | Represents a single UDP packet (data + address + port) |
| **Port 53 / 5359** | Standard DNS port is 53; this server uses 5359 (configurable) |
| **DNS Packet Format** | Header → Question → Answer (binary, manually built) |
| **Thread Pool** | `ExecutorService` with fixed thread count for concurrency |

---

## 🤝 Connect

- **GitHub:** [Ajayapk007](https://github.com/Ajayapk007)

---

⭐ If you found this useful, feel free to star the repo!
