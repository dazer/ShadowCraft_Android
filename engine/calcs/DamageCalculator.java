package calcs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import rogue.Settings;
import classes.Buffs;
import classes.GearBuffs;
import classes.Glyphs;
import classes.Proc;
import classes.ProcsList;
import classes.Race;
import classes.Stats;
import classes.Talents;
import classes.Weapon;
import core.InvalidInputException;
import core.util;

/**
 * This class holds the general interface for a damage calculator - the sorts of
 * parameters and calculated values that will be need by many (or most) classes
 * if they implement a damage calculator using this framework. Not saying that
 * will happen, but I want to leave my options open. Any calculations that are
 * specific to a particular class should go in
 * calcs.<class>.<Class>DamageCalculator instead - for an example, see
 * calcs.rogue.RogueDamageCalculator
 */
public class DamageCalculator {

    // If someone wants to have the constructor take a target level as well and
    // use it to initialize these to a level-dependent value, they're welcome
    // to. At the moment I'm hard-coding them to level 85 values.
    protected static final float TARGET_BASE_ARMOR =       (float) 11977.;
    protected static final float BASE_ONE_HAND_MISS_RATE = (float) .08;
    protected static final float BASE_DW_MISS_RATE =       (float) .27;
    protected static final float BASE_SPELL_MISS_RATE =    (float) .17;
    protected static final float BASE_DODGE_CHANCE =       (float) .065;
    protected static final float BASE_PARRY_CHANCE =       (float) .14;
    protected static final float GLANCE_RATE =             (float) .24;
    protected static final float GLANCE_MULTIPLIER =       (float) .75;

    private Set<String> default_ep_stats = new HashSet<String>();
    private String normalize_ep_stat = null;  // use the setter to initialize in your subclass.
    private int level = 85;
    private Stats stats;
    private Talents talents;
    private Glyphs glyphs;
    private Buffs buffs;
    private Race race;
    private Settings settings;
    private float armor_mitigation_parameter;
    private boolean dodgeable = true;
    private boolean parryable = false;
    private String ep_flag = "";  // EP helpers makes use of this variable.

    /**
     * Constructor. Initializes values and caches values for the information
     * every modeler will need to know about a character. Sets level and
     * eventually propagates it to every object.
     * 
     * @param stats
     *            Stats object; contains gear related stats: numerical stats,
     *            weapons, procs and gear boosts.
     * @param talents
     *            ClassTalent object; contains cached values for talents in the
     *            current game class.
     * @param glyphs
     *            Glyphs object; contains cached values for glyphs in the
     *            current game class.
     * @param buffs
     *            Buffs object; contains raid buffs, food and flasks.
     * @param race
     *            Race object; contains race related stats.
     * @param settings
     *            Settings object; contains fight settings.
     * @param level
     *            Player level.
     */
    public DamageCalculator(Stats stats, Talents talents, Glyphs glyphs, Buffs buffs, Race race, Settings settings, int level) {
        this.stats = stats;
        this.talents = talents;
        this.glyphs = glyphs;
        this.buffs = buffs;
        this.race = race;
        this.settings = settings;
        this.set_level(level);
        if (this.stats.gear_buffs().exists_buff("mixology") && this.buffs.get("agi_flask"))
            this.stats.set("agi", this.stats.get_num_stat("agi") + 80);
        if (this.stats.gear_buffs().exists_buff("master_of_anatomy"))
            this.stats.set("agi", this.stats.get_num_stat("crit") + 80);
    }

    /**
     * This is the general method to change the player level in level dependent
     * classes. Will call _set_constants_for_level().
     * 
     * @param level
     *            Player level.
     */
    public void set_level(int level) {
        this.level = level;
        this._set_constants_for_level();
    }

    public int level() {
        return this.level;
    }

    /**
     * Calculates and caches armor mitigation according to level. Propagates
     * level to every level dependent object.
     */
    protected void _set_constants_for_level() {
        this.buffs.set_level(this.level);
        this.stats.set_level(this.level);
        this.race.set_level(this.level);
        this.armor_mitigation_parameter = ArmorMitigation.parameter(this.level);
    }

