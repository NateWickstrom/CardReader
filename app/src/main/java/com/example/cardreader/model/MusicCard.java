package com.example.cardreader.model;

import com.google.gson.annotations.SerializedName;

/**
 * MusicCard PoJo.
 */
public class MusicCard extends Card {

    @SerializedName("musicVideoURL")
    private String mMusicVideoUrl;

    public String getMusicVideoUrl(){
        return mMusicVideoUrl;
    }

}
