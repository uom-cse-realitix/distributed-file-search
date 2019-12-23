# Distributed File Searcher

A distributed methodology to search files in a system.

## Phases

* Phase 1: Design network topology
* Phase 2: Design & Develop a socket-based solution to find files requested by different nodes.
* Phase 3: Web service (REST API) to support file transfer.

## Progress

1. Run `bootstrapserver-1.0-SNAPSHOT.jar` using `java -jar bootstrapserver-1.0-SNAPSHOT.jar`.
2. Build `filesearch` module using `mvn clean install`.
3. Run the `.jar` file generated in `filesearch/target` using `java -jar filesearch-1.0-SNAPSHOT.jar <PORT> <USERNAME>` (Replace `<PORT>` and `<USERNAME>` with your own data).
4. Observe the BootstrapServer console and consoles of each node (pay attention to console outputs. The response messages are logged. Check for the log message below for an instance)  by registering multiple nodes.
 
```
13:10:22.546 [nioEventLoopGroup-2-1] INFO org.realitix.dfilesearch.filesearch.socket.UDPClientHandler - Response message: 0042 REGOK 2 127.0.0.1 5001 127.0.0.1 5002
```
 
The above message shows that the bootstrap server has sent the `REGOK` along with the IPs and ports of the currently registered nodes when a third node has requested `REG`.

## Notes

1. After the initial handshakes and housekeeping, the client should connect to other peers (for file sharing). Thus, "host" and "port" should resemble those of that peers. This can be facilitated by giving some sort of a map. Think about it. Or, we should close the connection with the BS and initiate another connection with the peers after the initial handshakes.After the initial handshakes and housekeeping, the client should connect to other peers (for file sharing). Thus, "host" and "port" should resemble those of that peers. This can be facilitated by giving some sort of a map. Think about it. Or, we should close the connection with the BS and initiate another connection with the peers after the initial handshakes. This should be remedied in `UDPClient.java` file in its `run()` method.

2. TCP mainains reliability, flow control, order, low speed. 
3. TCP is best suited for 
    
    * World Wide Web (HTTP, HTTPS)
    * Secure Shell (SSH)
    * File Transfer Protocol (FTP)
    * Email (SMTP, IMAP/POP)

4. UDP is best suited for:

    * VPN tunneling
    * Streaming videos
    * Online games
    * Live broadcasts
    * Domain Name System (DNS)
    * Voice over Internet Protocol (VoIP)
    * Trivial File Transfer Protocol (TFTP)
