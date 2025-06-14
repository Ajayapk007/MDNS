# 🧠 MDNS - Custom Multithreaded DNS Server

This is a simple Multithreaded DNS (MDNS) Server written in Java. It listens for DNS requests over UDP, maps domain names to IP addresses using a config file, and handles multiple clients concurrently using a thread pool.

---

## 📦 Features

- ✅ Handles DNS queries over UDP
- 🔁 Thread pool support for concurrent requests
- 🗂 Configurable domain-IP mappings using `config.properties`
- 📝 Logging system using Java's built-in logging
- 🌐 Fallback to real DNS lookup if domain is not found
- 💾 Auto-adds resolved domains to config file for caching
- 🚀 Easily extendable for TTL caching or recursive DNS resolution

---

## 🛠 Setup

### 🔧 Prerequisites
- Java 8 or above
- IntelliJ IDEA (or any Java IDE)
- Git (optional, for cloning)

---

## 🚀 How to Run

### Step 1: Clone the Repo
```bash
git clone https://github.com/your-username/mdns-server.git
cd mdns-server
