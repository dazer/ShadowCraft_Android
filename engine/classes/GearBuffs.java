package classes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import core.InvalidInputException;
import core.util;

/**
 * Catch-all for non-proc gear based buffs (static or activated)
 */
public class GearBuffs {

    private static Map<String, HashMap<String, Object>> activated_boosts = Data.activated_boosts;
    private static Set<String> other_gear_buffs = util.mkSet(
            "leather_specialization",       // Increase %stat by 5%
            "chaotic_metagem",              // Increase critical damage by 3%
            "rogue_t11_2pc",                // Increase crit chance for BS, Mut, SS by 5%
            "rogue_t12_2pc",                // Add 6% of melee crit damage as a fire DOT
            "rogue_t12_4pc",                // Increase crit/haste/mastery rating by 25% every TotT
            "mixology",
            "master_of_anatomy"
            );

    @SuppressWarnings("unchecked")
    private static final Set<String> allowed_buffs = util.addSets(activated_boosts.keySet(), other_gear_buffs);
    private Set<String> present_g_buffs = new HashSet<String>();

    /**
     * Constructor. Caches the strings passed into present_g_buffs.
     * @param args
     */
    public GearBuffs(Set<String> args) {
        for (String arg : args) {
            set_buff(arg);
        }
    }

    /**
     * Constructor overload. This allows the usage of varargs.
     * @param args
     */
    public GearBuffs(String... args) {
        for (String arg : args) {
            set_buff(arg);
        }
    }

    /**
     * If no buffs are present this instantiates the object with an empty set.
     */
    public GearBuffs() {
    }

    /**
     * Allowed buffs getter. This is to be used by EP functions mainly.
     * @return The set of allowed buffs.
     */
    public static Set<String> allowed_buffs() {
        return allowed_buffs;
    }

    /**
     * Throws an exception if the buff is not allowed.
     * @param buff A string with the buff name to be checked.
     */
    public void _check_allowed(String buff) {
        if (!GearBuffs.allowed_buffs().contains(buff))
            throw new InvalidInputException(String.format("Boost %s is not allowed.", buff));
    }

    public void set_synapse_springs_stat(String stat) {
        GearBuffs.activated_boosts.get("synapse_springs").put("stat", stat);
    }

    /**
     * Checks the validity and appends to preset_g_buffs.
     * @param buff A string with the buff name to be set.
     */
    public void set_buff(String buff) {
        _check_allowed(buff);
        this.present_g_buffs.add(buff);
    }

    public boolean exists_buff(String buff) {
        return present_g_buffs.contains(buff);
    }

    public void del_buff(String buff) {
        if (present_g_buffs.contains(buff))
            this.present_g_buffs.remove(buff);
    }

    /**
     * This is used by the EP functions to switch the presence of a buff.
     * @param buff A string with the buff name to be switched.
     */
    public void switch_buff(String buff) {
        if (this.exists_buff(buff))
            this.del_buff(buff);
        else
            this.set_buff(buff);
    }

    /**
     * Meta gem value getter.
     * @return Crit bonus damage multiplier.
     */
    public double metagem_crit_mult() {
        return this.present_g_buffs.contains("chaotic_metagem") ? 1.03 : 1;
    }

    /**
     * Rogue T-11 2-set value getter.
     * @return Crit chance bonus.
     */
    public double rogue_t11_2pc_crit_bonus() {
        return this.present_g_buffs.contains("rogue_t11_2pc") ? 0.05 : 0;
    }

    /**
     * Rogue T-11 4-set value getter.
     * @return Damage bonus.
     */
    public double rogue_t12_2pc_damage_bonus() {
        return this.present_g_buffs.contains("rogue_t12_2pc") ? 0.06 : 0;
    }

    /**
     * Rogue T-12 4-set value getter.
     * @return Stat bonus.
     */
    public double rogue_t12_4pc_stat_bonus() {
        return this.present_g_buffs.contains("rogue_t12_4pc") ? 0.25 : 0;
    }

    /**
     * Leather specialization value getter.
     * @return Leather spec multiplier.
     */
    public double leather_spec_mult() {
        return this.present_g_buffs.contains("leather_specialization") ? 1.05 : 1;
    }

    /**
     * Activated agi boosts like Demon Panther.
     * @return A list of hashes with data per each agility boost.
     */
    public List<HashMap<String, Object>> get_all_activated_agi_boosts() {
        return get_all_activated_boosts_for_stat("agi");
    }

    /**
     * Activated stat boosts general getter.
     * @param stat The stat for which we want to retrieve boosts.
     * @return A list of hashes with data per each stat boost.
     */
    public List<HashMap<String, Object>> get_all_activated_boosts_for_stat(String stat) {
        List<HashMap<String, Object>> boosts;
        boosts = new ArrayList<HashMap<String, Object>>();
        for (String boost : GearBuffs.activated_boosts.keySet()) {
            if (exists_buff(boost) && (stat == null || GearBuffs.activated_boosts.get(boost).get("stat").equals(stat)))
                boosts.add(GearBuffs.activated_boosts.get(boost));
        }
        return boosts;
    }

    /**
     * Activated boost of any kind general getter.
     * @return A list of hashes with data per each boost.
     */
    public List<HashMap<String, Object>> get_all_activated_boosts() {
        return this.get_all_activated_boosts_for_stat(null);
    }

    /**
     * This is haste rating because the conversion to haste requires level.
     * If reported as a haste value, it must be added to the value from
     * other rating correctly.
     * This does too, but reinforces the fact that it's rating.
     * @return A list of hashes with data per each haste boost.
     */
    public List<HashMap<String, Object>> get_all_activated_haste_rating_boosts() {
        return this.get_all_activated_boosts_for_stat("haste");
    }

}
