package com.shadowcraft.android;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.util.Log;
import calcs.DamageCalculator;
public class CharHandler extends Activity{

    private String name;
    private String realm;
    private int gameClass;
    private int race;
    private int level;
    private String thumbnail;
    private List<Integer> professions = new ArrayList<Integer>();
    private List<String> talents = new ArrayList<String>();
    private Set<Integer> glyphs = new HashSet<Integer>();
    private Set<Integer> buffs = new HashSet<Integer>();
    private Map<String, HashMap<String, Object>> cycleSettings = new HashMap<String, HashMap<String, Object>>();
    private Map<String, Object> fightSettings = new HashMap<String, Object>();
    private JSONObject items;  // These are not final
    private Map<String, HashMap<String, Object>> itemStats;  // Info from the DB
    private Map<String, HashMap<String, Object>> charItems;  // Info from the snapshots (or bnet import)
    private DamageCalculator calculator;
    private DataBaseHelper dbHandler = getDbHandler();

    /**
     * Constructor. As it currently stands, this will attempt to get a stored
     * character in Bnet JSON style. Once we define how to store chars we should
     * change these constructor to function properly.
     * @param name The name of the character.
     * @param realm The realm of the character
     * @param region The region of the realm.
     */
    public CharHandler (String name, String realm, String region) {
        JSONObject cache = getCached(name, realm, region);
        if (cache == null) {
            String jsonString = Bnet.fetchChar(name, realm, region);
            JSONObject json = mkJSON(jsonString);
            try {json.get("name");}
            catch (JSONException e) {
                System.out.println(json);
                e.printStackTrace();
                // TODO handle nok errors and stop execution
            }
            populateFromBnetJSON(json);
            setDefault();
            buildModeler();
        }
        else {
            populateFromSnapshot(cache);
            buildModeler();
        }
        dbHandler.openDataBase();
        Map<String, Object> test =  dbHandler.getItem(52199);
        dbHandler.close();
        Log.v("ShadowCraft", " "+test.toString());
    }

    /**
     * Constructor overload. This will take a JSON string in the format we are
     * storing them. Note that this is to be used at run-time for now.
     * @param json Stored JSON string
     */
    public CharHandler (String json) {
        JSONObject charJSON = mkJSON(json);
        populateFromSnapshot(charJSON);
        buildModeler();
    }

