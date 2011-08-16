package classes;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import core.InvalidInputException;
import core.util;

public class Stats {

    private Map<String, Object> present_stats = new HashMap<String, Object>();
    private int level = 85;
    private Set<String> allowed_numeric_stats = util.mkSet(
            "str", "agi", "ap", "crit", "hit", "exp", "haste", "mastery"
            );
    private Set<String> allowed_weapons = util.mkSet(
            "mh", "oh", "ranged"
            );
    private Set<String> other_allowed_stats = util.mkSet(
            "procs", "gear_buffs", "level"
            );
    private Map<String, Float> combat_ratings_for_level = new HashMap<String, Float>();

    public Stats(Map<String, Object> args) {
        for (String stat : args.keySet()) {
            this.set(stat, args.get(stat));
        }
    }

    public void set_level(int level) {
        this.level = level;
        this.present_stats.put("level", level);
        this._set_constants_for_level();
    }

    protected void _set_constants_for_level() {
        for (Entry<String, Map<Integer, Float>> item : Data.combat_ratings.entrySet()) {
            String rating = item.getKey();
            Map<Integer, Float> rating_map = item.getValue();
            if (!rating_map.containsKey(this.level))
                throw new InvalidInputException(String.format("No conversion factor available for level %s.", this.level));
            else
                this.combat_ratings_for_level.put(rating, rating_map.get(this.level));
        }
    }

    public void set(String name, Object value) {
        if (!allowed_numeric_stats.contains(name)
                && !allowed_weapons.contains(name)
                && !other_allowed_stats.contains(name))
            throw new InvalidInputException(String.format("Stat %s is not allowed.", name));
        else
            this.present_stats.put(name, value);
        if (name.equals("level"))
            this.set_level((Integer) value);
    }

    public Object get(String name) {
        if (!present_stats.containsKey(name))
            throw new InvalidInputException(String.format("Stat %s is not defined.", name));
        else
            return this.present_stats.get(name);
    }

    public float get_num_stat(String name) {
        return (Float) this.get(name);
    }

    public Weapon weapon(String name) {
        return (Weapon) this.get(name);
    }

    public ProcsList procs() {
        return (ProcsList) this.get("procs");
    }

    public GearBuffs gear_buffs() {
        return (GearBuffs) this.get("gear_buffs");
    }

    public float get_rating_conversion(String rating) {
        String string = null;
        if (rating.equals("melee_hit"))
            string = "melee_hit_rating_conversion_values";
        else if (rating.equals("spell_hit"))
            string = "spell_hit_rating_conversion_values";
        else if (rating.equals("crit"))
            string = "crit_rating_conversion_values";
        else if (rating.equals("haste"))
            string = "haste_rating_conversion_values";
        else if (rating.equals("expertise"))
            string = "expertise_rating_conversion_values";
        else if (rating.equals("mastery"))
            string = "mastery_rating_conversion_values";
        else
            throw new InvalidInputException(String.format("No conversion factor available for %s rating.", rating));
        return this.combat_ratings_for_level.get(string);
    }

    public double get_mastery_from_rating(double rating) {
        return 8 + rating / this.get_rating_conversion("mastery");
    }

    public double get_mastery_from_rating() {
        return this.get_mastery_from_rating(this.get_num_stat("mastery"));
    }

    public double get_melee_hit_from_rating(double rating) {
        return rating / (100 * this.get_rating_conversion("melee_hit"));
    }

    public double get_melee_hit_from_rating() {
        return this.get_melee_hit_from_rating(this.get_num_stat("hit"));
    }

    public double get_expertise_from_rating(double rating) {
        return rating / (100 * this.get_rating_conversion("expertise"));
    }

    public double get_expertise_from_rating() {
        return this.get_expertise_from_rating(this.get_num_stat("exp"));
    }

    public double get_spell_hit_from_rating(double rating) {
        return rating / (100 * this.get_rating_conversion("spell_hit"));
    }

    public double get_spell_hit_from_rating() {
        return this.get_spell_hit_from_rating(this.get_num_stat("hit"));
    }

    public double get_crit_from_rating(double crit) {
        return crit / (100 * this.get_rating_conversion("crit"));
    }

    public double get_crit_from_rating() {
        return this.get_crit_from_rating(this.get_num_stat("crit"));
    }

    public double get_haste_mult_from_rating(double rating) {
        return 1 + rating / (100 * this.get_rating_conversion("haste"));
    }

    public double get_haste_mult_from_rating() {
        return this.get_haste_mult_from_rating(this.get_num_stat("haste"));
    }

}