    /**
     * Call this in your class specific subclass to list appropriate stats.
     * Allowed values are agi, str, spi, int, white_hit, spell_hit, yellow_hit,
     * haste, crit, mastery, dodge_exp, parry_exp, oh_dodge_exp, mh_dodge_exp,
     * oh_parry_exp, mh_parry_exp
     */
    public void set_default_ep_stats(Set<String> args) {
        this.default_ep_stats = args;
    }

    /**
     * normalize_ep_stat is the stat with value 1 EP, call in your subclass.
     */
    public void set_normalize_ep_stat(String stat) {
        this.normalize_ep_stat = stat;
    }

    /**
     * Most attacks by DPS aren't parryable due to positional negation. But if
     * you ever want to attacking from the front, you can just set that to True.
     */
    public void set_parryable(boolean arg) {
        this.parryable = arg;
    }

    public void set_dodgeable(boolean arg) {
        this.dodgeable = arg;
    }

    // getters
    public Stats stats() {
        return this.stats;
    }

    public Talents talents() {
        return this.talents;
    }

    public Glyphs glyphs() {
        return this.glyphs;
    }

    public Buffs buffs() {
        return this.buffs;
    }

    public Race race() {
        return this.race;
    }

    public Settings settings() {
        return this.settings;
    }

    // //////////////////////
    // EP functions.
    // //////////////////////

    /**
     * The EP methods use this function to bypass capped stats. If we are
     * checking a non capped stat it will add and subtract 1 to the stat and
     * output dps with the changed stat.
     * 
     * @param stat
     * @return Modified dps.
     */
    protected double ep_helper(String stat) {
        Set<String> cap_stats = util.mkSet("dodge_exp", "white_hit",
                "spell_hit", "yellow_hit", "parry_exp", "mh_dodge_exp",
                "oh_dodge_exp", "mh_parry_exp", "oh_parry_exp");
        if (!cap_stats.contains(stat))
            this.stats.set(stat, this.stats.get_num_stat(stat) + 1);
        else
            this.ep_flag = stat;
        double dps = get_dps();
        if (!cap_stats.contains(stat))
            this.stats.set(stat, this.stats.get_num_stat(stat) - 1);
        else
            this.ep_flag = "";

        return dps;
    }

    /**
     * The method to retrieve EP is to modify stats by a small amount and check
     * the dps variance. It deals with numeric stats using ep_helper to modify
     * them by 1. Overloads are provided for different overrides.
     * 
     * @param ep_stats
     *            A set with stats to check; can be defaulted.
     * @param normalize_ep_stat
     *            The stat everything is checked against.
     * @return A hash with EP for numeric stats.
     */
    public Map<String, Double> get_ep(Set<String> ep_stats, String normalize_ep_stat) {
        if (normalize_ep_stat == null)
            normalize_ep_stat = this.normalize_ep_stat;
        if (ep_stats == null)
            ep_stats = this.default_ep_stats;

        double baseline_dps = this.get_dps();
        double normalize_dps = this.ep_helper(normalize_ep_stat);
        double normalize_dps_difference = normalize_dps - baseline_dps;
        Map<String, Double> ep_values = new HashMap<String, Double>();

        for (String stat : ep_stats) {
            double dps = this.ep_helper(stat);
            double ep = Math.abs(dps - baseline_dps) / normalize_dps_difference;
            ep_values.put(stat, ep);
        }

        return ep_values;
    }

    /**
     * get_ep() overload. If you overwrite the default stats, this is the
     * method callers should point to.
     */
    public Map<String, Double> get_ep() {
        return get_ep(null, null);
    }

    /**
     * get_ep() overload. Call this if default stats were not overwritten.
     */
    public Map<String, Double> get_ep(Set<String> ep_stats) {
        return get_ep(ep_stats, null);
    }

    // //////////////////////
    // Weapon EP functions.
    // //////////////////////

