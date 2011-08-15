package com.shadowcraft.android;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class Bnet {

    public String getCached(String name, String realm, String region) {

        return null;
    }

    public String fetchChar(String name, String realm, String region) {
        String host = "http://" + region.toLowerCase() + ".battle.net";
        String charApi = String.format("/api/wow/character/%s/%s", name, realm);
        String fields = "?fields=items,talents,professions,appearance";

        URI uri = null;
        try {
            uri = new URI(host + charApi + fields);
        }
        catch (URISyntaxException e) {
            // throw warning here.
        }

        String json = getStringJSONFromRequest(uri.toString());

        return json;
    }

    /**
     * From https://github.com/chalverson/wowjavaapi.git
     * Returns the JSON from the supplied URL. This will always return some
     * sort of JSON. If there is a URL connection error it will return a JSON
     * string that is a error status.
     * 
     * @param url to send request
     * @return String of the returned JSON
     */
    public static String getStringJSONFromRequest(String url) {
        String ret = "";
        BufferedReader reader = null;
        HttpURLConnection urlConnection;

        try {
            urlConnection = (HttpURLConnection) new URL(url).openConnection();
            String line;
            StringBuilder sb = new StringBuilder();
            int responseCode = urlConnection.getResponseCode();
            // Read the correct stream based on the response code.
            if((responseCode == 404) || (responseCode == 500)) {
                InputStream input = urlConnection.getErrorStream();
                reader = new BufferedReader(new InputStreamReader(input));
            }
            else {
                InputStream input = urlConnection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(input));
            }
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
            ret = sb.toString();
        }
        catch (IOException e1) {
            // Some sort of connection error; return a JSON too.
            ret = "{\"status\":\"nok\", \"reason\":\"URL Connection Error\"}";
        }
        finally {
            if (reader != null) {
                try {
                    reader.close();
                }
                catch (IOException ignored) {
                }
            }
        }
        return ret;
    }

}

