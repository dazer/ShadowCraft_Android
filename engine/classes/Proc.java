package classes;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import core.InvalidInputException;

/**
 * When fed the mapping of a proc it builds a proc object and defines methods to
 * retrieve information about it.
 */
public class Proc {

    @SuppressWarnings("serial")
    public class InvalidProcException extends InvalidInputException {
        public InvalidProcException(String message) {
            super(message);
        }
    }

    @SuppressWarnings("serial")
    static final Map<String, String[]> trigger_map = new HashMap<String, String[]>(){{
        put("off_auto_attacks",          new String[] {"all_attacks", "auto_attacks", "all_spells_and_attacks"});
        put("off_strikes",               new String[] {"all_attacks", "strikes", "all_spells_and_attacks"});
        put("off_harmful_spells",        new String[] {"all_spells", "damaging_spells", "all_spells_and_attacks"});
        put("off_heals",                 new String[] {"all_spells", "healing_spells", "all_spells_and_attacks"});
        put("off_periodic_spell_damage", new String[] {"all_periodic_damage", "periodic_spell_damage"});
        put("off_periodic_heals",        new String[] {"hots"});
        put("off_bleeds",                new String[] {"all_periodic_damage", "bleeds"});
        put("off_apply_debuff",          new String[] {"all_spells_and_attacks", "all_attacks"});
    }};

    private String stat;
    private String innit_stat;
    private float value;
    private float duration;
    private float icd;
    private String trigger;
    private String proc_name;
    private float ppm;
    private float proc_chance;
    private boolean on_crit;
    private int max_stacks;
    private boolean can_crit;
    private String weapon_flag = "";
    private double uptime;

    /**
     * Constructor. Initializes every field except uptime and weapon_flag. Some
     * of them are defaulted as not every proc needs all fields. Notably ppm
     * and proc_chance are exclusive with each other plus one of them must be
     * initialized to a value different from the default.
     * See that, to instantiate procs, callers should interact with the class
     * ProcsList instead. Interaction with this class should be to retrieve
     * data or set very particular properties like the weapon flag.
     * @param arg A hash with data to instantiate the proc.
     */
    public Proc(HashMap<String, ?> arg) {
        this.innit_stat = (String) arg.get("stat");
        this.stat =       (String) arg.get("stat");
        this.value =       (Float) arg.get("value");
        this.duration =    (Float) arg.get("duration");
        this.icd =         (Float) arg.get("icd");
        this.trigger =    (String) arg.get("trigger");
        this.proc_name =  (String) arg.get("proc_name");

        this.ppm =         (arg.containsKey("ppm")?          (Float) arg.get("ppm") : 0);
        this.proc_chance = (arg.containsKey("proc_chance")?  (Float) arg.get("proc_chance") : 0);
        this.on_crit =     (arg.containsKey("on_crit")?    (Boolean) arg.get("on_crit") : false);
        this.max_stacks =  (arg.containsKey("max_stacks")? (Integer) arg.get("max_stacks") : 1);
        this.can_crit =    (arg.containsKey("can_crit")?   (Boolean) arg.get("can_crit") : true);

        this._check_validity();
    }

    /**
     * Every proc must have a ppm or a proc chance: either of the two but not
     * both. This is procedural check for XOR.
     */
    private void _check_validity() {
        boolean is_not_ppm = (this.proc_chance != 0 && this.ppm == 0);
        boolean is_ppm = (this.ppm != 0 && this.proc_chance == 0);
        if (!(is_ppm || is_not_ppm))
            throw new InvalidProcException(String.format("Invalid data for proc %s.", this.proc_name));
    }

    /**
     * Usually you will want to check if a proc is ppm before knowing the
     * actual ppm value.
     * @return True if ppm proc, False if not.
     */
    public boolean is_ppm() {
        this._check_validity();
        return this.ppm != 0;
    }