    /**
     * Changes the weapon dps by 1 and checks dps difference.
     * 
     * @param normalize_ep_stat
     *            The stat everything is checked against.
     * @return A List with two items: mh dps EP and oh dps EP
     */
    public double get_weapon_dps_ep(String hand, String normalize_ep_stat) {
        if (normalize_ep_stat == null)
            normalize_ep_stat = this.normalize_ep_stat;

        double baseline_dps = this.get_dps();
        double normalize_dps = this.ep_helper(normalize_ep_stat);

        double old_weapon_dps = this.stats.weapon(hand).weapon_dps();
        this.stats.weapon(hand).set_weapon_dps(old_weapon_dps + 1);
        double new_dps = this.get_dps();
        double dps_ep = Math.abs(new_dps - baseline_dps) / (normalize_dps - baseline_dps);
        this.stats.weapon(hand).set_weapon_dps(old_weapon_dps);

        return dps_ep;
    }

    /**
     * get_weapon_dps_ep(...) overload. Callers will point at this if
     * normalize_ep_stat was overridden.
     */
    public Double get_weapon_dps_ep(String hand) {
        return this.get_weapon_dps_ep(hand, null);
    }

    /**
     * Changes the weapon speed and checks dps difference. At some point we
     * should provide a default for these speeds according to class. For the
     * time being callers will need to provide them. Note, however, that speed
     * EP is prone to big variances so most callers won't be using it.
     * 
     * @param speeds A set with different speeds to check.
     * @param normalize_ep_stat The stat everything is checked against.
     * @return A List with two hashes: mh speed EPs and oh speed EPs
     */
    public HashMap<String, Double> get_weapon_speed_ep(String hand, Set<Double> speeds, String normalize_ep_stat) {
        if (normalize_ep_stat == null)
            normalize_ep_stat = this.normalize_ep_stat;

        double baseline_dps = this.get_dps();
        double normalize_dps = this.ep_helper(normalize_ep_stat);
        HashMap<String, Double> speed_ep_values = new HashMap<String, Double>();

        double old_weapon_speed = this.stats.weapon(hand).speed();
        for (double speed : speeds) {
            this.stats.weapon(hand).set_speed((float) speed);
            double new_dps = this.get_dps();
            double ep = (new_dps - baseline_dps) / (normalize_dps - baseline_dps);
            if (Math.abs(ep) < 0.0001)
                ep = 0;
            speed_ep_values.put(hand + "_" + speed, ep);
        }
        this.stats.weapon(hand).set_speed(old_weapon_speed);
        return speed_ep_values;
    }

    /**
     * get_weapon_speed_ep overload. Callers should point to this if the
     * normalize stat was overridden.
     * 
     * @param speeds A set with different speeds to check.
     * @return A List with two hashes: mh speed EPs and oh speed EPs
     */
    public HashMap<String, Double> get_weapon_speed_ep(String hand, Set<Double> speeds) {
        return this.get_weapon_speed_ep(hand, speeds, null);
    }

    public HashMap<String, Double> get_weapon_speed_ep(String hand, double... speeds) {
        Set<Double> set = new HashSet<Double>();
        for (double speed : speeds) {
            set.add(speed);
        }
        return this.get_weapon_speed_ep(hand, set);
    }

    /**
     * Checks the dps difference of every melee enchant available. To do so it
     * will del/set allowed enchants from the weapon.
     * 
     * @param normalize_ep_stat The stat everything is checked against.
     * @param hand The weapon we are checking.
     * @return A hash with enchant EPs.
     */
    public HashMap<String, Double> get_weapon_enchant_ep(String hand, String normalize_ep_stat) {
        if (normalize_ep_stat == null)
            normalize_ep_stat = this.normalize_ep_stat;

        HashMap<String, Double> enchants_ep_values = new HashMap<String, Double>();

        String old_enchant = this.stats.weapon(hand).enchant_name();
        this.stats.weapon(hand);
        for (String enchant : Weapon.allowed_melee_enchants().keySet()) {
            this.stats.weapon(hand).del_enchant();
            double no_enchant_dps = this.get_dps();
            double no_enchant_normalize_dps = this.ep_helper(normalize_ep_stat);
            this.stats.weapon(hand).set_enchant(enchant);
            double new_dps = this.get_dps();
            if (new_dps != no_enchant_dps) {
                double ep = Math.abs(new_dps - no_enchant_dps) / (no_enchant_normalize_dps - no_enchant_dps);
                enchants_ep_values.put(enchant, ep);
            }
            this.stats.weapon(hand).set_enchant(old_enchant);
        }
        return enchants_ep_values;
    }

