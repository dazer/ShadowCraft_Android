package classes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import core.InvalidInputException;
import core.util;

public class Race {

    @SuppressWarnings("serial")
    class InvalidRaceException extends InvalidInputException {
        public InvalidRaceException(String message) {
            super(message);
        }
    }

    static final Set<String> allowed_racials = util.mkSet(
            "axe_specialization",       //Orc
            "fist_specialization",      //Orc
            "mace_specialization",      //Human, Dwarf
            "stoneform",                //Dwarf
            "expansive_mind",           //Gnome
            "dagger_specialization",    //Gnome
            "sword_1h_specialization",  //Gnome, Human
            "human_spirit",             //Human
            "sword_2h_specialization",  //Human
            "quickness",                //Night Elf
            "heroic_presence",          //Draenei
            "viciousness",              //Worgen
            "blood_fury_physical",      //Orc
            "blood_fury_spell",         //Orc
            "command",                  //Orc
            "endurance",                //Tauren
            "berserking",               //Troll
            "regeneration",             //Troll
            "beast_slaying",            //Troll
            "throwing_specialization",  //Troll
            "bow_specialization",       //Troll
            "gun_specialization",       //Dwarf
            "arcane_torrent",           //Blood Elf
            "rocket_barrage",           //Goblin
            "time_is_money"             //Goblin
            );

    private Map<String, HashMap<Integer, float[]>> base_stats =          Data.base_stats;            // {class_name:{stat:value}}
    private Map<Integer, float[]> blood_fury_bonuses =                   Data.blood_fury_bonuses;    // {level:[ap, sp]}
    private Map<String, float[]> racial_stat_offset =                    Data.racial_stat_offset;    // {race, [str, agi, sta, int, spi]}
    private Map<String, HashMap<String, Object>> activated_racial_data = Data.activated_racial_data; // {racial:{key:value}
    private Map<String, String[]> racials_by_race =                      Data.racials_by_race;       // {race, [racials_names]}

    private int level = 85;
    private String character_class = "rogue";
    private String race_name;
    private Map<Integer, float[]> stat_set;
    private float[] stats;  // [racial_str, racial_agi, racial_sta, racial_int, racial_spi]
    private Map<String, Float> racial_stats = new HashMap<String, Float>();
    private Set<String> present_racials = new HashSet<String>();

    /**
     * 
     * @param race
     * @param character_class
     * @param level
     */
    public Race(String race, String character_class, int level) {
        this.character_class = character_class;
        this.race_name = race;
        if (!this.racial_stat_offset.containsKey(race))
            throw new InvalidRaceException(String.format("Invalid race %s", race));
        if (this.base_stats.containsKey(this.character_class))
            this.stat_set = base_stats.get(this.character_class);
        else
            throw new InvalidRaceException(String.format("Unsupported class %s", this.character_class));
        this.set_level(level);
        this.set_racials();
    }

    /**
     * Constructor overload; take 85 as default level.
     */
    public Race(String race, String character_class) {
        this(race, character_class, 85);
    }

    /**
     * Constructor overload; use "rogue" as default game class.
     */
    public Race(String race) {
        this(race, "rogue", 85);
    }

    private void set_racials() {
        present_racials.clear();
        for (String racial : this.racials_by_race.get(this.race_name)) {
            present_racials.add(racial);
        }
        racial_stats.put("racial_str", this.stats[0]);
        racial_stats.put("racial_agi", this.stats[1]);
        racial_stats.put("racial_sta", this.stats[2]);
        racial_stats.put("racial_int", this.stats[3]);
        racial_stats.put("racial_spi", this.stats[4]);
    }

    public void set_level(int level) {
        this.level = level;
        this._set_constants_for_level();
    }

    protected void _set_constants_for_level() {
        if (!this.stat_set.containsKey(this.level) ||
                !this.blood_fury_bonuses.containsKey(this.level) ||
                !this.racial_stat_offset.containsKey(this.race_name))
            throw new InvalidRaceException(String.format("'Unsupported class/level combination %s/%s", this.character_class, this.level));

        this.stats = this.stat_set.get(this.level);
        this.activated_racial_data.get("blood_fury_physical").put("value", this.blood_fury_bonuses.get(this.level)[0]);
        this.activated_racial_data.get("blood_fury_spell").put("value", this.blood_fury_bonuses.get(this.level)[1]);
        for (int i = 0; i<this.stats.length ; i++) {
            this.stats[i] += this.racial_stat_offset.get(this.race_name)[i];
        }
    }

    public double calculate_rocket_barrage(double ap, double spfi, double int_) {
        return (1 + 0.25 * ap + 0.429 * spfi + this.level * 2 + int_ * 0.50193);
    }

    public boolean get_racial(String racial) {
        return present_racials.contains(racial);
    }

    public float get_racial_stat(String arg) {
        return racial_stats.get(arg);
    }

    public double get_racial_expertise(String weapon_type) {
        if (util.mkSet("1h_axe", "2h_axe", "fist").contains(weapon_type) && get_racial("axe_specialization"))
            return 0.0075;
        else if (weapon_type.equals("1h_sword") && get_racial("sword_1h_specialization"))
            return 0.0075;
        else if (weapon_type.equals("2h_sword") && get_racial("sword_2h_specialization"))
            return 0.0075;
        else if (util.mkSet("1h_mace", "2h_mace").contains(weapon_type) && get_racial("mace_specialization"))
            return 0.0075;
        else if (weapon_type.equals("dagger") && get_racial("dagger_specialization"))
            return 0.0075;

        return 0;
    }

    public double get_racial_crit(String weapon_type) {
        if (get_racial("viciousness"))
            return .01;
        else {
            if (weapon_type.equals("thrown") && get_racial("throwing_specialization"))
                return .01;
            if (weapon_type.equals("gun") && get_racial("gun_specialization"))
                return .01;
            if (weapon_type.equals("bow") && get_racial("bow_specialization"))
                return .01;
        }
        return 0;
    }

    public double get_racial_crit() {
        return get_racial("viciousness") ? .01 : 0;
    }

    public double get_racial_hit() {
        return get_racial("heroic_presence") ? .01 : 0;
    }

    public double get_racial_haste() {
        return get_racial("time_is_money") ? .01 : 0;
    }

    public List<HashMap<String, Object>> get_stat_boosts() {
        List<HashMap<String, Object>> boosts = new ArrayList<HashMap<String, Object>>();
        if (get_racial("blood_fury_physical")) {
            boosts.add(this.activated_racial_data.get("blood_fury_physical"));
            boosts.add(this.activated_racial_data.get("blood_fury_spell"));
        }
        return boosts;
    }

}
