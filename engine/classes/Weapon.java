package classes;

import java.util.HashMap;
import java.util.Map;

import core.InvalidInputException;
import core.util;

public class Weapon {

    protected static final Map<String, HashMap<String, ?>> allowed_melee_enchants = Data.melee_enchants;
    private double speed;
    private double weapon_dps;
    private String type;
    private Proc enchant = null;
    private String enchant_name;
    private float _normalization_speed;

    public Weapon(double damage, double speed, String weapon_type, String enchant) {
        this.speed = speed;
        this.weapon_dps = damage * 1.0 / speed;
        this.type = weapon_type;

        if (this.type.equals("thrown"))
            this._normalization_speed = (float) 2.1;
        else if (util.mkSet("gun", "bow", "crossbow").contains(this.type))
            this._normalization_speed = (float) 2.8;
        else if (util.mkSet("2h_sword", "2h_mace", "2h_axe", "polearm").contains(this.type))
            this._normalization_speed = (float) 3.3;
        else if (this.type.equals("dagger"))
            this._normalization_speed = (float) 1.7;
        else
            this._normalization_speed = (float) 2.4;

        this.set_enchant(enchant);
    }

    public Weapon(double damage, double speed, String weapon_type) {
        this(damage, speed, weapon_type, "");
    }

    public void set_enchant(String enchant) {
        if (enchant.equals(""))
            this.enchant = null;
        else {
            if (!allowed_melee_enchants.containsKey(enchant))
                throw new InvalidInputException(String.format("Enchant %s is not allowed.", enchant));
            else {
                if (!this.is_melee())
                    throw new InvalidInputException(String.format("Only melee weapons can be enchanted with %s.", enchant));
                else
                    this.enchant = new Proc(allowed_melee_enchants.get(enchant));
            }
        }
        this.enchant_name = enchant;
    }

    public void del_enchant() {
        this.set_enchant("");
    }

    public double speed() {
        return this.speed;
    }

    public void set_speed(double speed) {
        this.speed = speed;
    }

    public double weapon_dps() {
        return this.weapon_dps;
    }

    public void set_weapon_dps(double value) {
        this.weapon_dps = value;
    }

    public static Map<String, HashMap<String, ?>> allowed_melee_enchants() {
        return allowed_melee_enchants;
    }

    public String type() {
        return this.type;
    }

    public Proc enchant() {
        return this.enchant;
    }

    public String enchant_name() {
        return this.enchant_name;
    }

    public boolean is_melee() {
        return !util.mkSet("gun", "bow", "crossbow", "thrown").contains(
                this.type);
    }

    public float damage(double average_ap) {
        return (float) (this.speed * (this.weapon_dps + average_ap / 14.));
    }

    public double normalized_dmg(double average_ap) {
        return this.speed * this.weapon_dps + this._normalization_speed * average_ap / 14.;
    }

}
