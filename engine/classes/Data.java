package classes;

import java.util.HashMap;
import java.util.Map;

import core.util;

@SuppressWarnings("serial")
public class Data {

    static final Map<String, HashMap<String, Object>> activated_boosts = new HashMap<String, HashMap<String, Object>>(){{
        // Duration and cool down in seconds.
        // Name is mandatory for damage-on-use boosts.
        put("unsolvable_riddle", new HashMap<String, Object>(){{
            put("stat",     new String("agi"));
            put("value",    new Float(1605));
            put("duration", new Float(20));
            put("cooldown", new Float(120));
        }}
                );
        put("demon_panther", new HashMap<String, Object>(){{
            put("stat",     new String("agi"));
            put("value",    new Float(1425));
            put("duration", new Float(20));
            put("cooldown", new Float(120));
        }}
                );
        put("skardyns_grace", new HashMap<String, Object>(){{
            put("stat",     new String("mastery"));
            put("value",    new Float(1260));
            put("duration", new Float(20));
            put("cooldown", new Float(120));
        }}
                );
        put("heroic_skardyns_grace", new HashMap<String, Object>(){{
            put("stat",     new String("mastery"));
            put("value",    new Float(1425));
            put("duration", new Float(20));
            put("cooldown", new Float(120));
        }}
                );
        put("potion_of_the_tolvir", new HashMap<String, Object>(){{
            put("stat",     new String("agi"));
            put("value",    new Float(1200));
            put("duration", new Float(25));
            put("cooldown", new Float(0)); //Cool-down = fight length
        }}
                );
        put("potion_of_the_tolvir_prepot", new HashMap<String, Object>(){{
            //Very rough guesstimate; actual modeling should be done with the opener sequence, alas, there"s no such thing.
            put("stat",     new String("agi"));
            put("value",    new Float(1200));
            put("duration", new Float(23));
            put("cooldown", new Float(0));
        }}
                );
        put("engineer_glove_enchant", new HashMap<String, Object>(){{
            //WotLK tinker
            put("stat",     new String("haste"));
            put("value",    new Float(340));
            put("duration", new Float(12));
            put("cooldown", new Float(60));
        }}
                );
        put("synapse_springs", new HashMap<String, Object>(){{
            //Overwrite stat in the model for the highest of agi, str, int
            put("stat",     new String("varies"));
            put("value",    new Float(480));
            put("duration", new Float(10));
            put("cooldown", new Float(60));
        }}
                );
        put("tazik_shocker", new HashMap<String, Object>(){{
            put("stat",     new String("spell_damage"));
            put("value",    new Float(4800));
            put("duration", new Float(0));
            put("cooldown", new Float(60));
            put("name",     new String("Tazik Shocker"));
        }}
                );
        put("lifeblood", new HashMap<String, Object>(){{
            put("stat",     new String("haste"));
            put("value",    new Float(480));
            put("duration", new Float(20));
            put("cooldown", new Float(120));
        }}
                );
        put("ancient_petrified_seed", new HashMap<String, Object>(){{
            put("stat",     new String("agi"));
            put("value",    new Float(1277));
            put("duration", new Float(15));
            put("cooldown", new Float(60));
        }}
                );
        put("heroic_ancient_petrified_seed", new HashMap<String, Object>(){{
            put("stat",     new String("agi"));
            put("value",    new Float(1441));
            put("duration", new Float(15));
            put("cooldown", new Float(60));
        }}
                );
        put("rickets_magnetic_fireball", new HashMap<String, Object>(){{
            put("stat",     new String("crit"));
            put("value",    new Float(1700));
            put("duration", new Float(20));
            put("cooldown", new Float(120));
        }}
                );
    }};

