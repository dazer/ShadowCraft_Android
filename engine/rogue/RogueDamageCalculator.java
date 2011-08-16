package rogue;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import calcs.DamageCalculator;
import classes.Buffs;
import classes.Glyphs;
import classes.Race;
import classes.Stats;
import classes.Talents;
import core.InvalidInputException;
import core.util;

/**
 * Functions of general use to rogue damage calculation go here. If a
 * calculation will reasonably used for multiple classes, it should go in
 * calcs.DamageCalculator instead. If its a specific intermediate
 * value useful to only your calculations, when you extend this you should
 * put the calculations in your object. But there are things - like
 * backstab damage as a function of AP - that (almost) any rogue damage
 * calculator will need to know, so things like that go here.
 */
public class RogueDamageCalculator extends DamageCalculator {

    static final double AGI_CRIT_INTERCEPT = -.00295;
    static final double MELEE_CRIT_REDUCTION = .048;
    static final double SPELL_CRIT_REDUCTION = .021;
    private Map<String, Float> parameters;
    private boolean is_bleeding = true;
    private boolean is_poisoned = true;

    /**
     * Constructor. Initializes and caches values for the information every
     * modeler will need to know about a character. Sets level and eventually
     * propagates it to every object.
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
     * @param level
     *            Player level.
     */
    public RogueDamageCalculator(Stats stats, Talents talents, Glyphs glyphs, Buffs buffs, Race race, Settings settings, int level) {
        super(stats, talents, glyphs, buffs, race, settings, level);
        super.set_default_ep_stats(util.mkSet("white_hit", "spell_hit",
                "yellow_hit", "str", "agi", "haste", "crit", "mastery",
                "dodge_exp"));
        super.set_normalize_ep_stat("ap");
    }

    /**
     * Initializes parameters to the selected level. Propagates level to the
     * superclass and, subsequently, to every calcs object.
     */
    @Override
    public void _set_constants_for_level() {
        super._set_constants_for_level();
        Map<String, Map<Integer, Float>> level_parameters;
        level_parameters = RogueModelerData.level_parameters;
        this.parameters = new HashMap<String, Float>();
        for (String key : level_parameters.keySet()) {
            if (level_parameters.get(key).containsKey(this.level())) {
                this.parameters.put(key, level_parameters.get(key).get(this.level()));
            }
            else
                throw new InvalidInputException(String.format("No %s formula available for level %s", key, this.level()));
        }
    }

    public void set_is_bleeding(boolean is_bleeding) {
        this.is_bleeding = is_bleeding;
    }

    @Override
    public double get_spell_hit_from_talents() {
        return .02 * this.talents().get("precision");
    }

    @Override
    public double get_melee_hit_from_talents() {
        return .02 * this.talents().get("precision");
    }

    /**
     * Override this in your modeler to implement weapon damage boosts such as
     * Unheeded Warning.
     */
    public double get_weapon_dmg_bonus() {
        return 0;
    }

    public double oh_penalty() {
        if (this.talents().is_specced("combat"))
            return .875;
        else
            return .5;
    }

    /**
     * We lookup the method in Stats only if talents_modifiers needs a value for
     * mastery; the mastery override passes a 0 to force this into getting the
     * base mastery form the Stats object.
     */
    public double get_mastery_from_rating(double mastery) {
        return (mastery == 0) ? this.stats().get_mastery_from_rating() : this.stats().get_mastery_from_rating(mastery);
    }

