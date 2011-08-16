package rogue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import classes.Buffs;
import classes.Glyphs;
import classes.Proc;
import classes.Race;
import classes.Stats;
import classes.Talents;
import core.InvalidInputException;
import core.util;

public class AldrianasRogueDamageCalculator extends RogueDamageCalculator {

    @SuppressWarnings("serial")
    class InputNotModeledException extends InvalidInputException {
        public InputNotModeledException(String message) {
            super(message);
        }
    }

    class Pair<A, B> extends util.Tuple_2<A, B> {
        public Pair(A first, B second) {
            super(first, second);
        }
    }

    public AldrianasRogueDamageCalculator(Stats stats, Talents talents,
            Glyphs glyphs, Buffs buffs, Race race, Settings settings, int level) {
        super(stats, talents, glyphs, buffs, race, settings, level);
    }

    private double base_e_regen;
    private double bonus_e_regen;
    private double relentless_strikes_e_return_per_cp;
    private double base_rup_e_cost;
    private double base_evis_e_cost;
    private double env_e_cost;
    private double base_rvs_e_cost;
    private double base_ss_e_cost;
    private double base_hemo_cost;
    private double base_bs_e_cost;
    private double base_ambush_e_cost;
    private double agi_mult;
    private double base_str;
    private double base_speed_mult;
    private double strike_hit_chance;
    private double vendetta_mult;
    private double max_bg_buff;
    private double bg_mult;
    private double rvs_mult;
    private double ksp_mult;
    private double hemo_interval;
    private double fw_uptime;
    private double ambush_shs_rate;

    private HashMap<String, Double> base_stats;

    // /////////////////////////////////////////////////////////////////////////
    // Main DPS comparison function. Calls the appropriate sub-function based
    // on talent tree.
    // /////////////////////////////////////////////////////////////////////////

    @Override
    public double get_dps() {
        if (this.talents().is_specced("assassination")) {
            this.init_assassination();
            return this.assassination_dps_estimate();
        }
        else if (this.talents().is_specced("combat"))
            return this.combat_dps_estimate();
        else if (this.talents().is_specced("subtlety"))
            return this.subtlety_dps_estimate();
        else
            throw new InputNotModeledException(String.format("You must have 31 points in at least one talent tree."));
    }

    @Override
    public Map<String, Double> get_dps_breakdown() {
        if (this.talents().is_specced("assassination")) {
            this.init_assassination();
            return this.assassination_dps_breakdown();
        }
        else if (this.talents().is_specced("combat"))
            return this.combat_dps_breakdown();
        else if (this.talents().is_specced("subtlety"))
            return this.subtlety_dps_breakdown();
        else
            throw new InputNotModeledException(String.format("You must have 31 points in at least one talent tree."));
    }

    // /////////////////////////////////////////////////////////////////////////
    // General object manipulation functions that we'll use multiple places.
    // /////////////////////////////////////////////////////////////////////////

    static final double PRECISION_REQUIRED = Math.pow(10, -7);

    private boolean are_close_enough(Map<String, Double[]> old_dist, Map<String, Double[]> new_dist) {
        for (Entry<String, Double[]> item : new_dist.entrySet()) {
            String key = item.getKey();
            Double[] values = item.getValue();
            if (!old_dist.keySet().contains(key))
                return false;
            else {
                for (int index = 0; index < values.length; index++) {
                    double diff = Math.abs(values[index] - old_dist.get(key)[index]);
                    if (diff > PRECISION_REQUIRED)
                        return false;
                }
            }
        }
        return true;
    }

    private double[] get_dps_contrib(double[] damage_tuple, double crit_rate, double frequency) {
        double base_damage = damage_tuple[0];
        double crit_damage = damage_tuple[1];
        double average_hit = base_damage * (1 - crit_rate) + crit_damage * crit_rate;
        double crit_contribution = crit_damage * crit_rate;
        return new double[] {average_hit * frequency, crit_contribution * frequency};
    }

    // /////////////////////////////////////////////////////////////////////////
    // General modeling functions for pulling information useful across all
    // models.
    // /////////////////////////////////////////////////////////////////////////

    private double heroism_uptime_per_fight() {
        if (!this.buffs().get("short_term_haste_buff"))
            return 0;

        double total_uptime = 0;
        double remaining_duration = this.settings().duration();
        while (remaining_duration > 0) {
            total_uptime += Math.min(remaining_duration, 40);
            remaining_duration -= 600;
        }
        return total_uptime * 1.0 / this.settings().duration();
    }

    private double get_heroism_haste_multiplier() {
        // Just average-casing for now. Should fix that at some point.
        return 1 + .3 * this.heroism_uptime_per_fight();
    }

    /**
     * Given the probability of one cpg to generate different amounts of cps and
     * the desired minimum finisher size, we output the probabilities of
     * attaining said minimum (or go over it) for every amount of moves
     * possible. To do so we build combinations (cps, moves) and keep adding
     * moves until the cps reach our target.
     * 
     * @param cp_dist_per_move
     *            A hash with items of the type {cp_per_move=probability}
     * @param target_cp_quantity
     *            Finisher size we want to perform.
     * @return A hash with items of the type {(cps, moves)=probability} and an
     *         array with time spent at each cp.
     */
    private Pair<HashMap<Pair<Integer, Integer>, Double>, double[]> get_cp_dist_for_cycle(HashMap<Integer, Double> cp_dist_per_move, int target_cp_quantity) {
        double[] time_spent_at_cp = new double[] { 0, 0, 0, 0, 0, 0 };
        int cur_min_cp = 0;
        double ruthlessness_chance = this.talents().get("ruthlessness") * .2;
        HashMap<Pair<Integer, Integer>, Double> cur_dist = new HashMap<Pair<Integer, Integer>, Double>();
        cur_dist.put(new Pair<Integer, Integer>(0, 0), 1 - ruthlessness_chance);
        cur_dist.put(new Pair<Integer, Integer>(1, 0), ruthlessness_chance);
        while (cur_min_cp < target_cp_quantity) {
            cur_min_cp += 1;
            HashMap<Pair<Integer, Integer>, Double> new_dist = new HashMap<Pair<Integer, Integer>, Double>();
            for (Entry<Pair<Integer, Integer>, Double> prob_dist : cur_dist.entrySet()) {
                Pair<Integer, Integer> tuple = prob_dist.getKey();
                int cps = tuple.getFirst();
                int moves = tuple.getSecond();
                double prob = prob_dist.getValue();
                if (cps >= cur_min_cp) {
                    if (new_dist.containsKey(tuple))
                        new_dist.put(tuple, prob + new_dist.get(tuple));
                    else
                        new_dist.put(tuple, prob);
                }
                else {
                    for (Entry<Integer, Double> cp_dist : cp_dist_per_move.entrySet()) {
                        int move_cp = cp_dist.getKey();
                        double move_prob = cp_dist.getValue();
                        int total_cps = cps + move_cp;
                        if (total_cps > 5)
                            total_cps = 5;
                        Pair<Integer, Integer> dist_entry = new Pair<Integer, Integer>(total_cps, moves + 1);
                        time_spent_at_cp[total_cps] += move_prob * prob;
                        if (new_dist.containsKey(dist_entry))
                            new_dist.put(dist_entry, move_prob * prob + new_dist.get(dist_entry));
                        else
                            new_dist.put(dist_entry, move_prob * prob);
                    }
                }
            }
            cur_dist = new_dist;
        }
        for (Entry<Pair<Integer, Integer>, Double> prob_dist : cur_dist.entrySet()) {
            int cps = prob_dist.getKey().getFirst();
            double prob = prob_dist.getValue();
            time_spent_at_cp[cps] += prob;
        }
        double total_weight = util.sumArray(time_spent_at_cp);
        for (int i = 0; i < time_spent_at_cp.length; i++) {
            time_spent_at_cp[i] /= total_weight;
        }
        return new Pair<HashMap<Pair<Integer, Integer>, Double>, double[]>(
                cur_dist, time_spent_at_cp);
    }

    private double get_snd_length(double size) {
        double duration = 6 + 3 * size;
        if (this.glyphs().exists_glyph("slice_and_dice"))
            duration += 6;
        return duration * (1 + .25 * this.talents().get("improved_slice_and_dice"));
    }

    /**
     * General setup that we'll use in all 3 cycles.
     */
    private void set_constants() {
        this.bonus_e_regen = 0;
        if (this.settings().tricks_on_cooldown() && !this.glyphs().exists_glyph("tricks_of_the_trade"))
            this.bonus_e_regen -= 15. / (30 + this.settings().response_time());
        if (this.race().get_racial("arcane_torrent"))
            this.bonus_e_regen += 15. / (120 + this.settings().response_time());

        this.base_stats = new HashMap<String, Double>();
        base_stats.put("agi", this.stats().get_num_stat("agi") + this.buffs().buff_agi() + this.race().get_racial_stat("racial_agi"));
        base_stats.put("ap", (double) this.stats().get_num_stat("ap") + 140);
        base_stats.put("crit", (double) this.stats().get_num_stat("crit"));
        base_stats.put("haste", (double) this.stats().get_num_stat("haste"));
        base_stats.put("mastery", (double) this.stats().get_num_stat("mastery"));

        for (HashMap<String, Object> boost : this.race().get_stat_boosts()) {
            String stat = (String) boost.get("stat");
            if (this.base_stats.containsKey(stat)) {
                double increase = (Double) boost.get("value") * (Double) boost.get("duration") * 1.0 / ((Double) boost.get("cooldown") + this.settings().response_time());
                this.base_stats.put(stat, this.base_stats.get(stat) + increase);
            }
        }

        if (this.stats().gear_buffs().exists_buff("synapse_springs"))
            this.stats().gear_buffs().set_synapse_springs_stat("agi");

        for (String stat : this.base_stats.keySet()) {
            for (HashMap<String, Object> boost : this.stats().gear_buffs().get_all_activated_boosts_for_stat(stat)) {
                double duration = new Float((Float) boost.get("duration"));
                double value = new Float((Float) boost.get("value"));
                double increase;
                if ((Float) boost.get("cooldown") != 0.) {
                    double cooldown = new Float((Float) boost.get("cooldown"));
                    increase = (value * duration) * 1.0 / (cooldown + this.settings().response_time());
                }
                else
                    increase = (value * duration) * 1.0 / this.settings().duration();
                this.base_stats.put(stat, this.base_stats.get(stat) + increase);
            }
        }

        this.agi_mult = this.buffs().stat_multiplier() * this.stats().gear_buffs().leather_spec_mult();
        this.base_str = this.stats().get_num_stat("str") + this.buffs().buff_str() + this.race().get_racial_stat("racial_str");
        this.base_str *= this.buffs().stat_multiplier();

        this.relentless_strikes_e_return_per_cp = (new double[] { 0, 1.75, 3.5, 5 })[this.talents().get("relentless_strikes")];

        this.base_speed_mult = 1.4 * this.buffs().melee_haste_multiplier() * this.get_heroism_haste_multiplier();
        if (this.race().get_racial("berserking"))
            this.base_speed_mult *= (1 + .2 * 10. / (180 + this.settings().response_time()));
        if (this.race().get_racial("time_is_money"))
            this.base_speed_mult *= 1.01;

        this.strike_hit_chance = this.one_hand_melee_hit_chance();
        this.base_rup_e_cost = 20 + 5 / this.strike_hit_chance;
        this.base_evis_e_cost = 28 + 7 / this.strike_hit_chance;

        if (this.stats().procs().exists_proc("heroic_matrix_restabilizer") || this.stats().procs().exists_proc("matrix_restabilizer"))
            this.set_matrix_restabilizer_stat(this.base_stats);
    }

