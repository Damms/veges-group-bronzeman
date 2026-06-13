package com.veges.vegesguide;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

/**
 * Run this main() to launch a RuneLite dev client with the plugin loaded.
 * In IntelliJ: right-click this file -> Run 'VegesGuidePluginTest.main()'.
 */
public class VegesGuidePluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(VegesGuidePlugin.class);
		RuneLite.main(args);
	}
}
