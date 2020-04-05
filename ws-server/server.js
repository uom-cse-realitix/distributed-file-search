/**
 * Opens a TCP server to receive server logs.
 */

/**
 * WebSocket Server
 * @type {WebSocket}
 */
const WebSocket = require('ws');
const wss = new WebSocket.Server({ port: 3001 });

wss.on('connection', function connection(ws) {
    ws.on('message', function incoming(message) {
        console.log('received: %s', message);
        wss.clients.forEach(function each(client) {
            if (client !== ws && client.readyState === WebSocket.OPEN) client.send(message);
        })
    });
});

/**
 * TCP Server
 */
// Include Nodejs' net module.
const Net = require("net");
// The port on which the server is listening.
const port = 3000;

// Use net.createServer() in your code. This is just for illustration purpose.
// Create a new TCP server.
const server = new Net.Server();
// The server listens to a socket for a client to make a connection request.
// Think of a socket as an end point.
server.listen(port, function() {
    console.log(`Server listening for connection requests on socket localhost:${port}`);
});

// When a client requests a connection with the server, the server creates a new
// socket dedicated to that client.
server.on('connection', function(socket) {
    console.log('A new connection has been established.');
    // The server can also receive data from the client by reading from its socket.
    socket.on('data', function(chunk) {
        console.log("Data received from client:" +  chunk.toString() );
        const ws = new WebSocket("ws://localhost:3001");
        // send incoming data to the websocket server
        ws.on('open', function open() {
            ws.send(chunk.toString());
            ws.close();
        })
    });
});