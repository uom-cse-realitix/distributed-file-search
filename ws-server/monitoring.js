const WebSocket = require('ws');

console.log("Monitoring starting");

const wss = new WebSocket.Server({port: 5000});

wss.on('connection', function connection(ws) {

    console.log("Connection opened");

    ws.on('message', function incoming(message) {
        console.log('received: %s', message);
    });
});