    static final Map<String, HashMap<String, ?>> proc_data = new HashMap<String, HashMap<String, ?>>() {{
        put("heroic_grace_of_the_herald", new HashMap<String, Object>() {{
            put("stat",        new String ("crit"));
            put("value",       new Float (1710));
            put("duration",    new Float (10));
            put("icd",         new Float (50));
            put("proc_chance", new Float (0.1));
            put("trigger",     new String ("all_attacks"));
            put("proc_name",   new String ("Herald of Doom"));
        }}
                );
        put("heroic_key_to_the_endless_chamber", new HashMap<String, Object>() {{
            put("stat",        new String ("agi"));
            put("value",       new Float (1710));
            put("duration",    new Float (15));
            put("icd",         new Float (75));
            put("proc_chance", new Float (0.1));
            put("trigger",     new String ("all_attacks"));
            put("proc_name",   new String ("Final Key"));
        }}
                );
        put("heroic_left_eye_of_rajh", new HashMap<String, Object>() {{
            put("stat",        new String("agi"));
            put("value",       new Float(1710));
            put("duration",    new Float(10));
            put("icd",         new Float(50));
            put("proc_chance", new Float(0.3));
            put("trigger",     new String("all_attacks"));
            put("on_crit",     new Boolean(true));
            put("proc_name",   new String("Eye of Vengeance"));
        }}
                );
        put("heroic_matrix_restabilizer", new HashMap<String, Object>() {{
            // Proc_chance is a guess and should be verified.
            put("stat",        new String("weird_proc"));
            put("value",       new Float(1834));
            put("duration",    new Float(30));
            put("icd",         new Float(105));
            put("proc_chance", new Float(0.1));
            put("trigger",     new String("all_attacks"));
            put("proc_name",   new String("Matrix Restabilized"));
        }}
                );
        put("heroic_prestors_talisman_of_machination", new HashMap<String, Object>() {{
            put("stat",        new String("haste"));
            put("value",       new Float(2178));
            put("duration",    new Float(15));
            put("icd",         new Float(75));
            put("proc_chance", new Float(0.1));
            put("trigger",     new String("all_attacks"));
            put("proc_name",   new String("Nefarious Plot"));
        }}
                );
        put("heroic_the_hungerer", new HashMap<String, Object>() {{
            put("stat",        new String("haste"));
            put("value",       new Float(1730));
            put("duration",    new Float(15));
            put("icd",         new Float(60));
            put("proc_chance", new Float(1.0));
            put("trigger",     new String("all_attacks"));
            put("proc_name",   new String("Devour"));
        }}
                );
        put("heroic_tias_grace", new HashMap<String, Object>() {{
            put("stat",        new String("agi"));
            put("value",       new Float(34));
            put("duration",    new Float(15));
            put("max_stacks",  new Integer(10));
            put("icd",         new Float(0));
            put("proc_chance", new Float(1.0));
            put("trigger",     new String("all_attacks"));
            put("proc_name",   new String("Grace"));
        }}
                );
        put("darkmoon_card_hurricane", new HashMap<String, Object>() {{
            put("stat",        new String("spell_damage"));
            put("value",       new Float(7000));
            put("can_crit",    new Boolean(false));
            put("duration",    new Float(0));
            put("max_stacks",  new Integer(0));
            put("icd",         new Float(0));
            put("ppm",         new Float(1.0));
            put("trigger",     new String("all_attacks"));
            put("proc_name",   new String("Lightning Strike"));
        }}
                );
        put("corens_chilled_chromium_coaster", new HashMap<String, Object>() {{
            // ICD is a guess and should be verified.
            put("stat",        new String("ap"));
            put("value",       new Float(4000));
            put("duration",    new Float(10));
            put("max_stacks",  new Integer(0));
            put("icd",         new Float(50));
            put("proc_chance", new Float(0.1));
            put("trigger",     new String("all_attacks"));
            put("on_crit",     new Boolean(true));
            put("proc_name",   new String("Reflection of Torment"));
        }}
                );
        put("essence_of_the_cyclone", new HashMap<String, Object>() {{
            put("stat",        new String("crit"));
            put("value",       new Float(1926));
            put("duration",    new Float(10));
            put("icd",         new Float(50));
            put("proc_chance", new Float(0.1));
            put("trigger",     new String("all_attacks"));
            put("proc_name",   new String("Twisted"));
        }}
                );
        put("heroic_essence_of_the_cyclone", new HashMap<String, Object>() {{
            put("stat",        new String("crit"));
            put("value",       new Float(2178));
            put("duration",    new Float(10));
            put("icd",         new Float(50));
            put("proc_chance", new Float(0.1));
            put("trigger",     new String("all_attacks"));
            put("proc_name",   new String("Twisted"));
        }}
                );
        put("fluid_death", new HashMap<String, Object>() {{
            put("stat",        new String("agi"));
            put("value",       new Float(38));
            put("duration",    new Float(15));
            put("max_stacks",  new Integer(10));
            put("icd",         new Float(0));
            put("proc_chance", new Float(1.0));
            put("trigger",     new String("all_attacks"));
            put("proc_name",   new String("River of Death"));
        }}
                );
        put("grace_of_the_herald", new HashMap<String, Object>() {{
            put("stat",        new String("crit"));
            put("value",       new Float(924));
            put("duration",    new Float(10));
            put("icd",         new Float(50));
            put("proc_chance", new Float(0.1));
            put("trigger",     new String("all_attacks"));
            put("proc_name",   new String("Herald of Doom"));
        }}
                );
        put("heart_of_the_vile", new HashMap<String, Object>() {{
            put("stat",        new String("crit"));
            put("value",       new Float(924));
            put("duration",    new Float(10));
            put("icd",         new Float(50));
            put("proc_chance", new Float(0.1));
            put("trigger",     new String("all_attacks"));
            put("proc_name",   new String("Herald of Doom"));
        }}
                );
        put("key_to_the_endless_chamber", new HashMap<String, Object>() {{
            put("stat",        new String("agi"));
            put("value",       new Float(1290));
            put("duration",    new Float(15));
            put("icd",         new Float(75));
            put("proc_chance", new Float(0.1));
            put("trigger",     new String("all_attacks"));
            put("proc_name",   new String("Final Key"));
        }}
                );
        put("left_eye_of_rajh", new HashMap<String, Object>() {{
            put("stat",        new String("agi"));
            put("value",       new Float(1512));
            put("duration",    new Float(10));
            put("icd",         new Float(50));
            put("proc_chance", new Float(0.3));
            put("trigger",     new String("all_attacks"));
            put("on_crit",     new Boolean(true));
            put("proc_name",   new String("Eye of Vengeance"));
        }}
                );
        put("matrix_restabilizer", new HashMap<String, Object>() {{
            // Proc_chance is a guess and should be verified.
            put("stat",        new String("weird_proc"));
            put("value",       new Float(1624));
            put("duration",    new Float(30));
            put("icd",         new Float(105));
            put("proc_chance", new Float(0.1));
            put("trigger",     new String("all_attacks"));
            put("proc_name",   new String("Matrix Restabilized"));
        }}
                );
        put("prestors_talisman_of_machination", new HashMap<String, Object>() {{
            put("stat",        new String("haste"));
            put("value",       new Float(1926));
            put("duration",    new Float(15));
            put("icd",         new Float(75));
            put("proc_chance", new Float(0.1));
            put("trigger",     new String("all_attacks"));
            put("proc_name",   new String("Nefarious Plot"));
        }}
                );
        put("rickets_magnetic_fireball_proc", new HashMap<String, Object>() {{
            // ICD should be verified.
            put("stat",        new String("physical_damage"));
            put("value",       new Float(500));
            put("duration",    new Float(0));
            put("max_stacks",  new Integer(0));
            put("icd",         new Float(120));
            put("proc_chance", new Float(0.2));
            put("trigger",     new String("all_attacks"));
            put("proc_name",   new String("Magnetic Fireball"));
        }}
                );
        put("rogue_t11_4pc", new HashMap<String, Object>() {{
            put("stat",        new String("weird_proc"));
            put("value",       new Float(1));
            put("duration",    new Float(15));
            put("icd",         new Float(0));
            put("proc_chance", new Float(0.01));
            put("trigger",     new String("auto_attacks"));
            put("proc_name",   new String("Deadly Scheme"));
        }}
                );
        put("schnottz_medallion_of_command", new HashMap<String, Object>() {{
            put("stat",        new String("mastery"));
            put("value",       new Float(918));
            put("duration",    new Float(20));
            put("icd",         new Float(100));
            put("proc_chance", new Float(0.1));
            put("trigger",     new String("all_attacks"));
            put("proc_name",   new String("Hardened Shell"));
        }}
                );
        put("swordguard_embroidery", new HashMap<String, Object>() {{
            put("stat",        new String("ap"));
            put("value",       new Float(1000));
            put("duration",    new Float(15));
            put("icd",         new Float(55));
            put("proc_chance", new Float(0.15));
            put("trigger",     new String("all_attacks"));
            put("proc_name",   new String("Swordguard Embroidery"));
        }}
                );
        put("the_hungerer", new HashMap<String, Object>() {{
            put("stat",        new String("haste"));
            put("value",       new Float(1532));
            put("duration",    new Float(15));
            put("icd",         new Float(60));
            put("proc_chance", new Float(1.0));
            put("trigger",     new String("all_attacks"));
            put("proc_name",   new String("Devour"));
        }}
                );
        put("the_twilight_blade", new HashMap<String, Object>() {{
            // PPM/ICD is a guess and should be verified.
            put("stat",        new String("crit"));
            put("value",       new Float(185));
            put("duration",    new Float(10));
            put("max_stacks",  new Integer(3));
            put("icd",         new Float(0));
            put("ppm",         new Float(1.0));
            put("trigger",     new String("all_attacks"));
            put("proc_name",   new String("The Deepest Night"));
        }}
                );
        put("tias_grace", new HashMap<String, Object>() {{
            put("stat",        new String("agi"));
            put("value",       new Float(30));
            put("duration",    new Float(15));
            put("max_stacks",  new Integer(10));
            put("icd",         new Float(0));
            put("proc_chance", new Float(1.0));
            put("trigger",     new String("all_attacks"));
            put("proc_name",   new String("Grace"));
        }}
                );
        put("unheeded_warning", new HashMap<String, Object>() {{
            put("stat",        new String("extra_weapon_damage"));
            put("value",       new Float(680));
            put("duration",    new Float(10));
            put("icd",         new Float(50));
            put("proc_chance", new Float(0.1));
            put("trigger",     new String("all_attacks"));
            put("proc_name",   new String("Heedless Carnage"));
        }}
                );
    }};

