package calcs;

import java.util.ArrayList;
import java.util.List;

import core.InvalidLevelException;

/**
 * Level dependent boss armor mitigation calculation.
 */
public class ArmorMitigation {

    /**
     * Tiered parameters for use in armor mitigation calculations. First float[]
     * element is the minimum level of the tier. The list must be in descending
     * order of minimum level for the lookup to work. Parameters taken from
     * http://elitistjerks.com/f15/t29453-combat_ratings_level_85_cataclysm/. It
     * is confirmed that the 81+ ratings are in effect in pre-cata 4.0.1 in
     * http://blue.mmo-champion.com/topic/22463/physical-mitigation-change-intentional
     */
    @SuppressWarnings("serial")
    static final List<float[]> PARAMETERS = new ArrayList<float[]>() {{
        add(new float[] {81, (float) 2167.5, (float) 158167.5 });
        add(new float[] {60, (float)  467.5, (float)  22167.5 });
        add(new float[] { 1, (float)   85.0, (float)   -400.0 }); // yes, negative 400
    }};

    /**
     * Figures what parameters to use given a player level. Throws exception if
     * no parameters are found.
     * 
     * @param level Player level.
     * @return array of parameters
     */
    public static float[] lookup_parameters(int level) {
        for (float[] parameters : PARAMETERS) {
            if (level >= parameters[0])
                return parameters;
        }
        throw new InvalidLevelException(String.format("No armor mitigation parameters available for level %s", level));
    }

    /**
     * Datamined formula is armor / (armor + parameter1 * level - parameter2)
     * this will calculate the latter part of it.
     * 
     * @param level
     * @return part of the formula not tied to target armor.
     */
    public static float parameter(int level) {
        float[] parameters = lookup_parameters(level);
        return level * parameters[1] - parameters[2];
    }

    /**
     * Overload of parameter(...)
     */
    public static float parameter() {
        return parameter(85);
    }

    /**
     * Returns the fraction of damage reduced by the armor.
     * 
     * @param armor Target armor.
     * @param cached_parameter Armor parameter if not cached.
     * @param level Player level.
     * @return Damage reduction DR.
     */
    public float mitigation(float armor, float cached_parameter, int level) {
        if (cached_parameter == 0)
            cached_parameter = parameter(level);
        return armor / (armor + cached_parameter);
    }

    /**
     * Overload of mitigation(...)
     */
    public float mitigation(float armor, float cached_parameter) {
        return mitigation(armor, cached_parameter, 85);
    }

    /**
     * Overload of mitigation(...)
     */
    public float mitigation(float armor) {
        return mitigation(armor, 0, 85);
    }

    /**
     * This is what callers should point to. Returns the fraction of damage
     * retained despite the armor, 1 - mitigation.
     * 
     * @param armor Target armor.
     * @param cached_parameter Armor parameter if not cached.
     * @param level Player level.
     * @return Damage multiplier, a number ranging from 0 to 1.
     */
    public static double multiplier(double armor, double cached_parameter, int level) {
        if (cached_parameter == 0)
            cached_parameter = parameter(level);
        return cached_parameter / (armor + cached_parameter);
    }

    /**
     * Overload of multiplier(...)
     */
    public static double multiplier(double armor, double cached_parameter) {
        return multiplier(armor, cached_parameter, 85);
    }

}