    private double[] get_proc_damage_contribution(Proc proc, double proc_count, Map<String, Double> current_stats) {
        double crit_rate;
        double multiplier;
        double crit_mult;
        if (proc.stat().equals("spell_damage")) {
            multiplier = this.raid_settings_mod("spell");
            crit_mult = this.crit_dmg_mods("is_spell");
            crit_rate = this.spell_crit_rate(current_stats.get("crit"));
        }
        else if (proc.stat().equals("physical_damage")) {
            multiplier = this.raid_settings_mod("physical");
            crit_mult = this.crit_dmg_mods();
            crit_rate = this.melee_crit_rate(current_stats.get("agi"),
                    current_stats.get("crit"));
        }
        else
            return new double[] {0, 0};

        if (!proc.can_crit())
            crit_rate = 0;

        double average_hit = proc.value() * multiplier;
        double average_damage = average_hit * (1 + crit_rate * (crit_mult - 1)) * proc_count;
        double crit_contrb = average_hit * crit_mult * crit_rate * proc_count;
        return new double[] { average_damage, crit_contrb };
    }

    private void append_damage_on_use(double average_ap, HashMap<String, Double> current_stats, HashMap<String, Double[]> dmg_brkdwn) {
        List<HashMap<String, Object>> on_use_damage_list = new ArrayList<HashMap<String, Object>>();
        for (String i : util.mkSet("spell_damage", "physical_damage'")) {
            on_use_damage_list.addAll(this.stats().gear_buffs()
                    .get_all_activated_boosts_for_stat(i));
        }
        if (this.race().get_racial("rocket_barrage")) {
            HashMap<String, Object> rb_dict = new HashMap<String, Object>();
            rb_dict.put("stat",     new String("spell_damage"));
            rb_dict.put("cooldown", new Double(120));
            rb_dict.put("name",     new String("Rocket Barrage"));
            rb_dict.put("value",    new Double(this.race()
                    .calculate_rocket_barrage(average_ap, 0, 0)));
            on_use_damage_list.add(rb_dict);
        }
        double crit_rate = 0;
        double modifier = 0;
        double crit_mult = 0;
        double hit_chance = 0;
        for (HashMap<String, Object> item : on_use_damage_list) {
            if (item.get("stat").equals("physical_damage")) {
                modifier = this.raid_settings_mod("physical");
                crit_mult = this.crit_dmg_mods();
                crit_rate = this.melee_crit_rate(current_stats.get("agi"), current_stats.get("crit"));
                hit_chance = this.strike_hit_chance;
            }
            else if (item.get("stat").equals("spell_damage")) {
                modifier = this.raid_settings_mod("spell");
                crit_mult = this.crit_dmg_mods("is_spell");
                crit_rate = this.spell_crit_rate(current_stats.get("crit"));
                hit_chance = this.spell_hit_chance();
            }

            double average_hit = ((Double) item.get("value")) * modifier;
            double frequency = 1. / ((Double) item.get("cooldown") + this.settings().response_time());
            double average_dps = average_hit * (1 + crit_rate * (crit_mult - 1)) * frequency
                    * hit_chance;
            double crit_contribution = average_hit * crit_mult
                    * crit_rate * frequency * hit_chance;

            dmg_brkdwn.put((String) item.get("name"), new Double[] {
                    average_dps, crit_contribution });
        }
    }

    private void set_matrix_restabilizer_stat(HashMap<String, Double> base_stats) {
        TreeMap<Double, String> stats_for_matrix = new TreeMap<Double, String>();
        for (Entry<String, Double> item : this.base_stats.entrySet()) {
            if (util.mkSet("haste", "mastery", "crit").contains(item.getKey()))
                stats_for_matrix.put(item.getValue(), item.getKey());
            String highest_stat = stats_for_matrix.get(stats_for_matrix.lastKey());
            if (this.stats().procs().exists_proc("heroic_matrix_restabilizer"))
                this.stats().procs().get_proc("heroic_matrix_restabilizer").set_stat(highest_stat);
            if (this.stats().procs().exists_proc("matrix_restabilizer"))
                this.stats().procs().get_proc("matrix_restabilizer").set_stat(highest_stat);
        }
    }

    private Double[] get_t12_2p_damage(HashMap<String, Double[]> dmg_brkdwn) {
        double crit_damage = 0;
        Set<String> abilities = util.mkSet("mutilate", "hemorrhage",
                "backstab", "sinister_strike", "revealing_strike",
                "main_gauche", "ambush", "killing_spree", "envenom",
                "eviscerate", "autoattack");
        for (Entry<String, Double[]> item : dmg_brkdwn.entrySet()) {
            if (abilities.contains(item.getKey())) {
                double crit_contribution = item.getValue()[1];
                crit_damage += crit_contribution;
            }
        }
        Set<String> munch_abilities = util.mkSet("mut_munch", "ksp_munch");
        for (String key : munch_abilities) {
            if (dmg_brkdwn.containsKey(key)) {
                double crit_contribution = dmg_brkdwn.get(key)[1];
                crit_damage -= crit_contribution;
                dmg_brkdwn.remove(key);
            }
        }
        return new Double[] {crit_damage * this.stats().gear_buffs().rogue_t12_2pc_damage_bonus(), 0.};
    }

    /**
     * Given the amount of hits per ability plus it's crit rate, this builds a
     * hash with the dps contribution of each ability. It will also append the
     * dps contribution of damage procs and damage-on-use abilities using their
     * proc_name/boost_name as key.
     * 
     * @param current_stats A hash with base stats after modifiers.
     * @param aps_dict A hash with hits/ticks per second per ability
     * @param crit_rates A hash with crit rates for current cycle.
     * @param dmg_procs A list with Proc objects that deal damage.
     * @return A hash of the type {ability=[dps, crit_contribution]}.
     */
    private HashMap<String, Double[]> get_dmg_brkdwn(HashMap<String, Double> current_stats, HashMap<String, Double[]> aps_dict, HashMap<String, Double> crit_rates, List<Proc> dmg_procs) {
        double avg_ap = current_stats.get("ap") + 2 * current_stats.get("agi") + this.base_str;
        avg_ap *= this.buffs().attack_power_multiplier();
        if (this.talents().is_specced("combat"))
            avg_ap *= 1.3;
        avg_ap *= (1 + .03 * this.talents().get("savage_combat"));

        HashMap<String, Double[]> dmg_brkdwn = new HashMap<String, Double[]>();

        for (String key : aps_dict.keySet()) {
            if (aps_dict.get(key) == null)
                aps_dict.remove(key);
        }

        double aps; // stands for "attacks per second"
        double crit_rate;

        aps = aps_dict.get("mh_autoattacks")[0];
        crit_rate = crit_rates.get("mh_autoattacks");
        double[] mh_tuple = this.mh_dmg(avg_ap);
        double mh_base_damage = mh_tuple[0];
        double mh_crit_damage = mh_tuple[1];
        double mh_hit_rate = this.dw_mh_hit_chance() - GLANCE_RATE - crit_rate;
        double average_mh_hit = GLANCE_RATE * GLANCE_MULTIPLIER * mh_base_damage + mh_hit_rate * mh_base_damage + crit_rate * mh_crit_damage;
        double crit_mh_hit = crit_rate * mh_crit_damage;
        double[] mh_dps_tuple = new double[] {average_mh_hit * aps, crit_mh_hit * aps};

        aps = aps_dict.get("oh_autoattacks")[0];
        crit_rate = crit_rates.get("oh_autoattacks");
        double[] oh_tuple = this.oh_dmg(avg_ap);
        double oh_base_damage = oh_tuple[0];
        double oh_crit_damage = oh_tuple[1];
        double oh_hit_rate = this.dw_oh_hit_chance() - GLANCE_RATE - crit_rate;
        double average_oh_hit = GLANCE_RATE * GLANCE_MULTIPLIER * oh_base_damage + oh_hit_rate * oh_base_damage + crit_rate * oh_crit_damage;
        double crit_oh_hit = crit_rate * oh_crit_damage;
        double[] oh_dps_tuple = new double[] {average_oh_hit * aps, crit_oh_hit * aps};

        dmg_brkdwn.put("autoattack", new Double[] {
                mh_dps_tuple[0] + oh_dps_tuple[0],
                mh_dps_tuple[1] + oh_dps_tuple[1] });

        for (Entry<String, Double[]> item : aps_dict.entrySet()) {
            if (item.getValue() == null)
                aps_dict.remove(item);
        }

        for (Entry<String, Double[]> item : aps_dict.entrySet()) {
            String aps_key = item.getKey();
            Double[] aps_tuple = item.getValue(); // finishers use the tuple.
            aps = aps_tuple[0]; // other attacks use the first item.
            double[] dmg_tuple;
            double[] dps_tuple;

            if (aps_key.equals("mutilate")) {
                crit_rate = crit_rates.get("mutilate");
                double[] mh_dmg = this.mh_mutilate_dmg(avg_ap);
                double[] oh_dmg = this.oh_mutilate_dmg(avg_ap);
                double[] dps_tuple_mh = this.get_dps_contrib(mh_dmg, crit_rate, aps);
                double[] dps_tuple_oh = this.get_dps_contrib(oh_dmg, crit_rate, aps);
                dmg_brkdwn.put("mutilate", new Double[] {dps_tuple_mh[0] + dps_tuple_oh[0], dps_tuple_mh[1] + dps_tuple_oh[1] });
                if (this.stats().gear_buffs().exists_buff("rogue_t12_2pc")) {
                    double p_double_crit = Math.pow(crit_rate, 2);
                    double munch_per_sec = aps * p_double_crit;
                    dmg_brkdwn.put("mut_munch", new Double[] {0., munch_per_sec * mh_dmg[1]});
                }
            }

            else if (aps_key.equals("hemorrhage")) {
                crit_rate = crit_rates.get("");
                dmg_tuple = this.hemorrhage_dmg(avg_ap);
                dps_tuple = this.get_dps_contrib(dmg_tuple, crit_rate, aps);
                dmg_brkdwn.put("hemorrhage", new Double[] {dps_tuple[0], dps_tuple[1] });
            }

            else if (aps_key.equals("backstab")) {crit_rate = crit_rates.get("backstab");
            dmg_tuple = this.backstab_dmg(avg_ap);
            dps_tuple = this.get_dps_contrib(dmg_tuple, crit_rate, aps);
            dmg_brkdwn.put("backstab", new Double[] {dps_tuple[0], dps_tuple[1]});
            }

            else if (aps_key.equals("sinister_strike")) {
                crit_rate = crit_rates.get("sinister_strike");
                dmg_tuple = this.sinister_strike_dmg(avg_ap);
                dps_tuple = this.get_dps_contrib(dmg_tuple, crit_rate, aps);
                dmg_brkdwn.put("sinister_strike", new Double[] {dps_tuple[0], dps_tuple[1]});
            }

            else if (aps_key.equals("revealing_strike")) {
                crit_rate = crit_rates.get("revealing_strike");
                dmg_tuple = this.revealing_strike_dmg(avg_ap);
                dps_tuple = this.get_dps_contrib(dmg_tuple, crit_rate, aps);
                dmg_brkdwn.put("revealing_strike", new Double[] {dps_tuple[0], dps_tuple[1]});
            }

            else if (aps_key.equals("main_gauche")) {
                crit_rate = crit_rates.get("main_gauche");
                dmg_tuple = this.main_gauche_dmg(avg_ap);
                dps_tuple = this.get_dps_contrib(dmg_tuple, crit_rate, aps);
                dmg_brkdwn.put("main_gauche", new Double[] {dps_tuple[0], dps_tuple[1]});
            }

            else if (aps_key.equals("ambush")) {
                crit_rate = crit_rates.get("ambush");
                dmg_tuple = this.ambush_dmg(avg_ap);
                dps_tuple = this.get_dps_contrib(dmg_tuple, crit_rate, aps);
                dmg_brkdwn.put("ambush", new Double[] {dps_tuple[0], dps_tuple[1]});
            }

            else if (aps_key.equals("mh_killing_spree")) {
                crit_rate = crit_rates.get("killing_spree");
                double aps_oh = aps_dict.get("oh_killing_spree")[0];
                double[] mh_dmg = this.mh_killing_spree_dmg(avg_ap);
                double[] oh_dmg = this.oh_killing_spree_dmg(avg_ap);
                double[] dps_tuple_mh = this.get_dps_contrib(mh_dmg, crit_rate, aps);
                double[] dps_tuple_oh = this.get_dps_contrib(oh_dmg, crit_rate, aps_oh);
                Double[] value = new Double [] {dps_tuple_mh[0] + dps_tuple_oh[0], dps_tuple_mh[1] + dps_tuple_oh[1]};
                dmg_brkdwn.put("killing_spree", value);
                if (this.stats().gear_buffs().exists_buff("rogue_t12_2pc")) {
                    double p_double_crit = Math.pow(crit_rate, 2);
                    double munch_per_sec = aps * p_double_crit;
                    dmg_brkdwn.put("ksp_munch", new Double[] {0., munch_per_sec * mh_dmg[1]});
                }
            }

            else if (aps_key.equals("rupture_ticks")) {
                double average_dps = 0;
                double crit_dps = 0;
                crit_rate = crit_rates.get("rupture_ticks");
                for (int i = 1; i < 6; i++) {
                    dmg_tuple = this.rupture_tick_dmg(avg_ap, i);
                    dps_tuple = this.get_dps_contrib(dmg_tuple, crit_rate, aps_tuple[i]);
                    average_dps += dps_tuple[0];
                    crit_dps += dps_tuple[1];
                }
                dmg_brkdwn.put("rupture", new Double[] {average_dps, crit_dps });
            }

            else if (aps_key.equals("garrote_ticks")) {
                crit_rate = crit_rates.get("garrote");
                dmg_tuple = this.garrote_tick_dmg(avg_ap);
                dps_tuple = this.get_dps_contrib(dmg_tuple, crit_rate, aps);
                dmg_brkdwn.put("garrote", new Double[] {dps_tuple[0], dps_tuple[1]});
            }

            else if (aps_key.equals("envenom")) {
                double average_dps = 0;
                double crit_dps = 0;
                crit_rate = crit_rates.get("envenom");
                for (int i = 1; i < 6; i++) {
                    dmg_tuple = this.envenom_dmg(avg_ap, i, current_stats.get("mastery"));
                    dps_tuple = this.get_dps_contrib(dmg_tuple, crit_rate, aps_tuple[i]);
                    average_dps += dps_tuple[0];
                    crit_dps += dps_tuple[1];
                }
                dmg_brkdwn.put("envenom", new Double[] {average_dps, crit_dps});
            }

            else if (aps_key.equals("eviscerate")) {
                double average_dps = 0;
                double crit_dps = 0;
                crit_rate = crit_rates.get("eviscerate");
                for (int i = 1; i < 6; i++) {
                    dmg_tuple = this.eviscerate_dmg(avg_ap, i);
                    dps_tuple = this.get_dps_contrib(dmg_tuple, crit_rate, aps_tuple[i]);
                    average_dps += dps_tuple[0];
                    crit_dps += dps_tuple[1];
                }
                dmg_brkdwn.put("eviscerate", new Double[] {average_dps, crit_dps});
            }

            else if (aps_key.equals("venomous_wounds")) {
                crit_rate = crit_rates.get("venomous_wounds");
                dmg_tuple = this.venomous_wounds_dmg(avg_ap, current_stats.get("mastery"));
                dps_tuple = this.get_dps_contrib(dmg_tuple, crit_rate, aps);
                dmg_brkdwn.put("venomous_wounds", new Double[] {dps_tuple[0], dps_tuple[1]});
            }

            else if (aps_key.equals("instant_poison")) {
                crit_rate = crit_rates.get("instant_poison");
                dmg_tuple = this.instant_poison_dmg(avg_ap, current_stats.get("mastery"));
                dps_tuple = this.get_dps_contrib(dmg_tuple, crit_rate, aps);
                dmg_brkdwn.put("instant_poison", new Double[] {dps_tuple[0], dps_tuple[1] });
            }

            else if (aps_key.equals("deadly_poison")) {
                crit_rate = crit_rates.get("deadly_poison");
                dmg_tuple = this.deadly_poison_tick_dmg(avg_ap, current_stats.get("mastery"));
                dps_tuple = this.get_dps_contrib(dmg_tuple, crit_rate, aps);
                dmg_brkdwn.put("deadly_poison", new Double[] {dps_tuple[0], dps_tuple[1]});
            }

            else if (aps_key.equals("wound_poison")) {
                crit_rate = crit_rates.get("wound_poison");
                dmg_tuple = this.wound_poison_dmg(avg_ap, current_stats.get("mastery"));
                dps_tuple = this.get_dps_contrib(dmg_tuple, crit_rate, aps);
                dmg_brkdwn.put("wound_poison", new Double[] {dps_tuple[0], dps_tuple[1]});
            }

            else if (aps_key.equals("hemorrhage_ticks")) {
                crit_rate = crit_rates.get("hemorrhage");
                double[] dmg_tuple_hit = this.hemorrhage_tick_dmg(avg_ap, false);
                double[] dmg_tuple_crit = this.hemorrhage_tick_dmg(avg_ap, true);
                double[] dps_from_hit_hemo = this.get_dps_contrib(dmg_tuple_hit, crit_rate, aps * (1 - crit_rate));
                double[] dps_from_crit_hemo = this.get_dps_contrib(dmg_tuple_crit, crit_rate, aps * crit_rate);
                dmg_brkdwn.put("hemorrhage_glyph", new Double[] {dps_from_hit_hemo[0] + dps_from_crit_hemo[0], dps_from_hit_hemo[1] + dps_from_crit_hemo[1]});
            }
        }

        for (Proc proc : dmg_procs) {
            if (!dmg_brkdwn.containsKey(proc.proc_name()))
                dmg_brkdwn.put(proc.proc_name(), new Double[] {0., 0.});
            Double[] old_value = dmg_brkdwn.get(proc.proc_name());
            double[] new_value = this.get_proc_damage_contribution(proc, aps_dict.get(proc.proc_name())[0], current_stats);
            dmg_brkdwn.put(proc.proc_name(), new Double[] {old_value[0] + new_value[0], old_value[1] + new_value[1]});
        }

        this.append_damage_on_use(avg_ap, current_stats, dmg_brkdwn);

        if (this.stats().gear_buffs().exists_buff("rogue_t12_2pc"))
            dmg_brkdwn.put("burning_wounds", this.get_t12_2p_damage(dmg_brkdwn));

        return dmg_brkdwn;
    }

