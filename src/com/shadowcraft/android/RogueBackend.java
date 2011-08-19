package com.shadowcraft.android;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

public class RogueBackend {

    @SuppressWarnings("serial")
    private static Map<Integer, String> procMap = new HashMap<Integer, String>(){{
        put(58181, "fluid_death");
        put(56295, "heroic_grace_of_the_herald");
        put(55266, "grace_of_the_herald");
        put(56328, "heroic_key_to_the_endless_chamber");
        put(56427, "heroic_left_eye_of_rajh");
        put(65026, "heroic_prestors_talisman_of_machination");
        put(56394, "heroic_tias_grace");
        put(62049, "darkmoon_card_hurricane");
        put(62051, "darkmoon_card_hurricane");
        put(59473, "essence_of_the_cyclone");
        put(65140, "heroic_essence_of_the_cyclone");
        put(66969, "heart_of_the_vile");
        put(55795, "key_to_the_endless_chamber");
        put(56102, "left_eye_of_rajh");
        put(59441, "prestors_talisman_of_machination");
        put(68163, "the_twilight_blade");
        put(55874, "tias_grace");
        put(59520, "unheeded_warning");
        put(71633, "aellas_bottle");
        put(68994, "matrix_restabilizer");
        put(69150, "heroic_matrix_restabilizer");
        put(71335, "corens_chilled_chromium_coaster");
        put(66969, "heart_of_the_vile");
        put(65805, "schnottz_medallion_of_command");
        put(68927, "the_hungerer");
        put(69112, "heroic_the_hungerer");
        put(70144, "rickets_magnetic_fireball_proc");
    }};

    @SuppressWarnings("serial")
    private static Map<Integer, String> gearBuffsMap = new HashMap<Integer, String>(){{
        put(56115, "skardyns_grace");
        put(56440, "heroic_skardyns_grace");
        put(68709, "unsolvable_riddle");
        put(62468, "unsolvable_riddle");
        put(62463, "unsolvable_riddle");
        put(52199, "demon_panther");
        put(69199, "heroic_ancient_petrified_seed");
        put(69001, "ancient_petrified_seed");
        put(70144, "rickets_magnetic_fireball");
    }};

    private static Set<Integer> tier11IDs = new HashSet<Integer>(Arrays.asList(
            60298, 65240, 60299, 65241, 60300, 65242, 60302, 65243, 60301, 65239));
    private static Set<Integer> tier12IDs = new HashSet<Integer>(Arrays.asList(
            71046, 71538, 71047, 71539, 71048, 71540, 71049, 71541, 71045, 71537));
    private static Set<Integer> PVPSetIDs = new HashSet<Integer>(Arrays.asList(
            60458, 60459, 60460, 60461, 60462, 64769, 64770, 64771, 64772, 64773,
            65545, 65546, 65547, 65548, 65549));


