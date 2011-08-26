package com.shadowcraft.android;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Data {
    @SuppressWarnings("serial")
    static final Map<Integer, String> raceMap = new HashMap<Integer, String>(){{
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
    static final Map<Integer, String> classMap = new HashMap<Integer, String>(){{
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
    static final Map<String, Integer> itemMap = new HashMap<String, Integer>(){{
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
    static final Map<Integer, String> invMap = new HashMap<Integer, String>(){{
        // this is the inventory type field from Bnet
        put( 0, null);
        put( 1, "head");
        put( 2, "neck");
        put( 3, "shoulder");
        put( 4, "shirt");
        put( 5, "chest");
        put( 6, "waist");
        put( 7, "legs");
        put( 8, "feet");
        put( 9, "wrist");
        put(10, "hands");
        put(11, "finger");
        put(12, "trinket");
        put(13, "one_hand");
        put(14, "shield");
        put(15, "ranged");
        put(16, "cloak");
        put(17, "two_hand");
        put(18, "bag");
        put(19, "tabard");
        put(20, "robe");
        put(21, "main_hand");
        put(22, "off_hand");
        put(23, "held_in_off_hand");
        put(24, "ammo");
        put(25, "thrown");
        put(26, "ranged_right");
        put(28, "relic");
    }};

    @SuppressWarnings("serial")
    static final Map<String, String[]> specMap = new HashMap<String, String[]>(){{
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
    static final Map<Integer, String> buffMap = new HashMap<Integer, String>(){{
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

    //    static final Map<Integer, String> warriorGlyphMap = new HashMap<Integer, String>();
    //    static final Map<Integer, String> paladinGlyphMap = new HashMap<Integer, String>();
    //    static final Map<Integer, String> hunterGlyphMap = new HashMap<Integer, String>();
    //    static final Map<Integer, String> priestGlyphMap = new HashMap<Integer, String>();
    //    static final Map<Integer, String> deathKnightGlyphMap = new HashMap<Integer, String>();
    //    static final Map<Integer, String> shamanGlyphMap = new HashMap<Integer, String>();
    //    static final Map<Integer, String> mageGlyphMap = new HashMap<Integer, String>();
    //    static final Map<Integer, String> warlockGlyphMap = new HashMap<Integer, String>();
    //    static final Map<Integer, String> druidGlyphMap = new HashMap<Integer, String>();

    @SuppressWarnings("serial")
    static final Map<Integer, String> rogueGlyphMap = new HashMap<Integer, String>(){{
        put(45766, "fan_of_knives");
        put(45767, "tricks_of_the_trade");
        put(45761, "vendetta");
        put(45762, "killing_spree");
        put(45764, "shadow_dance");
        put(42971, "kick");
        put(42959, "deadly_throw");
        put(45769, "cloak_of_shadows");
        put(42954, "adrenaline_rush");
        put(42968, "preparation");
        put(43378, "safe_fall");
        put(42969, "rupture");
        put(42963, "feint");
        put(42964, "garrote");
        put(42962, "expose_armor");
        put(64493, "blind");
        put(42965, "revealing_strike");
        put(42967, "hemorrhage");
        put(43376, "distract");
        put(42955, "ambush");
        put(42956, "backstab");
        put(42957, "blade_flurry");
        put(42958, "crippling_poison");
        put(42960, "evasion");
        put(42961, "eviscerate");
        put(42966, "gouge");
        put(42970, "sap");
        put(42972, "sinister_strike");
        put(42973, "slice_and_dice");
        put(42974, "sprint");
        put(43343, "pick_pocket");
        put(43377, "pick_lock");
        put(43379, "blurred_speed");
        put(43380, "poisons");
        put(45768, "mutilate");
        put(63420, "vanish");
    }};

    @SuppressWarnings("serial")
    static final List<Map<Integer, String>> glyphMapByClass = new ArrayList<Map<Integer, String>>(){{
        // Initialize all items to a new hash; it could be null though, but I'm
        // keeping it so we can test with other classes other than rogues. Size
        // is 11 because, for some reason, druids are not the 10th class; thus,
        // positions 0 and 10 are null;
        for(int i = 0; i<11; i++) {
            boolean check = classMap.containsKey(i);
            add(i, (!check) ? null : new HashMap<Integer, String>());
        }
        add(4, rogueGlyphMap);
    }};

    @SuppressWarnings("serial")
    static final Map<Integer, String> BnetStatsMap = new HashMap<Integer, String>(){{
        // put( 1, "health"});
        put( 3, "agi");      // "agility"
        put( 4, "str");      // "strength"
        put( 5, "int");      // "intellect"
        put( 6, "spi");      // "spirit"
        put( 7, "sta");      // "stamina"
        put(31, "hit");      // "hit_rating"
        put(32, "crit");     // "crit_rating"
        put(35, "resi");     // "resilience_rating"
        put(36, "haste");    // "haste_rating"
        put(37, "exp");      // "expertise_rating"
        put(38, "ap");       // "attack_power"
        put(45, "sp");       // "spell_power"
        put(49, "mastery");  // "mastery_rating"
        // 12, "defense_skill_rating"
        // 13, "dodge_rating"
        // 14, "parry_rating"
        // 15, "block_rating"
        // 16, "hit_melee_rating"
        // 17, "hit_ranged_rating"
        // 18, "hit_spell_rating"
        // 19, "crit_melee_rating"
        // 20, "crit_ranged_rating"
        // 21, "crit_spell_rating"
        // 22, "hit_melee_taken_rating"
        // 23, "hit_ranged_taken_rating"
        // 24, "hit_spell_taken_rating"
        // 25, "crit_melee_taken_rating"
        // 26, "crit_ranged_taken_rating"
        // 27, "crit_spell_taken_rating"
        // 28, "haste_melee_rating"
        // 29, "haste_ranged_rating"
        // 30, "haste_spell_rating"
        // 33, "hit_taken_rating"
        // 34, "crit_taken_rating"
        // 39, "ranged_attack_power"
        // 40, "feral_attack_power"
        // 41, "spell_healing_done"
        // 42, "spell_damage_done"
        // 43, "mana_regeneration"
        // 44, "armor_penetration_rating"
        // 46, "health_regeneration"
        // 47, "spell_penetration"
        // 48, "block_value"
        // 50, "extra_armor"
        // 51, "fire_resistance"
        // 52, "frost_resistance"
        // 54, "shadow_resistance"
        // 55, "nature_resistance"
        // 56, "arcane_resistance"
    }};

    @SuppressWarnings("serial")
    static final Map<Integer, String[]> reforgeMap = new HashMap<Integer, String[]>(){{
        put(113, new String[] {"spi",       "dodge"});
        put(114, new String[] {"spi",       "parry"});
        put(115, new String[] {"spi",       "hit"});
        put(116, new String[] {"spi",       "crit"});
        put(117, new String[] {"spi",       "haste"});
        put(118, new String[] {"spi",       "exp"});
        put(119, new String[] {"spi",       "mastery"});
        put(120, new String[] {"dodge",     "spi"});
        put(121, new String[] {"dodge",     "parry"});
        put(122, new String[] {"dodge",     "hit"});
        put(123, new String[] {"dodge",     "crit"});
        put(124, new String[] {"dodge",     "haste"});
        put(125, new String[] {"dodge",     "exp"});
        put(126, new String[] {"dodge",     "mastery"});
        put(127, new String[] {"parry",     "spi"});
        put(128, new String[] {"parry",     "dodge"});
        put(129, new String[] {"parry",     "hit"});
        put(130, new String[] {"parry",     "crit"});
        put(131, new String[] {"parry",     "haste"});
        put(132, new String[] {"parry",     "exp"});
        put(133, new String[] {"parry",     "mastery"});
        put(134, new String[] {"hit",       "spi"});
        put(135, new String[] {"hit",       "dodge"});
        put(136, new String[] {"hit",       "parry"});
        put(137, new String[] {"hit",       "crit"});
        put(138, new String[] {"hit",       "haste"});
        put(139, new String[] {"hit",       "exp"});
        put(140, new String[] {"hit",       "mastery"});
        put(141, new String[] {"crit",      "spi"});
        put(142, new String[] {"crit",      "dodge"});
        put(143, new String[] {"crit",      "parry"});
        put(144, new String[] {"crit",      "hit"});
        put(145, new String[] {"crit",      "haste"});
        put(146, new String[] {"crit",      "exp"});
        put(147, new String[] {"crit",      "mastery"});
        put(148, new String[] {"haste",     "spi"});
        put(149, new String[] {"haste",     "dodge"});
        put(150, new String[] {"haste",     "parry"});
        put(151, new String[] {"haste",     "hit"});
        put(152, new String[] {"haste",     "crit"});
        put(153, new String[] {"haste",     "exp"});
        put(154, new String[] {"haste",     "mastery"});
        put(155, new String[] {"exp",       "spi"});
        put(156, new String[] {"exp",       "dodge"});
        put(157, new String[] {"exp",       "parry"});
        put(158, new String[] {"exp",       "hit"});
        put(159, new String[] {"exp",       "crit"});
        put(160, new String[] {"exp",       "haste"});
        put(161, new String[] {"exp",       "mastery"});
        put(162, new String[] {"mastery",   "spi"});
        put(163, new String[] {"mastery",   "dodge"});
        put(164, new String[] {"mastery",   "parry"});
        put(165, new String[] {"mastery",   "hit"});
        put(166, new String[] {"mastery",   "crit"});
        put(167, new String[] {"mastery",   "haste"});
        put(168, new String[] {"mastery",   "exp"});
    }};

    static final int[][] reforgeWithStatIDMap = new int[][]{
        //reforge ID, from stat ID, to stat ID
        {113, 6, 13},
        {114, 6, 14},
        {115, 6, 31},
        {116, 6, 32},
        {117, 6, 36},
        {118, 6, 37},
        {119, 6, 49},
        {120, 13, 6},
        {121, 13, 14},
        {122, 13, 31},
        {123, 13, 32},
        {124, 13, 36},
        {125, 13, 37},
        {126, 13, 49},
        {127, 14, 6},
        {128, 14, 13},
        {129, 14, 31},
        {130, 14, 32},
        {131, 14, 36},
        {132, 14, 37},
        {133, 14, 49},
        {134, 31, 6},
        {135, 31, 13},
        {136, 31, 14},
        {137, 31, 32},
        {138, 31, 36},
        {139, 31, 37},
        {140, 31, 49},
        {141, 32, 6},
        {142, 32, 13},
        {143, 32, 14},
        {144, 32, 31},
        {145, 32, 36},
        {146, 32, 37},
        {147, 32, 49},
        {148, 36, 6},
        {149, 36, 13},
        {150, 36, 14},
        {151, 36, 31},
        {152, 36, 32},
        {153, 36, 37},
        {154, 36, 49},
        {155, 37, 6},
        {156, 37, 13},
        {157, 37, 14},
        {158, 37, 31},
        {159, 37, 32},
        {160, 37, 36},
        {161, 37, 49},
        {162, 49, 6},
        {163, 49, 13},
        {164, 49, 14},
        {165, 49, 31},
        {166, 49, 32},
        {167, 49, 36},
        {168, 49, 37},
    };
    @SuppressWarnings("serial")
    static final Map<Integer, String> professionsMap = new HashMap<Integer, String>(){{
        put(773, "inscription");
        put(755, "jewelcrafting");
        put(393, "skinning");
        put(333, "enchanting");
        put(202, "engineering");
        put(197, "tailoring");
        put(186, "mining");
        put(182, "herbalism");
        put(171, "alchemy");
        put(165, "leatherworking");
        put(164, "blacksmithing");
        // 794, "archaeology");
        // 356, "fishing");
        // 185, "cooking"
        // 129, "first_aid");
    }};

    @SuppressWarnings("serial")
    static final Map<Integer, String[]> qualityMap = new HashMap<Integer, String[]>(){{
        put(0, new String[]{"poor",         "9D9D9D"});
        put(1, new String[]{"common",       "FFFFFF"});
        put(2, new String[]{"uncommon",     "1EFF00"});
        put(3, new String[]{"rare",         "0070DD"});
        put(4, new String[]{"epic",         "A335EE"});
        put(5, new String[]{"legendary",    "FF8000"});
        // put(6, new String[]{"artifact",     "E5CC80"});
        put(7, new String[]{"heirloom",     "E5CC80"});
        // put(8, new String[]{"quality_8",    "FFFF98"});
        // put(9, new String[]{"quality_9",    "71D5FF"});
    }};

    static final String[][][][] talentIconMap = new String[][][][] {
        // [class][tree][tier][talent]
        {null},
        {null},
        {null},
        {null},
        {
            {
                {"ability_rogue_deadlymomentum", "ability_rogue_eviscerate", "ability_criticalstrike", null},
                {"ability_druid_disembowel", "ability_rogue_quickrecovery", "ability_backstab", "ability_rogue_blackjack"},
                {"ability_rogue_deadlybrew", "spell_ice_lament", "ability_rogue_feigndeath", null},
                {"ability_rogue_deadenednerves", "ability_rogue_stayofexecution", null, null},
                {"spell_shadow_deathscream", "ability_hunter_rapidkilling", "ability_creature_poison_06", "ability_warrior_riposte"},
                {null, "ability_rogue_cuttothechase", "ability_rogue_venomouswounds", null},
                {null, "ability_rogue_deadliness", null, null}
            },
            {
                {"ability_rogue_improvedrecuperate", "spell_shadow_ritualofsacrifice", "ability_marksmanship", null},
                {"ability_rogue_slicedice", "ability_rogue_sprint", "ability_racial_avatar", "ability_kick"},
                {"spell_nature_invisibilty", "inv_sword_97", "ability_rogue_reinforcedleather", "ability_gouge"},
                {null, "inv_weapon_shortblade_38", "ability_rogue_bladetwisting", null},
                {"ability_rogue_throwingspecialization", "spell_shadow_shadowworddominate", "ability_creature_disease_03", null},
                {"ability_rogue_preyontheweak", null, "ability_rogue_restlessblades", null},
                {null, "ability_rogue_murderspree", null, null}
            },
            {
                {"ability_stealth", "ability_rogue_ambush", "ability_warrior_decisivestrike", null},
                {"spell_magic_lesserinvisibilty", "ability_rogue_waylay", "ability_rogue_bloodsplatter", "spell_shadow_fumble"},
                {"ability_rogue_sturdyrecuperate", "ability_rogue_findweakness", "spell_shadow_lifedrain", null},
                {"ability_rogue_honoramongstthieves", "spell_shadow_possession", null, "ability_rogue_envelopingshadows"},
                {"ability_rogue_cheatdeath", "ability_rogue_preparation", "ability_rogue_sanguinaryvein", null},
                {null, "ability_rogue_slaughterfromtheshadows", "inv_sword_17", null},
                {null, "ability_rogue_shadowdance", null, null}
            }
        }
    };

    static final int[][][] maxTalentMap = new int[][][] {
        // [class][tree][talent]
        {null},
        {null},
        {null},
        {null},
        {{2,3,3,0,3,2,3,2,2,1,3,0,3,2,0,0,2,1,1,2,0,3,2,0,0,1,0,0},
            {2,3,3,0,2,2,3,2,3,1,2,2,0,3,2,0,2,1,2,0,3,0,2,0,0,1,0,0},
            {2,3,3,0,2,2,3,2,3,2,1,0,3,1,0,3,3,1,2,0,0,3,2,0,0,1,0,0}},
            {null}
    };

    static final String[] statsMap = new String[] {
        "str",      // 0
        "agi",      // 1
        "ap",       // 2
        "crit",     // 3
        "hit",      // 4
        "exp",      // 5
        "haste",    // 6
        "mastery"   // 7
    };

}
