const ws = new WebSocket('ws://localhost:3001', 'a');

ws.onopen = function () {
    console.log("ws connection open");
};

ws.onmessage = function (event) {
    console.log(event.data);
    $('#console-terminal').append(event.data).append("<br/>");
};

function execute() {
    let value = document.getElementById("command").value;
    const xhr = new XMLHttpRequest();
    xhr.open("POST", "/api/file/command", false);   // false for a synchronous operation
    xhr.setRequestHeader('Content-Type', 'application/json');
    console.log("Sending command: " + value);
    xhr.send(JSON.stringify({value: value}));
    document.getElementById("command").value = "";
}

function join() {
    const xhr = new XMLHttpRequest();
    xhr.open("GET", "/api/file/join", false);   // false for a synchronous operation
    xhr.setRequestHeader('Content-Type', 'application/json');
    xhr.send();
}

$('#characters').text(0);
$('input[name="cmd"]').on('keyup keydown', function () {
    $("#characters").text($(this).val().length);
});