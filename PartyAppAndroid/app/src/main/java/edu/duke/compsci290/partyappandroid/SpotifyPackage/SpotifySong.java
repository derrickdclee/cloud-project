package edu.duke.compsci290.partyappandroid.SpotifyPackage;

/**
 * Created by kennethkoch on 4/26/18.
 */

public class SpotifySong {
    private String songTitle;
    private String spotifyUrl;
    private String albumImageUrl;
    private String artistName;
    private String albumTitle;

    public void setSongTitle(String songTitle){
        this.songTitle = songTitle;
    }
    public String getSongTitle(){
        return songTitle;
    }

    public void setSpotifyUrl(String spotifyUrl){
        this.spotifyUrl = spotifyUrl;
    }
    public String getSpotifyUrl(){
        return spotifyUrl;
    }

    public void setAlbumImageUrl(String albumImageUrl){
        this.albumImageUrl = albumImageUrl;
    }
    public String getAlbumImageUrl(){
        return albumImageUrl;
    }

    public void setArtistName(String artistName){
        this.artistName = artistName;
    }
    public String getArtistName(){
        return artistName;
    }

    public void setAlbumTitle(String albumTitle){
        this.albumTitle = albumTitle;
    }
    public String getAlbumTitle(){
        return albumTitle;
    }

}
