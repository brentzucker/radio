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

	// var curl_duration = getSongDuration(current_id);
	// console.log(curl_duration);
	
	json_string = '{ song_id: ' + current_id + ', playback_time: ' + playback_time + '}';
	response.send(json_string);
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

function getSongDuration(song_id) {
	
	var http = require("http");
	var key = 'ebd2eeac20536a7baf8e43a15537cc25';

	var options = {
		host: 'api.soundcloud.com',
		path: '/tracks/' + song_id + '?client_id=' + key
	};

	var req = http.request(options, function(res) {
		// console.log('STATUS: ' + res.statusCode);
		// console.log('HEADERS: ' + JSON.stringify(res.headers));
		res.setEncoding('utf8');
		res.on('data', function (chunk) {
			console.log('BODY: ' + chunk);

			json_obj = JSON.parse(chunk);
			console.log(json_obj['duration']);

			// console.log(chunk);
			// return chunk;
		});
	});

	req.on('error', function(e) {
		console.log('problem with request: ' + e.message);
	});

	// write data to request body
	req.write('data\n');
	req.write('data\n');
	req.end();
}