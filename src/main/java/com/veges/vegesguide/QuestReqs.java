package com.veges.vegesguide;

import static com.veges.vegesguide.GuideData.req;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.runelite.api.Quest;
import net.runelite.api.Skill;

/**
 * Prerequisite-quest graph + per-quest skill requirements, used to render the
 * recursive "Prerequisites" tree under each guide quest. Each node lists a
 * quest's DIRECT prerequisite quests and its own skill requirements; the full
 * chain is built by recursing into prereqs. All edges/levels sourced from the
 * OSRS Wiki.
 *
 * Covers every quest in the guide and its transitive prerequisites, including
 * the grandmaster chains (Recipe for Disaster, Dragon Slayer II, King's Ransom).
 * A quest with no node here simply shows no prereq tree. Note: Recipe for
 * Disaster's "Free King Awowogei" needs Monkey Madness I (not II), and "Free Sir
 * Amik Varze" only needs Legends' Quest *started* (not completed), so Legends'
 * is not listed as an RFD prerequisite.
 */
final class QuestReqs
{
	static final class Node
	{
		final List<Quest> prereqs;
		final List<GuideData.SkillReq> reqs;

		Node(List<Quest> prereqs, List<GuideData.SkillReq> reqs)
		{
			this.prereqs = prereqs;
			this.reqs = reqs;
		}
	}

	private static final Map<Quest, Node> NODES = new EnumMap<>(Quest.class);

	private static void add(Quest quest, GuideData.SkillReq[] reqs, Quest... prereqs)
	{
		NODES.put(quest, new Node(Arrays.asList(prereqs), Arrays.asList(reqs)));
	}

	private static GuideData.SkillReq[] reqs(GuideData.SkillReq... r)
	{
		return r;
	}

