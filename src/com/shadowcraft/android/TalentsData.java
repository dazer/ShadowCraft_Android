package com.shadowcraft.android;

public class TalentsData {

    static final int[][][] maxTalentMap = new int[][][] {
        // [class][tree][talent]
        {null},
        {null},
        {null},
        {null},
        {
            {2,3,3,0,3,2,3,2,2,1,3,0,3,2,0,0,2,1,1,2,0,3,2,0,0,1,0,0},
            {2,3,3,0,2,2,3,2,3,1,2,2,0,3,2,0,2,1,2,0,3,0,2,0,0,1,0,0},
            {2,3,3,0,2,2,3,2,3,2,1,0,3,1,0,3,3,1,2,0,0,3,2,0,0,1,0,0}
        },
        {null}
    };

    static final int[][][] talentIconID = new int[][][] {
        // [class][tree][talent]
        {},
        {},
        {},
        {},
        {
            {
                R.drawable.ability_rogue_deadlymomentum,
                R.drawable.ability_rogue_eviscerate,
                R.drawable.ability_criticalstrike,
                R.drawable.ability_druid_disembowel,
                R.drawable.ability_rogue_quickrecovery,
                R.drawable.ability_backstab,
                R.drawable.ability_rogue_blackjack,
                R.drawable.ability_rogue_deadlybrew,
                R.drawable.spell_ice_lament,
                R.drawable.ability_rogue_feigndeath,
                R.drawable.ability_rogue_deadenednerves,
                R.drawable.ability_rogue_stayofexecution,
                R.drawable.spell_shadow_deathscream,
                R.drawable.ability_hunter_rapidkilling,
                R.drawable.ability_creature_poison_06,
                R.drawable.ability_warrior_riposte,
                R.drawable.ability_rogue_cuttothechase,
                R.drawable.ability_rogue_venomouswounds,
                R.drawable.ability_rogue_deadliness,
            },
            {   /*
                R.drawable.ability_rogue_improvedrecuperate,
                R.drawable.spell_shadow_ritualofsacrifice,
                R.drawable.ability_marksmanship,
                R.drawable.ability_rogue_slicedice,
                R.drawable.ability_rogue_sprint,
                R.drawable.ability_racial_avatar,
                R.drawable.ability_kick,
                R.drawable.spell_nature_invisibilty,
                R.drawable.inv_sword_97,
                R.drawable.ability_rogue_reinforcedleather,
                R.drawable.ability_gouge,
                R.drawable.inv_weapon_shortblade_38,
                R.drawable.ability_rogue_bladetwisting,
                R.drawable.ability_rogue_throwingspecialization,
                R.drawable.spell_shadow_shadowworddominate,
                R.drawable.ability_creature_disease_03,
                R.drawable.ability_rogue_preyontheweak,
                R.drawable.ability_rogue_restlessblades,
                R.drawable.ability_rogue_murderspree,
             */
            },
            {   /*
                R.drawable.ability_stealth,
                R.drawable.ability_rogue_ambush,
                R.drawable.ability_warrior_decisivestrike,
                R.drawable.spell_magic_lesserinvisibilty,
                R.drawable.ability_rogue_waylay,
                R.drawable.ability_rogue_bloodsplatter,
                R.drawable.spell_shadow_fumble,
                R.drawable.ability_rogue_sturdyrecuperate,
                R.drawable.ability_rogue_findweakness,
                R.drawable.spell_shadow_lifedrain,
                R.drawable.ability_rogue_honoramongstthieves,
                R.drawable.spell_shadow_possession,
                R.drawable.ability_rogue_envelopingshadows,
                R.drawable.ability_rogue_cheatdeath,
                R.drawable.ability_rogue_preparation,
                R.drawable.ability_rogue_sanguinaryvein,
                R.drawable.ability_rogue_slaughterfromtheshadows,
                R.drawable.inv_sword_17,
                R.drawable.ability_rogue_shadowdance,
             */
            }
        },
        {}
    };
}