    /**
     * get_weapon_enchant_ep overload. Callers should point to this if the
     * normalize stat was overridden.
     * 
     * @return A Hash with enchant EPs.
     */
    public HashMap<String, Double> get_weapon_enchant_ep(String hand) {
        return get_weapon_enchant_ep(hand, null);
    }

    // //////////////////////
    // Other EP functions.
    // //////////////////////

    /**
     * Checks the dps difference of every boost/proc passed. To do so it will
     * del/set boosts and procs from the appropriate object. Note that activated
     * abilities like trinkets, potions, or engineering gizmos are handled as
     * gear buffs by the engine.
     * 
     * @param args
     *            A set with procs/boosts to check.
     * @param normalize_ep_stat
     *            The stat everything is checked against.
     * @return A hash containing the EP for each boost or proc
     */
    public Map<String, Double> get_other_ep(Set<String> args, String normalize_ep_stat) {
        if (normalize_ep_stat.equals(""))
            normalize_ep_stat = this.normalize_ep_stat;

        double baseline_dps = this.get_dps();
        double normalize_dps = this.ep_helper(normalize_ep_stat);
        Set<String> proc_set = new HashSet<String>();
        Set<String> boost_set = new HashSet<String>();
        Map<String, Double> ep_values = new HashMap<String, Double>();

        for (String i : args) {
            if (ProcsList.allowed_procs().contains(i))
                proc_set.add(i);
            else if (GearBuffs.allowed_buffs().contains(i))
                boost_set.add(i);
            else
                ep_values.put(i, -1.);  // not allowed
        }

        for (String i : boost_set) {
            this.stats.gear_buffs().switch_buff(i);
            double new_dps = this.get_dps();
            double ep = Math.abs(new_dps - baseline_dps) / (normalize_dps - baseline_dps);
            ep_values.put(i, ep);
            this.stats.gear_buffs().switch_buff(i);
        }

        for (String i : proc_set) {
            try {
                if (this.stats.procs().exists_proc(i))
                    this.stats.procs().del_proc(i);
                else
                    this.stats.procs().append_proc(i);
                double new_dps = this.get_dps();
                double ep = Math.abs(new_dps - baseline_dps) / (normalize_dps - baseline_dps);
                ep_values.put(i, ep);
                if (this.stats.procs().exists_proc(i))
                    this.stats.procs().del_proc(i);
                else
                    this.stats.procs().append_proc(i);
            }
            catch (Proc.InvalidProcException e) {
                ep_values.put(i, -2.);  // not supported proc
                this.stats.procs().del_proc(i);
            }
        }
        return ep_values;
    }

    /**
     * get_other_ep(...) overload. This is what callers should point to if the
     * normalize stat was overridden.
     * 
     * @param args A set with procs/boosts to check.
     * @return A hash containing the EP for each boost or proc
     */
    public Map<String, Double> get_other_ep(Set<String> args) {
        return get_other_ep(args, "");
    }

    /**
     * get_other_ep(...) overload with varargs. Call this only if the normalize
     * stat was overridden.
     * 
     * @param args A set with procs/boosts to check.
     * @return A hash containing the EP for each boost or proc
     */
    public Map<String, Double> get_other_ep(String... args) {
        return get_other_ep(util.mkSet(args), "");
    }

    // //////////////////////
    // Ranking functions.
    // //////////////////////

    /**
     * Checks the dps difference of every glyph passed. If no glyphs are passed
     * it will check every available one to the game class. This returns dps
     * difference, not EP.
     * 
     * @param glyphs
     *            A set with glyphs to check.
     * @return A hash containing the dps difference for each glyph.
     */
    public Map<String, Double> get_glyphs_ranking(Set<String> glyphs) {
        if (glyphs == null)
            glyphs = this.glyphs.allowed_glyphs();

        Map<String, Double> glyph_ranking = new HashMap<String, Double>();
        double baseline_dps = this.get_dps();

        for (String i : glyphs) {
            this.glyphs.switch_glyph(i);
            try {
                double new_dps = get_dps();
                if ((float) new_dps != (float) baseline_dps) {
                    double diff = Math.abs(new_dps - baseline_dps);
                    glyph_ranking.put(i, diff);
                }
            }
            catch (InvalidInputException e) {
                glyph_ranking.put(i, -1.);  // not implemented
            }
            this.glyphs.switch_glyph(i);
        }

        return glyph_ranking;
    }