	static
	{
		// ---- terminals: no prereq quests, no skill requirements ----
		add(Quest.PRIEST_IN_PERIL, reqs());
		add(Quest.THE_RESTLESS_GHOST, reqs());
		add(Quest.ERNEST_THE_CHICKEN, reqs());
		add(Quest.WATERFALL_QUEST, reqs());
		add(Quest.DEATH_PLATEAU, reqs());
		add(Quest.SHIELD_OF_ARRAV, reqs());
		add(Quest.MERLINS_CRYSTAL, reqs());
		add(Quest.DRAGON_SLAYER_I, reqs());
		add(Quest.THE_FREMENNIK_TRIALS, reqs());
		add(Quest.RUNE_MYSTERIES, reqs());
		add(Quest.DRUIDIC_RITUAL, reqs());
		add(Quest.THE_GRAND_TREE, reqs());
		add(Quest.TREE_GNOME_VILLAGE, reqs());
		add(Quest.COOKS_ASSISTANT, reqs());
		add(Quest.GOBLIN_DIPLOMACY, reqs());
		add(Quest.MURDER_MYSTERY, reqs());
		add(Quest.DEMON_SLAYER, reqs());
		add(Quest.GERTRUDES_CAT, reqs());
		add(Quest.PLAGUE_CITY, reqs());
		add(Quest.CLIENT_OF_KOUREND, reqs());
		add(Quest.BLACK_KNIGHTS_FORTRESS, reqs());

		// ---- no prereq quests, but with skill requirements ----
		add(Quest.LOST_CITY, reqs(req(Skill.WOODCUTTING, 36), req(Skill.CRAFTING, 31)));
		add(Quest.THE_DIG_SITE, reqs(req(Skill.AGILITY, 10), req(Skill.HERBLORE, 10), req(Skill.THIEVING, 25)));
		add(Quest.TEMPLE_OF_IKOV, reqs(req(Skill.RANGED, 40), req(Skill.THIEVING, 42)));
		add(Quest.THE_TOURIST_TRAP, reqs(req(Skill.FLETCHING, 10), req(Skill.SMITHING, 20)));
		add(Quest.FAMILY_CREST, reqs(req(Skill.MINING, 40), req(Skill.SMITHING, 40), req(Skill.MAGIC, 59), req(Skill.CRAFTING, 40)));
		add(Quest.THE_GOLEM, reqs(req(Skill.CRAFTING, 20), req(Skill.THIEVING, 25)));
		add(Quest.BIG_CHOMPY_BIRD_HUNTING, reqs(req(Skill.FLETCHING, 5), req(Skill.COOKING, 30), req(Skill.RANGED, 30)));
		add(Quest.FISHING_CONTEST, reqs(req(Skill.FISHING, 10)));

		// ---- with prerequisite quests (and any skill reqs) ----
		add(Quest.TROLL_STRONGHOLD, reqs(req(Skill.AGILITY, 15)), Quest.DEATH_PLATEAU);
		add(Quest.JUNGLE_POTION, reqs(req(Skill.HERBLORE, 3)), Quest.DRUIDIC_RITUAL);
		add(Quest.SHILO_VILLAGE, reqs(req(Skill.CRAFTING, 20), req(Skill.AGILITY, 32)), Quest.JUNGLE_POTION);
		add(Quest.BIOHAZARD, reqs(), Quest.PLAGUE_CITY);
		add(Quest.UNDERGROUND_PASS, reqs(req(Skill.RANGED, 25)), Quest.BIOHAZARD);
		add(Quest.SHADOW_OF_THE_STORM, reqs(req(Skill.CRAFTING, 30)), Quest.THE_GOLEM, Quest.DEMON_SLAYER);
		add(Quest.ICTHLARINS_LITTLE_HELPER, reqs(), Quest.GERTRUDES_CAT);
		add(Quest.A_TAIL_OF_TWO_CATS, reqs(), Quest.ICTHLARINS_LITTLE_HELPER, Quest.GERTRUDES_CAT);
		add(Quest.EADGARS_RUSE, reqs(req(Skill.HERBLORE, 31)), Quest.DRUIDIC_RITUAL, Quest.TROLL_STRONGHOLD);
		add(Quest.HOLY_GRAIL, reqs(req(Skill.ATTACK, 20)), Quest.MERLINS_CRYSTAL);
		add(Quest.ONE_SMALL_FAVOUR,
			reqs(req(Skill.AGILITY, 36), req(Skill.CRAFTING, 25), req(Skill.HERBLORE, 18), req(Skill.SMITHING, 30)),
			Quest.RUNE_MYSTERIES, Quest.SHILO_VILLAGE);
		add(Quest.BONE_VOYAGE, reqs(), Quest.THE_DIG_SITE);
		add(Quest.NATURE_SPIRIT, reqs(), Quest.PRIEST_IN_PERIL, Quest.THE_RESTLESS_GHOST);
		add(Quest.GHOSTS_AHOY, reqs(req(Skill.AGILITY, 25), req(Skill.COOKING, 20)),
			Quest.PRIEST_IN_PERIL, Quest.THE_RESTLESS_GHOST);
		add(Quest.MONKEY_MADNESS_I, reqs(), Quest.THE_GRAND_TREE, Quest.TREE_GNOME_VILLAGE);
		add(Quest.ANIMAL_MAGNETISM,
			reqs(req(Skill.SLAYER, 18), req(Skill.CRAFTING, 19), req(Skill.RANGED, 30), req(Skill.WOODCUTTING, 35)),
			Quest.THE_RESTLESS_GHOST, Quest.ERNEST_THE_CHICKEN, Quest.PRIEST_IN_PERIL);
		add(Quest.HEROES_QUEST,
			reqs(req(Skill.COOKING, 53), req(Skill.FISHING, 53), req(Skill.HERBLORE, 25), req(Skill.MINING, 50)),
			Quest.SHIELD_OF_ARRAV, Quest.LOST_CITY, Quest.MERLINS_CRYSTAL, Quest.DRAGON_SLAYER_I);
		add(Quest.LUNAR_DIPLOMACY,
			reqs(req(Skill.CRAFTING, 61), req(Skill.DEFENCE, 40), req(Skill.FIREMAKING, 49), req(Skill.MAGIC, 65),
				req(Skill.MINING, 60), req(Skill.WOODCUTTING, 55), req(Skill.HERBLORE, 5)),
			Quest.THE_FREMENNIK_TRIALS, Quest.LOST_CITY, Quest.RUNE_MYSTERIES, Quest.SHILO_VILLAGE);
		add(Quest.DESERT_TREASURE_I,
			reqs(req(Skill.MAGIC, 50), req(Skill.FIREMAKING, 50), req(Skill.THIEVING, 53), req(Skill.SLAYER, 10)),
			Quest.THE_DIG_SITE, Quest.TEMPLE_OF_IKOV, Quest.THE_TOURIST_TRAP, Quest.TROLL_STRONGHOLD,
			Quest.PRIEST_IN_PERIL, Quest.WATERFALL_QUEST);
		add(Quest.LEGENDS_QUEST,
			reqs(req(Skill.AGILITY, 50), req(Skill.CRAFTING, 50), req(Skill.HERBLORE, 45), req(Skill.MAGIC, 56),
				req(Skill.MINING, 52), req(Skill.PRAYER, 42), req(Skill.SMITHING, 50), req(Skill.STRENGTH, 50),
				req(Skill.THIEVING, 50), req(Skill.WOODCUTTING, 50)),
			Quest.FAMILY_CREST, Quest.HEROES_QUEST, Quest.SHILO_VILLAGE, Quest.UNDERGROUND_PASS, Quest.WATERFALL_QUEST);
		add(Quest.DREAM_MENTOR, reqs(), Quest.LUNAR_DIPLOMACY, Quest.EADGARS_RUSE);

		// ---- grandmaster guide quests (own skill reqs shown on the guide row) ----
		add(Quest.KINGS_RANSOM, reqs(),
			Quest.BLACK_KNIGHTS_FORTRESS, Quest.HOLY_GRAIL, Quest.MURDER_MYSTERY, Quest.ONE_SMALL_FAVOUR);
		add(Quest.DRAGON_SLAYER_II, reqs(),
			Quest.LEGENDS_QUEST, Quest.DREAM_MENTOR, Quest.A_TAIL_OF_TWO_CATS, Quest.ANIMAL_MAGNETISM,
			Quest.GHOSTS_AHOY, Quest.BONE_VOYAGE, Quest.CLIENT_OF_KOUREND);
		add(Quest.RECIPE_FOR_DISASTER, reqs(),
			Quest.COOKS_ASSISTANT, Quest.FISHING_CONTEST, Quest.GOBLIN_DIPLOMACY, Quest.BIG_CHOMPY_BIRD_HUNTING,
			Quest.BIOHAZARD, Quest.DEMON_SLAYER, Quest.MURDER_MYSTERY, Quest.NATURE_SPIRIT, Quest.THE_RESTLESS_GHOST,
			Quest.PRIEST_IN_PERIL, Quest.GERTRUDES_CAT, Quest.SHADOW_OF_THE_STORM, Quest.FAMILY_CREST,
			Quest.HEROES_QUEST, Quest.SHILO_VILLAGE, Quest.UNDERGROUND_PASS, Quest.WATERFALL_QUEST,
			Quest.MONKEY_MADNESS_I);
	}

	static List<Quest> directPrereqs(Quest quest)
	{
		Node n = NODES.get(quest);
		return n == null ? Collections.emptyList() : n.prereqs;
	}

	static List<GuideData.SkillReq> reqsOf(Quest quest)
	{
		Node n = NODES.get(quest);
		return n == null ? Collections.emptyList() : n.reqs;
	}

	static boolean hasPrereqs(Quest quest)
	{
		return !directPrereqs(quest).isEmpty();
	}

	/** Every quest referenced in the graph, so completion state can be snapshotted. */
	static Set<Quest> allQuests()
	{
		Set<Quest> set = EnumSet.noneOf(Quest.class);
		for (Map.Entry<Quest, Node> e : NODES.entrySet())
		{
			set.add(e.getKey());
			set.addAll(e.getValue().prereqs);
		}
		return set;
	}

	private QuestReqs()
	{
	}
}