    /**
     * Call this function in every ability affected by talents. They get passed
     * as strings in talents_list. It returns the final modifier for their
     * respective additive/multiplicative values
     * 
     * @param talents_list
     *            A set of strings with talent names.
     * @param mastery
     *            Optional mastery; will use base mastery if non is passed.
     * @return damage modifier form talents.
     */
    public double talents_mod(Set<String> talents_list, double mastery) {
        float modifier = 1;
        if (talents_list.contains("opportunity"))
            modifier += .1 * this.talents().get("opportunity");
        if (talents_list.contains("coup_de_grace"))
            modifier += new double[] {0, .07, .14, .2}[this.talents().get("coup_de_grace")];
        if (talents_list.contains("executioner") && this.talents().is_specced("subtlety"))
            modifier += .025 * this.get_mastery_from_rating(mastery);
        if (talents_list.contains("aggression"))
            modifier += new double[] {0, .07, .14, .2}[this.talents().get("aggression")];
        if (talents_list.contains("improved_sinister_strike"))
            modifier += .1 * this.talents().get("improved_sinister_strike");
        if (talents_list.contains("vile_poisons"))
            modifier += .12 * this.talents().get("vile_poisons");
        if (talents_list.contains("improved_ambush"))
            modifier += .05 * this.talents().get("improved_ambush");
        if (talents_list.contains("potent_poisons") && this.talents().is_specced("assassination"))
            modifier += .035 * this.get_mastery_from_rating(mastery);
        if (talents_list.contains("assassins_resolve") && this.talents().is_specced("assassination") && this.stats().weapon("mh").type().equals("dagger"))
            modifier *= 1.2;
        if (this.is_bleeding)  // Passing Sanguinary Vein without talent lookup (it affects all damage).
            modifier *= (1 + .08 * this.talents().get("sanguinary_vein"));

        return modifier;
    }

    /**
     * talents_modifiers overload.
     */
    public double talents_mods(Set<String> talents_list) {
        return talents_mod(talents_list, 0);
    }

    /**
     * This formula may need to be splited in two and bring the meta and
     * base_modifier to the general object if/when we start to support another
     * classes The obscure formulae for the different crit enhancers can be
     * found here
     * http://elitistjerks.com/f31/t13300-shaman_relentless_earthstorm_ele
     * /#post404567
     * 
     * @param args
     *            "is_spell" "lethality" are the only allowed values
     * @return damage modifier for critical hits.
     */
    public double crit_dmg_mods(String... args) {
        float modifier = 2;
        double crit_dmg_bonus_mod = 1;
        double crit_dmg_mod = this.stats().gear_buffs().metagem_crit_mult();
        for (String arg : args) {
            if (arg.equals("is_spell"))
                modifier = (float) 1.5;
            else if (arg.equals("lethality"))
                crit_dmg_bonus_mod = 1 + .1 * this.talents().get("lethality");
        }
        return 1 + (modifier * crit_dmg_mod - 1) * crit_dmg_bonus_mod;
    }

    /**
     * Main hand swing damage
     * 
     * @param ap
     *            Attack Power
     * @param armor
     *            Target armor override
     * @return Main hand swing damage vector
     */
    public double[] mh_dmg(double ap, float armor) {
        double weapon_dmg = this.stats().weapon("mh").damage(ap) + this.get_weapon_dmg_bonus();
        double mult = this.talents_mods(util.mkSet("assassins_resolve"));
        mult *= this.raid_settings_mods("physical", armor);
        double crit_mult = this.crit_dmg_mods();

        double dmg = weapon_dmg * mult;
        double crit_dmg = dmg * crit_mult;

        return new double[] {dmg, crit_dmg};
    }

    public double[] mh_dmg(double ap) {
        return mh_dmg(ap, 0);
    }

    /**
     * Off hand swing damage
     * 
     * @param ap
     *            Attack Power
     * @param armor
     *            Target armor override
     * @return Off hand swing damage vector
     */
    public double[] oh_dmg(double ap, float armor) {
        double weapon_dmg = this.stats().weapon("oh").damage(ap) + this.get_weapon_dmg_bonus();
        double mult = this.talents_mods(util.mkSet("assassins_resolve"));
        mult *= this.raid_settings_mods("physical", armor);
        double crit_mult = this.crit_dmg_mods();

        double dmg = this.oh_penalty() * weapon_dmg * mult;
        double crit_dmg = dmg * crit_mult;

        return new double[] {dmg, crit_dmg};
    }

    public double[] oh_dmg(double ap) {
        return oh_dmg(ap, 0);
    }

