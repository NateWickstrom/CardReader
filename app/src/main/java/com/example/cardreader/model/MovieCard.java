package com.example.cardreader.model;

import com.google.gson.annotations.SerializedName;

/**
 * MovieCard PoJo.
 */
public class MovieCard extends Card {

    @SerializedName("movieExtraImageURL")
    private String mMovieExtraImageUrl;

    public String getMovieExtraImageUrl(){
        return mMovieExtraImageUrl;
    }

}