    private double get_mh_procs_per_second(Proc proc, HashMap<String, Double[]> aps, HashMap<String, Double> crit_rates) {
        double triggers_ps = 0;  // stands for triggers per second.
        if (proc.procs_off("off_auto_attacks")) {
            if (proc.procs_on_crit_only())
                triggers_ps += aps.get("mh_autoattacks")[0] * crit_rates.get("mh_autoattacks");
            else
                triggers_ps += aps.get("mh_autoattack_hits")[0];
        }
        if (proc.procs_off("off_strikes")) {
            for (String ability : util.mkSet("mutilate", "backstab",
                    "revealing_strike", "sinister_strike", "ambush",
                    "hemorrhage", "mh_killing_spree", "main_gauche")) {
                if (aps.containsKey(ability)) {
                    if (proc.procs_on_crit_only())
                        triggers_ps += aps.get(ability)[0] * crit_rates.get(ability);
                    else
                        triggers_ps += aps.get(ability)[0];
                }
            }
            for (String ability : util.mkSet("envenom", "eviscerate")) {
                if (aps.containsKey(ability)) {
                    if (proc.procs_on_crit_only())
                        triggers_ps += util.sumArray(aps.get(ability)) * crit_rates.get(ability);
                    else
                        triggers_ps += util.sumArray(aps.get(ability));
                }
            }
        }
        if (proc.procs_off("off_apply_debuff")) {
            if (aps.containsKey("rupture")) {
                if (!proc.procs_on_crit_only())
                    triggers_ps += aps.get("rupture")[0];
            }
            if (aps.containsKey("garrote")) {
                if (!proc.procs_on_crit_only())
                    triggers_ps += aps.get("garrote")[0];
            }
            if (aps.containsKey("hemorrhage_ticks")) {
                if (!proc.procs_on_crit_only())
                    triggers_ps += aps.get("hemorrhage")[0];
            }
        }
        return triggers_ps * proc.proc_rate(this.stats().weapon("mh").speed());
    }

    private double get_oh_procs_per_second(Proc proc, HashMap<String, Double[]> aps, HashMap<String, Double> crit_rates) {
        double triggers_ps = 0;  // stands for triggers per second.
        if (proc.procs_off("off_auto_attacks")) {
            if (proc.procs_on_crit_only())
                triggers_ps += aps.get("oh_autoattacks")[0] * crit_rates.get("oh_autoattacks");
            else
                triggers_ps += aps.get("oh_autoattack_hits")[0];
        }
        if (proc.procs_off("off_strikes")) {
            for (String ability : util.mkSet("mutilate", "oh_killing_spree")) {
                if (aps.containsKey(ability)) {
                    if (proc.procs_on_crit_only())
                        triggers_ps += aps.get(ability)[0] * crit_rates.get(ability);
                    else
                        triggers_ps += aps.get(ability)[0];
                }
            }
        }
        return triggers_ps * proc.proc_rate(this.stats().weapon("oh").speed());
    }

    private double get_other_procs_per_second(Proc proc, HashMap<String, Double[]> aps, HashMap<String, Double> crit_rates) {
        double triggers_ps = 0;  // stands for triggers per second.
        if (proc.procs_off("off_harmful_spells")) {
            for (String ability : util.mkSet("instant_poison", "wound_poison", "venomous_wounds")) {
                if (aps.containsKey(ability)) {
                    if (proc.procs_on_crit_only())
                        triggers_ps += aps.get(ability)[0] * crit_rates.get(ability);
                    else
                        triggers_ps += aps.get(ability)[0];
                }
            }
        }
        if (proc.procs_off("off_bleeds")) {
            if (aps.containsKey("rupture_ticks")) {
                if (!proc.procs_on_crit_only())
                    triggers_ps += util.sumArray(aps.get("rupture_ticks")) * crit_rates.get("rupture");
                else
                    triggers_ps += util.sumArray(aps.get("rupture_ticks"));
            }
            if (aps.containsKey("garrote_ticks")) {
                if (!proc.procs_on_crit_only())
                    triggers_ps += util.sumArray(aps.get("garrote_ticks")) * crit_rates.get("garrote");
                else
                    triggers_ps += util.sumArray(aps.get("garrote_ticks"));
            }
            if (aps.containsKey("hemorrhage_ticks")) {
                if (!proc.procs_on_crit_only())
                    triggers_ps += util.sumArray(aps.get("hemorrhage_ticks")) * crit_rates.get("hemorrhage");
                else
                    triggers_ps += util.sumArray(aps.get("hemorrhage_ticks"));
            }
        }
        if (proc.is_ppm()) {
            if (triggers_ps == 0)
                return 0;
            else
                throw new InputNotModeledException(String.format("PPMs that also proc off spells are not yet modeled."));
        }
        return triggers_ps * proc.proc_rate();
    }

    private double get_procs_ps(Proc proc, HashMap<String, Double[]> aps, HashMap<String, Double> crit_rates) {
        // TO DO: Include damaging proc hits in figuring out how often
        // everything else procs.
        double procs_ps;  // stands for procs per second.

        if (proc.weapon_flag().equals("mh_only"))
            procs_ps = this.get_mh_procs_per_second(proc, aps, crit_rates);
        else if (proc.weapon_flag().equals("oh_only"))
            procs_ps = this.get_oh_procs_per_second(proc, aps, crit_rates);
        else
            procs_ps = this.get_mh_procs_per_second(proc, aps, crit_rates) + this.get_oh_procs_per_second(proc, aps, crit_rates) + this.get_other_procs_per_second(proc, aps, crit_rates);

        return procs_ps;
    }

    private void set_uptime(Proc proc, HashMap<String, Double[]> aps,
            HashMap<String, Double> crit_rates) {
        double procs_ps = this.get_procs_ps(proc, aps, crit_rates);

        if (proc.icd() != 0)
            proc.set_uptime(proc.duration() / (proc.icd() + 1. / procs_ps));
        else {
            if (procs_ps >= 1 && proc.duration() >= 1)
                proc.set_uptime(proc.max_stacks());
            else {
                double q = 1 - procs_ps;
                double Q = Math.pow(q, proc.duration());
                double P = 1 - Q;
                proc.set_uptime(P * (1 - Math.pow(P, proc.max_stacks())) / Q);
            }
        }
    }