    /**
     * Backstab damage
     * 
     * @param ap
     *            Attack Power
     * @param armor
     *            Target armor override
     * @return Backstab damage vector
     */
    public double[] backstab_dmg(double ap, float armor) {
        double weapon_dmg = this.stats().weapon("mh").normalized_dmg(ap) + this.get_weapon_dmg_bonus();
        double mult = this.talents_mods(util.mkSet("opportunity", "aggression", "assassins_resolve"));
        mult *= this.raid_settings_mods("physical", armor);
        double crit_mult = this.crit_dmg_mods("lethality");

        double percentage_dmg_bonus = 2;
        if (this.talents().is_specced("subtlety"))
            percentage_dmg_bonus *= 1.4;

        double dmg = percentage_dmg_bonus * (weapon_dmg + this.parameters.get("bs_bonus_dmg"))* mult;
        double crit_dmg = dmg * crit_mult;

        return new double[] {dmg, crit_dmg};
    }

    public double[] backstab_dmg(double ap) {
        return backstab_dmg(ap, 0);
    }

    /**
     * Main hand component mutilate damage
     * 
     * @param ap
     *            Attack Power
     * @param armor
     *            Target armor override
     * @return Main hand component mutilate damage vector
     */
    public double[] mh_mutilate_dmg(double ap, float armor) {
        double mh_weapon_dmg = this.stats().weapon("mh").normalized_dmg(ap) + this.get_weapon_dmg_bonus();
        double mult = this.talents_mods(util.mkSet("opportunity", "assassins_resolve"));
        mult *= this.raid_settings_mods("physical", armor);
        double crit_mult = this.crit_dmg_mods("lethality");

        double mh_dmg = 1.5 * (mh_weapon_dmg + this.parameters.get("mut_bonus_dmg")) * mult;
        if (this.is_poisoned)
            mh_dmg *= 1.2;
        double crit_mh_dmg = mh_dmg * crit_mult;

        return new double[] {mh_dmg, crit_mh_dmg};
    }

    public double[] mh_mutilate_dmg(double ap) {
        return mh_mutilate_dmg(ap, 0);
    }

    /**
     * Off hand component mutilate damage
     * 
     * @param ap
     *            Attack Power
     * @param armor
     *            Target armor override
     * @return Off hand component mutilate damage vector
     */
    public double[] oh_mutilate_dmg(double ap, float armor) {
        double oh_weapon_dmg = this.stats().weapon("oh").normalized_dmg(ap) + this.get_weapon_dmg_bonus();
        double mult = this.talents_mods(util.mkSet("opportunity", "assassins_resolve"));
        mult *= this.raid_settings_mods("physical", armor);
        double crit_mult = this.crit_dmg_mods("lethality");

        double oh_dmg = 1.5 * (this.oh_penalty() * oh_weapon_dmg + this.parameters.get("mut_bonus_dmg")) * mult;
        if (this.is_poisoned)
            oh_dmg *= 1.2;
        double crit_oh_dmg = oh_dmg * crit_mult;

        return new double[] {oh_dmg, crit_oh_dmg};
    }

    public double[] oh_mutilate_dmg(double ap) {
        return oh_mutilate_dmg(ap, 0);
    }

    /**
     * Sinister strike damage
     * 
     * @param ap
     *            Attack Power
     * @param armor
     *            Target armor override
     * @return Sinister strike damage vector
     */
    public double[] sinister_strike_dmg(double ap, float armor) {
        double weapon_dmg = this.stats().weapon("mh").normalized_dmg(ap) + this.get_weapon_dmg_bonus();
        double mult = this.talents_mods(util.mkSet("aggression", "improved_sinister_strike", "assassins_resolve"));
        mult *= this.raid_settings_mods("physical", armor);
        double crit_multiplier = this.crit_dmg_mods("lethality");

        double dmg = (weapon_dmg + this.parameters.get("ss_bonus_dmg")) * mult;
        double crit_dmg = dmg * crit_multiplier;

        return new double[] {dmg, crit_dmg};
    }

