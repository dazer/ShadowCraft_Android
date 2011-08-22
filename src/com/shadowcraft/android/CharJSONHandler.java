package com.shadowcraft.android;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import calcs.DamageCalculator;

public class CharJSONHandler {

    private JSONObject charJSON;  // We could extract its values to fields.

    public CharJSONHandler (String name, String realm, String region) {
        this.charJSON = fetchChar(name, realm, region);
        cleanCharJSON();
        setDefault();
    }

    public CharJSONHandler (String json) {
        this.charJSON = mkJSON(json);
    }

    /**
     * Calls the appropriate back-end based on class
     * @return a DamageCalculator object to retrieve data from.
     */
    public DamageCalculator buildModeler() {
        DamageCalculator calculator = null;
        if (this.isClass("rogue"))
            calculator = RogueBackend.build(this);
        else if (this.isClass(""))
            ;
        return calculator;
    }

    /**
     * Once we have a way to cache the fields in the JSON, we should have this
     * method build a new JSONString from the cache. For the time being we will
     * be using the raw JSON and getters that return java structures.
     */
    @Override
    public String toString() {
        return this.charJSON.toString();
    }

    public JSONObject fetchChar(String name, String realm, String region) {
        JSONObject json = null;
        JSONObject cache = getCached(name, realm, region);
        if (cache != null) {
            return cache;
        }
        else {
            String JSONString = Bnet.fetchChar(name, realm, region);
            json = mkJSON(JSONString);
            // TODO catch nok errors
        }

        return json;
    }

    /**
     * This should, somehow, check for cached chars (outside the app). We're
     * returning null if no cache is found (all cases for now)
     * @param name
     * @param realm
     * @param region
     * @return
     */
    public JSONObject getCached(String name, String realm, String region) {
        // define a way to retrieve cached characters.
        return null;
    }

    // /////////////////////////////////////////////////////////////////////////
    // These functions append the default cycle and combat settings to newly
    // created characters.
    // /////////////////////////////////////////////////////////////////////////

    public void setDefault() {
        if (this.isClass("rogue"))
            rogueDefault();
        else if (this.isClass("otherClass"))
            otherClassDefault();
    }

    public void rogueDefault() {
        int[] buffArray = {1, 2, 4, 5, 6, 9, 15, 20, 22, 26, 27, 28, 30, 31};
        JSONObject assassination = new JSONObject();
        JSONObject combat = new JSONObject();
        JSONObject subtlety = new JSONObject();
        JSONObject cycleSettings = new JSONObject();
        JSONObject fightSettings = new JSONObject();
        try {
            JSONArray buffs = new JSONArray();
            for (int i : buffArray) {
                buffs.put(i);
            }

            assassination.put("min_envenom_size_mutilate", 4);
            assassination.put("min_envenom_size_backstab", 5);
            assassination.put("prioritize_rupture_uptime_mutilate", true);
            assassination.put("prioritize_rupture_uptime_backstab", true);
            cycleSettings.put("assassination", assassination);

            combat.put("use_rupture", true);
            combat.put("use_revealing_strike", "sometimes");
            combat.put("ksp_immediately", false);
            cycleSettings.put("combat", combat);

            subtlety.put("raid_crits_per_second", 5);
            subtlety.put("clip_recuperate", false);
            subtlety.put("use_hemorrhage", "never");
            subtlety.put("hemo_interval", 24);
            cycleSettings.put("subtlety", subtlety);

            fightSettings.put("time_in_execute_range", .35);
            fightSettings.put("tricks_on_cooldown", true);
            fightSettings.put("mh_poison", "ip");
            fightSettings.put("oh_poison", "dp");
            fightSettings.put("duration", 300);
            fightSettings.put("response_time", .5);
            fightSettings.put("pre_pot", false);
            fightSettings.put("combat_pot", true);

            charJSON.put("buffs", buffs);
            charJSON.put("cycleSettings", cycleSettings);
            charJSON.put("fightSettings", fightSettings);
        }
        catch (JSONException ignore) {}
    }

    public void otherClassDefault () {
    }

    // /////////////////////////////////////////////////////////////////////////
    // When fetching a character from Bnet, we need to clean it up: flatten some
    // nested entities and delete some fields
    // /////////////////////////////////////////////////////////////////////////

