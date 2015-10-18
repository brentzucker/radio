var express = require('express');
var bodyParser = require('body-parser')
var app = express();

app.use( bodyParser.json() );       // to support JSON-encoded bodies
app.use(bodyParser.urlencoded({     // to support URL-encoded bodies
	extended: true
})); 

/* Song Variables */

var queue = [];
var current_id = 000000000;
var current_title = 'null';
var start_time = 0;
var playback_time = 0;
duration = 0;

app.get('/api/getCurrentSong.json', function (request, response) {

	var current_time = new Date().getTime();

	// If new song, reset start time and get song duration
	if (current_id == 000000000) {
		console.log('new song');
		start_time = new Date().getTime();

		if (queue.length > 0) {
			current_id = queue[0].song_id;
			current_title = queue[0].song_title;
		} else {
			current_id = 000000000;
			current_title = 'null';
		}
		getSongDuration(current_id);
	}
	console.log("duration: " + duration);

	playback_time = current_time - start_time;
	
	// If the song is over get a new song
	if (duration != 0 && playback_time >= duration) {

		playback_time = 0;
		queue.shift();

		if (queue.length > 0) {
			current_id = queue[0].song_id;
			current_title = queue[0].song_title;
		} else {
			current_id = 000000000;
			current_title = 'null';
		}
	}
	
	json_string = '{song_title: \"' + current_title + '\", song_id: ' + current_id + ', playback_time: ' + playback_time + '}';
	response.send(json_string);
});

app.get('/api/getQueue.json', function (request, response) {

	json = '{ \"song_queue\": [';
	for (var i = 0; i < queue.length; i++) {
		
		json += '{ \"song_title\": \"' + (queue[i].song_id == 0 ? "null" : queue[i].song_title) + '\", \"song_id\": ' + queue[i].song_id + '}';
		if (i + 1 < queue.length) json += ', ';
	}
	json += ']}';

	response.send(json);
});

app.get('/api/room/:room_name', function (request, response) {
	response.send('Room: ' + request.params.room_name);
});

app.post('/api/addSong', function (request, response) {

	console.log("add song");
	var song_id = request.body.song_id;
	var song_title = request.body.song_title;
	response.sendStatus(song_title + " " + song_id);
	console.log(song_title + " " + song_id);

	queue.push({'song_id': song_id, 'song_title': song_title});
});

/* Run App */

app.listen(8080);

/* Helper Functions */

function getSongDuration(song_id) {
	
	var http = require("http");
	var key = 'ebd2eeac20536a7baf8e43a15537cc25';

	var options = {
		host: 'api.soundcloud.com',
		path: '/tracks/' + song_id + '?client_id=' + key
	};
	
	var req = http.request(options, function(res) {
		res.setEncoding('utf8');
		// console.log('STATUS: ' + res.statusCode);
		// console.log('HEADERS: ' + JSON.stringify(res.headers));

		var data = '';
		res.on('data', function (chunk) {
			data += chunk;
		});

		res.on('end', function() {

			var obj = JSON.parse(data);
			console.log(obj.duration);
			duration = obj.duration;

		});
	});

	req.on('error', function(e) {
		console.log('problem with request: ' + e.message);
	});
	
	req.end();
}