    public double[] sinister_strike_dmg(double ap) {
        return sinister_strike_dmg(ap, 0);
    }

    /**
     * Hemorrhage damage
     * 
     * @param ap
     *            Attack Power
     * @param armor
     *            Target armor override
     * @return Hemorrhage damage vector
     */
    public double[] hemorrhage_dmg(double ap, float armor) {
        double weapon_dmg = this.stats().weapon("mh").normalized_dmg(ap) + this.get_weapon_dmg_bonus();
        double mult = this.talents_mods(util.mkSet(""));
        mult *= this.raid_settings_mods("physical", armor);
        double crit_mult = this.crit_dmg_mods("lethality");

        double percentage_dmg_bonus = 1.55;
        if (this.stats().weapon("mh").type().equals("dagger"))
            percentage_dmg_bonus *= 1.45;
        if (this.talents().is_specced("subtlety"))
            percentage_dmg_bonus *= 1.4;

        double dmg = percentage_dmg_bonus * weapon_dmg * mult;
        double crit_dmg = dmg * crit_mult;

        return new double[] {dmg, crit_dmg};
    }

    public double[] hemorrhage_dmg(double ap) {
        return hemorrhage_dmg(ap, 0);
    }

    /**
     * Hemorrhage glyph tick damage. Call this function twice to get all four
     * crit/non-crit hemorrhage glyph values.
     * 
     * @param ap
     *            Attack Power
     * @param from_crit
     *            Critical hemorrhage switch
     * @param armor
     *            Target armor override
     * @return Hemorrhage glyph tick damage vector
     */
    public double[] hemorrhage_tick_dmg(double ap, boolean from_crit, float armor) {
        int crit_switch = (from_crit) ? 1 : 0;
        double hemo_dmg = this.hemorrhage_dmg(ap, armor)[crit_switch];
        double mult = this.talents_mods(util.mkSet(""));
        mult *= this.raid_settings_mods("bleed", armor);
        double crit_mult = this.crit_dmg_mods("lethality");

        double tick_conversion_factor = .4 / 8;
        double tick_dmg = hemo_dmg * mult * tick_conversion_factor;
        double crit_tick_dmg = tick_dmg * crit_mult;

        return new double[] {tick_dmg, crit_tick_dmg};
    }

    public double[] hemorrhage_tick_dmg(double ap, boolean from_crit_hemo) {
        return hemorrhage_tick_dmg(ap, from_crit_hemo, 0);
    }

    /**
     * Ambush damage
     * 
     * @param ap Attack Power
     * @param armor Target armor override
     * @return Ambush damage vector
     */
    public double[] ambush_dmg(double ap, float armor) {
        double weapon_dmg = this.stats().weapon("mh").normalized_dmg(ap) + this.get_weapon_dmg_bonus();
        double mult = this.talents_mods(util.mkSet("opportunity", "improved_ambush", "assassins_resolve"));
        mult *= this.raid_settings_mods("physical", armor);
        double crit_mult = this.crit_dmg_mods();

        double percentage_dmg_bonus = 1.9;
        if (this.stats().weapon("mh").type().equals("dagger"))
            percentage_dmg_bonus *= 1.447;

        double dmg = percentage_dmg_bonus * (weapon_dmg + this.parameters.get("ambush_bonus_dmg")) * mult;
        double crit_dmg = dmg * crit_mult;

        return new double[] {dmg, crit_dmg};
    }

    public double[] ambush_dmg(double ap) {
        return ambush_dmg(ap, 0);
    }

    /**
     * RvS damage
     * 
     * @param ap
     *            Attack Power
     * @param armor
     *            Target armor override
     * @return RvS damage vector
     */
    public double[] revealing_strike_dmg(double ap, float armor) {
        double weapon_dmg = this.stats().weapon("mh").damage(ap) + this.get_weapon_dmg_bonus();
        double mult = this.raid_settings_mods("physical", armor);
        double crit_mult = this.crit_dmg_mods();

        double dmg = 1.25 * weapon_dmg * mult;
        double crit_dmg = dmg * crit_mult;

        return new double[] {dmg, crit_dmg};
    }