    private void update_with_damaging_proc(Proc proc, HashMap<String, Double[]> aps, HashMap<String, Double> crit_rates) {
        double frequency;

        if (proc.icd() != 0)
            frequency = 1. / (proc.icd() + 0.5 / this.get_procs_ps(proc, aps, crit_rates));
        else
            frequency = this.get_procs_ps(proc, aps, crit_rates);

        if (proc.stat().equals("spell_damage"))
            aps.put(proc.proc_name(), new Double[] {frequency * this.spell_hit_chance()});
        else if (proc.stat().equals("physical_damage"))
            aps.put(proc.proc_name(), new Double[] {frequency * this.strike_hit_chance});
    }

    /*
    @Override
    public double get_weapon_damage_bonus() {
        double bonus = 0;
        return bonus;
    }
     */

    private void update_crit_rates_for_4pc_t11(HashMap<String, Double[]> aps, HashMap<String, Double> crit_rates) {
        boolean t11_4pc_bonus = this.stats().procs().exists_proc("rogue_t11_4pc");
        String direct_damage_finisher;
        if (t11_4pc_bonus) {
            direct_damage_finisher = "";
            for (String key : util.mkSet("envenom", "eviscerate")) {
                if (aps.containsKey(key) && util.sumArray(aps.get(key)) != 0) {
                    if (!direct_damage_finisher.equals(""))
                        throw new InputNotModeledException(String.format("Unable to model the 4pc T11 set bonus in a cycle that uses both eviscerate and envenom."));
                    direct_damage_finisher = key;
                }
            }
            if (!direct_damage_finisher.equals("")) {
                Proc proc = this.stats().procs().get_proc("rogue_t11_4pc");
                double procs_per_second = this.get_procs_ps(proc, aps, crit_rates);
                double finisher_spacing = Math.min(1 / util.sumArray(aps.get(direct_damage_finisher)), proc.duration());
                double p = 1 - Math.pow((1 - procs_per_second), finisher_spacing);
                crit_rates.put(direct_damage_finisher, p + (1 - p) * crit_rates.get(direct_damage_finisher));
            }
        }
    }

    private double get_4pc_t12_multiplier() {
        if (this.settings().tricks_on_cooldown()) {
            double tricks_uptime = 30. / (30 + this.settings().response_time());
            return 1 + this.stats().gear_buffs().rogue_t12_4pc_stat_bonus() * tricks_uptime / 3;
        }
        return 1.;
    }

    private void get_poison_counts(double total_mh_hits, double total_oh_hits, HashMap<String, Double[]> aps) {
        if (this.settings().mh_poison().equals("dp") || this.settings().oh_poison().equals("dp"))
            aps.put("deadly_poison", new Double[] {1. / 3});

        double mh_proc_rate;
        if (this.settings().mh_poison().equals("ip"))
            mh_proc_rate = this.stats().weapon("mh").speed() / 7.;
        else if (this.settings().mh_poison().equals("wp"))
            mh_proc_rate = this.stats().weapon("mh").speed() / 2.8;
        else
            mh_proc_rate = .3;  // Deadly Poison


        double oh_proc_rate;
        if (this.settings().oh_poison().equals("ip"))
            oh_proc_rate = this.stats().weapon("oh").speed() / 7.;
        else if (this.settings().mh_poison().equals("wp"))
            oh_proc_rate = this.stats().weapon("oh").speed() / 2.8;
        else
            oh_proc_rate = .3;  // Deadly Poison

        double mh_poison_procs = total_mh_hits * mh_proc_rate * this.spell_hit_chance();
        double oh_poison_procs = total_oh_hits * oh_proc_rate * this.spell_hit_chance();

        String poison_setup = this.settings().mh_poison() + this.settings().oh_poison();
        if (util.mkSet("ipip", "ipdp", "dpip").contains(poison_setup))
            aps.put("instant_poison", new Double[] {mh_poison_procs + oh_poison_procs});
        else if (util.mkSet("wpwp", "wpdp", "dpwp").contains(poison_setup))
            aps.put("wound_poison", new Double[] {mh_poison_procs + oh_poison_procs});
        else if (poison_setup.equals("ipwp")) {
            aps.put("instant_poison", new Double[] {mh_poison_procs});
            aps.put("wound_poison", new Double[] {oh_poison_procs});
        } else if (poison_setup.equals("wpip")) {
            aps.put("wound_poison", new Double[] {mh_poison_procs});
            aps.put("instant_poison", new Double[] {oh_poison_procs});
        }
    }

    private HashMap<String, Double> compute_dmg(String attack_counts_string) {
        // TO DO: Crit cap
        //
        // TO DO: Hit/Exp procs
        Pair<HashMap<String, Double[]>, HashMap<String, Double>> aps_return;
        HashMap<String, Double[]> aps;
        HashMap<String, Double> crit_rates;

        HashMap<String, Double> current_stats = new HashMap<String, Double>();
        current_stats.put("agi",     this.base_stats.get("agi") * this.agi_mult);
        current_stats.put("ap",      this.base_stats.get("ap"));
        current_stats.put("crit",    this.base_stats.get("crit"));
        current_stats.put("haste",   this.base_stats.get("haste"));
        current_stats.put("mastery", this.base_stats.get("mastery"));

        List<Proc> active_procs = new ArrayList<Proc>();
        List<Proc> dmg_procs = new ArrayList<Proc>();
        List<Proc> weapon_damage_procs = new ArrayList<Proc>();

        for (Proc proc_info : this.stats().procs().get_all_procs_for_stat()) {
            if (current_stats.containsKey(proc_info.stat()) && !proc_info.is_ppm())
                active_procs.add(proc_info);
            else if (util.mkSet("spell_damage", "physical_damage").contains(proc_info.stat()))
                dmg_procs.add(proc_info);
            else if (proc_info.stat().equals("extra_weapon_damage"))
                weapon_damage_procs.add(proc_info);
        }

        Proc mh_enchant = this.stats().weapon("mh").enchant();
        if (mh_enchant != null && mh_enchant.proc_name().equals("Landslide")) {
            mh_enchant.set_weapon_flag("mh_only");
            active_procs.add(mh_enchant);
        }
        if (mh_enchant != null && mh_enchant.proc_name().equals("Hurricane")) {
            mh_enchant.set_weapon_flag("mh_only");
            active_procs.add(mh_enchant);
        }

        Proc oh_enchant = this.stats().weapon("oh").enchant();
        if (oh_enchant != null && oh_enchant.proc_name().equals("Landslide")) {
            oh_enchant.set_weapon_flag("oh_only");
            active_procs.add(oh_enchant);
        }
        if (oh_enchant != null && oh_enchant.proc_name().equals("Hurricane")) {
            oh_enchant.set_weapon_flag("oh_only");
            active_procs.add(oh_enchant);
        }

        aps_return = run_attack_counts(current_stats, attack_counts_string);
        aps = aps_return.getFirst();
        crit_rates = aps_return.getSecond();

        while (true) {
            current_stats = new HashMap<String, Double>();
            current_stats.put("agi",     this.base_stats.get("agi"));
            current_stats.put("ap",      this.base_stats.get("ap"));
            current_stats.put("crit",    this.base_stats.get("crit"));
            current_stats.put("haste",   this.base_stats.get("haste"));
            current_stats.put("mastery", this.base_stats.get("mastery"));

            this.update_crit_rates_for_4pc_t11(aps, crit_rates);

            for (Proc proc : dmg_procs) {
                if (proc.icd() == 0)
                    this.update_with_damaging_proc(proc, aps, crit_rates);
            }
            for (Proc proc : active_procs) {
                if (proc.icd() == 0) {
                    this.set_uptime(proc, aps, crit_rates);
                    current_stats.put(proc.stat(), current_stats.get(proc.stat()) + proc.uptime() * proc.value());
                }
            }

            current_stats.put("agi", current_stats.get("agi") * this.agi_mult);
            for (String stat : util.mkSet("crit", "haste", "mastery")) {
                current_stats.put(stat, current_stats.get(stat) * this.get_4pc_t12_multiplier());
            }

            HashMap<String, Double[]> old_aps = aps;
            aps_return = run_attack_counts(current_stats, attack_counts_string);
            aps = aps_return.getFirst();
            crit_rates = aps_return.getSecond();

            if (this.are_close_enough(old_aps, aps))
                break;
        }

        for (Proc proc : active_procs) {
            if (proc.icd() != 0) {
                this.set_uptime(proc, aps, crit_rates);
                if (proc.stat().equals("agi"))
                    current_stats.put("agi", current_stats.get("agi") + proc.uptime()* proc.value() * this.agi_mult);
                else if (util.mkSet("crit", "haste", "mastery").contains(proc.stat()))
                    current_stats.put(proc.stat(), current_stats.get(proc.stat()) + proc.uptime() * proc.value() * this.get_4pc_t12_multiplier());
                else
                    current_stats.put(proc.stat(), current_stats.get(proc.stat()) + proc.uptime() * proc.value());
            }
        }

        aps_return = run_attack_counts(current_stats, attack_counts_string);
        aps = aps_return.getFirst();
        crit_rates = aps_return.getSecond();

        this.update_crit_rates_for_4pc_t11(aps, crit_rates);

        for (Proc proc : dmg_procs) {
            this.update_with_damaging_proc(proc, aps, crit_rates);
        }

        for (Proc proc : weapon_damage_procs) {
            this.set_uptime(proc, aps, crit_rates);
        }

        HashMap<String, Double[]> dmg_brkdwn = this.get_dmg_brkdwn(current_stats, aps, crit_rates, dmg_procs);

        HashMap<String, Double> dmg_brkdwn_no_crit = new HashMap<String, Double>();
        for (Entry<String, Double[]> item : dmg_brkdwn.entrySet()) {
            dmg_brkdwn_no_crit.put(item.getKey(), item.getValue()[0]);
        }

        return dmg_brkdwn_no_crit;
    }

    // /////////////////////////////////////////////////////////////////////////
    // Attack counts function selection.
    // /////////////////////////////////////////////////////////////////////////

    private Pair<HashMap<String, Double[]>, HashMap<String, Double>> run_attack_counts(HashMap<String, Double> current_stats, String selection) {
        if (selection.equals("assassination_attack_counts_mutilate"))
            return this.assassination_attack_counts(current_stats, "mutilate", this.settings().cycle().min_envenom_size_mutilate());
        else if (selection.equals("assassination_attack_counts_backstab"))
            return this.assassination_attack_counts(current_stats, "backstab", this.settings().cycle().min_envenom_size_backstab());
        else if (selection.equals("combat_attack_counts"))
            return this.combat_attack_counts(current_stats);
        else if (selection.equals("subtlety_attack_counts"))
            return this.subtlety_attack_counts(current_stats);

        return null;
    }

    // /////////////////////////////////////////////////////////////////////////
    // Assassination DPS functions.
    // /////////////////////////////////////////////////////////////////////////

