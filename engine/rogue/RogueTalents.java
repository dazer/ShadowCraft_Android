package rogue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import classes.Talents;
import core.InvalidInputException;
import core.util;

public class RogueTalents extends Talents{

    /**
     * Constructor. Feeds the strings to the superclass to eventually be
     * processed by the populate method.
     * @param string1 Assassination string
     * @param string2 Combat string
     * @param string3 Subtlety string
     */
    public RogueTalents(String string1, String string2, String string3) {
        super(string1, string2, string3, RogueTalents.specs, RogueTalents.rogue_allowed_talents);
    }

    /**
     * Constructor overload. This is used if the input is a matrix, or by the
     * single string constructor.
     * @param strings A matrix with three talent strings.
     */
    public RogueTalents(String[] strings) {
        this(strings[0], strings[1], strings[2]);
    }

    /**
     * Use this constructor if you want to have the talents in one long string.
     * Note that this enforces initialization of all talents and the string
     * needs to be exactly 57 chars long.
     * @param string A string holding all talents.
     */
    public RogueTalents(String string) {
        this(split_string(string));
    }

    public static String[] split_string(String entry) {
        String[] strings = new String[3];
        if (entry.length() != 19 + 19 + 19)
            throw new InvalidInputException(String.format("Invalid talent string %s for a rogue", entry));
        strings[0] = entry.substring(0, 19);
        strings[1] = entry.substring(19, 38);
        strings[2] = entry.substring(38);
        return strings;
    }

    static final List<String> specs = util.mkList("assassination", "combat", "subtlety");

    @SuppressWarnings("serial")
    static final HashMap<String, HashMap<String, List<Integer>>> rogue_allowed_talents = new HashMap<String, HashMap<String, List<Integer>>>(){{
        put("assassination", new HashMap<String, List<Integer>>(){{
            put("deadly_momentum",              util.mkList (2, 1));
            put("coup_de_grace",                util.mkList (3, 1));
            put("lethality",                    util.mkList (3, 1));
            put("ruthlessness",                 util.mkList (3, 2));
            put("quickening",                   util.mkList (2, 2));
            put("puncturing_wounds",            util.mkList (3, 2));
            put("blackjack",                    util.mkList (2, 2));
            put("deadly_brew",                  util.mkList (2, 3));
            put("cold_blood",                   util.mkList (1, 3));
            put("vile_poisons",                 util.mkList (3, 3));
            put("deadened_nerves",              util.mkList (3, 4));
            put("seal_fate",                    util.mkList (2, 4));
            put("murderous_intent",             util.mkList (2, 5));
            put("overkill",                     util.mkList (1, 5));
            put("master_poisoner",              util.mkList (1, 5));
            put("improved_expose_armor",        util.mkList (2, 5));
            put("cut_to_the_chase",             util.mkList (3, 6));
            put("venomous_wounds",              util.mkList (2, 6));
            put("vendetta",                     util.mkList (1, 7));
        }}
                );
        put("combat", new HashMap<String, List<Integer>>(){{
            put("improved_recuperate",          util.mkList (2, 1));
            put("improved_sinister_strike",     util.mkList (3, 1));
            put("precision",                    util.mkList (3, 1));
            put("improved_slice_and_dice",      util.mkList (2, 2));
            put("improved_sprint",              util.mkList (2, 2));
            put("aggression",                   util.mkList (3, 2));
            put("improved_kick",                util.mkList (2, 2));
            put("lightning_reflexes",           util.mkList (3, 3));
            put("revealing_strike",             util.mkList (1, 3));
            put("reinforced_leather",           util.mkList (2, 3));
            put("improved_gouge",               util.mkList (2, 3));
            put("combat_potency",               util.mkList (3, 4));
            put("blade_twisting",               util.mkList (2, 4));
            put("throwing_specialization",      util.mkList (2, 5));
            put("adrenaline_rush",              util.mkList (1, 5));
            put("savage_combat",                util.mkList (2, 5));
            put("bandits_guile",                util.mkList (3, 6));
            put("restless_blades",              util.mkList (2, 6));
            put("killing_spree",                util.mkList (1, 7));
        }}
                );
        put("subtlety", new HashMap<String, List<Integer>>(){{
            put("nightstalker",                 util.mkList (2, 1));
            put("improved_ambush",              util.mkList (3, 1));
            put("relentless_strikes",           util.mkList (3, 1));
            put("elusiveness",                  util.mkList (2, 2));
            put("waylay",                       util.mkList (2, 2));
            put("opportunity",                  util.mkList (3, 2));
            put("initiative",                   util.mkList (2, 2));
            put("energetic_recovery",           util.mkList (3, 3));
            put("find_weakness",                util.mkList (2, 3));
            put("hemorrhage",                   util.mkList (1, 3));
            put("honor_among_thieves",          util.mkList (3, 4));
            put("premeditation",                util.mkList (1, 4));
            put("enveloping_shadows",           util.mkList (3, 4));
            put("cheat_death",                  util.mkList (3, 5));
            put("preparation",                  util.mkList (1, 5));
            put("sanguinary_vein",              util.mkList (2, 5));
            put("slaughter_from_the_shadows",   util.mkList (3, 6));
            put("serrated_blades",              util.mkList (2, 6));
            put("shadow_dance",                 util.mkList (1, 7));
        }}
                );
    }};

    @SuppressWarnings("serial")
    static final HashMap<String, ArrayList<String>> sorted_talents = new HashMap<String, ArrayList<String>>(){{
        put("assassination", (ArrayList<String>) util.mkList(
                "deadly_momentum",
                "coup_de_grace",
                "lethality",
                "ruthlessness",
                "quickening",
                "puncturing_wounds",
                "blackjack",
                "deadly_brew",
                "cold_blood",
                "vile_poisons",
                "deadened_nerves",
                "seal_fate",
                "murderous_intent",
                "overkill",
                "master_poisoner",
                "improved_expose_armor",
                "cut_to_the_chase",
                "venomous_wounds",
                "vendetta"
                ));
        put("combat", (ArrayList<String>) util.mkList(
                "improved_recuperate",
                "improved_sinister_strike",
                "precision",
                "improved_slice_and_dice",
                "improved_sprint",
                "aggression",
                "improved_kick",
                "lightning_reflexes",
                "revealing_strike",
                "reinforced_leather",
                "improved_gouge",
                "combat_potency",
                "blade_twisting",
                "throwing_specialization",
                "adrenaline_rush",
                "savage_combat",
                "bandits_guile",
                "restless_blades",
                "killing_spree"
                ));
        put("subtlety", (ArrayList<String>) util.mkList(
                "nightstalker",
                "improved_ambush",
                "relentless_strikes",
                "elusiveness",
                "waylay",
                "opportunity",
                "initiative",
                "energetic_recovery",
                "find_weakness",
                "hemorrhage",
                "honor_among_thieves",
                "premeditation",
                "enveloping_shadows",
                "cheat_death",
                "preparation",
                "sanguinary_vein",
                "slaughter_from_the_shadows",
                "serrated_blades",
                "shadow_dance"
                ));
    }};

    @Override
    public void populate(List<Integer> list, String spec) {
        int talent_counter = 0;
        for (int i = 0; i < list.size(); i++) {
            int value = list.get(i);
            talent_counter += value;
            this.set_talent(RogueTalents.sorted_talents.get(spec).get(i), value, true);
        }
        super.set_talents_in_spec(spec, talent_counter);
    }

}