    public double[] revealing_strike_dmg(double ap) {
        return revealing_strike_dmg(ap, 0);
    }

    /**
     * Venomous wounds tick damage
     * 
     * @param ap
     *            Attack Power
     * @param mastery
     *            Mastery from rating
     * @return Venomous wounds tick damage vector
     */
    public double[] venomous_wounds_dmg(double ap, double mastery) {
        double mult = this.talents_mod(util.mkSet("potent_poisons"), mastery);
        mult *= this.raid_settings_mod("spell");
        double crit_mult = this.crit_dmg_mods("is_spell");

        double dmg = (this.parameters.get("vw_base_dmg") + this.parameters.get("vw_percentage_dmg") * ap) * mult;
        double crit_dmg = dmg * crit_mult;

        return new double[] {dmg, crit_dmg};
    }

    public double[] venomous_wounds_dmg(double ap) {
        return venomous_wounds_dmg(ap, 0);
    }

    /**
     * MG damage
     * 
     * @param ap
     *            Attack Power
     * @param armor
     *            Target armor override
     * @return MG damage vector
     */
    public double[] main_gauche_dmg(double ap, float armor) {
        double weapon_dmg = this.stats().weapon("mh").normalized_dmg(ap) + this.get_weapon_dmg_bonus();
        double mult = this.raid_settings_mods("physical", armor);
        double crit_mult = this.crit_dmg_mods();

        double dmg = weapon_dmg * mult;
        double crit_dmg = dmg * crit_mult;

        return new double[] {dmg, crit_dmg};
    }

    public double[] main_gauche_dmg(double ap) {
        return main_gauche_dmg(ap, 0);
    }

    /**
     * KsP main hand component damage for one assault
     * 
     * @param ap
     *            Attack Power
     * @param armor
     *            Target armor override
     * @return KsP main hand component damage for one assault vector
     */
    public double[] mh_killing_spree_dmg(double ap, float armor) {
        double mh_weapon_dmg = this.stats().weapon("mh").normalized_dmg(ap) + this.get_weapon_dmg_bonus();
        double mult = this.raid_settings_mods("physical", armor);
        double crit_mult = this.crit_dmg_mods();

        double mh_dmg = mh_weapon_dmg * mult;
        double crit_mh_dmg = mh_dmg * crit_mult;

        return new double[] {mh_dmg, crit_mh_dmg};
    }

    public double[] mh_killing_spree_dmg(double ap) {
        return mh_killing_spree_dmg(ap, 0);
    }

    /**
     * KsP main hand component damage for one assault
     * 
     * @param ap
     *            Attack Power
     * @param armor
     *            Target armor override
     * @return KsP main hand component damage for one assault vector
     */
    public double[] oh_killing_spree_dmg(double ap, float armor) {
        double oh_weapon_dmg = this.stats().weapon("oh").normalized_dmg(ap) + this.get_weapon_dmg_bonus();
        double mult = this.raid_settings_mods("physical", armor);
        double crit_mult = this.crit_dmg_mods();

        double oh_dmg = this.oh_penalty() * oh_weapon_dmg * mult;
        double crit_oh_dmg = oh_dmg * crit_mult;

        return new double[] {oh_dmg, crit_oh_dmg};
    }

    public double[] oh_killing_spree_dmg(double ap) {
        return oh_killing_spree_dmg(ap, 0);
    }

    /**
     * IP damage
     * 
     * @param ap
     *            Attack Power
     * @param mastery
     *            Mastery from rating
     * @return IP damage vector
     */
    public double[] instant_poison_dmg(double ap, double mastery) {
        double mult = this.talents_mod(util.mkSet("potent_poisons", "vile_poisons"), mastery);
        mult *= this.raid_settings_mod("spell");
        double crit_mult = this.crit_dmg_mods("is_spell");

        double dmg = (this.parameters.get("ip_base_dmg") + 0.09 * ap) * mult;
        double crit_dmg = dmg * crit_mult;

        return new double[] {dmg, crit_dmg};
    }

