package rogue;

import java.util.List;
import java.util.Map;

import core.util;

public class Cycle {

    private String _cycle_type;

    public void set_cycle_type(String cycle_type) {
        this._cycle_type = cycle_type;
    }

    public String cycle_type() {
        return this._cycle_type;
    }

    // Assassination cycle settings
    public int min_envenom_size_mutilate() {
        return 0;
    }

    public int min_envenom_size_backstab() {
        return 0;
    }

    public boolean prioritize_rupture_uptime_mutilate() {
        return false;
    }

    public boolean prioritize_rupture_uptime_backstab() {
        return false;
    }

    // Combat cycle settings
    public boolean use_rupture() {
        return false;
    }

    public String use_revealing_strike() {
        return null;
    }

    public boolean ksp_immediately() {
        return false;
    }

    // Subtlety cycle settings
    public double raid_crits_per_second() {
        return 0;
    }

    public boolean clip_recuperate() {
        return false;
    }

    public String use_hemorrhage() {
        return null;
    }

    public double hemo_interval() {
        return 0;
    }

    public static class AssassinationCycle extends Cycle {
        private int min_envenom_size_mutilate;
        private int min_envenom_size_backstab;
        private boolean prioritize_rupture_uptime_mutilate;
        private boolean prioritize_rupture_uptime_backstab;
        private List<Integer> allowed_values = util.mkList(1, 2, 3, 4, 5);

        public AssassinationCycle(Map<String, Object> cycle_settings) {
            this.set_default_settings();
            for (String setting : cycle_settings.keySet()) {
                if (setting.equals("min_envenom_size_mutilate"))
                    this.min_envenom_size_mutilate = (Integer) cycle_settings.get(setting);
                else if (setting.equals("min_envenom_size_backstab"))
                    this.min_envenom_size_backstab = (Integer) cycle_settings.get(setting);
                else if (setting.equals("prioritize_rupture_uptime_mutilate"))
                    this.prioritize_rupture_uptime_mutilate = (Boolean) cycle_settings.get(setting);
                else if (setting.equals("prioritize_rupture_uptime_backstab"))
                    this.prioritize_rupture_uptime_backstab = (Boolean) cycle_settings.get(setting);
            }
            if (!this.allowed_values.contains(this.min_envenom_size_mutilate))
                throw new core.InvalidInputException(String.format("%s is not a valid envenom size during mutilate time.", this.min_envenom_size_mutilate));
            if (!this.allowed_values.contains(this.min_envenom_size_backstab))
                throw new core.InvalidInputException(String.format("%s is not a valid envenom size during backstab time.", this.min_envenom_size_backstab));
        }

        public AssassinationCycle() {
            this.set_default_settings();
        }

        private void set_default_settings() {
            super.set_cycle_type("assassination");
            this.min_envenom_size_mutilate = 4;
            this.min_envenom_size_backstab = 5;
            this.prioritize_rupture_uptime_mutilate = true;
            this.prioritize_rupture_uptime_backstab = true;
        }

        @Override
        public int min_envenom_size_mutilate() {
            return this.min_envenom_size_mutilate;
        }

        @Override
        public int min_envenom_size_backstab() {
            return this.min_envenom_size_backstab;
        }

        @Override
        public boolean prioritize_rupture_uptime_mutilate() {
            return this.prioritize_rupture_uptime_mutilate;
        }

        @Override
        public boolean prioritize_rupture_uptime_backstab() {
            return this.prioritize_rupture_uptime_backstab;
        }

    }

    public static class CombatCycle extends Cycle {
        private boolean use_rupture;
        private String use_revealing_strike;
        private boolean ksp_immediately;

        public CombatCycle(Map<String, Object> cycle_settings) {
            this.set_default_settings();
            for (String setting : cycle_settings.keySet()) {
                if (setting.equals("use_rupture"))
                    this.use_rupture = (Boolean) cycle_settings.get(setting);
                else if (setting.equals("use_revealing_strike"))
                    this.use_revealing_strike = (String) cycle_settings.get(setting);
                else if (setting.equals("ksp_immediately"))
                    this.ksp_immediately = (Boolean) cycle_settings.get(setting);
            }
        }

        public CombatCycle() {
            this.set_default_settings();
        }

        private void set_default_settings() {
            super.set_cycle_type("combat");
            this.use_rupture = true;
            this.use_revealing_strike = "sometimes";
            this.ksp_immediately = false;
        }

        @Override
        public boolean use_rupture() {
            return this.use_rupture;
        }

        @Override
        public String use_revealing_strike() {
            return this.use_revealing_strike;
        }

        @Override
        public boolean ksp_immediately() {
            return this.ksp_immediately;
        }

    }

    public static class SubtletyCycle extends Cycle {
        private double raid_crits_per_second;
        private boolean clip_recuperate = false;
        private String use_hemorrhage = "never";
        private double hemo_interval = 24;

        public SubtletyCycle(Map<String, Object> cycle_settings) {
            super.set_cycle_type("subtlety");
            this.raid_crits_per_second = (Double) cycle_settings.get("raid_crits_per_second");
            for (String setting : cycle_settings.keySet()) {
                if (setting.equals("clip_recuperate"))
                    this.clip_recuperate = (Boolean) cycle_settings.get(setting);
                else if (setting.equals("use_hemorrhage"))
                    this.use_hemorrhage = (String) cycle_settings.get(setting);
                else if (setting.equals("hemo_interval"))
                    this.hemo_interval = (Double) cycle_settings.get(setting);
            }
        }

        public SubtletyCycle(int raid_crits_per_second) {
            this.set_default_settings();
            this.raid_crits_per_second = raid_crits_per_second;
        }

        public SubtletyCycle() {
            this.set_default_settings();
        }

        private void set_default_settings() {
            super.set_cycle_type("subtlety");
            this.clip_recuperate = false;
            this.use_hemorrhage = "never";
            this.hemo_interval = 24;
        }

        @Override
        public double raid_crits_per_second() {
            return this.raid_crits_per_second;
        }

        @Override
        public boolean clip_recuperate() {
            return this.clip_recuperate;
        }

        @Override
        public String use_hemorrhage() {
            return this.use_hemorrhage;
        }

        @Override
        public double hemo_interval() {
            return this.hemo_interval;
        }

    }

}
