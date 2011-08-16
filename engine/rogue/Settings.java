package rogue;

import java.util.Map;

/**
 * Settings object for AldrianasRogueDamageCalculator.
 */
public class Settings {

    private Cycle cycle;
    private double time_in_execute_range = .35;
    private Boolean tricks_on_cooldown = true;
    private double response_time = .5;
    private String mh_poison = "ip";
    private String oh_poison = "dp";
    private double duration = 300;

    public Settings(Map<String, Object> settings) {
        this.cycle = (Cycle) settings.get("cycle");
        for (String setting : settings.keySet()) {
            if (setting.equals("time_in_execute_range"))
                this.time_in_execute_range = (Double) settings.get(setting);
            else if (setting.equals("tricks_on_cooldown"))
                this.tricks_on_cooldown = (Boolean) settings.get(setting);
            else if (setting.equals("response_time"))
                this.response_time = (Double) settings.get(setting);
            else if (setting.equals("mh_poison"))
                this.mh_poison = (String) settings.get(setting);
            else if (setting.equals("oh_poison"))
                this.oh_poison = (String) settings.get(setting);
            else if (setting.equals("duration"))
                this.duration = (Double) settings.get(setting);
        }
    }

    public Cycle cycle() {
        return this.cycle;
    }

    public double time_in_execute_range() {
        return this.time_in_execute_range;
    }

    public Boolean tricks_on_cooldown() {
        return this.tricks_on_cooldown;
    }

    public double response_time() {
        return this.response_time;
    }

    public String mh_poison() {
        return this.mh_poison;
    }

    public String oh_poison() {
        return this.oh_poison;
    }

    public double duration() {
        return this.duration;
    }

}