    /**
     * get_glyphs_ranking(...) overload.
     */
    public Map<String, Double> get_glyphs_ranking() {
        return get_glyphs_ranking(null);
    }

    /**
     * Checks the dps difference of every talent passed. If no talent is passed
     * it will check every available one to the game class. This returns dps
     * difference, not EP.
     * 
     * @param talents
     *            A set with talents to check.
     * @return A hash containing the dps difference for each talent.
     */
    public List<HashMap<String, Double>> get_talents_ranking(Set<String> talents) {
        Map<String, Double> talents_ranking = new HashMap<String, Double>();
        double baseline_dps = this.get_dps();
        Set<String> main_tree_talents = this.talents().get_all_talents_for_active_spec();

        if (talents == null) {
            @SuppressWarnings("unchecked")
            HashSet<String> talent_list = util.addSets(main_tree_talents, this.talents().get_all_talents_up_to_tier(2));
            talents = talent_list;
        }
        for (String talent : talents) {
            int new_talent_value;
            int old_talent_value = this.talents().get(talent);
            if (old_talent_value == 0)
                new_talent_value = 1;
            else
                new_talent_value = old_talent_value - 1;
            this.talents().set_talent(talent, new_talent_value);
            try {
                double new_dps = get_dps();
                if ((float) new_dps != (float) baseline_dps) {
                    double diff = Math.abs(new_dps - baseline_dps);
                    talents_ranking.put(talent, diff);
                }
            }
            catch (InvalidInputException e) {
                talents_ranking.put(talent, -1.);  // not implemented
            }
            this.talents().set_talent(talent, old_talent_value);
        }

        HashMap<String, Double> main_tree_talents_ranking = new HashMap<String, Double>();
        HashMap<String, Double> off_trees_talents_ranking = new HashMap<String, Double>();
        for (Entry<String, Double> talent : talents_ranking.entrySet()) {
            String key = talent.getKey();
            double value = talent.getValue();
            if (main_tree_talents.contains(key))
                main_tree_talents_ranking.put(key, value);
            else
                off_trees_talents_ranking.put(key, value);
        }
        List<HashMap<String, Double>> return_list = new ArrayList<HashMap<String, Double>>();
        return_list.add(main_tree_talents_ranking);
        return_list.add(off_trees_talents_ranking);

        return return_list;
    }

    /**
     * get_talents_ranking(...) overload.
     */
    public List<HashMap<String, Double>> get_talents_ranking() {
        return this.get_talents_ranking(null);
    }

    // //////////////////////
    // Modeler functions.
    // //////////////////////

    /**
     * Overwrite this function with your calculations/simulations/whatever; this
     * is what callers will (initially) be looking at.
     * 
     * @return dps for current spec and stats.
     */
    public double get_dps() {
        return 0;
    }

    /**
     * Overwrite this function with your calculations/simulations/whatever; this
     * is what callers will (initially) be looking at.
     * 
     * @return damage breakdown per ability.
     */
    public Map<String, Double> get_dps_breakdown() {
        return null;
    }

    /**
     * Override this in your subclass to implement talents that modify spell hit
     * chance.
     */
    public double get_spell_hit_from_talents() {
        return 0;
    }

    /**
     * Override this in your subclass to implement talents that modify melee hit
     * chance.
     */
    public double get_melee_hit_from_talents() {
        return 0;
    }

    /**
     * Gets activated boosts from gear and racials.
     * 
     * @return A list containing data for the modeler.
     */
    public List<HashMap<String, Object>> get_all_activated_stat_boosts() {
        List<HashMap<String, Object>> racial_boosts = this.race.get_stat_boosts();
        List<HashMap<String, Object>> gear_boosts = this.stats.gear_buffs().get_all_activated_boosts();
        List<HashMap<String, Object>> boosts = new ArrayList<HashMap<String, Object>>();
        boosts.addAll(racial_boosts);
        boosts.addAll(gear_boosts);
        return boosts;
    }