    /**
     * Builds a string that can be parsed to JSON for easy storage of snapshots.
     * It will delete whitespace where possible. Notice that the talents field
     * needs special casing or it could be trimmed off leading zeros.
     */
    @Override
    public String toString() {
        class Strip {String strip(Object o) {
            return o.toString().replaceAll(" ", "");
        }}
        Strip s = new Strip();

        final StringBuilder sb = new StringBuilder();
        sb.append("{name:'").append(name).append('\'');
        sb.append(",realm:'").append(realm).append('\'');
        sb.append(",class:").append(gameClass);
        sb.append(",race:").append(race);
        sb.append(",level:").append(level);
        sb.append(",thumbnail:'").append(thumbnail).append('\'');
        sb.append(",professions:").append(s.strip(professions));
        sb.append(",glyphs:").append(s.strip(glyphs));
        sb.append(",talents:['");
        sb.append(talents.get(0)).append("','");
        sb.append(talents.get(1)).append("','");
        sb.append(talents.get(2)).append("']");
        sb.append(",items:").append(items);
        sb.append(",buffs:").append(s.strip(buffs));
        sb.append(",cycleSettings:").append(s.strip(cycleSettings));
        sb.append(",fightSettings:").append(s.strip(fightSettings));
        sb.append("}");
        return sb.toString();
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

    /**
     * Calls the appropriate back-end based on class
     * @return a DamageCalculator object to retrieve data from.
     */
    public DamageCalculator buildModeler() {
        if (this.isClass("rogue"))
            calculator = RogueBackend.build(this);
        else if (this.isClass(""))
            ;
        return calculator;
    }

    public DataBaseHelper getDbHandler() {
        dbHandler = new DataBaseHelper(this);
        try {
            dbHandler.createDataBase();
        }
        catch (IOException ioe) {
            throw new Error("Unable to create database");
        }
        dbHandler.close();
        return dbHandler;
        //try {
        //    dbHandler.openDataBase();
        //}
        //catch(SQLException sqle) {
        //    throw sqle;
        //}
    }

    // /////////////////////////////////////////////////////////////////////////
    // Initialize fields.
    // /////////////////////////////////////////////////////////////////////////

    public void populateFromBnetJSON(JSONObject charJSON) {
        try {
            name = charJSON.getString("name");
            realm = charJSON.getString("realm");
            gameClass = charJSON.getInt("class");
            race = charJSON.getInt("race");
            level = charJSON.getInt("level");
            thumbnail = charJSON.getString("thumbnail");
            items = charJSON.getJSONObject("items");
            setProfessionsFromJSON(charJSON);
            setTalentsFromJSON(charJSON);  // sets glyphs too
            setItemsFromJSON(charJSON);
            cleanItems();
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Parses the objects and arrays in the JSON input into java structures.
     * @param charJSON The JSON input.
     */
    public void populateFromSnapshot(JSONObject charJSON) {
        class Deserializer {
            void intArray(Collection<Integer> field, JSONArray a)
                    throws JSONException {
                for (int i = 0; i<a.length(); i++) {
                    field.add(a.getInt(i));
                }
            }
            void strArray(Collection<String> field, JSONArray a)
                    throws JSONException {
                for (int i = 0; i<a.length(); i++) {
                    field.add(a.getString(i));
                }
            }
            void objHash(Map<String, Object> field, JSONObject o)
                    throws JSONException {
                for(Iterator<?> it = o.keys(); it.hasNext();) {
                    String key = (String) it.next();
                    field.put(key, o.get(key));
                }
            }
        }
        Deserializer d = new Deserializer();

        try {
            name = charJSON.getString("name");
            realm = charJSON.getString("realm");
            gameClass = charJSON.getInt("class");
            race = charJSON.getInt("race");
            level = charJSON.getInt("level");
            thumbnail = charJSON.getString("thumbnail");
            items = charJSON.getJSONObject("items");
            d.intArray(professions, charJSON.getJSONArray("professions"));
            d.strArray(talents, charJSON.getJSONArray("talents"));
            d.intArray(glyphs, charJSON.getJSONArray("glyphs"));
            d.intArray(buffs, charJSON.getJSONArray("buffs"));
            d.objHash(fightSettings, charJSON.getJSONObject("fightSettings"));
            JSONObject cycleSettings = charJSON.getJSONObject("cycleSettings");
            JSONObject specSettings;
            HashMap<String, Object> specSettingsMap;
            for (Iterator<?> it = cycleSettings.keys(); it.hasNext();) {
                String spec = (String) it.next();
                specSettings = cycleSettings.getJSONObject(spec);
                specSettingsMap = new HashMap<String, Object>();
                for (Iterator<?> it2 = specSettings.keys(); it2.hasNext();) {
                    String setting = (String) it2.next();
                    specSettingsMap.put(setting, specSettings.get(setting));
                }
                this.cycleSettings.put(spec, specSettingsMap);
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setProfessionsFromJSON (JSONObject json) throws JSONException {
        JSONObject fetchedProfs = json.getJSONObject("professions");
        JSONArray primProfs = fetchedProfs.getJSONArray("primary");
        for (int i = 0; i<primProfs.length(); i++) {
            JSONObject prof = primProfs.getJSONObject(i);
            professions.add(prof.getInt("id"));
        }
    }

    public void setTalentsFromJSON (JSONObject json) throws JSONException {
        JSONArray talents = json.getJSONArray("talents");
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
            for (String s : new String[] {"prime", "minor", "major"}) {
                JSONArray glyphs = allGlyphs.getJSONArray(s);
                for (int j=0; j<glyphs.length(); j++) {
                    JSONObject glyph = glyphs.getJSONObject(j);
                    int a = glyph.getInt("item");
                    this.glyphs.add(a);
                }
            }

            JSONArray allTrees = build.getJSONArray("trees");
            for (int j=0; j<allTrees.length(); j++) {
                JSONObject spec = allTrees.getJSONObject(j);
                String specString = spec.getString("points");
                this.talents.add(specString);
            }
        }
    }

    public void cleanItems() {
        // TODO need to better format this field. Once we have the DB set up we
        // will start trimming this down.
        try {
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
     * Parse the items JSON into POJO. This will pup empty hashes on empty slots
     * and always create a gems fields, which can be an empty list. We use
     * multiple try/catch since not every field is always available.
     * @param json The JSON input form Bnet
     * @throws JSONException if the input doesn't make sense.
     */
    public void setItemsFromJSON(JSONObject json) throws JSONException {
        itemStats = new HashMap<String, HashMap<String, Object>>();
        charItems = new HashMap<String, HashMap<String, Object>>();
        JSONObject items = json.getJSONObject("items");
        for (String slot : Data.itemMap.keySet()) {
            if (slot.equals("shirt") || slot.equals("tabard"))
                continue;
            HashMap<String, Object> item = new HashMap<String, Object>();
            JSONObject itemJSON = items.getJSONObject(slot);

            // ID; not having this field implies the slot is empty, so we
            // populate with an empty hash and continue on to the next item.
            try {
                item.put("id", itemJSON.getInt("id"));
            }
            catch (JSONException e) {
                charItems.put(slot, item);
                continue;
            }

            // Extract the enchant and reforge fields if they exist.
            JSONObject params = itemJSON.getJSONObject("tooltipParams");
            for (String param : Arrays.asList("enchant", "reforge")) {
                try {
                    item.put(param, params.getInt(param));
                }
                catch (JSONException ignore) {}
            }

            // This ensures we always have a gem list in every item.
            // 0 indicates an empty slot.
            List<Integer> gems = new ArrayList<Integer>();
            gems.addAll(Arrays.asList(0, 0, 0));
            for (int i = 0; i<3; i++) {
                try {
                    gems.set(i, params.getInt("gem" + i));
                }
                catch (JSONException ignore) {}
            }
            // Remove trailing zeros in the gems field
            // TODO this should be done in toString()
            for (int i = 2; i>=0; i--) {
                if (gems.get(i) == 0)
                    gems.remove(i);
                else
                    break;
            }
            item.put("gems", gems);

            charItems.put(slot, item);
        }
    }



    // /////////////////////////////////////////////////////////////////////////
    // These functions append the default cycle and combat settings to newly
    // created characters.
    // /////////////////////////////////////////////////////////////////////////

    public void setDefault() {
        if (this.isClass("rogue")) {
            rogueDefault();
        }
        else if (this.isClass("otherClass"))
            otherClassDefault();
    }

    public void rogueDefault() {
        int[] buffArray = {1, 2, 4, 5, 6, 9, 15, 20, 22, 26, 27, 28, 30, 31};
        for (int i : buffArray) {
            buffs.add(i);
        }
        HashMap<String, Object> assassination = new HashMap<String, Object>();
        HashMap<String, Object> combat = new HashMap<String, Object>();
        HashMap<String, Object> subtlety = new HashMap<String, Object>();

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
    }

    public void otherClassDefault () {
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

    // /////////////////////////////////////////////////////////////////////////
    // Field getters
    // /////////////////////////////////////////////////////////////////////////

    /**
     * Maps the integer value of the class to its string value.
     * @return The string for one of the classes.
     */
    public String gameClass() {
        return Data.classMap.get(gameClass);
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
        return Data.raceMap.get(race);
    }

    /**
     * Level getter.
     * @return the level of the char.
     */
    public int level() {
        return level;
    }

    /**
     * Professions getter
     * @return The List with primary professions by ID.
     */
    public List<Integer> professionsIDs() {
        return professions;
    }

    /**
     * Maps the professions to their string value.
     * @return A list with primary professions; or an empty list if no prof is
     * present.
     */
    public List<String> professions() {
        List<String> profList = new ArrayList<String>();
        for (int i : professions) {
            profList.add(Data.professionsMap.get(i));
        }
        return profList;
    }

    /**
     * Converts the the talent list into an array
     * @return the 3 strings for the talents
     */
    public String[] talents() {
        return this.talents.toArray(new String[3]);
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
     * Converts the the glyph set into an array
     * @return An array of integers (not ordered)
     */
    public Integer[] glyphsIDs() {
        return this.glyphs.toArray(new Integer[]{});
    }

    /**
     * Maps the glyphs to their string values (those used by the engine). If no
     * map is found it will return the ID with preceding underscore.
     * @return An array of Strings (not ordered)
     */
    public String[] glyphs() {
        // this places _id if the glyph is not mapped.
        Set<String> glyphSet = new HashSet<String>();
        Map<Integer, String> glyphMap = Data.glyphMapByClass.get(gameClass);
        for (int id : glyphs) {
            String glyphString = glyphMap.get(id);
            glyphSet.add((glyphString != null) ? glyphString : "_" + id);
        }
        return glyphSet.toArray(new String[]{});
    }

    /**
     * Maps the buffs to their string values (those used by the engine). See that
     * some buffs map to an empty String since the engine does not support those
     * yet.
     * @return An Array of Strings.
     */
    public String[] buffs() {
        List<String> l = new ArrayList<String>();
        for (int i : this.buffs) {
            l.add(Data.buffMap.get(i));
        }
        return l.toArray(new String[]{});
    }

    /**
     * Finds one fight setting and returns a value for it.
     * @param setting The string for the queried setting.
     * @return the value for the queried setting.
     */
    public Object fightSettings(String setting) {
        return fightSettings.get(setting);
    }

    /**
     * Finds in the JSON various fight settings.
     * @param args The strings for the queried settings.
     * @return A hash with all the settings requested.
     */
    public Map<String, Object> fightSettings(String... args) {
        Map<String, Object> settingsMap = new HashMap<String, Object>();
        for (String key : args) {
            settingsMap.put(key, fightSettings.get(key));
        }
        return settingsMap;
    }

    /**
     * fightSettings getter
     * @return A hash with all the settings.
     */
    public Map<String, Object> fightSettings() {
        return fightSettings;
    }

    /**
     * Finds one cycle setting and returns a value for it. It will only search in
     * the current spec sub field.
     * @param setting The string for the queried setting.
     * @return the value for the queried setting.
     */
    public Object cycleSettings(String setting) {
        return cycleSettings.get(specced()).get(setting);
    }

    /**
     * Finds all the cycle settings for the current spec.
     * @return A hash with all the settings.
     */
    public Map<String, Object> cycleSettings() {
        return cycleSettings.get(specced());
    }

    public Integer[] itemIDs() {
        JSONObject items = this.items;
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
        JSONObject items = this.items;
        try {
            JSONObject queryItem = items.getJSONObject(item);
            return queryItem.get(info);
        }
        catch (JSONException e) {
            return null;
        }
    }

    // /////////////////////////////////////////////////////////////////////////
    // Modeler getters. Here lie some of the main methods from the modeler.
    // /////////////////////////////////////////////////////////////////////////

    /**
     * If we want to access the modeler directly we can return the modeler
     * object.
     * 
     * @return A DamageCaldualtor object initialized with our variables.
     */
    public DamageCalculator getDamageCalculator() {
        return calculator;
    }

    /**
     * Calls the get_dps method of the modeler.
     * 
     * @return The dps.
     */
    public double getDPS() {
        return calculator.get_dps();
    }
}
