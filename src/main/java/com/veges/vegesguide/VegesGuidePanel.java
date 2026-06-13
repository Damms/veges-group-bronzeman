package com.veges.vegesguide;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.util.Map;
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
		JPanel rowPanel = new JPanel(new BorderLayout(6, 0));
		rowPanel.setBorder(BorderFactory.createEmptyBorder(3, 0, 3, 0));
		rowPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

		StringBuilder html = new StringBuilder("<html><body style='width:170px'>");
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
			rowPanel.add(cb, BorderLayout.WEST);
		}
		rowPanel.add(label, BorderLayout.CENTER);

		rowPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, rowPanel.getPreferredSize().height));
		return rowPanel;
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
