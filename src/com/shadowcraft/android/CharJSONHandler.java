package com.shadowcraft.android;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CharJSONHandler {

    @SuppressWarnings("serial")
    private static Map<Integer, String> raceMap = new HashMap<Integer, String>(){{
        put( 1, "human");
        put( 2, "orc");
        put( 3, "dwarf");
        put( 4, "night_elf");
        put( 5, "undead");
        put( 6, "tauren");
        put( 7, "gnome");
        put( 8, "troll");
        put( 9, "goblin");
        put(10, "blood_elf");
        put(11, "draenei");
        put(22, "worgen");
    }};

    @SuppressWarnings("serial")
    private static Map<Integer, String> classMap = new HashMap<Integer, String>(){{
        put( 1, "warrior");
        put( 2, "paladin");
        put( 3, "hunter");
        put( 4, "rogue");
        put( 5, "priest");
        put( 6, "death_knight");
        put( 7, "shaman");
        put( 8, "mage");
        put( 9, "warlock");
        put(11, "druid");
    }};

    @SuppressWarnings("serial")
    private static Map<String, Integer> itemMap = new HashMap<String, Integer>(){{
        put("head",     0);
        put("neck",     1);
        put("shoulder", 2);
        put("back",     14);
        put("chest",    4);
        put("wrist",    8);
        put("hands",    9);
        put("waist",    5);
        put("legs",     6);
        put("feet",     7);
        put("finger1",  10);
        put("finger2",  11);
        put("trinket1", 12);
        put("trinket2", 13);
        put("mainHand", 15);
        put("offHand",  16);
        put("ranged",   17);
    }};

    @SuppressWarnings("serial")
    private static Map<String, String[]> specMap = new HashMap<String, String[]>(){{
        put("warrior",      new String[] {"arms", "fury", "protection"});
        put("paladin",      new String[] {"holy", "protection", "retribution"});
        put("hunter",       new String[] {"beast_mastery", "marksmanship", "survival"});
        put("rogue",        new String[] {"assassination", "combat", "subtlety"});
        put("priest",       new String[] {"discipline", "holy", "shadow"});
        put("death_knight", new String[] {"blood", "frost", "unholy"});
        put("shaman",       new String[] {"elemental", "enhancement", "restoration"});
        put("mage",         new String[] {"arcane", "fire", "frost"});
        put("warlock",      new String[] {"affliction", "demonology", "destruction"});
        put("druid",        new String[] {"balance", "feral_combat", "restoration"});
    }};

    @SuppressWarnings("serial")
    private static Map<Integer, String> buffMap = new HashMap<Integer, String>(){{
        put( 1, "short_term_haste_buff");           // BL
        put( 2, "str_and_agi_buff");                // SoE
        put( 3, "");                                // armor
        put( 4, "attack_power_buff");               // BoM
        put( 5, "crit_chance_buff");                // LotP
        put( 6, "all_damage_buff");                 // ArcTac
        put( 7, "");                                // ArcInt
        put( 8, "");                                // CasterBoM
        put( 9, "melee_haste_buff");                // WFury
        put(10, "");                                // DmgReduction
        put(11, "");                                // PushbackProtection
        put(12, "");                                // SpellHaste
        put(13, "");                                // SpellPower
        put(14, "");                                // Stamina
        put(15, "stat_multiplier_buff");            // BoK
        put(20, "armor_debuff");                    // Sunder
        put(21, "");                                // AttackSpeedSlow
        put(22, "bleed_damage_debuff");             // Mangle
        put(23, "");                                // CastSpeedSlow
        put(24, "");                                // MortalStrike
        put(25, "");                                // DemoShout
        put(26, "physical_vulnerability_debuff");   // Savage Combat
        put(27, "spell_crit_debuff");               // CritMass
        put(28, "spell_damage_debuff");             // CoE
        put(30, "guild_feast");                     // Food
        put(31, "agi_flask");                       // Flask
        put(40, "");                                // Replenishment
        put(41, "");                                // Spell resist
    }};

    @SuppressWarnings("serial")
    private static Map<Integer, String> glyphMap = new HashMap<Integer, String>(){{
        put(45766, "Fan of Knives");
        put(45767, "Tricks of the Trade");
        put(45761, "Vendetta");
        put(45762, "Killing Spree");
        put(45764, "Shadow Dance");
        put(42971, "Kick");
        put(42959, "Deadly Throw");
        put(45769, "Cloak of Shadows");
        put(42954, "Adrenaline Rush");
        put(42968, "Preparation");
        put(43378, "Safe Fall");
        put(42969, "Rupture");
        put(42963, "Feint");
        put(42964, "Garrote");
        put(42962, "Expose Armor");
        put(64493, "Blind");
        put(42965, "Revealing Strike");
        put(42967, "Hemorrhage");
        put(43376, "Distract");
        put(42955, "Ambush");
        put(42956, "Backstab");
        put(42957, "Blade Flurry");
        put(42958, "Crippling Poison");
        put(42960, "Evasion");
        put(42961, "Eviscerate");
        put(42966, "Gouge");
        put(42970, "Sap");
        put(42972, "Sinister Strike");
        put(42973, "Slice and Dice");
        put(42974, "Sprint");
        put(43343, "Pick Pocket");
        put(43377, "Pick Lock");
        put(43379, "Blurred Speed");
        put(43380, "Poisons");
        put(45768, "Mutilate");
        put(63420, "Vanish");
    }};



    private JSONObject charJSON;  // We could extract its values to fields.

    public CharJSONHandler (String name, String realm, String region) {
        this.charJSON = fetchChar(name, realm, region);
    }

    public CharJSONHandler (String json) {
        this.charJSON = mkJSON(json);
    }

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
            json = cleanCharJSON(json);
            json = rogueDefault(json);
        }

        return json;
    }

    public JSONObject rogueDefault(JSONObject json) {
        int[] defBuffs = {1, 2, 4, 5, 6, 9, 15, 20, 22, 26, 27, 28, 30, 31};
        try {
            json.put("buffs", defBuffs);
        }
        catch (JSONException ignore) {}
        return json;
    }

    public JSONObject cleanCharJSON(JSONObject json) {
        json.remove("lastModified");
        json.remove("gender");
        json.remove("achievementPoints");
        // json.remove("thumbnail");  // catch and use?
        // json.remove("name");
        // json.remove("realm");

        try {
            // this places nulls if no profession is retrieved.
            JSONObject profs =  json.getJSONObject("professions");
            JSONArray primProfs = profs.getJSONArray("primary");
            String [] profsStrings = new String[2];
            for (int i = 0; i<primProfs.length(); i++) {
                JSONObject prof = primProfs.getJSONObject(i);
                String profString = prof.getString("name");
                profsStrings[i] = profString.toLowerCase();
            }
            json.put("professions", profsStrings);

            // glyphs and talents come in the same field.
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
                List<Integer> glyphsIds = new ArrayList<Integer>();
                for (String s : new String[] {"prime","minor","major"}) {
                    JSONArray glyphs = allGlyphs.getJSONArray(s);
                    for (int j=0; j<glyphs.length(); j++) {
                        JSONObject glyph = glyphs.getJSONObject(j);
                        int a = glyph.getInt("item");
                        glyphsIds.add(a);
                    }
                }

                JSONArray allTrees = build.getJSONArray("trees");
                String[] talentStrings = new String[3];
                for (int j=0; j<allTrees.length(); j++) {
                    JSONObject spec = allTrees.getJSONObject(j);
                    String specString = spec.getString("points");
                    talentStrings[j] = specString;
                }

                // json.put("fetchedGLyphs_" + (i + 1), glyphsIds);
                // json.put("fetchedTalents_" + (i + 1), talentStrings);
                json.put("glyphs", glyphsIds);
                json.put("talents", talentStrings);
            }

            JSONObject items = json.getJSONObject("items");
            items.remove("averageItemLevel");
            items.remove("averageItemLevelEquipped");
            items.remove("tabard");
            items.remove("shirt");

            for (String slot : itemMap.keySet()) {
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

        return json;
    }

    public JSONObject getCached(String name, String realm, String region) {
        // define a way to retrieve cached characters.
        return null;
    }

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

    // general getters

    public Object get(String name) {
        try {
            return this.charJSON.get(name);
        }
        catch (JSONException ignore) {}
        return null;
    }

    public String getString(String name) {
        try {
            return this.charJSON.getString(name);
        }
        catch (JSONException ignore) {}
        return null;
    }

    public int getInt(String name) {
        try {
            return this.charJSON.getInt(name);
        }
        catch (JSONException ignore) {}
        return 0;
    }

    public String[] getArray(String name) {
        JSONArray a = (JSONArray) this.get(name);
        List<String> l = new ArrayList<String>();
        for (int i = 0; i<a.length(); i++) {
            try {
                l.add(a.getString(i));
            }
            catch (JSONException ignore) {}
        }
        return l.toArray(new String[]{});
    }

    // particular getters

    public String gameClass() {
        return classMap.get(this.getInt("class"));
    }

    public boolean isClass(String gameClass) {
        return gameClass().equals(gameClass);
    }

    public String race() {
        return raceMap.get(this.getInt("race"));
    }

    public int level() {
        return this.getInt("level");
    }

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
            return null;
        if (totalSpent <= 41 && totalSpent > 0)
            return specMap.get(gameClass())[maxSpec];
        return null;
    }

    public Boolean isSpecced(String spec) {
        return specced().equals(spec);
    }

    public Integer[] glyphsIDs() {
        JSONArray glyphs = (JSONArray) this.get("glyphs");
        List<Integer> l = new ArrayList<Integer>();
        for (int i = 0; i<glyphs.length(); i++) {
            try {
                l.add(glyphs.getInt(i));
            }
            catch (JSONException ignore) {}
        }
        return l.toArray(new Integer[]{});
    }

    public String[] glyphs() {
        // this places nulls if the glyph is not mapped.
        JSONArray glyphs = (JSONArray) this.get("glyphs");
        List<String> l = new ArrayList<String>();
        for (int i = 0; i<glyphs.length(); i++) {
            try {
                l.add(glyphMap.get(glyphs.getInt(i)));
            }
            catch (JSONException ignore) {}
        }
        return l.toArray(new String[]{});
    }

    public String[] buffs() {
        JSONArray buffs = (JSONArray) this.get("buffs");
        List<String> l = new ArrayList<String>();
        for (int i = 0; i<buffs.length(); i++) {
            try {
                l.add(buffMap.get(buffs.getInt(i)));
            }
            catch (JSONException ignore) {}
        }
        return l.toArray(new String[]{});
    }

    public Integer[] itemIDs() {
        JSONObject items = (JSONObject) this.get("items");
        List<Integer> l = new ArrayList<Integer>();
        for (String s : itemMap.keySet()) {
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
