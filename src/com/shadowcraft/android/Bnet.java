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

    public static String fetchChar(String name, String realm, String region) {
        String host = "http://" + region.toLowerCase() + ".battle.net";
        String api = String.format("/api/wow/character/%s/%s", realm, name);
        String fields = "?fields=items,talents,professions";
        URI uri = mkURI(host + api + fields);
        return getStringJSONFromRequest(uri.toString());
    }

    public static String fetchItem(String id) {
        String host = "http://us.battle.net";
        String api = String.format("/api/wow//item/%s", id);  // double slash
        URI uri = mkURI(host + api);
        return getStringJSONFromRequest(uri.toString());
    }

    public static URI mkURI(String uriString) {
        URI uri = null;
        try {
            uri = new URI(uriString);
        }
        catch (URISyntaxException e) {
            // throw warning here.
        }
        return uri;
    }

    /**
     * From https://github.com/chalverson/wowjavaapi.git Returns the JSON from
     * the supplied URL. This will always return some sort of JSON. If there is
     * a URL connection error it will return a JSON string that is a error
     * status.
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
            if ((responseCode == 404) || (responseCode == 500)) {
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
