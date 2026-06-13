package com.veges.vegesguide;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup(VegesGuideConfig.GROUP)
public interface VegesGuideConfig extends Config
{
	String GROUP = "vegesguide";

	@ConfigItem(
		keyName = "showProgress",
		name = "Show progress bar",
		description = "Show the completion progress bar on trackable builds"
	)
	default boolean showProgress()
	{
		return true;
	}
}