    /**
     * Computes the armor mitigation modifier.
     * 
     * @param armor
     *            Target armor
     */
    public double armor_mitig_mult(double armor) {
        return ArmorMitigation.multiplier(armor, this.armor_mitigation_parameter);
    }

    /**
     * Pass in raw physical damage and armor value, get armor-mitigated damage
     * value.
     */
    public double armor_mitigate(double damage, double armor) {
        return damage * this.armor_mitig_mult(armor);
    }

    /**
     * General function that we use in subsequent methods. The field variables
     * dodgeable and parryable drive the output of this method. Note that miss
     * chance is capped at 0. We make extensive use of the EP_flag in all of
     * these methods too, to figure add points to capped stats.
     * 
     * @param base_miss_chance
     *            This is provided by the modeler for each call.
     * @param weapon_type
     *            racials have an effect on hit depending on type.
     * @return melee hit chance
     */
    public double melee_hit_chance(float base_miss_chance, String weapon_type) {
        double hit_chance = this.stats.get_melee_hit_from_rating() + this.race.get_racial_hit() + this.get_melee_hit_from_talents();
        double miss_chance = Math.max(base_miss_chance - hit_chance, 0);

        // Expertise represented as the reduced chance to be dodged or parried,
        // not true "Expertise"
        double expertise = this.stats.get_expertise_from_rating() + this.race.get_racial_expertise(weapon_type);

        double dodge_chance = 0;
        if (this.dodgeable) {
            dodge_chance = Math.max(BASE_DODGE_CHANCE - expertise, 0);
            if (this.ep_flag.equals("dodge_exp"))
                dodge_chance += this.stats.get_expertise_from_rating(1);
        }

        double parry_chance = 0;
        if (this.parryable) {
            parry_chance = Math.max(BASE_PARRY_CHANCE - expertise, 0);
            if (util.mkSet("parry_exp", "dodge_exp").contains(this.ep_flag))
                parry_chance += this.stats.get_expertise_from_rating(1);
        }

        return 1 - (miss_chance + dodge_chance + parry_chance);
    }

    /**
     * If no weapon is provided it will take the mh. Uses the field variables
     * dodgeable/parryable along with the ep_flag to add 1 to this capped stat.
     * 
     * @param weapon
     *            Weapon object.
     * @return 1h melee hit chance
     */
    public double one_hand_melee_hit_chance(Weapon weapon) {
        if (weapon == null)
            weapon = this.stats.weapon("mh");
        double hit_chance = this.melee_hit_chance(BASE_ONE_HAND_MISS_RATE, weapon.type());
        if (this.ep_flag.equals("yellow_hit"))
            hit_chance -= this.stats.get_melee_hit_from_rating(1);
        if ((this.ep_flag.equals("mh_dodge_exp") && this.dodgeable) || (this.ep_flag.equals("mh_parry_exp") && this.parryable))
            hit_chance -= this.stats.get_expertise_from_rating(1);
        return hit_chance;
    }

    /**
     * one_hand_melee_hit_chance(...) overload.
     */
    public double one_hand_melee_hit_chance() {
        return one_hand_melee_hit_chance(null);
    }

    /**
     * If no weapon is provided it will take the oh. Uses the field variables
     * dodgeable/parryable along with the ep_flag to add 1 to this capped stat.
     * 
     * @param weapon
     *            Weapon object.
     * @return oh melee hit chance
     */
    public double off_hand_melee_hit_chance(Weapon weapon) {
        if (weapon == null)
            weapon = this.stats.weapon("oh");
        double hit_chance = this.melee_hit_chance(BASE_ONE_HAND_MISS_RATE, weapon.type());
        if (this.ep_flag.equals("yellow_hit"))
            hit_chance -= this.stats.get_melee_hit_from_rating(1);
        if ((this.ep_flag.equals("oh_dodge_exp") && this.dodgeable) || (this.ep_flag.equals("oh_parry_exp") && this.parryable))
            hit_chance -= this.stats.get_expertise_from_rating(1);
        return hit_chance;
    }

