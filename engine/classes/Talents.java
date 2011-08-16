package classes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import core.InvalidInputException;

public class Talents {

    @SuppressWarnings("serial")
    class InvalidTalentException extends InvalidInputException {
        public InvalidTalentException(String message) {
            super(message);
        }
    }

    private Map<String, Integer> present_talents = new HashMap<String, Integer>();  // talent_value: value
    private Map<String, HashMap<String, List<Integer>>> allowed_talents;  // spec_name: {talent_name: (max_value, tier)}
    private List<String> specs;  // spec_name
    private Map<String, Integer> talents_in_spec = new HashMap<String, Integer>(); // spec_name: talents_spent
    private String active_spec = null;

    public Talents(String string1, String string2, String string3, List<String> specs, HashMap<String, HashMap<String, List<Integer>>> allowed_talents) {
        this.allowed_talents = allowed_talents;
        this.specs = specs;
        set_talents_from_string(string1, this.specs.get(0));
        set_talents_from_string(string2, this.specs.get(1));
        set_talents_from_string(string3, this.specs.get(2));
        set_active_spec();
    }

    public void set_talents_from_string(String talent_string, String spec) {
        if ((talent_string.length() > allowed_talents.get(spec).size()))
            throw new InvalidTalentException(String.format("Invalid talent string %s for spec %s", talent_string, spec));
        List<Integer> list = new ArrayList<Integer>();
        for (int i=0; i<talent_string.length() ;i++) {
            list.add(Character.getNumericValue(talent_string.toCharArray()[i]));
        }
        this.populate(list, spec);
    }

    /**
     * Override in your subclass to implement a way to set talents given a
     * ordered list of integers with values to initialize talents.
     * 
     * @param list
     * @param spec
     */
    public void populate(List<Integer> list, String spec) {
    }

    /**
     * Setter
     * 
     * @param talent
     *            Talent name
     * @param value
     *            Talent value
     */
    public void set_talent(String talent, int value) {
        check_valid_talent(talent, value);
        present_talents.put(talent, value);
    }

    public void set_talent(String talent, int value, boolean check) {
        if (check)
            this.set_talent(talent, value);
        else
            present_talents.put(talent, value);
    }

    /**
     * If someone tries to access a talent that is defined for the tree but has
     * not had a value assigned to it yet (i.e., the initialization did not put
     * any points into it), we return 0 for the value of the talent.
     * 
     * @param talent
     *            Talent name
     * @return talent value
     */
    public int get(String talent) {
        if (present_talents.containsKey(talent))
            return present_talents.get(talent);
        else if (this.check_valid_talent(talent, 0))
            present_talents.put(talent, 0);
        return 0;
    }

    public Set<String> allowed_talents() {
        Set<String> allowed_talents = new HashSet<String>();
        for (String key : this.allowed_talents.keySet()) {
            for (String talent_name : this.allowed_talents.get(key).keySet()) {
                allowed_talents.add(talent_name);
            }
        }
        return allowed_talents;
    }

    public void set_talents_in_spec(String spec, int talents_spent) {
        this.talents_in_spec.put(spec, talents_spent);
    }

    public void set_active_spec() {
        int max_talents = 0;
        String max_spec = null;
        for (String key : talents_in_spec.keySet()) {
            int spent_in_spec = talents_in_spec.get(key);
            if (spent_in_spec > 31)
                throw new InvalidTalentException(String.format("You cannot spend more than 31 talents in one tree"));
            else if (spent_in_spec > max_talents) {
                max_talents = spent_in_spec;
                max_spec = key;
            }
        }
        this.active_spec = max_spec;
    }

    public boolean is_specced(String spec) {
        if (this.active_spec == null)
            this.set_active_spec();
        return active_spec.equals(spec);
    }

    public boolean check_valid_talent(String talent, int value) {
        boolean check1 = (is_allowed_talent(talent) && value >= 0);
        boolean check2 = false;
        try {
            check2 = allowed_talents.get(tree_for_talent(talent)).get(talent)
                    .get(0) >= value;
        }
        finally {}
        if (!check1 || !check2) {
            throw new InvalidTalentException(String.format("Invalid value %s for talent %s", value, talent));
        }
        return check1 && check2;
    }

    /**
     * Use the method tree_for_talent to check if the talent is allowed.
     * 
     * @param talent
     *            Talent name
     * @return True if the talent is allowed
     */
    public boolean is_allowed_talent(String talent) {
        boolean check = true;
        try {
            tree_for_talent(talent);
        }
        catch (InvalidTalentException e) {
            check = false;
        }
        return check;
    }

    /**
     * In the original pythonic back-end we used to need to use this function
     * every time we used getattr. This current implementation has a
     * spec-agnostic cache. However, the setter function will run through this,
     * given the current setup of the allowed_talents hash.
     * 
     * @param talent
     *            Talent name
     * @return A string with the spec/tree in which the talent lives.
     */
    public String tree_for_talent(String talent) {
        String tree = null;
        for (String spec : this.specs) {
            tree = (allowed_talents.get(spec).containsKey(talent)) ? spec : tree;
        }
        if (tree == null)
            throw new InvalidTalentException(String.format("Invalid talent name %s", talent));
        return tree;
    }

    public Set<String> get_all_talents_for_tier(int tier) {
        Set<String> talents_in_tier = new HashSet<String>();
        for (String spec : this.specs) {
            for (Entry<String, List<Integer>> talent : this.allowed_talents.get(spec).entrySet()) {
                if (talent.getValue().get(1).equals(tier))
                    talents_in_tier.add(talent.getKey());
            }
        }
        return talents_in_tier;
    }

    public Set<String> get_all_talents_up_to_tier(int tier) {
        Set<String> talents_up_to_tier = new HashSet<String>();
        for (String spec : this.specs) {
            for (Entry<String, List<Integer>> talent : this.allowed_talents.get(spec).entrySet()) {
                if (talent.getValue().get(1) <= tier)
                    talents_up_to_tier.add(talent.getKey());
            }
        }
        return talents_up_to_tier;
    }

    public Set<String> get_all_talents_for_spec(String spec_name) {
        Set<String> talents_in_spec = new HashSet<String>();
        for (String spec : this.specs) {
            if (spec.equals(spec_name)) {
                for (Entry<String, List<Integer>> talent : this.allowed_talents.get(spec).entrySet()) {
                    talents_in_spec.add(talent.getKey());
                }
            }
        }
        return talents_in_spec;
    }

    public Set<String> get_all_talents_for_active_spec() {
        return get_all_talents_for_spec(this.active_spec);
    }

}
