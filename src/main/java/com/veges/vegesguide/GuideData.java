package com.veges.vegesguide;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.runelite.api.Quest;
import net.runelite.api.Skill;

/**
 * All guide content lives here. To extend the guide, add Items to a Section,
 * add Sections to a Build, or add a whole new Build in all().
 *
 * Checkable items must have a stable, unique id within their build — that id
 * is what gets saved to the RuneLite config, so don't renumber existing ones.
 */
final class GuideData
{
	static final class SkillReq
	{
		final Skill skill;
		final int level;

		SkillReq(Skill skill, int level)
		{
			this.skill = skill;
			this.level = level;
		}
	}

	static SkillReq req(Skill skill, int level)
	{
		return new SkillReq(skill, level);
	}

	static final class Item
	{
		final String id;          // stable id for saved state (checkable items only)
		final String name;        // bold title; escaped as plain text
		final String desc;        // short line under the title; escaped as plain text
		final boolean checkable;
		final Quest quest;        // if set, auto-ticked when this quest is FINISHED in-game
		final List<SkillReq> reqs; // skill levels needed; shown met/unmet against the player

		Item(String id, String name, String desc, boolean checkable)
		{
			this(id, name, desc, checkable, (Quest) null);
		}

		Item(String id, String name, String desc, boolean checkable, Quest quest, SkillReq... reqs)
		{
			this.id = id;
			this.name = name;
			this.desc = desc;
			this.checkable = checkable;
			this.quest = quest;
			this.reqs = Arrays.asList(reqs);
		}
	}

	static final class Section
	{
		final String title;
		final List<Item> items;

		Section(String title, Item... items)
		{
			this.title = title;
			this.items = Arrays.asList(items);
		}
	}

	static final class Build
	{
		final String key;          // used in saved-state keys
		final String name;         // shown in the dropdown
		final boolean trackable;   // shows a progress bar + checkboxes
		final String summaryHtml;  // raw HTML intro block
		final List<Section> sections;

		Build(String key, String name, boolean trackable, String summaryHtml, Section... sections)
		{
			this.key = key;
			this.name = name;
			this.trackable = trackable;
			this.summaryHtml = summaryHtml;
			this.sections = Arrays.asList(sections);
		}
	}

	static List<Build> all()
	{
		List<Build> b = new ArrayList<>();
		b.add(pure());
		b.add(fortyDef());
		b.add(zerker());
		b.add(med());
		b.add(pivot());
		return b;
	}

	private static String summary(String tagline, String s, String pros, String play, String pk)
	{
		return "<html><body style='width:195px'>"
			+ "<i>" + tagline + "</i><br><br>"
			+ "<b style='color:#c9a227'>SUMMARY</b><br>" + s + "<br><br>"
			+ "<b style='color:#c9a227'>PROS</b><br>" + pros + "<br><br>"
			+ "<b style='color:#c9a227'>PLAYSTYLE</b><br>" + play + "<br><br>"
			+ "<b style='color:#c9a227'>PKING YOU'LL DO</b><br>" + pk
			+ "</body></html>";
	}

