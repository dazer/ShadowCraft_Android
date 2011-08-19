package com.shadowcraft.android;
import java.util.ArrayList;
import java.util.Arrays;
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

    static final List<String> BnetStatsMap = new ArrayList<String>(Arrays.asList(
            "health",
            "powerType",
            "power",
            "str",
            "agi",
            "sta",
            "int",
            "spr",
            "attackPower",
            "rangedAttackPower",
            "mastery",
            "masteryRating",
            "crit",
            "critRating",
            "hitPercent",
            "hitRating",
            "hasteRating",
            "expertiseRating",
            "spellPower",
            "spellPen",
            "spellCrit",
            "spellCritRating",
            "spellHitPercent",
            "spellHitRating",
            "mana5",
            "mana5Combat",
            "armor",
            "dodge",
            "dodgeRating",
            "parry",
            "parryRating",
            "block",
            "blockRating",
            "resil",
            "mainHandDmgMin",
            "mainHandDmgMax",
            "mainHandSpeed",
            "mainHandDps",
            "mainHandExpertise",
            "offHandDmgMin",
            "offHandDmgMax",
            "offHandSpeed",
            "offHandDps",
            "offHandExpertise",
            "rangedDmgMin",
            "rangedDmgMax",
            "rangedSpeed",
            "rangedDps",
            "rangedCrit",
            "rangedCritRating",
            "rangedHitPercent",
            "rangedHitRating"
            ));

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
