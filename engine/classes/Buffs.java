package classes;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import core.InvalidInputException;
import core.InvalidLevelException;
import core.util;

/**
 * General modifiers: raid buffs, food and flasks. Provides methods to retrieve
 * their values as well as methods to change the presence of said modifiers.
 * Will need to add the caster/tank (de)buffs at some point if we want to
 * support other classes with this framework.
 */
public class Buffs {

    @SuppressWarnings("serial")
    class InvalidBuffException extends InvalidInputException {
        public InvalidBuffException(String message) {
            super(message);
        }
    }

    static final Set<String> allowed_buffs = util.mkSet(
            "short_term_haste_buff",            // Heroism/Blood Lust, Time Warp
            "stat_multiplier_buff",             // Mark of the Wild, Blessing of Kings
            "crit_chance_buff",                 // Leader of the Pack, HAT, Elemental Oath, Rampage
            "all_damage_buff",                  // Arcane Tactics, Communion, Ferocious Inspiration
            "melee_haste_buff",                 // Windfury, Improved Icy Talons
            "attack_power_buff",                // Trueshot, Unleashed Rage, Abomination"s Might, Blessing of Might
            "str_and_agi_buff",                 // Horn of Winter, Strength of Earth, Battle Shout
            "armor_debuff",                     // Sunder, Expose Armor, Faerie Fire
            "physical_vulnerability_debuff",    // Brittle Bones, Savage Combat, Blood Frenzy
            "spell_damage_debuff",              // Ebon Plaguebringer, Master Poisoner, Earth and Moon, Curse of Elements
            "spell_crit_debuff",                // Critical Mass, Shadow and Flame
            "bleed_damage_debuff",              // Hemo, Mangle, Trauma
            "agi_flask",                        // Flask of the Winds
            "guild_feast"                       // Seafood Magnifique Feast
            );

    /**
     * Level dependent map for HoW/SoE/BS. Appropriate entry gets cached once
     * player level is introduced or defaulted to 85.
     */
    @SuppressWarnings("serial")
    private static final Map<Integer, Float> str_and_agi_buff_values = new HashMap<Integer, Float>() {{
        put(80, (float) 155);
        put(85, (float) 549);
    }};

    private float str_and_agi_buff_bonus = str_and_agi_buff_values.get(85);
    private HashSet<String> present_buffs = new HashSet<String>();
    private int level;

    /**
     * Constructor. Caches the strings passed into present_buffs.
     */
    public Buffs(List<String> args) {
        for (String buff : args) {
            this.set_buff(buff);
        }
    }

    /**
     * Constructor overload. This allows the usage of varargs.
     * @param args Buffs as strings.
     */
    public Buffs(String... args) {
        for (String buff : args) {
            this.set_buff(buff);
        }
    }

    /**
     * If no buffs are present this instantiates the object with an empty set.
     */
    public Buffs() {
    }

    /**
     * This is the general method to change the player level in level dependent
     * classes. Will call _set_constants_for_level().
     * 
     * @param level Player level.
     */
    public void set_level(int level) {
        this.level = level;
        this._set_constants_for_level();
    }

    /**
     * Will cache values for str and agi buffs according to level.
     * 
     * @param level Player level.
     */
    protected void _set_constants_for_level() {
        if (Buffs.str_and_agi_buff_values.containsKey(this.level))
            this.str_and_agi_buff_bonus = Buffs.str_and_agi_buff_values.get(this.level);
        else
            throw new InvalidLevelException(String.format("No conversion factor available for level %s.", this.level));
    }

    /**
     * Tests the existence of a buff. Poor man's getattr.
     * 
     * @param arg
     *            Buff name
     * @return
     */
    public boolean get(String buff) {
        return (present_buffs.contains(buff));
    }

    /**
     * General setter for buffs. In case we want to offer EP for these, a
     * switch_buff method could come in handy. See GearBuffs for such
     * implementation.
     * 
     * @param buff
     *            Buff name
     */
    public void set_buff(String buff) {
        if (!allowed_buffs.contains(buff))
            throw new InvalidBuffException(String.format("Invalid buff %s.",buff));
        this.present_buffs.add(buff);
    }

    public double stat_multiplier() {
        return this.get("stat_multiplier_buff") ? 1.05 : 1;
    }

    public double all_damage_multiplier() {
        return this.get("all_damage_buff") ? 1.03 : 1;
    }

    public double spell_dmg_mult() {
        double mult = this.all_damage_multiplier();
        return this.get("spell_damage_debuff") ? 1.08 * mult : mult;
    }

    public double physical_dmg_mult() {
        double mult = this.all_damage_multiplier();
        return this.get("physical_vulnerability_debuff") ? 1.04 * mult : mult;
    }

    public double bleed_dmg_mult() {
        double mult = this.physical_dmg_mult();
        return this.get("bleed_damage_debuff") ? 1.30 * mult : mult;
    }

    public double attack_power_multiplier() {
        return this.get("attack_power_buff") ? 1.10: 1;
    }

    public double melee_haste_multiplier() {
        return this.get("melee_haste_buff") ? 1.10 : 1;
    }

    /**
     * Str related buffs.
     * 
     * @return Level dependent HoW/SoE/BS.
     */
    public float buff_str() {
        return this.get("str_and_agi_buff") ? this.str_and_agi_buff_bonus : 0;
    }

    /**
     * Agi related buffs.
     * 
     * @return HoW/SoE/BS plus food and flask.
     */
    public double buff_agi() {
        double flask_agi = this.get("agi_flask") ? 300 : 0;
        double food_agi = this.get("guild_feast") ? 90 : 0;
        double raid_agi = this.get("str_and_agi_buff") ? this.str_and_agi_buff_bonus : 0;
        return raid_agi + food_agi + flask_agi;
    }

    public double buff_all_crit() {
        return this.get("crit_chance_buff") ? 0.05 : 0;
    }

    public double buff_spell_crit() {
        return this.get("spell_crit_debuff") ? 0.05 : 0;
    }

    public double armor_reduction_mult() {
        return this.get("armor_debuff") ? 0.88 : 1;
    }

}