    /**
     * Call this before calling any of the assassination_dps functions directly.
     * If you're just calling get_dps, you can ignore this as it happens
     * automatically; however, if you're going to pull a damage breakdown or
     * other sub-result, make sure to call this, as it initializes many values
     * that are needed to perform the calculations.
     */
    public void init_assassination() {
        if (!this.settings().cycle().cycle_type().equals("assassination"))
            throw new InputNotModeledException("You must specify an assassination cycle to match your assassination spec.");
        if (!this.stats().weapon("mh").type().equals("dagger") || !this.stats().weapon("oh").type().equals("dagger"))
            throw new InputNotModeledException("Assassination modeling requires daggers in both hands.");
        if (!util.mkSet("ipdp", "dpip").contains(this.settings().mh_poison() + this.settings().oh_poison()))
            throw new InputNotModeledException("Assassination modeling requires instant poison on one weapon and deadly on the other");
        // These talents have huge, hard-to-model implications on cycle and
        // will always be taken in any serious DPS build. Hence, I'm not going
        // to worry about modeling them for the foreseeable future.
        if (this.talents().get("master_poisoner") != 1)
            throw new InputNotModeledException("Assassination modeling requires one point in Master Poisoner");
        if (this.talents().get("cut_to_the_chase") != 3)
            throw new InputNotModeledException("Assassination modeling requires three points in Cut to the Chase");

        this.set_constants();

        this.env_e_cost = 28 + 7 / this.strike_hit_chance;

        this.base_e_regen = 10;
        if (this.talents().get("overkill") == 1) {
            this.base_e_regen += 60 / (180. + this.settings().response_time() - 30 * this.talents().get("elusiveness"));
            this.base_e_regen += 60. / this.settings().duration() * (1 - 20. / this.settings().duration());
        }
        if (this.talents().get("cold_blood") == 1) {
            this.bonus_e_regen += 25. / (120 + this.settings().response_time());
        }

        if (this.talents().get("vendetta") == 1) {
            if (this.glyphs().exists_glyph("vendetta"))
                this.vendetta_mult = 1.06;
            else
                this.vendetta_mult = 1.05;
        }
        else
            this.vendetta_mult = 1.;
    }


    public double assassination_dps_estimate() {
        double mutilate_dps = this.assassination_dps_estimate_mutilate() * (1 - this.settings().time_in_execute_range());
        double backstab_dps = this.assassination_dps_estimate_backstab() * this.settings().time_in_execute_range();
        return backstab_dps + mutilate_dps;
    }


    public double assassination_dps_estimate_backstab() {
        HashMap<String, Double> dps_breakdown = this.assassination_dps_breakdown_backstab();
        double dps_estimate = 0;
        for (Entry<String, Double> item : dps_breakdown.entrySet()) {
            dps_estimate += item.getValue();
        }
        return dps_estimate;
    }


    public double assassination_dps_estimate_mutilate() {
        Map<String, Double> dps_breakdown = this.assassination_dps_breakdown_mutilate();
        double dps_estimate = 0;
        for (Entry<String, Double> item : dps_breakdown.entrySet()) {
            dps_estimate += item.getValue();
        }
        return dps_estimate;
    }


    public Map<String, Double> assassination_dps_breakdown() {
        Map<String, Double> mut_dps_brkdwn = this.assassination_dps_breakdown_mutilate();
        Map<String, Double> bstab_dps_brkdwn = this.assassination_dps_breakdown_backstab();

        double mutilate_weight = 1 - this.settings().time_in_execute_range();
        double backstab_weight = this.settings().time_in_execute_range();

        Map<String, Double> dps_brkdwn = new HashMap<String, Double>();
        for (Entry<String, Double> item : mut_dps_brkdwn.entrySet()) {
            String source = item.getKey();
            double quantity = item.getValue();
            dps_brkdwn.put(source, quantity * mutilate_weight);
        }
        for (Entry<String, Double> item : bstab_dps_brkdwn.entrySet()) {
            String source = item.getKey();
            double quantity = item.getValue();
            if (dps_brkdwn.containsKey(source))
                dps_brkdwn.put(source, dps_brkdwn.get(source) + quantity * backstab_weight);
            else
                dps_brkdwn.put(source, quantity * backstab_weight);
        }
        return dps_brkdwn;
    }


    public HashMap<String, Double> assassination_dps_breakdown_mutilate() {
        HashMap<String, Double> damage_breakdown = this.compute_dmg("assassination_attack_counts_mutilate");
        for (Entry<String, Double> item : damage_breakdown.entrySet()) {
            damage_breakdown.put(item.getKey(), item.getValue() * this.vendetta_mult);
        }
        return damage_breakdown;
    }


    public HashMap<String, Double> assassination_dps_breakdown_backstab() {
        HashMap<String, Double> damage_breakdown = this.compute_dmg("assassination_attack_counts_backstab");
        for (Entry<String, Double> item : damage_breakdown.entrySet()) {
            damage_breakdown.put(item.getKey(), item.getValue() * this.vendetta_mult);
        }
        return damage_breakdown;
    }


    public Pair<HashMap<String, Double[]>, HashMap<String, Double>> assassination_attack_counts(HashMap<String, Double> current_stats, String cpg, int finisher_size) {

        double base_melee_crit_rate = this.melee_crit_rate(current_stats.get("agi"), current_stats.get("crit"));
        double base_spell_crit_rate = this.spell_crit_rate(current_stats.get("crit"));

        double haste_mult = this.stats().get_haste_mult_from_rating(current_stats.get("haste"));

        double e_regen = this.base_e_regen * haste_mult;
        e_regen += this.bonus_e_regen;

        double garrote_base_cost = 9 + 36 * this.strike_hit_chance;
        double garrote_e_return = 6 * this.talents().get("venomous_wounds") * 3 * this.strike_hit_chance;
        double garrote_net_cost = garrote_base_cost - garrote_e_return;
        double garrote_spacing = (180. + this.settings().response_time() - 30 * this.talents().get("elusiveness"));
        double total_garrotes_ps = (1 - 20. / this.settings().duration()) / this.settings().duration() + 1 / garrote_spacing;

        e_regen -= garrote_net_cost * total_garrotes_ps;

        double e_regen_with_rup = e_regen + 1.5 * this.talents().get("venomous_wounds");

        double attack_speed_mult = this.base_speed_mult * haste_mult;

        double cpg_crit_rate = base_melee_crit_rate + this.stats().gear_buffs().rogue_t11_2pc_crit_bonus();
        if (cpg.equals("mutilate"))
            cpg_crit_rate += .05 * this.talents().get("puncturing_wounds");
        else
            cpg_crit_rate += .1 * this.talents().get("puncturing_wounds");

        if (cpg_crit_rate > 1)
            cpg_crit_rate = 1;

        HashMap<String, Double> crit_rates = new HashMap<String, Double>();
        crit_rates.put("mh_autoattacks",  new Double(Math.min(base_melee_crit_rate, this.dw_mh_hit_chance() - GLANCE_RATE)));
        crit_rates.put("oh_autoattacks",  new Double(Math.min(base_melee_crit_rate, this.dw_oh_hit_chance() - GLANCE_RATE)));
        crit_rates.put(cpg,               new Double(cpg_crit_rate));
        crit_rates.put("envenom",         new Double(base_melee_crit_rate));
        crit_rates.put("rupture_ticks",   new Double(base_melee_crit_rate));
        crit_rates.put("venomous_wounds", new Double(base_spell_crit_rate));
        crit_rates.put("instant_poison",  new Double(base_spell_crit_rate));
        crit_rates.put("deadly_poison",   new Double(base_spell_crit_rate));
        crit_rates.put("garrote",         new Double(base_melee_crit_rate));

        double cpg_e_cost;
        if (cpg.equals("mutilate")) {
            cpg_e_cost = 48 + 12 / this.strike_hit_chance;
            if (this.glyphs().exists_glyph("mutilate"))
                cpg_e_cost -= 5;
        }
        else {
            cpg_e_cost = 48 + 12 / this.strike_hit_chance;
            cpg_e_cost -= 15 * this.talents().get("murderous_intent");
            if (this.glyphs().exists_glyph("backstab"))
                cpg_e_cost -= 5 * cpg_crit_rate;
        }

        double seal_fate_proc_rate;
        HashMap<Integer, Double> cp_per_cpg = new HashMap<Integer, Double>();
        if (cpg.equals("mutilate")) {
            seal_fate_proc_rate = 1 - Math.pow((1 - cpg_crit_rate * .5 * this.talents().get("seal_fate")), 2);
            cp_per_cpg.put(2, new Double(1 - seal_fate_proc_rate));
            cp_per_cpg.put(3, new Double(seal_fate_proc_rate));
        }
        else {
            seal_fate_proc_rate = cpg_crit_rate * .5 * this.talents().get("seal_fate");
            cp_per_cpg.put(1, new Double(1 - seal_fate_proc_rate));
            cp_per_cpg.put(2, new Double(seal_fate_proc_rate));
        }
        double avg_cp_per_cpg = 0;
        for (Entry<Integer, Double> item : cp_per_cpg.entrySet()) {
            avg_cp_per_cpg += item.getKey() * item.getValue();
        }

        Pair<HashMap<Pair<Integer, Integer>, Double>, double[]> return_tuple;
        return_tuple = this.get_cp_dist_for_cycle(cp_per_cpg, finisher_size);
        HashMap<Pair<Integer, Integer>, Double> cp_dist = return_tuple.getFirst();
        double[] rup_sizes = return_tuple.getSecond();

        double avg_rup_size = 0;
        for (int i = 0; i < 6; i++) {
            avg_rup_size += i * rup_sizes[i];
        }

        double avg_rup_length = 2 * (3 + avg_rup_size + 2 * this.glyphs().get("rupture"));
        double avg_gap = .5 * (1 / this.strike_hit_chance - 1 + .5 * this.settings().response_time());
        double avg_cycle_length = avg_gap + avg_rup_length;

        double cpg_per_rup = (avg_rup_size - .2 * this.talents().get("ruthlessness")) / avg_cp_per_cpg;
        double e_for_rupture = cpg_per_rup * cpg_e_cost + this.base_rup_e_cost - avg_rup_size * this.relentless_strikes_e_return_per_cp;

        double cpg_per_finisher = 0;
        double cp_per_finisher = 0;
        double[] env_size_brkdwn = new double[] {0, 0, 0, 0, 0, 0};
        for (Entry<Pair<Integer, Integer>, Double> item : cp_dist.entrySet()) {
            int cps = item.getKey().getFirst();
            int cpgs = item.getKey().getSecond();
            double prob = item.getValue();
            cpg_per_finisher += cpgs * prob;
            cp_per_finisher += cps * prob;
            env_size_brkdwn[cps] += prob;
        }

        double e_per_cycle = avg_rup_length * e_regen_with_rup + avg_gap * e_regen;
        double e_for_envenoms = e_per_cycle - e_for_rupture;
        double env_e_cost_ = cpg_per_finisher * cpg_e_cost + this.env_e_cost - cp_per_finisher * this.relentless_strikes_e_return_per_cp;
        double envenoms_per_cycle = e_for_envenoms / env_e_cost_;

        HashMap<String, Double[]> aps = new HashMap<String, Double[]>();

        double envenoms_ps = envenoms_per_cycle / avg_cycle_length;
        aps.put("rupture", new Double[] {1 / avg_cycle_length});
        aps.put(cpg,       new Double[] {envenoms_ps * cpg_per_finisher + aps.get("rupture")[0] * cpg_per_rup});
        aps.put("garrote", new Double[] {this.strike_hit_chance * total_garrotes_ps});

        envenoms_ps += aps.get("garrote")[0] / cp_per_finisher;

        double envenoms_per_cold_blood;
        if (this.talents().get("cold_blood") != 0) {
            envenoms_per_cold_blood = 120 * envenoms_ps;
            double env_crit_rate = ((envenoms_per_cold_blood - 1) * crit_rates.get("envenom") + 1) / envenoms_per_cold_blood;
            crit_rates.put("envenom", env_crit_rate);
        }

        aps.put("envenom", new Double[] {0., 0., 0., 0., 0., 0.});
        for (int i = 0; i < env_size_brkdwn.length; i++) {
            aps.get("envenom")[i] = env_size_brkdwn[i] * envenoms_ps;
        }

        aps.put("rupture_ticks", new Double[] {0., 0., 0., 0., 0., 0.});
        for (int i = 0; i < 6; i++) {
            int ticks_per_rup = 3 + i + 2 * this.glyphs().get("rupture");
            aps.get("rupture_ticks")[i] = ticks_per_rup * aps.get("rupture")[0] * rup_sizes[i];
        }

        double total_rup_ticks = util.sumArray(aps.get("rupture_ticks"));
        aps.put("garrote_ticks", new Double[] {6 * aps.get("garrote")[0]});
        aps.put("venomous_wounds", new Double[] {(total_rup_ticks + aps.get("garrote_ticks")[0]) * .3 * this.talents().get("venomous_wounds") * this.spell_hit_chance()});

        double mh_speed = this.stats().weapon("mh").speed();
        double oh_speed = this.stats().weapon("oh").speed();
        double mh_aps = attack_speed_mult / mh_speed * (1 - Math.max((1 - .5 * mh_speed / attack_speed_mult), 0) / garrote_spacing);
        double oh_aps = attack_speed_mult / oh_speed * (1 - Math.max((1 - .5 * oh_speed / attack_speed_mult), 0) / garrote_spacing);
        aps.put("mh_autoattacks",     new Double[] {mh_aps});
        aps.put("oh_autoattacks",     new Double[] {oh_aps});
        aps.put("mh_autoattack_hits", new Double[] {mh_aps * this.dw_mh_hit_chance()});
        aps.put("oh_autoattack_hits", new Double[] {oh_aps * this.dw_oh_hit_chance()});

        double total_mh_hits_ps = aps.get("mh_autoattack_hits")[0] + aps.get(cpg)[0] + envenoms_ps + aps.get("rupture")[0] + aps.get("garrote")[0];
        double total_oh_hits_ps = aps.get("oh_autoattack_hits")[0];
        if (cpg.equals("mutilate"))
            total_oh_hits_ps += aps.get(cpg)[0];

        double ip_base_proc_rate;
        if (this.settings().mh_poison().equals("ip"))
            ip_base_proc_rate = .3 * this.stats().weapon("mh").speed() / 1.4;
        else
            ip_base_proc_rate = .3 * this.stats().weapon("oh").speed() / 1.4;

        double ip_env_proc_rate = ip_base_proc_rate * 1.5;

        double dp_base_proc_rate = .5;
        double dp_env_proc_rate = dp_base_proc_rate + .15;

        double sum = 0;
        for (int cps = 1; cps < 6; cps++) {
            sum += (1 / this.strike_hit_chance + cps) * aps.get("envenom")[cps];
        }
        double env_uptime = Math.min(sum, 1);
        double avg_ip_proc_rate = ip_base_proc_rate * (1 - env_uptime) + ip_env_proc_rate * env_uptime;
        double avg_dp_proc_rate = dp_base_proc_rate * (1 - env_uptime) + dp_env_proc_rate * env_uptime;

        double mh_poison_procs;
        double oh_poison_procs;
        if (this.settings().mh_poison().equals("ip")) {
            mh_poison_procs = avg_ip_proc_rate * total_mh_hits_ps;
            oh_poison_procs = avg_dp_proc_rate * total_oh_hits_ps;
        }
        else {
            mh_poison_procs = avg_dp_proc_rate * total_mh_hits_ps;
            oh_poison_procs = avg_ip_proc_rate * total_oh_hits_ps;
        }

        aps.put("instant_poison", new Double[] {(mh_poison_procs + oh_poison_procs) * this.spell_hit_chance()});
        aps.put("deadly_poison", new Double[] {1. / 3});

        return new Pair<HashMap<String, Double[]>, HashMap<String, Double>>(aps, crit_rates);
    }