    static final Map<String, HashMap<String, ?>> melee_enchants = new HashMap<String, HashMap<String, ?>>() {{
        put("hurricane", new HashMap<String, Object>() {{
            put("stat",      new String("haste"));
            put("value",     new Float(450));
            put("duration",  new Float(12));
            put("icd",       new Float(0));
            put("ppm",       new Float(1));
            put("trigger",   new String("all_spells_and_attacks"));
            put("proc_name", new String("Hurricane"));
        }}
                );
        put("landslide", new HashMap<String, Object>() {{
            put("stat",      new String("ap"));
            put("value",     new Float(1000));
            put("duration",  new Float(12));
            put("icd",       new Float(0));
            put("ppm",       new Float(1));
            put("trigger",   new String("all_attacks"));
            put("proc_name", new String("Landslide"));
        }}
                );
    }};

    /*
     * The _set_behaviour method takes these parameters: trigger, icd,
     * proc_chance=False, ppm=False, on_crit=False You can't set a value for
     * both 'ppm' and 'proc_chance': one must be False Allowed triggers are:
     * 'all_spells_and_attacks', 'all_damaging_attacks', 'all_attacks',
     * 'strikes', 'auto_attacks', 'damaging_spells', 'all_spells',
     * 'healing_spells', 'all_periodic_damage', 'bleeds',
     * 'spell_periodic_damage' and 'hots'.
     */
    static final Map<String, HashMap<String, ?>> behaviours = new HashMap<String, HashMap<String, ?>>(){{
        put("avalanche_melee", new HashMap<String, Object>() {{
            put("icd",         new Float(0));
            put("ppm",         new Float(5));
            put("trigger",     new String("all_attacks"));
        }});
        put("avalanche_spell", new HashMap<String, Object>() {{
            // As per EnhSim and SimCraft
            put("icd",         new Float(10));
            put("proc_chance", new Float(.25));
            put("trigger",     new String("all_periodic_damage"));
        }});
        put("hurricane_melee", new HashMap<String, Object>() {{
            // Completely guessing at proc behavior.
            put("icd",         new Float(0));
            put("ppm",         new Float(1));
            put("trigger",     new String("all_spells_and_attacks"));
        }});
        put("hurricane_spell", new HashMap<String, Object>() {{
            put("icd",         new Float(45));
            put("proc_chance", new Float(.15));
            put("trigger",     new String("all_spells"));
        }});
        put("landslide", new HashMap<String, Object>() {{
            // Completely guessing at proc behavior.
            put("icd",         new Float(0));
            put("ppm",         new Float(1));
            put("trigger",     new String("all_attacks"));
        }});
        put("corens_chilled_chromium_coaster", new HashMap<String, Object>() {{
            // ICD is a guess and should be verified.
            put("icd",         new Float(50));
            put("proc_chance", new Float(.1));
            put("trigger",     new String("all_attacks"));
            put("on_crit",     new Boolean(  true));
        }});
        put("darkmoon_card_hurricane", new HashMap<String, Object>() {{
            put("icd",         new Float(0));
            put("ppm",         new Float(1));
            put("trigger",     new String("all_attacks"));
        }});
        put("essence_of_the_cyclone", new HashMap<String, Object>() {{
            put("icd",         new Float(50));
            put("proc_chance", new Float(.1));
            put("trigger",     new String("all_attacks"));
        }});
        put("fluid_death",     new HashMap<String, Object>() {{
            put("icd",         new Float(0));
            put("proc_chance", new Float(1));
            put("trigger",     new String("all_attacks"));
        }});
        put("grace_of_the_herald", new HashMap<String, Object>() {{
            put("icd",         new Float(50));
            put("proc_chance", new Float(.1));
            put("trigger",     new String("all_attacks"));
        }});
        put("heart_of_the_vile", new HashMap<String, Object>() {{
            put("icd",         new Float(50));
            put("proc_chance", new Float(.1));
            put("trigger",     new String("all_attacks"));
        }});
        put("key_to_the_endless_chamber", new HashMap<String, Object>() {{
            put("icd",         new Float(75));
            put("proc_chance", new Float(.1));
            put("trigger",     new String("all_attacks"));
        }});
        put("left_eye_of_rajh", new HashMap<String, Object>() {{
            put("icd",         new Float(50));
            put("proc_chance", new Float(.3));
            put("trigger",     new String("all_attacks"));
            put("on_crit",     new Boolean(true));
        }});
        put("matrix_restabilizer", new HashMap<String, Object>() {{
            // Proc_chance is a guess and should be verified.
            put("icd",         new Float(105));
            put("proc_chance", new Float(.1));
            put("trigger",     new String("all_attacks"));
        }});
        put("prestors_talisman_of_machination", new HashMap<String, Object>() {{
            put("icd",         new Float(75));
            put("proc_chance", new Float(.1));
            put("trigger",     new String("all_attacks"));
        }});
        put("rickets_magnetic_fireball", new HashMap<String, Object>() {{
            // ICD should be verified.
            put("icd",         new Float(120));
            put("proc_chance", new Float(.2));
            put("trigger",     new String("all_attacks"));
        }});
        put("rogue_t11_4pc", new HashMap<String, Object>() {{
            put("icd",         new Float(0));
            put("proc_chance", new Float(.01));
            put("trigger",     new String("auto_attacks"));
        }});
        put("schnottz_medallion_of_command", new HashMap<String, Object>() {{
            put("icd",         new Float(100));
            put("proc_chance", new Float(.1));
            put("trigger",     new String("all_attacks"));
        }});
        put("swordguard_embroidery", new HashMap<String, Object>() {{
            put("icd",         new Float(55));
            put("proc_chance", new Float(.15));
            put("trigger",     new String("all_attacks"));
        }});
        put("the_hungerer", new HashMap<String, Object>() {{
            put("icd",         new Float(60));
            put("proc_chance", new Float(1.));
            put("trigger",     new String("all_attacks"));
        }});
        put("the_twilight_blade", new HashMap<String, Object>() {{
            // PPM/ICD is a guess and should be verified.
            put("icd",         new Float(0));
            put("ppm",         new Float(1));
            put("trigger",     new String("all_attacks"));
        }});
        put("tias_grace", new HashMap<String, Object>() {{
            put("icd",         new Float(0));
            put("proc_chance", new Float(1));
            put("trigger",     new String("all_attacks"));
        }});
        put("unheeded_warning", new HashMap<String, Object>() {{
            put("icd",         new Float(50));
            put("proc_chance", new Float(.1));
            put("trigger",     new String("all_attacks"));
        }});
    }};

