package com.shadowcraft.android;

import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;

import rogue.AldrianasRogueDamageCalculator;
import rogue.Cycle;
import rogue.RogueGlyphs;
import rogue.RogueTalents;
import rogue.Settings;
import calcs.DamageCalculator;
import classes.Buffs;
import classes.GearBuffs;
import classes.Glyphs;
import classes.ProcsList;
import classes.Race;
import classes.Stats;
import classes.Talents;
import classes.Weapon;

public class BuildModeler {

    public JSONArray getCharJSON(String name, String realm, String region) {
        JSONArray json = null;
        JSONArray cache = getCached(name, realm, region);
        if (cache != null) {
            return cache;
        }
        else {
            String JSONString = Bnet.fetchChar(name, realm, region);
            json = mkJSON(JSONString);
            // add default settings.
        }

        return json;
    }

    public JSONArray getCached(String name, String realm, String region) {
        // define a way to retrieve cached characters.
        return null;
    }

    public JSONArray mkJSON(String jsonString) {
        JSONArray jsonArray = null;
        try {
            jsonArray = new JSONArray(jsonString);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonArray;
    }

    public void addDefault() {

    }

    public DamageCalculator Build(JSONArray json) {
        return null;
    }

    public DamageCalculator Build() {
        // Using the script values for the moment.

        // Set up buffs.
        String[] buff_list = new String[] {
                "short_term_haste_buff",
                "stat_multiplier_buff",
                "crit_chance_buff",
                "all_damage_buff",
                "melee_haste_buff",
                "attack_power_buff",
                "str_and_agi_buff",
                "armor_debuff",
                "physical_vulnerability_debuff",
                "spell_damage_debuff",
                "spell_crit_debuff",
                "bleed_damage_debuff",
                "agi_flask",
                "guild_feast"
        };
        Buffs raid_buffs = new Buffs(buff_list);

        // Set up weapons.
        Weapon mh =     new Weapon(  1121, 1.8, "dagger", "landslide");
        Weapon oh =     new Weapon(   872, 1.4, "dagger", "landslide");
        Weapon ranged = new Weapon(1679.5, 2.0, "thrown");

        // Set up procs.
        ProcsList procs = new ProcsList("heroic_the_hungerer", "heroic_matrix_restabilizer");

        // Set up gear buffs.
        GearBuffs gear_buffs = new GearBuffs("rogue_t12_2pc", "rogue_t12_4pc", "leather_specialization", "potion_of_the_tolvir", "chaotic_metagem");

        // Set up a calcs object.
        HashMap<String, Object> stat_hash = new HashMap<String, Object>();
        stat_hash.put("str",        new Float(20));
        stat_hash.put("agi",        new Float(6248));
        stat_hash.put("ap",         new Float(190));
        stat_hash.put("crit",       new Float(624));
        stat_hash.put("hit",        new Float(1331));
        stat_hash.put("exp",        new Float(297));
        stat_hash.put("haste",      new Float(1719));
        stat_hash.put("mastery",    new Float(2032));
        stat_hash.put("mh",         mh);
        stat_hash.put("oh",         oh);
        stat_hash.put("ranged",     ranged);
        stat_hash.put("procs",      procs);
        stat_hash.put("gear_buffs", gear_buffs);

        Stats stats = new Stats(stat_hash);

        // Initialize talents
        Talents talents = new RogueTalents("0333230113022110321", "0020000000000000000", "2030030000000000000");
        /* Talents test_talents = new RogueTalents("033323011302211032100200000000000000002030030000000000000"); */


        // Set up glyphs.
        Glyphs glyphs = new RogueGlyphs("backstab", "mutilate", "rupture", "tricks_of_the_trade");

        // Set up race.
        Race race = new Race("night_elf", "rogue");

        // Set up cycle.
        /*
         * HashMap<String, Object> cycle_hash = new HashMap<String, Object>();
         * cycle_hash.put("min_envenom_size_mutilate", new Integer (4));
         * cycle_hash.put("min_envenom_size_backstab", new Integer (5));
         * cycle_hash.put("prioritize_rupture_uptime_mutilate", new Boolean
         * (true)); cycle_hash.put("prioritize_rupture_uptime_backstab", new
         * Boolean (true));
         */
        Cycle cycle = new Cycle.AssassinationCycle();

        // Set up settings.
        HashMap<String, Object> settings_hash = new HashMap<String, Object>();
        settings_hash.put("cycle",          cycle);
        settings_hash.put("response_time",  new Double(1));
        settings_hash.put("duration",       new Double(360));
        /*
         * settings_hash.put("time_in_execute_range", new Float (.35));
         * settings_hash.put("tricks_on_cooldown", new Boolean (true));
         * settings_hash.put("mh_poison", new String ("ip"));
         * settings_hash.put("oh_poison", new String ("dp"));
         */
        Settings settings = new Settings(settings_hash);

        // Set up level
        int level = 85;

        // Build a DPS object.
        DamageCalculator calculator = new AldrianasRogueDamageCalculator(
                stats, talents, glyphs, raid_buffs, race,
                settings, level);

        return calculator;
    }
}
