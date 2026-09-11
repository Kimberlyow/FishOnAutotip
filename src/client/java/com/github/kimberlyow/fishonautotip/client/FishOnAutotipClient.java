package com.github.kimberlyow.fishonautotip.client;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.Minecraft;
import com.github.kimberlyow.fishonautotip.config.FishOnAutotipConfig;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;

public class FishOnAutotipClient implements ClientModInitializer {
	/*
	* yapping:
	* [A-Za-z0-9_]{2,16} fits the limitations of a Minecraft Username.
	* the character limit is 3-16 now, but there are legacy accounts from before 2015 with 1 and 2 letters.
	* only 2 letter usernames can still join multiplayer in theory (theres 320 of them).
	* source: https://hypixel.net/threads/1-letter-and-2-letter-names-as-of-july-8th-2020.3102654/
	*
	* Regex101 example of this: 
	* https://regex101.com/?regex=REACTIONS+%C2%BB+%28%5BA-Za-z0-9_%5D%7B3%2C16%7D%29+has+typed&testString=REACTIONS+%C2%BB+SaltyZebra+has+typed+Li+in+2.32+seconds&flags=&flavor=pcre2&delimiter=%2F
	*/
	private static final Pattern ReactionPattern = Pattern.compile("REACTIONS » ([A-Za-z0-9_]{2,16}) has typed");
	// Unicode of rarity or message-format might change in the future but very unlikely
	private static final Pattern CommonChummerPattern = Pattern.compile("CHUMMER » ([A-Za-z0-9_]{2,16}) activated a \uF033 Chummer");
	private static final Pattern RareChummerPattern = Pattern.compile("CHUMMER » ([A-Za-z0-9_]{2,16}) activated a \uF034 Chummer");
	private static final Pattern EpicChummerPattern = Pattern.compile("CHUMMER » ([A-Za-z0-9_]{2,16}) activated a \uF035 Chummer");
	private static final Pattern LegendaryChummerPattern = Pattern.compile("CHUMMER » ([A-Za-z0-9_]{2,16}) activated a \uF036 Chummer");
	private static final Pattern MythicalChummerPattern = Pattern.compile("CHUMMER » ([A-Za-z0-9_]{2,16}) activated a \uF037 Chummer");
	// needed for pay cooldown
	private long lastPayTime = 0;

	@Override
	public void onInitializeClient() {	
		
		AutoConfig.register(FishOnAutotipConfig.class, GsonConfigSerializer::new);

		ClientReceiveMessageEvents.GAME.register((chatmessagetext, overlay) -> {
			String chatmessage = chatmessagetext.getString();

			// early return to optimize performance
			if (!chatmessage.contains("»")) { return; };

			FishOnAutotipConfig config = AutoConfig.getConfigHolder(FishOnAutotipConfig.class).getConfig();

			// startsWith is very cheap on performance compared to regex of course, and also many early returns to optimize performance
			if (chatmessage.startsWith("REACTIONS »")) {
				matchMessage(chatmessage, ReactionPattern, config.reactionTipAmount);
			} else if (chatmessage.startsWith("CHUMMER »")) {
				if (matchMessage(chatmessage, CommonChummerPattern, config.commonChummerTipAmount)) { return; };
				if (matchMessage(chatmessage, RareChummerPattern, config.rareChummerTipAmount)) { return; };
				if (matchMessage(chatmessage, EpicChummerPattern, config.epicChummerTipAmount)) { return; };
				if (matchMessage(chatmessage, LegendaryChummerPattern, config.legendaryChummerTipAmount)) { return; };
				matchMessage(chatmessage, MythicalChummerPattern, config.mythicalChummerTipAmount);
			}
		});
	}

	private boolean matchMessage(String chatmessage, Pattern pattern, int reactionTipAmount) {
		// FishOn doesnt allow /pay with less than 100$
		if (reactionTipAmount < 100) { return false; }
		
		Matcher matcher = pattern.matcher(chatmessage);
			if (matcher.find()) {
				onMatchFound(matcher.group(1), reactionTipAmount);
				return true;
		}
		return false;
	}

	private void onMatchFound(String paytarget, int reactionTipAmount) {
		Minecraft client = Minecraft.getInstance();
		client.execute(() -> payPlayer(client, paytarget, reactionTipAmount));
	}

	private void payPlayer(Minecraft client, String paytarget, int payamount) {
			if (client.player == null) { return; }
			if (client.getConnection() == null) { return; }
			// dont pay yourself
			if (client.player.getName().getString().equals(paytarget)) { return; }
			
			// /pay has a 3 second cooldown on FishOn, might change in the future but unlikely
			if (System.currentTimeMillis() - lastPayTime < 3000) { return; }
			lastPayTime = System.currentTimeMillis();

			client.getConnection().sendCommand("pay " + paytarget + " " + payamount);
	}
}