	private static Build pure()
	{
		return new Build("pure", "1 Def Pure", true,
			summary(
				"The glass cannon. Defence stays at 1 to keep combat level as low as possible.",
				"1 Defence, high Strength/Ranged/Magic, Attack capped at 50/60/75. Combat ~40-75. Cheapest and fastest pure to build.",
				"Lowest combat bracket so you fight the weakest opponents. Cheap, quick, huge burst.",
				"All offence, no defence. Out-damage, out-eat and out-spec rather than out-tank.",
				"Edgeville single, F2P/P2P pure brackets, LMS, Deadman, NH rune pure, DDS+Gmaul combo, MSB ranging."
			),
			new Section("Phase 1 - fresh start",
				new Item("q01", "Cook's Assistant", "300 Cooking XP; RFD prereq", true, Quest.COOKS_ASSISTANT),
				new Item("q02", "Witch's Potion", "325 Magic XP", true, Quest.WITCHS_POTION),
				new Item("q03", "Imp Catcher", "875 Magic XP", true, Quest.IMP_CATCHER),
				new Item("q04", "The Restless Ghost", "1,125 Prayer XP", true, Quest.THE_RESTLESS_GHOST),
				new Item("q05", "Goblin Diplomacy", "RFD prereq", true, Quest.GOBLIN_DIPLOMACY),
				new Item("q06", "Romeo & Juliet", "5 Quest Points", true, Quest.ROMEO__JULIET)
			),
			new Section("Phase 2 - combat XP via questing",
				new Item("q07", "Waterfall Quest", "13,750 Atk + 13,750 Str XP. Core milestone", true, Quest.WATERFALL_QUEST),
				new Item("q08", "Fight Arena", "12,175 Atk XP", true, Quest.FIGHT_ARENA),
				new Item("q09", "Tree Gnome Village", "11,450 Atk XP; Grand Tree prereq", true, Quest.TREE_GNOME_VILLAGE),
				new Item("q10", "The Grand Tree", "18,400 Atk XP; MM prereq", true, Quest.THE_GRAND_TREE),
				new Item("q11", "Vampire Slayer", "4,825 Atk XP", true, Quest.VAMPYRE_SLAYER),
				new Item("q12", "Death Plateau", "Climbing Boots", true, Quest.DEATH_PLATEAU),
				new Item("q13", "Mountain Daughter", "1,000 Atk + 2,000 Prayer; Bear Head", true, Quest.MOUNTAIN_DAUGHTER)
			),
			new Section("Phase 3 - weapon unlocks",
				new Item("q14", "Lost City", "Dragon Dagger (p++) access", true, Quest.LOST_CITY,
					req(Skill.WOODCUTTING, 36), req(Skill.CRAFTING, 31)),
				new Item("q15", "Monkey Madness I", "Dragon Scimitar. Do NOT revisit Ape Atoll", true, Quest.MONKEY_MADNESS_I)
			),
			new Section("Phase 4 - prayer/ranged/magic",
				new Item("q16", "Priest in Peril", "Morytania; Ava's prereq", true, Quest.PRIEST_IN_PERIL),
				new Item("q17", "Animal Magnetism", "Ava's Accumulator", true, Quest.ANIMAL_MAGNETISM,
					req(Skill.SLAYER, 18), req(Skill.CRAFTING, 19), req(Skill.RANGED, 30), req(Skill.WOODCUTTING, 35)),
				new Item("q18", "Horror from the Deep", "Unholy Book (god book)", true, Quest.HORROR_FROM_THE_DEEP),
				new Item("q19", "Desert Treasure I", "Ancient Magicks / Ice Barrage", true, Quest.DESERT_TREASURE_I,
					req(Skill.MAGIC, 50), req(Skill.FIREMAKING, 50), req(Skill.THIEVING, 53), req(Skill.SLAYER, 10))
			),
			new Section("Phase 5 - endgame",
				new Item("q20", "Recipe for Disaster (6 subquests)", "Mithril Gloves (easiest 5 subquests)", true, Quest.RECIPE_FOR_DISASTER,
					req(Skill.COOKING, 31)),
				new Item("q21", "Ghosts Ahoy", "2,400 Prayer XP", true, Quest.GHOSTS_AHOY)
			),
			new Section("Beyond the quests - key unlocks",
				new Item("u01", "Fighter Torso (Barb Assault)", "Top strength body, free, no def req", false),
				new Item("u02", "Fire Cape (Fight Caves)", "BiS melee cape", false),
				new Item("u03", "Ardougne Cape (Ardy Diary)", "Strong cape slot before fire cape", false),
				new Item("u04", "Mage Arena I & II", "God cape + imbued god cape (best mage cape)", false),
				new Item("u05", "Void (Pest Control)", "Ranged void is excellent at low combat", false),
				new Item("u06", "Salve amulet (Haunted Mine)", "BiS vs undead; choice XP keeps it pure-safe", false)
			)
		);
	}

	private static Build fortyDef()
	{
		return new Build("fortydef", "40 Def", false,
			summary(
				"A pure with a touch of armour. Stops at exactly 40 Defence.",
				"Defence 40, otherwise built like a pure. Combat ~78, just below a zerker.",
				"Fighter Hat + Fighter Torso + Rune armour + Barrows gloves, at lower combat than a 45 zerker.",
				"Melee-led with d'hide switches. Tankier than a 1 def but still aggressive. No Berserker helm.",
				"Low zerker bracket, Edgeville single, multi-combat, NH/hybrid, LMS."
			),
			new Section("How to build it",
				new Item(null, "1. No-defence quests first", "Same Phase 1-3 quests as the 1 Def Pure tab", false),
				new Item(null, "2. Defence quests", "Nature Spirit, Dragon Slayer I, Heroes' Quest (~lvl 35)", false),
				new Item(null, "3. Skip Daero's MM training", "Its 20k Def XP overshoots 40 - this is the key difference", false),
				new Item(null, "4. Train to exactly 40", "Check Def XP before every quest", false)
			),
			new Section("Beyond the quests",
				new Item(null, "Same unlocks as 1 Def Pure", "Fighter Torso, Fire Cape, Ardy Cape, Mage Arena, Void, Salve. Fighter Hat pairs with Rune armour", false)
			)
		);
	}

