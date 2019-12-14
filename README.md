# Distributed File Searcher

A distributed methodology to search files in a system.

## Phases

* Phase 1: Design network topology
* Phase 2: Design & Develop a socket-based solution to find files requested by different nodes.
* Pase 3: Web service (REST API) to support file transfer.

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