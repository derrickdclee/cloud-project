package edu.duke.compsci290.partyappandroid.SpotifyPackage;

import java.util.List;

/**
 * Created by kennethkoch on 4/26/18.
 */

public class SongVote {
    private String album_image;
    private String album_title;
    private String artist_name;
    private int num_votes;
    private String song_title;
    private String spotify_url;
    private List<String> upvoter_ids;
    private List<String> downvoter_ids;

    public void setAlbum_image(String album_image){
        this.album_image = album_image;
    }
    public String getAlbum_image(){
        return album_image;
    }

    public void setAlbum_title(String album_title){
        this.album_title = album_title;
    }
    public String getAlbum_title(){
        return album_title;
    }

    public void setArtist_name(String artist_name){
        this.artist_name = artist_name;
    }
    public String getArtist_name(){
        return artist_name;
    }

    public void setNum_votes(int num_votes){
        this.num_votes = num_votes;
    }
    public int getNum_votes(){
        return num_votes;
    }

    public void setSong_title(String song_title){
        this.song_title = song_title;
    }
    public String getSong_title(){
        return song_title;
    }

    public void setSpotify_url(String spotify_url){
        this.spotify_url = spotify_url;
    }
    public String getSpotify_url(){
        return spotify_url;
    }

    public void setUpvoter_ids(List<String> upvoter_ids){
        this.upvoter_ids = upvoter_ids;
    }
    public List<String> getUpvoter_ids(){
        return upvoter_ids;
    }

    public void setDownvoter_ids(List<String> downvoter_ids){
        this.downvoter_ids = downvoter_ids;
    }
    public List<String> getDownvoter_ids(){
        return downvoter_ids;
    }
}
