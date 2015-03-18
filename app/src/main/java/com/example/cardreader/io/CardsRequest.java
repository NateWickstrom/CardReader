package com.example.cardreader.io;

import android.util.Log;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.example.cardreader.model.Card;
import com.example.cardreader.model.MovieCard;
import com.example.cardreader.model.MusicCard;
import com.example.cardreader.model.PlaceCard;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;


/**
 * Custom Volley request for fetching Cards.
 */
public class CardsRequest extends Request<List<Card>> {

    /** tag for logging **/
    private static final String TAG = CardsRequest.class.getSimpleName();
    /** Cards APIs **/
    private static final String CARDS_API = "https://gist.githubusercontent.com/helloandrewpark/0a407d7c681b833d6b49/raw/5f3936dd524d32ed03953f616e19740bba920bcd/gistfile1.js";
    /** listener for successful responses **/
    private final Response.Listener<List<Card>> mListener;

    /** Creates new request object
     * @param listener listener for successful responses
     * @param errorListener error listener in case an error happened
     */
    public CardsRequest(Response.Listener<List<Card>> listener, Response.ErrorListener errorListener) {
        super(Method.GET, CARDS_API, errorListener);
        mListener = listener;
    }

    @Override
    protected void deliverResponse(List<Card> response) {
        // notify observer object, thereby notifying front-end UI
        mListener.onResponse(response);
    }

    /**
     * Volley Request method override to catch raw network Response
     *
     * @param response the raw network response
     * @return Response Card list
     */
    @Override
    protected final Response<List<Card>> parseNetworkResponse(NetworkResponse response) {
        try {
            String json = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
            List<Card> data = parseResponse(json);
            return Response.success(data, HttpHeaderParser.parseCacheHeaders(response));
        } catch (JsonParseException e) {
            Log.e(TAG, "Json Parse Exception", e);
            return Response.error(new ParseError(e));
        } catch (JSONException e) {
            Log.e(TAG, "Json Exception", e);
            return Response.error(new ParseError(e));
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "Unsupported Encoding Exception", e);
            return Response.error(new ParseError(e));
        }
    }

    private List<Card> parseResponse(String response) throws JsonParseException, JSONException {

        ArrayList<Card> cards = new ArrayList<>();
        JSONObject jsonObject = new JSONObject(response);
        JSONArray jsonArray = jsonObject.getJSONArray("cards");
        Gson gson = new Gson();

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject cardJson = jsonArray.getJSONObject(i);
            Card card = null;
            switch(cardJson.getString("type")) {
                case "place":
                    card = gson.fromJson(cardJson.toString(), PlaceCard.class);
                    break;
                case "movie":
                    card = gson.fromJson(cardJson.toString(), MovieCard.class);
                    break;
                case "music":
                    card = gson.fromJson(cardJson.toString(), MusicCard.class);
                    break;
            }
            if (card != null)
                cards.add(card);

        }

        return cards;
    }
}
