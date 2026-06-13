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

	@ConfigItem(
		keyName = "showAllPrereqs",
		name = "Show all prerequisites",
		description = "In the prerequisite trees, show completed quests and met requirements too, instead of only what is still outstanding"
	)
	default boolean showAllPrereqs()
	{
		return false;
	}

	@ConfigItem(
		keyName = "hideCompleted",
		name = "Hide completed quests",
		description = "Hide quests entirely once they are checked off, instead of showing them as a collapsed done line"
	)
	default boolean hideCompleted()
	{
		return false;
	}
}