    /**
     * Removes unused fields and reformats talents, glyphs and professions. It
     * will discard the non-active spec (talents and glyphs) in the current
     * implementation.
     */
    public void cleanCharJSON() {
        charJSON.remove("lastModified");
        charJSON.remove("gender");
        charJSON.remove("achievementPoints");
        // json.remove("thumbnail");  // catch and use?
        // json.remove("name");
        // json.remove("realm");
        flattenProfs();
        flattenTalentsAndGlyphs();  // glyphs and talents come in the same field.

        try {
            // TODO need to better format this field; consider caching items from
            // the database.
            JSONObject items = charJSON.getJSONObject("items");
            items.remove("averageItemLevel");
            items.remove("averageItemLevelEquipped");
            items.remove("tabard");
            items.remove("shirt");

            for (String slot : Data.itemMap.keySet()) {
                JSONObject item = null;
                try {
                    item = items.getJSONObject(slot);
                }
                catch(JSONException ignore) {
                    continue;
                }
                item.remove("name");
                item.remove("icon");
                item.remove("quality");
                JSONObject tooltipParams = item.getJSONObject("tooltipParams");
                tooltipParams.remove("tinker");
                tooltipParams.remove("set");
            }
        }
        catch (JSONException ignore) {
            ignore.printStackTrace();
        }
    }

    /**
     * We are only interested in primary professions. This will replace the
     * professions field with an array of, at most, two profession IDs.
     */
    public void flattenProfs() {
        JSONArray profs = new JSONArray();
        try {
            JSONObject fetchedProfs = charJSON.getJSONObject("professions");
            JSONArray primProfs = fetchedProfs.getJSONArray("primary");
            for (int i = 0; i<primProfs.length(); i++) {
                JSONObject prof = primProfs.getJSONObject(i);
                profs.put(prof.getInt("id"));
            }
            charJSON.put("professions", profs);
        }
        catch (JSONException ignore) {}
    }

    /**
     * We are only interested in the active spec. If we want to have those the
     * non-active one (for cached specs) there's some code stubs to do so.
     */
    public void flattenTalentsAndGlyphs() {
        try {
            JSONArray talents = charJSON.getJSONArray("talents");
            for (int i = 0; i<2; i++) {
                JSONObject build = (JSONObject) talents.get(i);
                boolean active = false;
                try {
                    active = (build.getBoolean("selected")) ? true : false;
                }
                catch (JSONException ignore) {}
                if (!active) {
                    continue;  // discard non-active build.
                }

                JSONObject allGlyphs = build.getJSONObject("glyphs");
                JSONArray glyphsIds = new JSONArray();
                for (String s : new String[] {"prime", "minor", "major"}) {
                    JSONArray glyphs = allGlyphs.getJSONArray(s);
                    for (int j=0; j<glyphs.length(); j++) {
                        JSONObject glyph = glyphs.getJSONObject(j);
                        int a = glyph.getInt("item");
                        glyphsIds.put(a);
                    }
                }

                JSONArray allTrees = build.getJSONArray("trees");
                JSONArray talentStrings = new JSONArray();
                for (int j=0; j<allTrees.length(); j++) {
                    JSONObject spec = allTrees.getJSONObject(j);
                    String specString = spec.getString("points");
                    talentStrings.put(specString);
                }

                // json.put("fetchedGLyphs_" + (i + 1), glyphsIds);
                // json.put("fetchedTalents_" + (i + 1), talentStrings);
                charJSON.put("glyphs", glyphsIds);
                charJSON.put("talents", talentStrings);
            }
        }
        catch (JSONException ignore) {}
    }

    // /////////////////////////////////////////////////////////////////////////
    // Utility methods
    // /////////////////////////////////////////////////////////////////////////