    static final Map<String, float[]> racial_stat_offset = new HashMap<String, float[]>() {{
        // values are {str, agi, sta, int, spi}
        put("human",      new float[] { 0,  0, 0,  0,  0});
        put("night_elf",  new float[] {-4,  4, 0,  0,  0});
        put("dwarf",      new float[] { 5, -4, 1, -1, -1});
        put("gnome",      new float[] {-5,  2, 0,  3,  0});
        put("draenei",    new float[] { 1, -3, 0,  0,  2});
        put("worgen",     new float[] { 3,  2, 0, -4, -1});
        put("orc",        new float[] { 3, -3, 1, -3,  2});
        put("undead",     new float[] {-1, -2, 0, -2,  5});
        put("tauren",     new float[] { 5, -4, 1, -4,  2});
        put("troll",      new float[] { 1,  2, 0, -4,  1});
        put("blood_elf",  new float[] {-3,  2, 0,  3, -2});
        put("goblin",     new float[] {-3,  2, 0,  3, -2});
    }};

    static final Map<String, HashMap<String, Object>> activated_racial_data = new HashMap<String, HashMap<String, Object>>() {{
        put("blood_fury_physical", new HashMap<String, Object>() {{
            put("stat",     new String("ap"));
            put("value",    new Float(0));
            put("duration", new Float(15));
            put("cooldown", new Float(120));
        }}
                );
        put("blood_fury_spell", new HashMap<String, Object>() {{
            put("stat",     new String("sp"));
            put("value",    new Float(0));
            put("duration", new Float(15));
            put("cooldown", new Float(120));
        }}
                );
        put("berserking", new HashMap<String, Object>() {{
            put("stat",     new String("haste_multiplier"));
            put("value",    new Float(1.2));
            put("duration", new Float(10));
            put("cooldown", new Float(180));
        }}
                );
        put("arcane_torrent", new HashMap<String, Object>() {{
            put("stat",     new String("energy"));
            put("value",    new Float(15));
            put("duration", new Float(0));
            put("cooldown", new Float(120));
        }}
                );
        put("rocket_barrage", new HashMap<String, Object>() {{
            put("stat",     new String("damage"));
            //put("value",    calculate_rocket_barrage);
            put("duration", new Float(0));
            put("cooldown", new Float(120));
        }}
                );
    }};