    /**
     * The rate at which a proc triggers is a function of the weapon speed if
     * ppm, or the proc chance if not a ppm proc.
     * @param speed Weapon speed.
     * @return Proc rate.
     */
    public double proc_rate(double speed) {
        if (this.is_ppm()) {
            if (speed == 0)
                throw new InvalidProcException(String.format("Weapon speed needed to calculate the proc rate of %s.", this.proc_name));
            else
                return this.ppm * speed / 60.;
        }
        return this.proc_chance;
    }

    /**
     * Proc rate overload. No speed needed, so only useful for non ppm procs.
     * @return Proc rate.
     */
    public double proc_rate() {
        return proc_rate(0);
    }

    //trigger methods

    //    public boolean procs_off_auto_attacks() {
    //        return Arrays.asList(trigger_map.get("off_auto_attacks")).contains(this.trigger);
    //    }
    //    public boolean procs_off_strikes() {
    //        return Arrays.asList(trigger_map.get("off_strikes")).contains(this.trigger);
    //    }
    //    public boolean procs_off_harmful_spells() {
    //        return Arrays.asList(trigger_map.get("off_harmful_spells")).contains(this.trigger);
    //    }
    //    public boolean procs_off_heals() {
    //        return Arrays.asList(trigger_map.get("off_heals")).contains(this.trigger);
    //    }
    //    public boolean procs_off_periodic_spell_damage() {
    //        return Arrays.asList(trigger_map.get("off_periodic_spell_damage")).contains(this.trigger);
    //    }
    //    public boolean procs_off_periodic_heals() {
    //        return Arrays.asList(trigger_map.get("off_periodic_heals")).contains(this.trigger);
    //    }
    //    public boolean procs_off_bleeds() {
    //        return Arrays.asList(trigger_map.get("off_bleeds")).contains(this.trigger);
    //    }
    //    public boolean procs_off_apply_debuff() {
    //        return Arrays.asList(trigger_map.get("off_apply_debuff")).contains(this.trigger);
    //    }
    /**
     * General trigger getter method. The triggers stored in the proc are
     * described as the in-game tool-tip says; this uses the mapping form
     * actual abilities to triggers. See the mapping for reference.
     * @param procs_off Name of the ability kind, for instance: off_strikes
     * @return True if procs from the ability, False if not.
     */
    public boolean procs_off(String procs_off) {
        return Arrays.asList(trigger_map.get(procs_off)).contains(this.trigger);
    }

    /**
     * The on_crit field is a special casing of a trigger. you will need to
     * call this in conjunction with procs_off(...) to get the whole behavior.
     * @return
     */
    public boolean procs_on_crit_only() {
        return this.on_crit;
    }

    // getters
    public String stat() {
        return this.stat;
    }

    public float value() {
        return this.value;
    }

    public float duration() {
        return this.duration;
    }

    public float icd() {
        return this.icd;
    }

    public String proc_name() {
        return this.proc_name;
    }

    public int max_stacks() {
        return this.max_stacks;
    }

    /**
     * Sometimes, damage procs don't hit critically. This helps to filter them.
     * @return True if it can crit, False if not.
     */
    public boolean can_crit() {
        return this.can_crit;
    }

    public float proc_chance() {
        return this.proc_chance;
    }

    public String weapon_flag() {
        return this.weapon_flag;
    }

    public double uptime() {
        return this.uptime;
    }

    /**
     * Used by the modeler to tag weapon-tied procs. You can access this value
     * from the weapon_flag() getter.
     * @param weapon_flag A comprehensive string used to filter the proc
     */
    public void set_weapon_flag(String weapon_flag) {
        this.weapon_flag = weapon_flag;
    }

    /**
     * Once you figure this value, you can tag the proc with it for easy
     * access thereon.
     * @param uptime Proc uptime.
     */
    public void set_uptime(double uptime) {
        this.uptime = uptime;
    }

    /**
     * Stat setter; some procs are tagged as 'weird' to let the modeler know
     * that it's its task to figure what to do with it. this is to be used for
     * procs like matrix restabilizer.
     * @param stat Stat name.
     */
    public void set_stat(String stat) {
        if (this.innit_stat.equals("weird_proc"))
            this.stat = stat;
        else
            throw new InvalidProcException(String.format("The stat for proc %s is not to be changed.", this.proc_name));
    }

}