	private static Build zerker()
	{
		return new Build("zerker", "Zerker - 45 Def", true,
			summary(
				"The most popular hybrid PK build. Exactly 45 Defence for the Berserker helm.",
				"45 Def, 99 Str, 60-75 Atk, 94+ Magic. Combat ~83-101. Strong PvP, viable PvM.",
				"Berserker helm + Barrows gloves + Vengeance. Best burst in its bracket, can PvM to fund itself.",
				"Tribrid/hybrid - swap melee, ranged and Ice Barrage, land Veng combos and spec KOs.",
				"Multi Veng clans, Edgeville single, tribrid, LMS. Fights zerkers, voiders, 70 def."
			),
			new Section("Foundation",
				new Item("z01", "Finish all 1 Def Pure combat/weapon quests", "No-defence quests give your base for free", true)
			),
			new Section("Defence quests (in order -> ~44)",
				new Item("z02", "Nature Spirit", "Morytania swamp; 2,000 Def XP", true, Quest.NATURE_SPIRIT),
				new Item("z03", "Dragon Slayer I", "Rune platebody; 18,650 Def XP", true, Quest.DRAGON_SLAYER_I),
				new Item("z04", "Heroes' Quest", "Heroes' Guild; 3,075 Def XP", true, Quest.HEROES_QUEST,
					req(Skill.COOKING, 53), req(Skill.FISHING, 53), req(Skill.HERBLORE, 25), req(Skill.MINING, 50)),
				new Item("z05", "Monkey Madness I - accept Daero's training", "20,000 Def XP. Zerker only!", true, Quest.MONKEY_MADNESS_I),
				new Item("z06", "The Fremennik Trials", "Berserker Helm access; 2,812 Def XP", true, Quest.THE_FREMENNIK_TRIALS),
				new Item("z07", "Train Defence to exactly 45", "Never do King's Ransom (breaks the build)", true)
			),
			new Section("Barrows Gloves",
				new Item("z08", "Recipe for Disaster (full questline)", "Barrows Gloves (+12 all combat)", true, Quest.RECIPE_FOR_DISASTER,
					req(Skill.COOKING, 70), req(Skill.AGILITY, 48), req(Skill.MINING, 50), req(Skill.FISHING, 53),
					req(Skill.THIEVING, 53), req(Skill.HERBLORE, 25), req(Skill.MAGIC, 59), req(Skill.SMITHING, 40),
					req(Skill.FIREMAKING, 50), req(Skill.RANGED, 40), req(Skill.CRAFTING, 40), req(Skill.FLETCHING, 10),
					req(Skill.WOODCUTTING, 36))
			),
			new Section("Endgame",
				new Item("z09", "Lunar Diplomacy", "Vengeance", true, Quest.LUNAR_DIPLOMACY,
					req(Skill.CRAFTING, 61), req(Skill.DEFENCE, 40), req(Skill.FIREMAKING, 49), req(Skill.MAGIC, 65),
					req(Skill.MINING, 60), req(Skill.WOODCUTTING, 55), req(Skill.HERBLORE, 5)),
				new Item("z10", "Desert Treasure I", "Ancient Magicks / Ice Barrage", true, Quest.DESERT_TREASURE_I,
					req(Skill.MAGIC, 50), req(Skill.FIREMAKING, 50), req(Skill.THIEVING, 53), req(Skill.SLAYER, 10)),
				new Item("z11", "Animal Magnetism", "Ava's Accumulator", true, Quest.ANIMAL_MAGNETISM,
					req(Skill.SLAYER, 18), req(Skill.CRAFTING, 19), req(Skill.RANGED, 30), req(Skill.WOODCUTTING, 35))
			),
			new Section("Beyond the quests - key unlocks",
				new Item("zu1", "Fighter Torso (Barb Assault)", "Pairs with the Berserker helm", false),
				new Item("zu2", "Fire Cape (Fight Caves)", "BiS melee cape", false),
				new Item("zu3", "Imbued god cape (Mage Arena II)", "Best mage cape for your barrage switch", false),
				new Item("zu4", "Void (Pest Control)", "Ranged void for tribrid", false),
				new Item("zu5", "Imbued rings (NMZ/Soul Wars)", "Berserker/Archer/Seers (i)", false),
				new Item("zu6", "Salve amulet (Haunted Mine)", "BiS vs undead for PvM", false)
			)
		);
	}