    // /////////////////////////////////////////////////////////////////////////
    // Combat DPS functions.
    // /////////////////////////////////////////////////////////////////////////

    public double combat_dps_estimate() {
        Map<String, Double> dps_breakdown = this.combat_dps_breakdown();
        double dps_estimate = 0;
        for (Entry<String, Double> item : dps_breakdown.entrySet()) {
            dps_estimate += item.getValue();
        }
        return dps_estimate;
    }

    public Map<String, Double> combat_dps_breakdown() {
        if (!this.settings().cycle().cycle_type().equals("combat"))
            throw new InputNotModeledException("You must specify a combat cycle to match your combat spec.");
        if (!(util.mkSet("sometimes", "always", "never")).contains(this.settings().cycle().use_revealing_strike()))
            throw new InputNotModeledException("Revealing strike usage must be set to always, sometimes, or never.");
        if (this.talents().get("revealing_strike") == 0 && !this.settings().cycle().use_revealing_strike().equals("never"))
            throw new InputNotModeledException("Cannot specify revealing strike usage in cycle without taking the talent.");

        this.set_constants();

        if (this.talents().get("bandits_guile") != 0)
            this.max_bg_buff = 1.3;
        else
            this.max_bg_buff = 1;

        this.base_rvs_e_cost = 32 + 8 / this.strike_hit_chance;
        this.base_ss_e_cost = 36 + 9 / this.strike_hit_chance - 2 * this.talents().get("improved_sinister_strike");

        this.base_e_regen = 12.5;

        HashMap<String, Double> dmg_brkdwn = this.compute_dmg("combat_attack_counts");

        for (Entry<String, Double> item : dmg_brkdwn.entrySet()) {
            String ability = item.getKey();
            double value = item.getValue();
            if (ability.equals("killing_spree")) {
                int ksp_bool_value = this.glyphs().get("killing_spree");
                if (this.settings().cycle().ksp_immediately())
                    dmg_brkdwn.put(ability, value * this.bg_mult * (1.2 + .1 * ksp_bool_value));
                else
                    dmg_brkdwn.put(ability, value * this.max_bg_buff * (1.2 + .1 * ksp_bool_value));
            }
            else if (util.mkSet("sinister_strike", "revealing_strike").contains(ability))
                dmg_brkdwn.put(ability, value * this.bg_mult);
            else if (ability.equals("eviscerate"))
                dmg_brkdwn.put(ability, value * this.bg_mult * this.rvs_mult);
            else if (ability.equals("rupture"))
                dmg_brkdwn.put(ability, value * this.bg_mult * this.ksp_mult * this.rvs_mult);
            else if (util.mkSet("autoattack", "instant_poison", "deadly_poison", "main_gauche").contains(ability))
                dmg_brkdwn.put(ability, value * this.bg_mult * this.ksp_mult);
            else
                dmg_brkdwn.put(ability, value * this.ksp_mult);
        }
        return dmg_brkdwn;
    }

