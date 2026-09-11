package com.github.kimberlyow.fishonautotip.compat;

import com.terraformersmc.modmenu.api.ModMenuApi;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.github.kimberlyow.fishonautotip.config.FishOnAutotipConfig;
import me.shedaniel.autoconfig.AutoConfig;

public class ModmenuIntegration implements ModMenuApi {
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return parent -> AutoConfig.getConfigScreen(FishOnAutotipConfig.class, parent).get();
	}
}