    /**
     * off_hand_melee_hit_chance(...) overload.
     */
    public double oh_melee_hit_chance() {
        return off_hand_melee_hit_chance(null);
    }

    /**
     * General function that we use in subsequent methods related to dual
     * wielding. It makes uses of the previous general method melee_hit_chance.
     * 
     * @param weapon_type
     *            racials have an effect on hit depending on type.
     * @return dw hit chance
     */
    public double dw_hit_chance(String weapon_type) {
        double hit_chance = this.melee_hit_chance(
                DamageCalculator.BASE_DW_MISS_RATE, weapon_type);
        if (util.mkSet("yellow_hit", "spell_hit", "white_hit").contains(this.ep_flag))
            hit_chance -= this.stats.get_melee_hit_from_rating(1);
        return hit_chance;
    }

    /**
     * Uses the field variables dodgeable/parryable along with the ep_flag to
     * add 1 to this capped stat.
     * 
     * @return dw mh hit chance
     */
    public double dw_mh_hit_chance() {
        double hit_chance = this.dw_hit_chance(this.stats.weapon("mh").type());
        if ((this.ep_flag.equals("mh_dodge_exp") && this.dodgeable) || (this.ep_flag.equals("mh_parry_exp") && this.parryable))
            hit_chance -= this.stats.get_expertise_from_rating(1);
        return hit_chance;
    }

    /**
     * Uses the field variables dodgeable/parryable along with the ep_flag to
     * add 1 to this capped stat.
     * 
     * @return dw oh hit chance
     */
    public double dw_oh_hit_chance() {
        double hit_chance = this.dw_hit_chance(this.stats.weapon("oh").type());
        if ((this.ep_flag.equals("oh_dodge_exp") && this.dodgeable) || (this.ep_flag.equals("oh_parry_exp") && this.parryable))
            hit_chance -= this.stats.get_expertise_from_rating(1);
        return hit_chance;
    }

    public double spell_hit_chance() {
        float base_miss = BASE_SPELL_MISS_RATE;
        double rating = this.stats.get_spell_hit_from_rating();
        double talents = this.get_spell_hit_from_talents();
        double race = this.race.get_racial_hit();
        double hit_chance = 1 - Math.max(base_miss - rating - talents - race, 0);
        if (util.mkSet("yellow_hit", "spell_hit").contains(this.ep_flag))
            hit_chance -= this.stats.get_spell_hit_from_rating(1);
        return hit_chance;
    }

    public double buff_melee_crit() {
        return this.buffs.buff_all_crit();
    }

    public double buff_spell_crit() {
        return this.buffs.buff_spell_crit() + this.buffs.buff_all_crit();
    }

    /**
     * Passes base armor reduced by armor debuffs or overridden armor
     * 
     * @param armor
     *            Target armor override.
     */
    public double target_armor(float armor) {
        if (armor == 0)
            armor = TARGET_BASE_ARMOR;
        return this.buffs.armor_reduction_mult() * armor;
    }

    /**
     * target_armor(...) overload.
     */
    public double target_armor() {
        return target_armor(0);
    }

    /**
     * This function wraps spell, bleed and physical debuffs from raid along
     * with all-damage buff and armor reduction. It should be called from every
     * damage dealing formula. Armor can be overridden if needed.
     */
    public double raid_settings_mods(String attack_kind, float armor) {
        double modifier = 0;
        if (attack_kind.equals("spell"))
            modifier = this.buffs.spell_dmg_mult();
        else if (attack_kind.equals("bleed"))
            modifier = this.buffs.bleed_dmg_mult();
        else if (attack_kind.equals("physical")) {
            double armor_override = this.target_armor(armor);
            modifier = this.buffs.physical_dmg_mult() * this.armor_mitig_mult(armor_override);
        }
        else
            throw new InvalidInputException(String.format("Attacks must be categorized as physical, spell or bleed"));
        return modifier;
    }

    /**
     * raid_settings_modifiers overload.
     */
    public double raid_settings_mod(String attack_kind) {
        return raid_settings_mods(attack_kind, 0);
    }

}
