package com.veges.vegesguide;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import net.runelite.api.Quest;
import net.runelite.api.Skill;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.ui.PluginPanel;

class VegesGuidePanel extends PluginPanel
{
	private final ConfigManager configManager;
	private final JComboBox<String> buildSelector = new JComboBox<>();
	private final JProgressBar progressBar = new JProgressBar();
	private final JButton resetButton = new JButton("Reset this build's checklist");
	private final JPanel content = new JPanel();

	// Snapshot of the player's real skill levels, pushed from the plugin on the
	// client thread. Null until the first sync (e.g. before login) - reqs then
	// render in a neutral colour rather than met/unmet.
	private volatile Map<Skill, Integer> playerLevels;

	// Snapshot of which prereq-graph quests are FINISHED. Null before login.
	private volatile Map<Quest, Boolean> questFinished;

	// Which quests' prerequisite trees are currently expanded (key build/quest).
	private final Set<String> expanded = new HashSet<>();

	VegesGuidePanel(ConfigManager configManager)
	{
		super(false);
		this.configManager = configManager;
		setLayout(new BorderLayout());
		setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

		// ----- header -----
		JPanel header = new JPanel();
		header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
		header.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));

		JLabel title = new JLabel("Phob's Veges PK Guide");
		title.setFont(title.getFont().deriveFont(Font.BOLD, 15f));
		title.setForeground(new java.awt.Color(0xC9A227));
		title.setAlignmentX(Component.LEFT_ALIGNMENT);
		header.add(title);
		header.add(javax.swing.Box.createVerticalStrut(8));

		for (GuideData.Build b : GuideData.all())
		{
			buildSelector.addItem(b.name);
		}
		buildSelector.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
		buildSelector.setAlignmentX(Component.LEFT_ALIGNMENT);
		buildSelector.addActionListener(e -> render());
		header.add(buildSelector);
		header.add(javax.swing.Box.createVerticalStrut(6));

		progressBar.setStringPainted(true);
		progressBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 18));
		progressBar.setAlignmentX(Component.LEFT_ALIGNMENT);
		header.add(progressBar);
		header.add(javax.swing.Box.createVerticalStrut(6));

		resetButton.setAlignmentX(Component.LEFT_ALIGNMENT);
		resetButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
		resetButton.addActionListener(e -> resetCurrent());
		header.add(resetButton);

		add(header, BorderLayout.NORTH);

		// ----- scrollable content -----
		content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
		JScrollPane scroll = new JScrollPane(content,
			ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
			ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scroll.setBorder(BorderFactory.createEmptyBorder());
		scroll.getVerticalScrollBar().setUnitIncrement(16);
		add(scroll, BorderLayout.CENTER);

		render();
	}

	/** Re-read saved state and rebuild the view. Call on the EDT. */
	void refresh()
	{
		render();
	}

	/** Update the cached skill-level snapshot used for met/unmet display. */
	void setPlayerLevels(Map<Skill, Integer> levels)
	{
		this.playerLevels = levels;
	}

	/** Update the cached quest-completion snapshot used by the prereq trees. */
	void setQuestStates(Map<Quest, Boolean> finished)
	{
		this.questFinished = finished;
	}

	private GuideData.Build current()
	{
		String name = (String) buildSelector.getSelectedItem();
		for (GuideData.Build b : GuideData.all())
		{
			if (b.name.equals(name))
			{
				return b;
			}
		}
		return GuideData.all().get(0);
	}

	private void render()
	{
		GuideData.Build b = current();
		content.removeAll();

		JLabel summary = new JLabel(b.summaryHtml);
		summary.setAlignmentX(Component.LEFT_ALIGNMENT);
		summary.setBorder(BorderFactory.createEmptyBorder(4, 0, 10, 0));
		content.add(summary);

		for (GuideData.Section section : b.sections)
		{
			JLabel st = new JLabel(section.title.toUpperCase());
			st.setFont(st.getFont().deriveFont(Font.BOLD, 11f));
			st.setForeground(new java.awt.Color(0xC9A227));
			st.setAlignmentX(Component.LEFT_ALIGNMENT);
			st.setBorder(BorderFactory.createEmptyBorder(8, 0, 4, 0));
			content.add(st);

			for (GuideData.Item item : section.items)
			{
				content.add(row(b, item));
			}
		}

		progressBar.setVisible(b.trackable);
		resetButton.setVisible(b.trackable);
		if (b.trackable)
		{
			updateProgress(b);
		}

		content.revalidate();
		content.repaint();
	}

	private JPanel row(GuideData.Build b, GuideData.Item item)
	{
		JPanel outer = new JPanel();
		outer.setLayout(new BoxLayout(outer, BoxLayout.Y_AXIS));
		outer.setBorder(BorderFactory.createEmptyBorder(3, 0, 3, 0));
		outer.setAlignmentX(Component.LEFT_ALIGNMENT);

		JPanel line = new JPanel(new BorderLayout(6, 0));
		line.setAlignmentX(Component.LEFT_ALIGNMENT);

		StringBuilder html = new StringBuilder("<html><body style='width:150px'>");
		html.append("<b>").append(esc(item.name)).append("</b>");
		if (item.desc != null && !item.desc.isEmpty())
		{
			html.append("<br><span style='color:#9c9c9c'>").append(esc(item.desc)).append("</span>");
		}
		html.append(reqsHtml(item));
		html.append("</body></html>");
		JLabel label = new JLabel(html.toString());

		if (item.checkable && item.id != null)
		{
			JCheckBox cb = new JCheckBox();
			cb.setSelected(isDone(b.key, item.id));
			cb.addActionListener(e -> {
				setDone(b.key, item.id, cb.isSelected());
				updateProgress(b);
			});
			line.add(cb, BorderLayout.WEST);
		}
		line.add(label, BorderLayout.CENTER);
		line.setMaximumSize(new Dimension(Integer.MAX_VALUE, line.getPreferredSize().height));
		outer.add(line);

		if (item.quest != null && QuestReqs.hasPrereqs(item.quest))
		{
			addPrereqSection(outer, b, item.quest);
		}

		outer.setMaximumSize(new Dimension(Integer.MAX_VALUE, outer.getPreferredSize().height));
		return outer;
	}

	private boolean showAllPrereqs()
	{
		Boolean v = configManager.getConfiguration(VegesGuideConfig.GROUP, "showAllPrereqs", Boolean.class);
		return Boolean.TRUE.equals(v);
	}

	private boolean isFinished(Quest quest)
	{
		Map<Quest, Boolean> qf = questFinished;
		return qf != null && Boolean.TRUE.equals(qf.get(quest));
	}

	// Adds the collapsible "Prerequisites" toggle (and the tree when expanded)
	// beneath a quest row.
	private void addPrereqSection(JPanel outer, GuideData.Build b, Quest quest)
	{
		boolean showAll = showAllPrereqs();
		int count = countChainQuests(quest, showAll);
		String key = b.key + "/" + quest.name();
		boolean isExpanded = expanded.contains(key);

		JLabel toggle = new JLabel();
		toggle.setAlignmentX(Component.LEFT_ALIGNMENT);
		toggle.setBorder(BorderFactory.createEmptyBorder(4, 22, 1, 0));
		toggle.setFont(toggle.getFont().deriveFont(Font.BOLD, 11f));

		if (!showAll && count == 0)
		{
			toggle.setText("<html><span style='color:#4CAF50'>&#10003; Prerequisites done</span></html>");
			outer.add(toggle);
			return;
		}

		String caret = isExpanded ? "&#9662;" : "&#9656;"; // down / right triangle
		String label = showAll ? ("Prerequisites (" + count + ")") : ("Prerequisites (" + count + " left)");
		// Pill-style highlight so the expander is easy to spot in the list.
		toggle.setText("<html><span style='color:#1a1a1a; background-color:#c9a227'>&nbsp;"
			+ caret + " " + label + "&nbsp;</span></html>");
		toggle.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		toggle.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				if (isExpanded)
				{
					expanded.remove(key);
				}
				else
				{
					expanded.add(key);
				}
				render();
			}
		});
		outer.add(toggle);

		if (isExpanded)
		{
			JLabel tree = new JLabel(buildPrereqTree(quest, showAll));
			tree.setAlignmentX(Component.LEFT_ALIGNMENT);
			tree.setBorder(BorderFactory.createEmptyBorder(2, 22, 2, 0));
			outer.add(tree);
		}
	}

	// Number of distinct prerequisite quests in the chain. In "only unmet" mode
	// this counts just the unfinished ones (finished quests prune their subtree).
	private int countChainQuests(Quest root, boolean showAll)
	{
		Set<Quest> seen = EnumSet.noneOf(Quest.class);
		countChain(root, showAll, seen);
		return seen.size();
	}

	private void countChain(Quest quest, boolean showAll, Set<Quest> seen)
	{
		for (Quest p : QuestReqs.directPrereqs(quest))
		{
			if (seen.contains(p))
			{
				continue;
			}
			boolean finished = isFinished(p);
			if (!showAll && finished)
			{
				continue;
			}
			seen.add(p);
			countChain(p, showAll, seen);
		}
	}

	private String buildPrereqTree(Quest root, boolean showAll)
	{
		StringBuilder sb = new StringBuilder("<html><body style='width:150px'>");
		Set<Quest> visited = EnumSet.noneOf(Quest.class);
		appendChildren(root, 0, showAll, visited, sb);
		sb.append("</body></html>");
		return sb.toString();
	}

	// Depth-first, de-duplicated across the whole tree so a shared prereq only
	// appears once. Visual indent is capped so deep chains stay readable.
	private void appendChildren(Quest quest, int depth, boolean showAll, Set<Quest> visited, StringBuilder sb)
	{
		for (Quest p : QuestReqs.directPrereqs(quest))
		{
			if (visited.contains(p))
			{
				continue;
			}
			boolean finished = isFinished(p);
			if (!showAll && finished)
			{
				continue;
			}
			visited.add(p);

			int indentLevels = Math.min(depth, 3);
			StringBuilder indent = new StringBuilder();
			for (int i = 0; i < indentLevels; i++)
			{
				indent.append("&nbsp;&nbsp;&nbsp;");
			}
			String mark = finished
				? "<span style='color:#4CAF50'>&#10003;</span> "
				: "<span style='color:#E25C5C'>&#10007;</span> ";
			sb.append(indent).append(mark).append(esc(p.getName()));

			String reqs = prereqReqsInline(p, showAll);
			if (!reqs.isEmpty())
			{
				sb.append(" <span style='color:#9c9c9c'>(").append(reqs).append(")</span>");
			}
			sb.append("<br>");

			appendChildren(p, depth + 1, showAll, visited, sb);
		}
	}

	private String prereqReqsInline(Quest quest, boolean showAll)
	{
		List<GuideData.SkillReq> reqs = QuestReqs.reqsOf(quest);
		if (reqs.isEmpty())
		{
			return "";
		}
		Map<Skill, Integer> levels = playerLevels;
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (GuideData.SkillReq r : reqs)
		{
			boolean met = levels != null && levels.getOrDefault(r.skill, 0) >= r.level;
			if (!showAll && met)
			{
				continue;
			}
			if (!first)
			{
				sb.append(", ");
			}
			first = false;

			String text = r.level + " " + skillName(r.skill);
			if (levels == null)
			{
				sb.append(text);
			}
			else
			{
				sb.append("<span style='color:").append(met ? "#4CAF50" : "#E25C5C").append("'>")
					.append(text).append("</span>");
			}
		}
		return sb.toString();
	}

	private void updateProgress(GuideData.Build b)
	{
		int total = 0, done = 0;
		for (GuideData.Section s : b.sections)
		{
			for (GuideData.Item i : s.items)
			{
				if (i.checkable && i.id != null)
				{
					total++;
					if (isDone(b.key, i.id))
					{
						done++;
					}
				}
			}
		}
		progressBar.setMaximum(Math.max(total, 1));
		progressBar.setValue(done);
		progressBar.setString(total == 0 ? "reference only" : done + " / " + total + " quests");
	}

	private void resetCurrent()
	{
		GuideData.Build b = current();
		for (GuideData.Section s : b.sections)
		{
			for (GuideData.Item i : s.items)
			{
				if (i.checkable && i.id != null)
				{
					setDone(b.key, i.id, false);
				}
			}
		}
		render();
	}

	static String stateKey(String buildKey, String id)
	{
		return "done." + buildKey + "." + id;
	}

	private boolean isDone(String buildKey, String id)
	{
		Boolean v = configManager.getConfiguration(VegesGuideConfig.GROUP, stateKey(buildKey, id), Boolean.class);
		return Boolean.TRUE.equals(v);
	}

	private void setDone(String buildKey, String id, boolean done)
	{
		if (done)
		{
			configManager.setConfiguration(VegesGuideConfig.GROUP, stateKey(buildKey, id), true);
		}
		else
		{
			configManager.unsetConfiguration(VegesGuideConfig.GROUP, stateKey(buildKey, id));
		}
	}

	// Builds the "Requires:" line. Met reqs are green with a tick, unmet are red
	// with a cross; when levels are unknown (logged out) they render neutral grey.
	private String reqsHtml(GuideData.Item item)
	{
		if (item.reqs.isEmpty())
		{
			return item.quest != null
				? "<br><span style='color:#9c9c9c'>No skill requirements</span>"
				: "";
		}

		Map<Skill, Integer> levels = playerLevels;
		StringBuilder sb = new StringBuilder("<br><span style='color:#9c9c9c'>Requires: </span>");
		boolean first = true;
		for (GuideData.SkillReq r : item.reqs)
		{
			if (!first)
			{
				sb.append("<span style='color:#9c9c9c'>, </span>");
			}
			first = false;

			String label = r.level + " " + skillName(r.skill);
			if (levels == null)
			{
				sb.append("<span style='color:#9c9c9c'>").append(label).append("</span>");
				continue;
			}

			int have = levels.getOrDefault(r.skill, 0);
			boolean met = have >= r.level;
			String colour = met ? "#4CAF50" : "#E25C5C";
			String mark = met ? "&#10003; " : "&#10007; ";
			sb.append("<span style='color:").append(colour).append("'>")
				.append(mark).append(label).append("</span>");
		}
		return sb.toString();
	}

	// "WOODCUTTING" -> "Woodcutting"
	private static String skillName(Skill skill)
	{
		String n = skill.name();
		return n.charAt(0) + n.substring(1).toLowerCase();
	}

	private static String esc(String s)
	{
		return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
	}
}