    /**
     * Utility method to construct a JSON from a string. Use only on strings you
     * already know to be JSON: for the most part, the snapshots. The intent is
     * to skip the try/catch during development, but proper exception handling
     * should be done at some point. TODO
     * @param jsonString The string to be converted.
     * @return A JSONObject for the string.
     */
    public JSONObject mkJSON(String jsonString) {
        JSONObject json = null;
        try {
            json = new JSONObject(jsonString);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

    /**
     * Utility method to construct a JSONArray from a string.
     * @see mkJSON
     */
    public JSONArray mkJSONArray(String jsonString) {
        JSONArray json = null;
        try {
            json = new JSONArray(jsonString);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

    // /////////////////////////////////////////////////////////////////////////
    // General getters. Used during development.
    // /////////////////////////////////////////////////////////////////////////

    /**
     * General object getter. It returns null to emulate the Hash behavior.
     * @param name The field to retrieve
     * @return The value or null if no value found.
     */
    public Object get(String name) {
        try {
            return this.charJSON.get(name);
        }
        catch (JSONException e) {
            return null;
        }
    }

    /**
     * General Number getter. It returns 0 to imply that the key is not present
     */
    public Number getNum(String name) {
        try {
            return (Number) this.charJSON.get(name);
        }
        catch (JSONException e) {
            return 0;
        }
    }

    /**
     * General string getter. It returns null to emulate the Hash behavior.
     * @param name The field to retrieve
     * @return The string or null if no string found.
     */
    public String getString(String name) {
        return (String) this.get(name);
    }

    /**
     * General integer getter.
     * @param name The field to retrieve
     * @return The integer or 0 if no integer found.
     */
    public int getInt(String name) {
        return (Integer) this.getNum(name);
    }

    /**
     * General Double getter
     * @param name The field to retrieve
     * @return The double or 0 if no integer found.
     */
    public double getDouble(String name) {
        return (Double) this.getNum(name);
    }


    // /////////////////////////////////////////////////////////////////////////
    // Particular getters. If/when we cache the JSON to actual class fields,
    // these should look a lot better.
    // /////////////////////////////////////////////////////////////////////////

    /**
     * Maps the integer value of the class to its string value.
     * @return The string for one of the classes.
     */
    public String gameClass() {
        return Data.classMap.get(this.getInt("class"));
    }

    /**
     * Checks if the char is the queried class.
     * @param gameClass class to query
     * @return True if match, false otherwise.
     */
    public boolean isClass(String gameClass) {
        return gameClass().equals(gameClass);
    }

    /**
     * Maps the integer value of the race to its string value.
     * @return The string for one of the races.
     */
    public String race() {
        return Data.raceMap.get(this.getInt("race"));
    }

    /**
     * Finds the level in the JSON.
     * @return the level of the char.
     */
    public int level() {
        return this.getInt("level");
    }

    /**
     * Finds the professions in the JSON
     * @return The JSONArray with primary professions by ID.
     */
    public JSONArray professionsIDs() {
        return (JSONArray) this.get("professions");
    }

    /**
     * Maps the professions in the JSON to their string value.
     * @return A list with primary professions; or an empty list if no prof is
     * present.
     */
    public List<String> professions() {
        JSONArray profArray = professionsIDs();
        List<String> profList = new ArrayList<String>();
        for (int i = 0; i<profArray.length(); i++) {
            try {
                String prof = Data.professionsMap.get(profArray.get(i));
                if (prof != null)
                    profList.add(prof);
            }
            catch (JSONException ignore) {}
        }
        return profList;
    }

    /**
     * Finds the talents in the JSON
     * @return the 3 strings for the talents
     */
    public String[] talents() {
        JSONArray stringsFromJSON;
        String[] strings = new String[3];
        try {
            stringsFromJSON = this.charJSON.getJSONArray("talents");
            for (int i = 0; i<stringsFromJSON.length(); i++) {
                strings[i] = stringsFromJSON.getString(i);
            }
        }
        catch (JSONException ignore) {}
        return strings;
    }

    /**
     * Sums talents in every tree to return the spec in 31/2/8 format.
     * @return An integer array of three elements to convey the spec.
     */
    public Integer[] sumTalents() {
        String[] strings = talents();
        Integer[] spent = new Integer[3];
        for (int i = 0; i<strings.length; i++) {
            int sum = 0;
            for (int j = 0; j<strings[i].length(); j++) {
                sum += Integer.parseInt(String.valueOf(strings[i].charAt(j)));
            }
            spent[i] = sum;
        }
        return spent;
    }

    /**
     * Sums talents to get the spec in which most talents are spent. Then maps
     * the result to an actual spec string.
     * @return The string of the spec (for instance "assassination")
     */
    public String specced() {
        Integer[] spent = sumTalents();
        int totalSpent = spent[0] + spent[1] + spent[2];
        int maxSpec = 0;
        int maxSpent = 0;
        for (int i = 0; i<spent.length; i++) {
            if (maxSpent < spent[i]) {
                maxSpent = spent[i];
                maxSpec = i;
            }
        }
        if (maxSpent < 31 && totalSpent != maxSpent)
            return null;  // TODO throw exception instead
        if (totalSpent <= 41 && totalSpent > 0)
            return Data.specMap.get(gameClass())[maxSpec];
        return Data.specMap.get(gameClass())[0];
    }

    /**
     * Checks if the char is the queried spec.
     * @param spec The spec to query
     * @return True if match, false otherwise
     */
    public Boolean isSpecced(String spec) {
        return specced().equals(spec);
    }

    /**
     * Finds the glyphs in the JSON and returns them in ID format.
     * @return An array of integers (not ordered)
     */
    public Integer[] glyphsIDs() {
        JSONArray glyphs = (JSONArray) this.get("glyphs");
        List<Integer> l = new ArrayList<Integer>();
        try {
            for (int i = 0; i<glyphs.length(); i++) {
                l.add(glyphs.getInt(i));
            }
        }
        catch (JSONException ignore) {}
        return l.toArray(new Integer[]{});
    }

    /**
     * Maps the glyphs to their string values (those used by the engine). If no
     * map is found it will return the ID with preceding underscore.
     * @return An array of Strings (not ordered)
     */
    public String[] glyphs() {
        // this places _id if the glyph is not mapped.
        JSONArray glyphs = (JSONArray) this.get("glyphs");
        List<String> l = new ArrayList<String>();
        try {
            int gameClass = charJSON.getInt("class");
            Map<Integer, String> glyphMap = Data.glyphMapByClass.get(gameClass);
            for (int i = 0; i<glyphs.length(); i++) {
                int glyphID = glyphs.getInt(i);
                String glyphString = glyphMap.get(glyphID);
                l.add((glyphString != null) ? glyphString : "_" + glyphID);
            }
        }
        catch (JSONException ignore) {}
        return l.toArray(new String[]{});
    }

    /**
     * Maps the buffs to their string values (those used by the engine). See that
     * some buffs map to an empty String since the engine does not support those
     * yet.
     * @return An Array of Strings.
     */
    public String[] buffs() {
        JSONArray buffs = (JSONArray) this.get("buffs");
        List<String> l = new ArrayList<String>();
        for (int i = 0; i<buffs.length(); i++) {
            try {
                l.add(Data.buffMap.get(buffs.getInt(i)));
            }
            catch (JSONException ignore) {}
        }
        return l.toArray(new String[]{});
    }

    /**
     * Finds in the JSON one fight setting and returns a value for it.
     * @param setting The string for the queried setting.
     * @return the value for the queried setting.
     */
    public Object fightSettings(String setting) {
        try {
            JSONObject settings = (JSONObject) charJSON.get("fightSettings");
            return settings.get(setting);
        }
        catch (JSONException e) {
            return null;
        }
    }

    /**
     * Finds in the JSON various fight settings.
     * @param args The strings for the queried settings.
     * @return A hash with all the settings requested.
     */
    public Map<String, Object> fightSettings(String... args) {
        Map<String, Object> settingsMap = new HashMap<String, Object>();
        JSONObject settings = null;
        try {
            settings = (JSONObject) charJSON.get("fightSettings");
        }
        catch (JSONException e) {
            return null;
        }
        for (String key : args) {
            try {
                settingsMap.put(key, settings.get(key));
            }
            catch (JSONException e) {
                settingsMap.put(key, null);
            }
        }
        return settingsMap;
    }

    /**
     * Finds in the JSON all the fight settings.
     * @return A hash with all the settings.
     */
    public Map<String, Object> fightSettings() {
        Map<String, Object> settingsMap = new HashMap<String, Object>();
        try {
            JSONObject settings = (JSONObject) charJSON.get("fightSettings");
            for (Iterator<?> iter =  settings.keys(); iter.hasNext();) {
                String key = (String) iter.next();
                settingsMap.put(key, settings.get(key));
            }
        }
        catch (JSONException ignore) {}
        return settingsMap;
    }

    /**
     * Finds in the JSON one cycle setting and returns a value for it. It will
     * only search in the current spec sub field.
     * @param setting The string for the queried setting.
     * @return the value for the queried setting.
     */
    public Object cycleSettings(String setting) {
        try {
            JSONObject settings = (JSONObject) charJSON.get("cycleSettings");
            JSONObject cycleSettings = (JSONObject) settings.get(this.specced());
            return cycleSettings.get(setting);
        }
        catch (JSONException e) {
            return null;
        }
    }

    /**
     * Finds in the JSON all the cycle settings for the current spec.
     * @return A hash with all the settings.
     */
    public Map<String, Object> cycleSettings() {
        Map<String, Object> settingsMap = new HashMap<String, Object>();
        JSONObject cycleSettings = null;
        try {
            JSONObject settings = (JSONObject) charJSON.get("cycleSettings");
            cycleSettings = (JSONObject) settings.get(this.specced());
            for (Iterator<?> iter =  cycleSettings.keys(); iter.hasNext();) {
                String key = (String) iter.next();
                settingsMap.put(key, cycleSettings.get(key));
            }
        }
        catch (JSONException e) {
            return null;
        }
        return settingsMap;
    }

    public Integer[] itemIDs() {
        JSONObject items = (JSONObject) this.get("items");
        List<Integer> l = new ArrayList<Integer>();
        for (String s : Data.itemMap.keySet()) {
            try {
                JSONObject item = items.getJSONObject(s);
                l.add(item.getInt("id"));
            }
            catch (JSONException ignore) {}
        }
        return l.toArray(new Integer[]{});
    }

    public Object itemInfo(String item, String info) {
        JSONObject items = (JSONObject) this.get("items");
        try {
            JSONObject queryItem = items.getJSONObject(item);
            return queryItem.get(info);
        }
        catch (JSONException e) {
            return null;
        }
    }

}