    public static DamageCalculator build(CharJSONHandler input) {
        String specced = input.specced();
        List<String> professions = input.professions();
        Integer[] itemIDs = input.itemIDs();
        Set<Integer> itemIDsSet = new HashSet<Integer>(Arrays.asList(itemIDs));
        Set<Integer> tier11Pieces = new HashSet<Integer>(itemIDsSet);
        tier11Pieces.retainAll(tier11IDs);
        Set<Integer> tier12Pieces = new HashSet<Integer>(itemIDsSet);
        tier12Pieces.retainAll(tier12IDs);
        Set<Integer> arenaPieces = new HashSet<Integer>(itemIDsSet);
        arenaPieces.retainAll(PVPSetIDs);

        // Set up buffs.
        Buffs raid_buffs = new Buffs(input.buffs());

        // Set up weapons. TODO
        Weapon mh =     new Weapon(  1121, 1.8, "dagger", "landslide");
        Weapon oh =     new Weapon(   872, 1.4, "dagger", "landslide");
        Weapon ranged = new Weapon(1679.5, 2.0, "thrown");

        // Set up procs.
        Set<Integer> procsIDs = new HashSet<Integer>(itemIDsSet);
        procsIDs.retainAll(procMap.keySet());
        Set<String> procsSet = new HashSet<String>();
        for (int i : procsIDs) {
            procsSet.add(procMap.get(i));
        }
        if (tier11Pieces.size() >= 4) {
            procsSet.add("rogue_t11_4pc");
        }
        ProcsList procs = new ProcsList(procsSet);

        // Set up gear buffs.
        Set<Integer> gearBuffsIDs = new HashSet<Integer>(itemIDsSet);
        gearBuffsIDs.retainAll(gearBuffsMap.keySet());
        Set<String> gearBuffsSet = new HashSet<String>();
        for (int i : gearBuffsIDs) {
            procsSet.add(gearBuffsMap.get(i));
        }
        gearBuffsSet.add("leather_specialization");  // defaulted
        if (tier11Pieces.size() >= 2)
            gearBuffsSet.add("rogue_t11_2pc");
        if (tier12Pieces.size() >= 2)
            gearBuffsSet.add("rogue_t12_2pc");
        if (tier12Pieces.size() >= 4)
            gearBuffsSet.add("rogue_t12_4pc");
        if ((Boolean) input.fightSettings("pre_pot"))
            gearBuffsSet.add("potion_of_the_tolvir_prepot");
        if ((Boolean) input.fightSettings("combat_pot"))
            gearBuffsSet.add("potion_of_the_tolvir");
        if (professions.contains("alchemy"))
            gearBuffsSet.add("mixology");
        if (professions.contains("herbalism"))
            gearBuffsSet.add("lifeblood");
        if (professions.contains("skinning"))
            gearBuffsSet.add("master_of_anatomy");
        if (professions.contains("engineering"))
            gearBuffsSet.add("synapse_springs");
        //TODO chaotic metaGem
        GearBuffs gear_buffs = new GearBuffs(gearBuffsSet);

        // Set up a calcs object.
        //TODO numeric stats
        HashMap<String, Object> stat_hash = new HashMap<String, Object>();
        stat_hash.put("str",        new Float(20));
        stat_hash.put("agi",        new Float(6248));
        stat_hash.put("ap",         new Float(190));
        stat_hash.put("crit",       new Float(624));
        stat_hash.put("hit",        new Float(1331));
        stat_hash.put("exp",        new Float(297));
        stat_hash.put("haste",      new Float(1719));
        stat_hash.put("mastery",    new Float(2032));
        stat_hash.put("mh",         mh);  //TODO
        stat_hash.put("oh",         oh);  //TODO
        stat_hash.put("ranged",     ranged);  //TODO
        stat_hash.put("procs",      procs);
        stat_hash.put("gear_buffs", gear_buffs);
        Stats stats = new Stats(stat_hash);

        // Initialize talents
        Talents talents = new RogueTalents(input.talents());

        // Set up glyphs.
        Glyphs glyphs = new RogueGlyphs(input.glyphs());

        // Set up race.
        Race race = new Race(input.race(), input.gameClass());

        // Set up cycle.
        Map<String, Object> cycleSettingsHash = input.cycleSettings();
        Cycle cycle = null;
        if (specced.equals("assassination"))
            cycle = new Cycle.AssassinationCycle(cycleSettingsHash);
        else if (specced.equals("combat"))
            cycle = new Cycle.CombatCycle(cycleSettingsHash);
        else if (specced.equals("subtlety"))
            cycle = new Cycle.SubtletyCycle(cycleSettingsHash);

        // Set up settings.
        String[] settingsQuery = null;
        if (specced.equals("assassination")) {
            settingsQuery = new String[] {"mh_poison", "oh_poison", "duration",
                    "response_time", "tricks_on_cooldown", "time_in_execute_range"
            };
        }
        else {
            settingsQuery = new String[] {"mh_poison", "oh_poison", "duration",
                    "response_time", "tricks_on_cooldown"
            };
        }
        Map<String, Object> settingsHash = input.fightSettings(settingsQuery);
        settingsHash.put("cycle", cycle);
        // This is a bit haxy but it does the job: the engine must take doubles
        // for these values, but JSON trims zero-float doubles to integers.
        String[] forceDbl = {"duration", "response_time", "time_in_execute_range"};
        double coerced;
        for (String setting : forceDbl) {
            try {
                coerced = (Double) settingsHash.get(setting);
            }
            catch (ClassCastException e){
                coerced = 1.0 * (Integer) settingsHash.get(setting);
                settingsHash.put(setting, coerced);
            }
        }
        Settings settings = new Settings(settingsHash);

        // Set up level
        int level = input.level();

        // Build a DPS object.
        DamageCalculator calculator = new AldrianasRogueDamageCalculator(
                stats, talents, glyphs, raid_buffs, race,
                settings, level);

        return calculator;

    }

}