    static final Map<String, String[]> racials_by_race = new HashMap<String, String[]>() {{
        put("human",     new String[] {"mace_specialization", "sword_1h_specialization", "sword_2h_specialization", "human_spirit"});
        put("night_elf", new String[] {"quickness"});
        put("dwarf",     new String[] {"stoneform", "gun_specialization", "mace_specialization"});
        put("gnome",     new String[] {"expansive_mind", "dagger_specialization", "sword_1h_specialization"});
        put("draenei",   new String[] {"heroic_presence"});
        put("worgen",    new String[] {"viciousness"});
        put("orc",       new String[] {"blood_fury_physical", "blood_fury_spell", "fist_specialization", "axe_specialization"});
        put("undead",    new String[] {});
        put("tauren",    new String[] {"endurance"});
        put("troll",     new String[] {"regeneration", "beast_slaying", "throwing_specialization", "bow_specialization", "berserking"});
        put("blood_elf", new String[] {"arcane_torrent"});
        put("goblin",    new String[] {"rocket_barrage", "time_is_money"});
    }};

    static final Map<String, HashMap<Integer, float[]>> base_stats = new HashMap<String, HashMap<Integer, float[]>>() {{
        put("rogue", new HashMap<Integer, float[]>() {{
            put(80, new float[] {113, 189, 105, 43, 67});
            put(85, new float[] {122, 206, 114, 46, 73});
        }}
                );
        put("", new HashMap<Integer, float[]>() {{
        }}
                );
    }};