    public double[] instant_poison_dmg(double ap) {
        return instant_poison_dmg(ap, 0);
    }

    /**
     * DP tick damage
     * 
     * @param ap
     *            Attack Power
     * @param mastery
     *            Mastery from rating
     * @param dp_stacks
     *            Deadly poison stacks
     * @return DP tick damage vector
     */
    public double[] deadly_poison_tick_dmg(double ap, double mastery, int dp_stacks) {
        double mult = this.talents_mod(util.mkSet("potent_poisons", "vile_poisons"), mastery);
        mult *= this.raid_settings_mod("spell");
        double crit_mult = this.crit_dmg_mods("is_spell");

        double tick_dmg = ((this.parameters.get("dp_base_dmg") + this.parameters.get("dp_percentage_dmg") * ap) * dp_stacks / 4) * mult;
        double crit_tick_dmg = tick_dmg * crit_mult;

        return new double[] {tick_dmg, crit_tick_dmg};
    }

    public double[] deadly_poison_tick_dmg(double ap, double mastery) {
        return deadly_poison_tick_dmg(ap, mastery, 5);
    }

    public double[] deadly_poison_tick_dmg(double ap) {
        return deadly_poison_tick_dmg(ap, 0, 5);
    }

    /**
     * WP damage
     * 
     * @param ap
     *            Attack Power
     * @param mastery
     *            Mastery from rating
     * @return WP damage vector
     */
    public double[] wound_poison_dmg(double ap, double mastery) {
        double mult = this.talents_mod(util.mkSet("potent_poisons", "vile_poisons"), mastery);
        mult *= this.raid_settings_mod("spell");
        double crit_mult = this.crit_dmg_mods("is_spell");

        double dmg = (this.parameters.get("wp_base_dmg") + this.parameters.get("wp_percentage_dmg") * ap) * mult;
        double crit_dmg = dmg * crit_mult;

        return new double[] {dmg, crit_dmg};
    }

    public double[] wound_poison_dmg(double ap) {
        return wound_poison_dmg(ap, 0);
    }

    /**
     * Garrote tick damage
     * 
     * @param ap
     *            Attack Power
     * @param mastery
     *            Mastery from rating
     * @return Garrote tick damage vector
     */
    public double[] garrote_tick_dmg(double ap, float mastery) {
        double mult = this.talents_mods(util.mkSet("opportunity"));
        mult *= this.raid_settings_mod("bleed");
        double crit_mult = this.crit_dmg_mods();

        double tick_dmg = (this.parameters.get("garrote_base_dmg") + ap * 1 * 0.07) * mult;
        double crit_tick_dmg = tick_dmg * crit_mult;

        return new double[] {tick_dmg, crit_tick_dmg};
    }

    public double[] garrote_tick_dmg(double ap) {
        return garrote_tick_dmg(ap, 0);
    }

    /**
     * Rupture tick damage. Assassasin's resolve was tested on melee, poisons,
     * weapon strikes and ap strikes, not bleeds. Although there's no reason to
     * believe it doesn't affect bleeds, I'm setting it to false until some
     * testing is done
     * 
     * @param ap
     *            Attack Power
     * @param cp
     *            Combo points
     * @return Rupture tick damage vector
     */
    public double[] rupture_tick_dmg(double ap, int cp) {
        double mult = this.talents_mods(util.mkSet("executioner"));
        mult *= this.raid_settings_mod("bleed");
        double crit_mult = this.crit_dmg_mods();

        double[] ap_mult_tuple = new double[] {0, .015, .024, .03, .03428571, .0375};
        double tick_dmg = (this.parameters.get("rup_base_dmg") + this.parameters.get("rup_bonus_dmg") * cp + ap_mult_tuple[cp] * ap) * mult;
        double crit_tick_dmg = tick_dmg * crit_mult;

        return new double[] {tick_dmg, crit_tick_dmg};
    }

