package com.github.kimberlyow.fishonautotip.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = "fishonautotip")
public class FishOnAutotipConfig implements ConfigData {
	/* 
	*  Default is 100 for Reactions, 500 (+500) for every rarity of Chummers
	*  Why? seems realistic for most players
	*/ 
	public int reactionTipAmount = 100;
	public int commonChummerTipAmount = 500;
	public int rareChummerTipAmount = 1000;
	public int epicChummerTipAmount = 1500;
	public int legendaryChummerTipAmount = 2000;
	public int mythicalChummerTipAmount = 2500;
}