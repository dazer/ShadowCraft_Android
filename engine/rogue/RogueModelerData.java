package rogue;

import java.util.HashMap;
import java.util.Map;

import core.util;

@SuppressWarnings("serial")
public class RogueModelerData {

    static final Map<String, Map<Integer, Float>> level_parameters = new HashMap<String, Map<Integer, Float>>() {{
        put("bs_bonus_dmg", util.mkMap(new double[][] {
                {80, 310}, {81, 317}, {82, 324}, {83, 331}, {84, 338},
                {85, 345}
        }));
        put("mut_bonus_dmg", util.mkMap(new double[][] {
                {80, 180},
                {85, 201}
        }));
        put("ss_bonus_dmg", util.mkMap(new double[][] {
                {80, 180}, {81, 184}, {82, 188}, {83, 192}, {84, 196},
                {85, 200}
        }));
        put("ambush_bonus_dmg", util.mkMap(new double[][] {
                {80, 330}, {81, 338}, {82, 345}, {83, 353}, {84, 360},
                {85, 368}}));
        put("vw_base_dmg", util.mkMap(new double[][] {
                {80, 363},
                {85, 675}
        }));
        put("vw_percentage_dmg", util.mkMap(new double[][] {
                {80, .135},
                {85, .176}
        }));
        put("ip_base_dmg", util.mkMap(new double[][] {
                {80, 350},
                {85, 352}
        }));
        put("dp_base_dmg", util.mkMap(new double[][] {
                {80, 296},
                {85, 540}
        }));
        put("dp_percentage_dmg", util.mkMap(new double[][] {
                {80, .108},
                {85, .14}
        }));
        put("wp_base_dmg", util.mkMap(new double[][] {
                {80, 231},
                {85, 276}
        }));
        put("wp_percentage_dmg", util.mkMap(new double[][] {
                {80, .036},
                {85, .04}
        }));
        put("garrote_base_dmg", util.mkMap(new double[][] {
                {80, 119}, {81, 122}, {82, 125}, {83, 127}, {84, 130},
                {85, 133}
        }));
        put("rup_base_dmg", util.mkMap(new double[][] {
                {80, 127}, {81, 130}, {82, 133}, {83, 136}, {84, 139},
                {85, 142}
        }));
        put("rup_bonus_dmg", util.mkMap(new double[][] {
                {80, 18}, {81, 19}, {82, 19}, {83, 19}, {84, 20},
                {85, 20}
        }));
        put("evis_base_dmg", util.mkMap(new double[][] {
                {80, 329}, {81, 334}, {82, 339}, {83, 344}, {84, 349},
                {85, 354}
        }));
        put("evis_bonus_dmg", util.mkMap(new double[][] {
                {80, 481}, {81, 488}, {82, 495}, {83, 503}, {84, 510},
                {85, 517}
        }));
        put("env_base_dmg", util.mkMap(new double[][] {
                {80, 216}, {81, 221}, {82, 226}, {83, 231}, {84, 236},
                {85, 241}
        }));
        put("agi_per_crit", util.mkMap(new double[][] {
                {80, 83.15 * 100}, {81, 109.18 * 100}, {82, 143.37 * 100}, {83, 188.34 * 100}, {84, 247.3 * 100},
                {85, 324.72 * 100}
        }));
    }};

}