    /**
     * Eviscerate damage
     * 
     * @param ap
     *            Attack Power
     * @param cp
     *            Combo points
     * @param armor
     *            Target armor override
     * @return Eviscerate damage vector
     */
    public double[] eviscerate_dmg(double ap, int cp, float armor) {
        double mult = this.talents_mods(util.mkSet("coup_de_grace", "aggression", "executioner", "assassins_resolve"));
        mult *= this.raid_settings_mods("physical", armor);
        double crit_mult = this.crit_dmg_mods();

        double dmg = (this.parameters.get("evis_base_dmg") + this.parameters.get("evis_bonus_dmg") * cp + .091 * cp * ap) * mult;
        double crit_dmg = dmg * crit_mult;

        return new double[] {dmg, crit_dmg};
    }

    public double[] eviscerate_dmg(double ap, int cp) {
        return eviscerate_dmg(ap, cp, 0);
    }

    /**
     * Envenom damage
     * 
     * @param ap
     *            Attack Power
     * @param cp
     *            Combo points
     * @param mastery
     *            Mastery from rating
     * @param dp_charges
     *            Deadly poison consumed doses
     * @return Envenom damage vector
     */
    public double[] envenom_dmg(double ap, int cp, double mastery, int dp_charges) {
        double mult = this.talents_mod(util.mkSet("coup_de_grace", "executioner", "assassins_resolve", "potent_poisons"), mastery);
        mult *= this.raid_settings_mod("spell");
        double crit_mult = this.crit_dmg_mods();

        double dmg = (this.parameters.get("env_base_dmg") * Math.min(dp_charges, cp) + .09 * cp * ap) * mult;
        double crit_dmg = dmg * crit_mult;

        return new double[] {dmg, crit_dmg};
    }

    public double[] envenom_dmg(double ap, int cp, double mastery) {
        return envenom_dmg(ap, cp, mastery, 5);
    }

    public double[] envenom_dmg(double ap, int cp) {
        return envenom_dmg(ap, cp, 0, 5);
    }

    /**
     * Melee crit rate.
     * 
     * @param agi
     *            Agility
     * @param crit
     *            Critical hit rating
     * @return Melee crit rate.
     */
    public double melee_crit_rate(double agi, double crit) {
        if (agi == 0)
            agi = this.stats().get_num_stat("agi");
        double base_crit = AGI_CRIT_INTERCEPT + agi / this.parameters.get("agi_per_crit");
        base_crit += this.stats().get_crit_from_rating(crit);
        return base_crit + this.buffs().buff_all_crit() + this.race().get_racial_crit() - MELEE_CRIT_REDUCTION;
    }

    /**
     * melee_crit_rate overload.
     */
    public double melee_crit_rate(double agi) {
        if (agi == 0)
            agi = this.stats().get_num_stat("agi");
        double base_crit = AGI_CRIT_INTERCEPT + agi / this.parameters.get("agi_per_crit");
        base_crit += this.stats().get_crit_from_rating();
        return base_crit + this.buffs().buff_all_crit() + this.race().get_racial_crit() - MELEE_CRIT_REDUCTION;
    }

    /**
     * melee_crit_rate overload.
     */
    public double melee_crit_rate() {
        return melee_crit_rate(0);
    }

    /**
     * Spell crit rate.
     * 
     * @param crit
     *            Critical hit rating
     * @return Spell crit rate.
     */
    public double spell_crit_rate(double crit) {
        double base_crit = this.stats().get_crit_from_rating(crit);
        return base_crit + this.buffs().buff_all_crit() + this.buffs().buff_spell_crit() + this.race().get_racial_crit() - SPELL_CRIT_REDUCTION;
    }

    /**
     * spell_crit_rate overload. //TODO
     */
    public double spell_crit_rate() {
        double base_crit = this.stats().get_crit_from_rating();
        return base_crit + this.buffs().buff_all_crit() + this.buffs().buff_spell_crit() + this.race().get_racial_crit() - SPELL_CRIT_REDUCTION;
    }

}
