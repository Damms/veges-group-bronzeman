package com.veges.vegesguide;

import com.google.inject.Provides;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import javax.inject.Inject;
import javax.swing.SwingUtilities;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Quest;
import net.runelite.api.QuestState;
import net.runelite.api.Skill;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.StatChanged;
import net.runelite.api.events.VarbitChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;

@PluginDescriptor(
	name = "Veges PK Guide",
	description = "Phob's guide to the Veges Group Bronzeman: pure/zerker/med build paths and a quest checklist",
	tags = {"pk", "pure", "zerker", "med", "bronzeman", "guide", "quest"}
)
public class VegesGuidePlugin extends Plugin
{
	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private ConfigManager configManager;

	@Inject
	private Client client;

	private VegesGuidePanel panel;
	private NavigationButton navButton;

	// Set when quest state or skill levels may have changed; the next GameTick
	// (on the client thread) performs the actual scan so we don't read varps
	// and stats every tick.
	private boolean syncPending;

	// Last skill-level snapshot pushed to the panel, for change detection.
	private Map<Skill, Integer> lastLevels;

	@Provides
	VegesGuideConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(VegesGuideConfig.class);
	}

	@Override
	protected void startUp()
	{
		panel = new VegesGuidePanel(configManager);
		navButton = NavigationButton.builder()
			.tooltip("Veges PK Guide")
			.icon(buildIcon())
			.priority(7)
			.panel(panel)
			.build();
		clientToolbar.addNavigation(navButton);

		// If enabled mid-session while logged in, scan on the next tick.
		syncPending = true;
	}

	@Override
	protected void shutDown()
	{
		clientToolbar.removeNavigation(navButton);
		panel = null;
		navButton = null;
		syncPending = false;
		lastLevels = null;
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() == GameState.LOGGED_IN)
		{
			syncPending = true;
		}
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged event)
	{
		// Quest progress is stored in varps/varbits; flag a scan and let the
		// next GameTick do the (cheap, throttled) read.
		syncPending = true;
	}

	@Subscribe
	public void onStatChanged(StatChanged event)
	{
		// A skill level may have changed; refresh the met/unmet requirements.
		syncPending = true;
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		if (syncPending)
		{
			syncPending = false;
			syncCompletedQuests();
		}
	}

	// Runs on the client thread (GameTick). Marks finished quests done and
	// snapshots the player's skill levels for the requirements display. Quest
	// state is only ever set to done - it never un-ticks, so manual ticks are
	// preserved.
	private void syncCompletedQuests()
	{
		if (panel == null || client.getGameState() != GameState.LOGGED_IN)
		{
			return;
		}

		boolean questsChanged = false;
		for (GuideData.Build build : GuideData.all())
		{
			if (!build.trackable)
			{
				continue;
			}

			for (GuideData.Section section : build.sections)
			{
				for (GuideData.Item item : section.items)
				{
					Quest quest = item.quest;
					if (quest == null || item.id == null)
					{
						continue;
					}

					if (quest.getState(client) == QuestState.FINISHED && !isDone(build.key, item.id))
					{
						configManager.setConfiguration(VegesGuideConfig.GROUP,
							VegesGuidePanel.stateKey(build.key, item.id), true);
						questsChanged = true;
					}
				}
			}
		}

		Map<Skill, Integer> levels = captureLevels();
		boolean levelsChanged = !levels.equals(lastLevels);
		lastLevels = levels;

		if (questsChanged || levelsChanged)
		{
			Map<Skill, Integer> snapshot = Collections.unmodifiableMap(levels);
			SwingUtilities.invokeLater(() ->
			{
				if (panel != null)
				{
					panel.setPlayerLevels(snapshot);
					panel.refresh();
				}
			});
		}
	}

	// Real (un-boosted) skill levels, matching how quest requirements are gated.
	private Map<Skill, Integer> captureLevels()
	{
		Map<Skill, Integer> levels = new EnumMap<>(Skill.class);
		for (Skill skill : Skill.values())
		{
			// Skip aggregate/unreleased entries (OVERALL is deprecated, so match
			// by name to avoid referencing the deprecated constant directly).
			String name = skill.name();
			if (name.equals("OVERALL") || name.equals("SAILING"))
			{
				continue;
			}
			levels.put(skill, client.getRealSkillLevel(skill));
		}
		return levels;
	}

	private boolean isDone(String buildKey, String id)
	{
		Boolean v = configManager.getConfiguration(VegesGuideConfig.GROUP,
			VegesGuidePanel.stateKey(buildKey, id), Boolean.class);
		return Boolean.TRUE.equals(v);
	}

	// Generated in code so the plugin needs no bundled image resource.
	private static BufferedImage buildIcon()
	{
		BufferedImage img = new BufferedImage(24, 24, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = img.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(new Color(168, 130, 31));
		g.fillRoundRect(1, 1, 22, 22, 7, 7);
		g.setColor(new Color(40, 30, 12));
		g.setFont(new Font("SansSerif", Font.BOLD, 15));
		g.drawString("V", 7, 18);
		g.dispose();
		return img;
	}
}
