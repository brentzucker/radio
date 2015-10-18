var express = require('express');
var app = express();

var current_id = 193781466;
var playback_time = 1;

app.get('/api/getCurrentSong.json', function (request, response) {

	json = '{' + current_id + ', ' + playback_time + '}';
	response.send(json);
});

app.get('/api/:room_name', function (request, response) {
	response.send('Room: ' + request.params.room_name);
});

app.listen(8080);