    static final Map<Integer, float[]> blood_fury_bonuses = new HashMap<Integer, float[]>() {{
        // values are {ap, sp}
        put(80, new float[] {330, 165});
        put(85, new float[] {1770, 585});
    }};

    static final Map<String, Map<Integer, Float>> combat_ratings = new HashMap<String, Map<Integer, Float>>() {{
        put("melee_hit_rating_conversion_values", util.mkMap(new double[][] {
                {60, 9.37931}, {70, 14.7905},
                {80, 30.7548}, {81, 40.3836}, {82, 53.0304}, {83, 69.6653}, {84, 91.4738},
                {85, 120.109001159667969}
        }));
        put("spell_hit_rating_conversion_values", util.mkMap(new double[][] {
                {60, 8}, {70, 12.6154},
                {80, 26.232}, {81, 34.4448}, {82, 45.2318}, {83, 59.4204}, {84, 78.0218},
                {85, 102.445999145507812}
        }));
        put("crit_rating_conversion_values", util.mkMap(new double[][] {
                {60, 14}, {70, 22.0769},
                {80, 45.906}, {81, 60.2784}, {82, 79.1556}, {83, 103.986}, {84, 136.53799},
                {85, 179.279998779296875}
        }));
        put("haste_rating_conversion_values", util.mkMap(new double[][] {
                {60, 10}, {70, 15.7692},
                {80, 32.79}, {81, 43.056}, {82, 56.5397}, {83, 74.2755}, {84, 97.5272},
                {85, 128.057006835937500}
        }));
        put("expertise_rating_conversion_values", util.mkMap(new double[][] {
                {60, 2.34483 * 4}, {70, 3.69761 * 4},
                {80, 7.68869 * 4}, {81, 10.0959 * 4}, {82, 13.2576 * 4}, {83, 17.4163 * 4}, {84, 22.8685 * 4},
                {85, 30.027200698852539 * 4}
        }));
        put("mastery_rating_conversion_values", util.mkMap(new double[][] {
                {60, 14}, {70, 22.0769},
                {80, 45.906}, {81, 60.2784}, {82, 79.1556}, {83, 103.986}, {84, 136.53799},
                {85, 179.279998779296875}
        }));
    }};

}
