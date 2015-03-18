package com.example.cardreader.model;

import com.google.gson.annotations.SerializedName;

/**
 * PlaceCard PoJo.
 */
public class PlaceCard extends Card {

    @SerializedName("placeCategory")
    private String mPlaceCategory;

    public String getPlaceCategory(){
        return mPlaceCategory;
    }

}
