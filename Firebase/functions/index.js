
'use strict';

const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);
var db = admin.firestore();

exports.suggestSong = functions.https.onCall((data, context) => {
	if (!context.auth){
		throw new functions.https.HttpsError('failed-precondition', 'The function must be called while authenticated');
	}
	const uid = context.auth.uid;
	const album_image = data.album_image;
	const album_title = data.album_title;
	const artist_name = data.artist_name;
	const party_id = data.party_id;
	const song_title = data.song_title;
	const spotify_url = data.spotify_url;


	return db.collection('queued_songs').where('party_id','==',party_id).where('spotify_url','==',spotify_url).get()
		.then(snapshot => {
			if (snapshot.size === 0){
				return db.collection('queued_songs').add({
					album_image: album_image,
					album_title: album_title,
					artist_name: artist_name,
					party_id: party_id,
					song_title: song_title,
					spotify_url: spotify_url,
					downvoter_ids: [],
					upvoter_ids: [uid],
					num_votes: 1
				});
			}
			throw new functions.https.HttpsError('already added', 'cant add the song again');
		})
		.then(result => {
			return "function complete";
		});

});

exports.voteOnSong = functions.https.onCall((data, context) => {
	if (!context.auth){
		throw new functions.https.HttpsError('failed-precondition', 'The function must be called while authenticated');
	}
	const uid = context.auth.uid;
	const party_id = data.party_id;
	const is_upvote = data.is_upvote;
	const spotify_url = data.spotify_url;
	var pointsToAdd = 0;
	var docRef = null;
	console.log("spotifyurl " + spotify_url);
	var compoundRef = db.collection('queued_songs').where('party_id','==',party_id).where('spotify_url','==',spotify_url);

	return compoundRef.get()
		.then(snapshot => {
			console.log("snapshot", snapshot);
			console.log("document id", snapshot.docs[0].id);
			if (snapshot.size>0){
				return db.collection('queued_songs').doc(snapshot.docs[0].id);
			}
			else {
				throw new functions.https.HttpsError('something went wrong', 'this document should exist');
			}
		})
		.then(docref => {
			console.log("docref is", docref)
			return db.runTransaction(t => {
				return t.get(docref)
					.then(doc => {
						// Add one person to the city population
						var upvoters = doc.data().upvoter_ids;
						var downvoters = doc.data().downvoter_ids;
						var upvoterindex = upvoters.indexOf(uid);
						var downvoterindex = downvoters.indexOf(uid);
						console.log("upvoters",doc.data().upvoter_ids);
						if (is_upvote){
							if (upvoterindex>-1){
								pointsToAdd = 0;
							}
							else if (downvoterindex>-1){
								pointsToAdd = 2;
								upvoters.push(uid);
								downvoters.splice(downvoterindex, 1);
								//downvoters = downvoters.filter(e => e !== uid);
							}
							else {
								pointsToAdd = 1;
								upvoters.push(uid);
							}
						}
						else{
							if (upvoterindex>-1){
								pointsToAdd = -2;
								upvoters.splice(upvoterindex, 1);
								//upvoters = upvoters.filter(e => e!==uid);
								downvoters.push(uid);

							}
							else if (downvoterindex>-1){
								pointsToAdd = 0;
							}
							else {
								pointsToAdd = -1;
								downvoters.push(uid);
							}
						}
				        var new_votes = doc.data().num_votes + pointsToAdd;
				        t.update(docref, { num_votes: new_votes, downvoter_ids: downvoters, upvoter_ids: upvoters });
				        return Promise.resolve('New count ' + new_votes);
					});
			});
		})
		.then(result => {
			console.log("success", result);
			return "Success";
		}).catch(err => {
			console.log("failure", err);
		});

});