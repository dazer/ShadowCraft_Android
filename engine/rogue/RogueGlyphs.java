package rogue;

import java.util.Set;

import classes.Glyphs;
import core.util;

/**
 * Passes Rogue glyphs to the Glyphs superclass.
 */
public class RogueGlyphs extends Glyphs {
    private static Set<String> allowed_glyphs = util.mkSet(
            // Prime
            "adrenaline_rush",
            "backstab",
            "eviscerate",
            "hemorrhage",
            "killing_spree",
            "mutilate",
            "revealing_strike",
            "rupture",
            "shadow_dance",
            "sinister_strike",
            "slice_and_dice",
            "vendetta",
            // Major
            "ambush",
            "blade_flurry",
            "blind",
            "cloak_of_shadows",
            "crippling_poison",
            "deadly_throw",
            "evasion",
            "expose_armor",
            "fan_of_knives",
            "feint",
            "garrote",
            "gouge",
            "kick",
            "preparation",
            "sap",
            "sprint",
            "tricks_of_the_trade",
            "vanish",
            // Minor
            "blurred_speed",
            "distract",
            "pick_lock",
            "pick_pocket",
            "poisons",
            "safe_fall");

    public RogueGlyphs(String... args) {
        super(allowed_glyphs, args);
    }

}