    public Pair<HashMap<String, Double[]>, HashMap<String, Double>> combat_attack_counts(HashMap<String, Double> current_stats) {
        HashMap<String, Double[]> aps = new HashMap<String, Double[]>();

        double base_melee_crit_rate = this.melee_crit_rate(current_stats.get("agi"), current_stats.get("crit"));
        double base_spell_crit_rate = this.spell_crit_rate(current_stats.get("crit"));

        double haste_mult = this.stats().get_haste_mult_from_rating(current_stats.get("haste"));

        double attack_speed_mult = this.base_speed_mult * haste_mult * (1 + .02 * this.talents().get("lightning_reflexes"));

        double mh_aps = attack_speed_mult / this.stats().weapon("mh").speed();
        double oh_aps = attack_speed_mult / this.stats().weapon("oh").speed();
        aps.put("mh_autoattacks", new Double[] {mh_aps});
        aps.put("oh_autoattacks", new Double[] {oh_aps});
        aps.put("mh_autoattack_hits", new Double[] {mh_aps * this.dw_mh_hit_chance()});
        aps.put("oh_autoattack_hits", new Double[] {oh_aps * this.dw_oh_hit_chance()});

        double mg_proc_rate = .02 * this.stats().get_mastery_from_rating(current_stats.get("mastery")) * this.one_hand_melee_hit_chance();
        aps.put("main_gauche", new Double[] {mg_proc_rate * aps.get("mh_autoattack_hits")[0]});

        double autoattack_cp_regen = this.talents() .get("combat_potency") * (aps.get("oh_autoattack_hits")[0] + aps.get("main_gauche")[0]);
        double e_regen = this.base_e_regen * haste_mult + this.bonus_e_regen + autoattack_cp_regen;

        double combat_potency_e_return = mg_proc_rate * this.talents().get("combat_potency");
        double rup_e_cost = this.base_rup_e_cost - combat_potency_e_return;
        double evis_e_cost = this.base_evis_e_cost - combat_potency_e_return;
        double rvs_e_cost = this.base_rvs_e_cost - combat_potency_e_return;
        double ss_e_cost = this.base_ss_e_cost - combat_potency_e_return;

        int evis_bool_value = this.glyphs().get("eviscerate");

        HashMap<String, Double> crit_rates = new HashMap<String, Double>();
        crit_rates.put("mh_autoattacks",   new Double(Math.min(base_melee_crit_rate, this.dw_mh_hit_chance() - GLANCE_RATE)));
        crit_rates.put("oh_autoattacks",   new Double(Math.min(base_melee_crit_rate, this.dw_oh_hit_chance() - GLANCE_RATE)));
        crit_rates.put("main_gauche",      new Double(base_melee_crit_rate));
        crit_rates.put("sinister_strike",  new Double(base_melee_crit_rate + this.stats().gear_buffs().rogue_t11_2pc_crit_bonus()));
        crit_rates.put("revealing_strike", new Double(base_melee_crit_rate));
        crit_rates.put("eviscerate",       new Double(base_melee_crit_rate + .1 * evis_bool_value));
        crit_rates.put("killing_spree",    new Double(base_melee_crit_rate));
        crit_rates.put("rupture_ticks",    new Double(base_melee_crit_rate));
        crit_rates.put("instant_poison",   new Double(base_spell_crit_rate));
        crit_rates.put("deadly_poison",    new Double(base_spell_crit_rate));
        crit_rates.put("wound_poison",     new Double(base_spell_crit_rate));

        double extra_cp_chance = (this.glyphs().exists_glyph("sinister_strike")) ? .2 : 0;

        HashMap<Integer, Double> cp_per_ss = new HashMap<Integer, Double>();
        cp_per_ss.put(1, 1 - extra_cp_chance);
        cp_per_ss.put(2, extra_cp_chance);
        int FINISHER_SIZE = 5;

        double rvs_per_finisher;
        double ss_per_finisher;
        double cp_per_finisher;
        double[] finisher_size_brkdwn;

        if (this.settings().cycle().use_revealing_strike().equals("never")) {
            Pair<HashMap<Pair<Integer, Integer>, Double>, double[]> dist = this.get_cp_dist_for_cycle(cp_per_ss, FINISHER_SIZE);
            HashMap<Pair<Integer, Integer>, Double> cp_dist = dist.getFirst();

            rvs_per_finisher = 0;
            ss_per_finisher = 0;
            cp_per_finisher = 0;
            finisher_size_brkdwn = new double[] {0, 0, 0, 0, 0, 0};
            for (Entry<Pair<Integer, Integer>, Double> item : cp_dist.entrySet()) {
                int cps = item.getKey().getFirst();
                int ss = item.getKey().getSecond();
                double probability = item.getValue();
                ss_per_finisher += ss * probability;
                cp_per_finisher += cps * probability;
                finisher_size_brkdwn[cps] += probability;
            }
        }
        else if (this.settings().cycle().use_revealing_strike().equals("sometimes")) {
            Pair<HashMap<Pair<Integer, Integer>, Double>, double[]> dist = this.get_cp_dist_for_cycle(cp_per_ss, FINISHER_SIZE - 1);
            HashMap<Pair<Integer, Integer>, Double> cp_dist = dist.getFirst();

            rvs_per_finisher = 0;
            ss_per_finisher = 0;
            cp_per_finisher = 0;
            finisher_size_brkdwn = new double[] {0, 0, 0, 0, 0, 0};
            for (Entry<Pair<Integer, Integer>, Double> item : cp_dist.entrySet()) {
                int cps = item.getKey().getFirst();
                int ss = item.getKey().getSecond();
                double probability = item.getValue();
                ss_per_finisher += ss * probability;
                int actual_cps = cps;
                if (cps < FINISHER_SIZE) {
                    actual_cps += 1;
                    rvs_per_finisher += probability;
                }
                cp_per_finisher += actual_cps * probability;
                finisher_size_brkdwn[actual_cps] += probability;
            }
        }
        else {
            Pair<HashMap<Pair<Integer, Integer>, Double>, double[]> dist = this.get_cp_dist_for_cycle(cp_per_ss, FINISHER_SIZE - 1);
            HashMap<Pair<Integer, Integer>, Double> cp_dist = dist.getFirst();

            rvs_per_finisher = 1;
            ss_per_finisher = 0;
            cp_per_finisher = 0;
            finisher_size_brkdwn = new double[] {0, 0, 0, 0, 0, 0};
            for (Entry<Pair<Integer, Integer>, Double> item : cp_dist.entrySet()) {
                int cps = item.getKey().getFirst();
                int ss = item.getKey().getSecond();
                double probability = item.getValue();
                ss_per_finisher += ss * probability;
                int actual_cps = Math.min(cps + 1, 5);
                cp_per_finisher += actual_cps * probability;
                finisher_size_brkdwn[actual_cps] += probability;
            }
        }

        int rvs_bool_value = this.glyphs().get("revealing_strike");
        this.rvs_mult = (1 + (.35 + .1 * rvs_bool_value) * rvs_per_finisher);

        double e_cost_to_generate_cps = rvs_per_finisher * rvs_e_cost + ss_per_finisher * ss_e_cost;
        double total_evis_cost = e_cost_to_generate_cps + evis_e_cost - cp_per_finisher * this.relentless_strikes_e_return_per_cp;
        double total_rup_cost = e_cost_to_generate_cps + rup_e_cost - cp_per_finisher * this.relentless_strikes_e_return_per_cp;

        double ss_per_snd = (total_evis_cost - cp_per_finisher * this.relentless_strikes_e_return_per_cp + 25) / ss_e_cost;
        double snd_size = ss_per_snd * (1 + extra_cp_chance) + .2 * this.talents().get("ruthlessness");
        double snd_cost = (ss_per_snd + .2 * this.talents().get("ruthlessness") / (1 + extra_cp_chance)) * ss_e_cost + 25 - snd_size * this.relentless_strikes_e_return_per_cp;

        double snd_duration = this.get_snd_length(snd_size);

        double e_spent_on_snd = snd_cost / (snd_duration - this.settings().response_time());

        int rup_bool_value = this.glyphs().get("rupture");
        double avg_rup_gap = (total_rup_cost - .5 * total_evis_cost) / e_regen;
        double avg_rup_duration = 2 * (3 + 2 * rup_bool_value + cp_per_finisher);
        if (this.settings().cycle().use_rupture())
            aps.put("rupture", new Double[] {1 / (avg_rup_duration + avg_rup_gap)});
        else
            aps.put("rupture", new Double[] {0.});
        double e_spent_on_rup = total_rup_cost * aps.get("rupture")[0];

        double e_available_for_evis = e_regen - e_spent_on_snd - e_spent_on_rup;
        double evis_ps = e_available_for_evis / total_evis_cost;

        double cp_spent_on_dmg_finishers_ps = (aps.get("rupture")[0] + evis_ps) * cp_per_finisher;

        double ar_duration;
        int ar_bool_value = this.glyphs().get("adrenaline_rush");
        if (this.talents().get("adrenaline_rush") != 0)
            ar_duration = 15 + 5 * ar_bool_value;
        else
            ar_duration = 0;

        double ar_bonus_cp_regen = autoattack_cp_regen * .2;
        double ar_bonus_e = ar_duration * (ar_bonus_cp_regen + 10 * haste_mult);
        double ar_bonus_evis = ar_bonus_e / total_evis_cost;
        double ar_cd_self_reduction = ar_bonus_evis * cp_per_finisher * this.talents().get("restless_blades");

        double ar_actual_cd = (180 - ar_cd_self_reduction) / (1 + cp_spent_on_dmg_finishers_ps * this.talents().get("restless_blades")) + this.settings().response_time();
        double total_evis_ps = evis_ps + ar_bonus_evis / ar_actual_cd;

        double ar_uptime = ar_duration / ar_actual_cd;
        double ar_autoattack_mult = 1 + .2 * ar_uptime;

        for (String attack : util.mkSet("mh_autoattacks", "mh_autoattack_hits", "oh_autoattacks", "oh_autoattack_hits", "main_gauche")) {
            aps.put(attack, new Double[] {aps.get(attack)[0] * ar_autoattack_mult});
        }

        double total_restless_blades_benefit = (total_evis_ps + aps.get("rupture")[0]) * cp_per_finisher * this.talents().get("restless_blades");
        double ksp_cd = 120 / (1 + total_restless_blades_benefit) + this.settings().response_time();

        double ss_aps = (total_evis_ps + aps.get("rupture")[0]) * ss_per_finisher + ss_per_snd / (snd_duration - this.settings().response_time());
        aps.put("sinister_strike", new Double[] {ss_aps});

        double rvs_aps = (total_evis_ps + aps.get("rupture")[0]) * rvs_per_finisher;
        aps.put("revealing_strike", new Double[] {rvs_aps});

        double mg_aps_increase = (aps.get("sinister_strike")[0] + aps.get("revealing_strike")[0] + total_evis_ps + aps.get("rupture")[0]) * mg_proc_rate;
        aps.put("main_gauche", new Double[] {aps.get("main_gauche")[0] + mg_aps_increase});

        if (this.talents().get("bandits_guile") != 0) {
            double time_at_level = 12 / ((aps.get("sinister_strike")[0] + aps.get("revealing_strike")[0]) * this.talents().get("bandits_guile"));
            double cycle_duration = 3 * time_at_level + 15;
            if (!this.settings().cycle().ksp_immediately()) {
                double wait_prob = 3. * time_at_level / cycle_duration;
                double avg_wait_if_waiting = 1.5 * time_at_level;
                double avg_wait_till_full_stack = wait_prob * avg_wait_if_waiting;
                ksp_cd += avg_wait_till_full_stack;
            }
            double avg_stacks = (3 * time_at_level + 45) / cycle_duration;
            this.bg_mult = 1 + .1 * avg_stacks;
        }
        else
            this.bg_mult = 1;

        if (this.talents().get("killing_spree") != 0) {
            aps.put("mh_killing_spree", new Double[] {5 * this.strike_hit_chance / ksp_cd});
            aps.put("oh_killing_spree", new Double[] {5 * this.oh_melee_hit_chance() / ksp_cd});
            double ksp_uptime = 2. / ksp_cd;

            int ksp_bool_value = this.glyphs().get("killing_spree");
            double ksp_buff = .2 + .1 * ksp_bool_value;
            if (this.settings().cycle().ksp_immediately())
                this.ksp_mult = 1 + ksp_uptime * ksp_buff;
            else
                this.ksp_mult = 1 + ksp_uptime * ksp_buff * this.max_bg_buff / this.bg_mult;
        }
        else {
            aps.put("mh_killing_spree", new Double[] {0.});
            aps.put("oh_killing_spree", new Double[] {0.});
            this.ksp_mult = 1;
        }

        Double[] evis_tuple = new Double[6];
        for (int i = 0; i < 6; i++) {
            evis_tuple[i] = finisher_size_brkdwn[i] * total_evis_ps;
        }
        aps.put("eviscerate", evis_tuple);

        Double[] rup_ticks_tuple = new Double[6];
        for (int i = 0; i < 6; i++) {
            int ticks_per_rup = 3 + i + 2 * rup_bool_value;
            rup_ticks_tuple[i] = ticks_per_rup * aps.get("rupture")[0] * finisher_size_brkdwn[i];
        }
        aps.put("rupture_ticks", rup_ticks_tuple);

        double total_mh_hits = aps.get("mh_autoattack_hits")[0] + aps.get("sinister_strike")[0] + aps.get("revealing_strike")[0] + aps.get("mh_killing_spree")[0] + aps.get("rupture")[0] + total_evis_ps + aps.get("main_gauche")[0];
        double total_oh_hits = aps.get("oh_autoattack_hits")[0] + aps.get("oh_killing_spree")[0];

        this.get_poison_counts(total_mh_hits, total_oh_hits, aps);

        return new Pair<HashMap<String, Double[]>, HashMap<String, Double>>(aps, crit_rates);
    }

    // /////////////////////////////////////////////////////////////////////////
    // Subtlety DPS functions.
    // /////////////////////////////////////////////////////////////////////////

    public double subtlety_dps_estimate() {
        Map<String, Double> dps_breakdown = this.subtlety_dps_breakdown();
        double dps_estimate = 0;
        for (Entry<String, Double> item : dps_breakdown.entrySet()) {
            dps_estimate += item.getValue();
        }
        return dps_estimate;
    }

    public Map<String, Double> subtlety_dps_breakdown() {
        if (!this.settings().cycle().cycle_type().equals("subtlety"))
            throw new InputNotModeledException("You must specify a subtlety cycle to match your subtlety spec.");
        if (!this.stats().weapon("mh").type().equals("dagger") && !this.settings().cycle().use_hemorrhage().equals("always"))
            throw new InputNotModeledException("Subtlety modeling requires a MH dagger if Hemorrhage is not the main combo point builder.");
        if (!util.mkSet("always", "never").contains(this.settings().cycle().use_hemorrhage())) {
            this.hemo_interval = this.settings().cycle().hemo_interval();
            if (this.hemo_interval < 0)
                throw new InputNotModeledException("Hemorrhage interval must be a positive number");
            if (this.hemo_interval > this.settings().duration())
                throw new InputNotModeledException("Interval between Hemorrhages cannot be higher than the fight duration");
        }
        if (this.talents().get("serrated_blades") != 2)
            throw new InputNotModeledException("Subtlety modeling currently requires 2 points in Serrated Blades");

        this.set_constants();

        this.base_hemo_cost = 28 + 7 / this.strike_hit_chance - 2 * this.talents().get("slaughter_from_the_shadows");

        double cost_reduction = new double[]{0, 7, 14, 20}[this.talents().get("slaughter_from_the_shadows")];
        this.base_bs_e_cost = 48 + 12 / this.strike_hit_chance - cost_reduction;
        this.base_ambush_e_cost = 48 + 12 / this.strike_hit_chance - cost_reduction;

        this.base_e_regen = 10;

        this.agi_mult *= 1.30;

        HashMap<String, Double> dmg_brkdwn = this.compute_dmg("subtlety_attack_counts");
        double fw_dmg_boost = 0;
        double fw_mult;
        if (this.talents().get("find_weakness") != 0) {
            double armor_value = this.target_armor();
            double armor_reduction = (1 - .35 * this.talents().get("find_weakness"));
            fw_dmg_boost = this.armor_mitig_mult(armor_reduction * armor_value) / this.armor_mitig_mult(armor_value);
            fw_mult = 1 + (fw_dmg_boost - 1) * this.fw_uptime;
        }
        else
            fw_mult = 1;

        for (Entry<String, Double> item : dmg_brkdwn.entrySet()) {
            String ability = item.getKey();
            double value = item.getValue();
            if (util.mkSet("autoattack", "backstab", "eviscerate", "hemorrhage", "hemorrhage_glyph", "burning_wounds").contains(ability))
                // Hemo dot and 2pc_t12 derive from physical attacks too.
                // Testing needed for physical damage procs.
                dmg_brkdwn.put(ability, value * fw_mult);
            else if (ability.equals("ambush")) {
                double mult = (1.3 * this.ambush_shs_rate) + (1 - this.ambush_shs_rate) * fw_dmg_boost;
                dmg_brkdwn.put(ability, value * mult);
            }
        }

        return dmg_brkdwn;
    }