	private static Build med()
	{
		return new Build("med", "Med - 70/75 Def", false,
			summary(
				"The bully of the mid-level brackets. 70-75 Defence unlocks Piety/Rigour/Augury.",
				"70-80 Def, 60-82 Atk, 77 Prayer. Combat ~95-110. Full PvM, most lenient build to train.",
				"Piety/Rigour/Augury, Barrows + high-tier defensive gear, real gear & stat edge over pures and zerkers.",
				"Prayer-boosted hybrid/tribrid. Out-tank and out-last lower builds, then spec out with Claws or AGS.",
				"Deep wilderness, multi, hybrid/tribrid, PvM+PK. Preys on zerkers and pures."
			),
			new Section("How to build it",
				new Item(null, "1. Combat-XP quests + 200 QP", "200 QP unlocks the big quests", false),
				new Item(null, "2. Dragon Slayer I & II", "DS2 also enables Vengeance + huge content", false),
				new Item(null, "3. Def to 70/75, then 77 Prayer", "Buy Dex + Arcane scrolls for Rigour/Augury", false),
				new Item(null, "4. Pick Attack cap", "60 for Dragon Claws (lower cb) or 75 for AGS", false)
			),
			new Section("Target stats",
				new Item(null, "Def 70 or 75 - Str 99 - Atk 60/75", "Ranged 99 - Magic 94+ - Prayer 77 - high HP", false)
			),
			new Section("Beyond the quests - key unlocks",
				new Item(null, "Piety, Rigour & Augury (70 Def)", "The biggest reason to be a med", false),
				new Item(null, "Barrows armour (70 Def)", "Dharok's, Karil's, Ahrim's", false),
				new Item(null, "High-tier gear (75 Def)", "Serp helm, DFS, primordials, spirit shields", false),
				new Item(null, "Vorkath head -> Ava's assembler", "Best ranged cape, after DS2", false),
				new Item(null, "Fire Cape / Infernal cape", "Core melee capes", false),
				new Item(null, "Salve amulet (i/ei)", "BiS vs undead across Slayer & bosses", false)
			),
			new Section("Bronzeman note",
				new Item(null, "Higher Defence = more content to farm", "A med can run Slayer, Barrows and most bosses, unlocking gear a 1 def pure can never reach. Often the strongest long-term path for a group Bronzeman", false)
			)
		);
	}

	private static Build pivot()
	{
		return new Build("pivot", "Pivot Guide", false,
			"<html><body style='width:195px'>"
				+ "<b style='color:#c9a227'>THE BUILD LADDER</b><br>"
				+ "You can climb up but never back down - Defence only goes up.<br><br>"
				+ "<b>1 Def Pure</b> ~ cb 40-75. Lowest bracket, glass cannon.<br>"
				+ "<b>40 Def</b> ~ cb 78. Fighter Torso + Rune armour, no zerk helm.<br>"
				+ "<b>Zerker 45</b> ~ cb 83-101. Berserker helm + Veng. Most popular.<br>"
				+ "<b>Med 70/75</b> ~ cb 95-110. Piety/Rigour/Augury + Barrows. Best for Bronzeman."
				+ "</body></html>",
			new Section("When to pivot",
				new Item(null, "Decide before Monkey Madness I", "Everything up to there is reusable on any path", false)
			),
			new Section("What changes on the zerker path",
				new Item(null, "Accept Daero's MM1 training", "20,000 Def XP - pures skip this", false),
				new Item(null, "Nature Spirit + Dragon Slayer I + Heroes' Quest", "The added defence quests", false),
				new Item(null, "The Fremennik Trials", "Berserker Helm", false),
				new Item(null, "Recipe for Disaster (full)", "Barrows Gloves instead of Mithril", false),
				new Item(null, "Lunar Diplomacy", "Vengeance", false)
			),
			new Section("Hard limits",
				new Item(null, "No King's Ransom", "33k Def XP - ruins a zerker", false),
				new Item(null, "No Piety on a zerker", "Requires King's Ransom", false),
				new Item(null, "Cap Defence at 45", "Stop the moment you hit it", false),
				new Item(null, "Don't revisit Ape Atoll", "If you still want to chin there", false)
			)
		);
	}

	private GuideData()
	{
	}
}
