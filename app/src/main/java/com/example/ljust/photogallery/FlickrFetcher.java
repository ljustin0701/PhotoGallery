package com.example.ljust.photogallery;

import android.net.Uri;
import android.util.Log;
import android.widget.Gallery;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ljust on 1/19/2016.
 */
public class FlickrFetcher {

    private static final String TAG = "FlickrFetcher";
    private static final String API_KEY = BuildConfig.API_KEY;
    private static final String FETCH_RECENTS_METHOD = "flickr.photos.getRecent";
    private static final String SEARCH_METHOD = "flickr.photos.search";
    private static final Uri ENDPOINT = Uri.parse("https://api.flickr.com/services/rest/")
                    .buildUpon()
                    .appendQueryParameter("api_key", API_KEY)
                    .appendQueryParameter("format", "json")
                    .appendQueryParameter("nojsoncallback", "1")
                    .appendQueryParameter("extras", "url_s")
                    .build();

    public byte[] getURLBytes(String urlSpec) throws IOException {
        URL url = new URL(urlSpec);
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = connection.getInputStream();

            if(connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException(connection.getResponseMessage() + ": with " + urlSpec);
            }

            int bytesRead = 0;
            byte[] buffer = new byte[1024];
            while((bytesRead = in.read(buffer)) > 0 ) {
                out.write(buffer, 0, bytesRead);
            }
            out.close();
            return out.toByteArray();
        } finally {
            connection.disconnect();
        }
    }

    public String getUrlString(String urlSpec) throws IOException {
        return new String(getURLBytes(urlSpec));
    }

    public List<GalleryItem> fetchRecentPhotos() {
        String url = buildURL(FETCH_RECENTS_METHOD, null);
        return downloadGalleryItems(url);
    }

    public List<GalleryItem> searchPhotos(String query) {
        String url = buildURL(SEARCH_METHOD, query);
        return downloadGalleryItems(url);
    }

    private List<GalleryItem> downloadGalleryItems(String url) {
        List<GalleryItem> items = new ArrayList<>();
        try {
//            .appendQueryParameter("page", Integer.toString(page))
            String jsonString = getUrlString(url);
//            Log.i(TAG, "Received JSON: " + jsonString);
            JsonParser parser = new JsonParser();
            JsonObject jsonBody = (JsonObject)parser.parse(jsonString);
            parseItems(items, jsonBody);
        } catch(JSONException je){
            Log.e(TAG, "Failed to parse JSON", je);
        } catch(IOException ioe) {
            Log.e(TAG, "Failed to fetch items", ioe);
        }

        return items;
    }

    private String buildURL(String method, String query) {
        Uri.Builder uriBuilder = ENDPOINT.buildUpon().appendQueryParameter("method", method);

        if(method.equals(SEARCH_METHOD)) {
            uriBuilder.appendQueryParameter("text", query);
        }
        return uriBuilder.build().toString();
    }

    private void parseItems(List<GalleryItem> items, JsonObject jsonBody) throws IOException, JSONException{
        /* Using Gson*/
        Gson gson = new Gson();
        JsonArray JsonArray = jsonBody
                .getAsJsonObject().get("photos")
                .getAsJsonObject().get("photo")
                .getAsJsonArray();
        for(JsonElement je : JsonArray){
            GalleryItem item = gson.fromJson(je, GalleryItem.class);
            items.add(item);
        }
        /* Using Regular JSON */
//        JSONObject photosJsonObject = jsonBody2.getJSONObject("photos");
//        JSONArray photoJsonArray = photosJsonObject.getJSONArray("photo");
//            JSONObject photoJsonObject = photoJsonArray.getJSONObject(i);
//            GalleryItem item = new GalleryItem();
//            item.setId(photoJsonObject.getString("id"));
//            item.setCaption(photoJsonObject.getString("title"));
//            if(!photoJsonObject.has("url_s")) continue;
//
//            item.setUrl(photoJsonObject.getString("url_s"));
//            items.add(item);
//        }
    }


}