    public Pair<HashMap<String, Double[]>, HashMap<String, Double>> subtlety_attack_counts(HashMap<String, Double> current_stats) {
        HashMap<String, Double[]> aps = new HashMap<String, Double[]>();

        double base_melee_crit_rate = this.melee_crit_rate(current_stats.get("agi"), current_stats.get("crit"));
        double base_spell_crit_rate = this.spell_crit_rate(current_stats.get("crit"));

        double haste_mult = this.stats().get_haste_mult_from_rating(current_stats.get("haste"));

        double mastery_snd_speed = 1 + .4 * (1 + .02 * this.stats().get_mastery_from_rating(current_stats.get("mastery")));

        double attack_speed_mult = this.base_speed_mult * haste_mult * mastery_snd_speed / 1.4;

        double mh_aps = attack_speed_mult / this.stats().weapon("mh").speed();
        double oh_aps = attack_speed_mult / this.stats().weapon("oh").speed();
        aps.put("mh_autoattacks", new Double[] {mh_aps});
        aps.put("oh_autoattacks", new Double[] {oh_aps});
        aps.put("mh_autoattack_hits", new Double[] {mh_aps * this.dw_mh_hit_chance()});
        aps.put("oh_autoattack_hits", new Double[] {oh_aps * this.dw_oh_hit_chance()});

        double bs_crit_rate = base_melee_crit_rate + this.stats().gear_buffs().rogue_t11_2pc_crit_bonus() + .1 * this.talents().get("puncturing_wounds");
        if (bs_crit_rate > 1)
            bs_crit_rate = 1.;

        double ambush_crit_rate = base_melee_crit_rate + .2 * this.talents().get("improved_ambush");
        if (ambush_crit_rate > 1)
            ambush_crit_rate = 1;

        HashMap<String, Double> crit_rates = new HashMap<String, Double>();
        crit_rates.put("mh_autoattacks", new Double(Math.min(base_melee_crit_rate, this.dw_mh_hit_chance() - GLANCE_RATE)));
        crit_rates.put("oh_autoattacks", new Double(Math.min(base_melee_crit_rate, this.dw_oh_hit_chance() - GLANCE_RATE)));
        crit_rates.put("eviscerate",     new Double(base_melee_crit_rate + .1 * this.glyphs().get("eviscerate")));
        crit_rates.put("backstab",       new Double(bs_crit_rate));
        crit_rates.put("ambush",         new Double(ambush_crit_rate));
        crit_rates.put("hemorrhage",     new Double(base_melee_crit_rate));
        crit_rates.put("rupture_ticks",  new Double(base_melee_crit_rate));
        crit_rates.put("instant_poison", new Double(base_spell_crit_rate));
        crit_rates.put("deadly_poison",  new Double(base_spell_crit_rate));
        crit_rates.put("wound_poison",   new Double(base_spell_crit_rate));

        double bs_e_cost = this.base_bs_e_cost;
        if (this.glyphs().exists_glyph("backstab"))
            bs_e_cost -= 5 * bs_crit_rate;

        double hat_cp_gen;
        if (this.talents().get("honor_among_thieves") != 0) {
            double hat_triggers_ps = this.settings().cycle().raid_crits_per_second() * this.talents().get("honor_among_thieves") / 3.;
            hat_cp_gen = 1 / (5 - this.talents().get("honor_among_thieves") + 1 / hat_triggers_ps);
        }
        else
            hat_cp_gen = 0;

        double e_regen = this.base_e_regen * haste_mult + this.bonus_e_regen;
        double e_regen_with_recup = e_regen + this.talents().get("energetic_recovery") * 4. / 3;

        double cpg_e_cost;
        double modified_e_regen;
        double hemo_interval = 0;
        if (this.settings().cycle().use_hemorrhage().equals("always")) {
            cpg_e_cost = this.base_hemo_cost;
            modified_e_regen = e_regen_with_recup;
            hemo_interval = cpg_e_cost / modified_e_regen;
        }
        else if (this.settings().cycle().use_hemorrhage().equals("never")) {
            cpg_e_cost = bs_e_cost;
            modified_e_regen = e_regen_with_recup;
        }
        else {
            hemo_interval = this.settings().cycle().hemo_interval();
            Double bs_interval = bs_e_cost / e_regen_with_recup;
            if (hemo_interval <= bs_interval)
                throw new InputNotModeledException(String.format("Interval between Hemorrhages cannot be lower than %s for this gearset", bs_interval));
            else {
                cpg_e_cost = bs_e_cost;
                double e_return_per_replaced_bs = bs_e_cost - this.base_hemo_cost;
                modified_e_regen = e_regen_with_recup + e_return_per_replaced_bs / hemo_interval;
            }
        }

        double cpg_interval = cpg_e_cost / modified_e_regen;
        double cp_per_cp_builder = 1 + cpg_interval * hat_cp_gen;

        double evis_net_e_cost = this.base_evis_e_cost - 5 * this.relentless_strikes_e_return_per_cp;
        double evis_net_cp_cost = 5 - .2 * this.talents().get("ruthlessness") - evis_net_e_cost * hat_cp_gen / modified_e_regen;

        double cpg_per_evis = evis_net_cp_cost / cp_per_cp_builder;
        double total_evis_cost = evis_net_e_cost + cpg_per_evis * cpg_e_cost;
        double total_evis_duration = total_evis_cost / modified_e_regen;

        double recup_duration = 30;
        double cycle_length;
        double total_cycle_regen;
        if (this.settings().cycle().clip_recuperate()) {
            cycle_length = recup_duration - .5 * total_evis_duration;
            total_cycle_regen = cycle_length * modified_e_regen;
        }
        else {
            double recup_net_e_cost = 30 - 5 * this.relentless_strikes_e_return_per_cp;
            double recup_net_cp_cost = recup_net_e_cost * hat_cp_gen / e_regen;
            double cpgs_under_previous_recup = .5 * total_evis_duration / cpg_e_cost;
            double cp_gained_under_previous_recup = cpgs_under_previous_recup * cp_per_cp_builder;
            double cp_needed_outside_recup = recup_net_cp_cost - cp_gained_under_previous_recup;
            double cpgs_after_recup = cp_needed_outside_recup / cp_per_cp_builder;
            double e_spent_after_recup = cpgs_after_recup * cpg_e_cost + recup_net_e_cost;

            cycle_length = 30 + e_spent_after_recup / e_regen;
            total_cycle_regen = 30 * modified_e_regen + e_spent_after_recup;
        }

        double snd_build_time = total_evis_duration / 2;
        double snd_build_e_for_cpgs = 5 * this.relentless_strikes_e_return_per_cp + modified_e_regen * snd_build_time - 25;
        double cp_builders_per_snd = snd_build_e_for_cpgs / cpg_e_cost;
        double hat_cp_ps = snd_build_time * hat_cp_gen;

        double snd_size = .2 * this.talents().get("ruthlessness") + hat_cp_ps + cp_builders_per_snd;
        double snd_duration = this.get_snd_length(snd_size);
        double snd_per_cycle = cycle_length / snd_duration;

        double vanish_cooldown = 180 - 30 * this.talents().get("elusiveness");
        double ambushes_from_vanish = 1. / (vanish_cooldown + this.settings().response_time()) + this.talents().get("preparation") / (300. + this.settings().response_time());
        if (this.talents().get("find_weakness") != 0)
            this.fw_uptime = 10 * ambushes_from_vanish;
        else
            this.fw_uptime = 0;

        double cp_per_ambush = 2 + .5 * this.talents().get("initiative");

        double bonus_cp_per_cycle = (hat_cp_gen + ambushes_from_vanish * (cp_per_ambush + 2 * this.talents().get("premeditation"))) * cycle_length;
        double cp_used_on_buffs = 5 + snd_size * snd_per_cycle - (1 + snd_per_cycle) * .2 * this.talents().get("ruthlessness");
        double bonus_eviscerates = (bonus_cp_per_cycle - cp_used_on_buffs) / (5 - .2 * this.talents().get("ruthlessness"));
        double e_spent_on_bonus_finishers = 30 + 25 * snd_per_cycle + 35 * bonus_eviscerates - (5 + snd_size * snd_per_cycle + 5 * bonus_eviscerates) * this.relentless_strikes_e_return_per_cp + cycle_length * ambushes_from_vanish * this.base_ambush_e_cost;
        double e_for_evis_spam = total_cycle_regen - e_spent_on_bonus_finishers;
        double total_cost_of_extra_evis = (5 - .2 * this.talents().get("ruthlessness")) * cpg_e_cost + this.base_evis_e_cost - 5 * this.relentless_strikes_e_return_per_cp;
        double extra_evis_per_cycle = e_for_evis_spam / total_cost_of_extra_evis;

        aps.put("cp_builder", new Double[] {(5 - .2 * this.talents().get("ruthlessness")) * extra_evis_per_cycle / cycle_length});
        aps.put("eviscerate", new Double[] {0., 0., 0., 0., 0., (bonus_eviscerates + extra_evis_per_cycle) / cycle_length});
        aps.put("ambush", new Double[] {ambushes_from_vanish});

        if (this.talents().get("shadow_dance") != 0) {
            double shd_duration = 6. + 2 * this.glyphs().get("shadow_dance");
            double shd_frequency = 1. / (60 + this.settings().response_time());

            double shd_bonus_cp_regen = shd_duration * hat_cp_gen + 2 * this.talents().get("premeditation");
            double shd_bonus_eviss = shd_bonus_cp_regen / (5 - .2 * this.talents().get("ruthlessness"));
            double shd_bonus_evis_cost = shd_bonus_eviss * (35 - 5 * this.relentless_strikes_e_return_per_cp);
            double shd_available_e = shd_duration * modified_e_regen - shd_bonus_evis_cost;

            double shd_evis_cost = (5 - .2 * this.talents().get("ruthlessness")) / cp_per_ambush * this.base_ambush_e_cost + (35 - 5 * this.relentless_strikes_e_return_per_cp);
            double shd_eviss_for_period = shd_available_e / shd_evis_cost;

            double base_bonus_cp_regen = shd_duration * hat_cp_gen;
            double base_bonus_eviss = base_bonus_cp_regen / (5 - .2 * this.talents().get("ruthlessness"));
            double base_bonus_evis_cost = base_bonus_eviss * (35 - 5 * this.relentless_strikes_e_return_per_cp);
            double base_available_e = shd_duration * modified_e_regen - base_bonus_evis_cost;

            double base_eviss_for_period = base_available_e / total_cost_of_extra_evis;

            double shd_extra_eviss = shd_eviss_for_period + shd_bonus_eviss - base_eviss_for_period - base_bonus_eviss;
            double shd_extra_ambushes = (5 - .2 * this.talents().get("ruthlessness")) / cp_per_ambush * shd_eviss_for_period;
            double shd_replaced_cpgs = (5 - .2 * this.talents().get("ruthlessness")) * base_eviss_for_period;

            this.ambush_shs_rate = (shd_frequency + ambushes_from_vanish) / (shd_extra_ambushes + ambushes_from_vanish);

            aps.put("cp_builder", new Double[] {aps.get("cp_builder")[0] - shd_replaced_cpgs * shd_frequency});
            aps.put("ambush", new Double[] {aps.get("ambush")[0] + shd_extra_ambushes * shd_frequency});
            aps.put("eviscerate", new Double[] {0., 0., 0., 0., 0., aps.get("eviscerate")[5] + shd_extra_eviss * shd_frequency});

            this.fw_uptime += (10 + shd_duration - this.settings().response_time()) * shd_frequency;
        }
        else
            this.ambush_shs_rate = 1;

        aps.put("rupture_ticks", new Double[] {0., 0., 0., 0., 0., .5});

        double total_mh_hits = aps.get("mh_autoattack_hits")[0] + aps.get("cp_builder")[0] + util.sumArray(aps.get("eviscerate")) + aps.get("ambush")[0];
        double total_oh_hits = aps.get("oh_autoattack_hits")[0];

        this.get_poison_counts(total_mh_hits, total_oh_hits, aps);

        if (this.settings().cycle().use_hemorrhage().equals("always"))
            aps.put("hemorrhage", new Double[] {aps.get("cp_builder")[0]});
        else if (this.settings().cycle().use_hemorrhage().equals("never"))
            aps.put("backstab", new Double[] {aps.get("cp_builder")[0]});
        else {
            aps.put("hemorrhage", new Double[] {1. / hemo_interval});
            aps.put("backstab", new Double[] {aps.get("cp_builder")[0] + aps.get("hemorrhage")[0]});
        }
        aps.remove("cp_builder");

        if (this.glyphs().exists_glyph("hemorrhage") && aps.containsKey("hemorrhage")) {
            // Not particularly accurate but good enough a ball-park for
            // something that won't get much of an use.
            double ticks_ps = Math.min(1. / 3, 8 / hemo_interval);
            aps.put("hemorrhage_ticks", new Double[] {ticks_ps});
        }

        return new Pair<HashMap<String, Double[]>, HashMap<String, Double>>(aps, crit_rates);
    }

}
