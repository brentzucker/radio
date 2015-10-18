var express = require('express');
var bodyParser = require('body-parser')
var app = express();

app.use( bodyParser.json() );       // to support JSON-encoded bodies
app.use(bodyParser.urlencoded({     // to support URL-encoded bodies
	extended: true
})); 

var queue = [193781466]
var current_id = queue[0];
var playback_time = 1;

app.get('/api/getCurrentSong.json', function (request, response) {

	json = '{' + current_id + ', ' + playback_time + '}';
	response.send(json);
});

app.get('/api/getQueue.json', function (request, response) {

	json = '{[';
	for (var i = 0; i < queue.length; i++) {
		
		json += queue[i];
		if (i + 1 < queue.length) json += ', ';
	}
	json += ']}';

	response.send(json);
});

app.get('/api/room/:room_name', function (request, response) {
	response.send('Room: ' + request.params.room_name);
});

app.post('/api/addSong', function (request, response) {

	var song_id = request.body.id;
	response.sendStatus(song_id);
	console.log(song_id);

	queue.push(song_id);
});

app.listen(8080);