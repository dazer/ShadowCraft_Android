package com.shadowcraft.android;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;

public class Bnet {

    public static String fetchChar(String name, String realm, String region) {
        name = normalize(name);
        realm = normalize(realm);
        String host = String.format("http://%s.battle.net", region);
        String api = String.format("/api/wow/character/%s/%s", realm, name);
        String fields = "?fields=items,talents,professions";
        URI uri = mkURI(host + api + fields);
        return getStringJSONFromRequest(uri.toString());
    }

    public static String fetchItem(int id) {
        String host = "http://us.battle.net";
        String api = String.format("/api/wow//item/%s", id);  // double slash
        URI uri = mkURI(host + api);
        return getStringJSONFromRequest(uri.toString());
    }

    public static String normalize(String string) {
        try {
            string = URLEncoder.encode(string, "UTF-8");
        }
        catch (UnsupportedEncodingException ignore) {}
        return string;
    }

    //    public static BufferedImage fetchPortrait(String region, String thumbnail) {
    //        region = region.toLowerCase();
    //        // -profilemain   whole body
    //        // -inset         waist up
    //        // -avatar        64x64 icon
    //        // -card          side view
    //        thumbnail = thumbnail.replaceAll("-avatar", "-card");
    //        String urlString = "http://%s.battle.net/static-render/%s/%s";
    //        urlString = String.format(urlString, region, region, thumbnail);
    //        BufferedImage img = null;
    //        try {
    //            URL url = new URL(urlString);
    //            img = ImageIO.read(url);
    //        }
    //        catch (IOException ignore) {}
    //        return img;
    //    }

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
                // System.out.println(urlConnection);
                InputStream input = urlConnection.getErrorStream();
                reader = new BufferedReader(new InputStreamReader(input));
            }
            else {
                InputStream input = urlConnection.getInputStream();
                int size = 16 * 1024;
                reader = new BufferedReader(new InputStreamReader(input), size);
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
