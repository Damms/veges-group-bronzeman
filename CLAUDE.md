@AGENTS.md

# Project context — Veges PK Guide

A **read-only** RuneLite sidebar guide plugin for the Veges Group Bronzeman.
It is NOT a gameplay automation plugin — it only reads the local player's own
quest and skill state. Built from the runelite/example-plugin template.

- Package: `com.veges.vegesguide` · config group: `vegesguide` · plugin name:
  "Veges PK Guide" · author: `Phob`.
- GitHub: plugin repo `https://github.com/Damms/veges-group-bronzeman` (branch
  `master`). Live in the Plugin Hub: PR
  `https://github.com/runelite/plugin-hub/pull/12582` was **merged** 2026-06-13
  (manifest `plugins/veges-pk-guide`). PR #12581 was the original (closed). To
  ship code changes once a Hub PR is already merged, the manifest now lives on
  `runelite/plugin-hub` master, so a **new** PR is needed each time: push to the
  plugin repo, then sync the `Damms/plugin-hub` fork, branch off master, bump
  `commit=` in `plugins/veges-pk-guide`, and open a fresh PR. Latest update PR:
  `https://github.com/runelite/plugin-hub/pull/12585` (branch
  `veges-pk-guide-collapse`).
- Five builds: `pure`, `fortydef`, `zerker`, `med` are trackable checklists
  (checkboxes + progress bar + auto-tick); `pivot` is reference-only. Stable id
  prefixes per build: pure=`q`, zerker=`z`, fortydef=`f`, med=`m`.

## Files
- `VegesGuidePlugin.java` — nav button + event wiring. Subscribes to
  GameStateChanged / VarbitChanged / StatChanged / GameTick; a `syncPending`
  flag throttles work to one scan per tick. Auto-ticks FINISHED quests (only
  ever adds, never un-ticks) and snapshots real skill levels for the panel.
- `VegesGuidePanel.java` — the Swing sidebar. Renders build summaries, the
  quest checklist, progress bar, and the per-quest "Requires:" line (green=met,
  red=unmet, grey=levels unknown). Reads/writes done-state via ConfigManager.
- `GuideData.java` — **all guide content** lives here (the only file to edit for
  content). Builds → Sections → Items. Items optionally carry a `Quest` (for
  auto-tick) and `SkillReq`s (verified against the OSRS Wiki).
- `VegesGuidePluginTest.java` — `main()` launches a dev client.

## Gotchas
- Saved checklist keys are `done.<buildKey>.<itemId>` (config group `vegesguide`).
  The item ids in GuideData are stable save keys — **do not renumber existing
  checkable ids** when editing content.
- Quest enum quirks used here: Vampire Slayer = `VAMPYRE_SLAYER`, Romeo & Juliet
  = `ROMEO__JULIET`, Desert Treasure I = `DESERT_TREASURE_I`. Skip `Skill.OVERALL`
  (deprecated) and `Skill.SAILING` by name when iterating skills.
- RFD has no "partial complete" state — the Mithril-gloves row only auto-ticks
  on full RFD completion.
- **Dev build/run**: gradle picks the Oracle JRE 8 (no compiler) by default and
  fails. Run with the Corretto JDK: `gradlew.bat run
  "-Dorg.gradle.java.home=C:\Users\Jaedyn\.jdks\corretto-23.0.2"`. IntelliJ runs
  fine without the flag. The ReflectUtil InaccessibleObjectException at startup
  is benign on JDK 23.

## Conventions
- Do **not** add a `Co-Authored-By: Claude` trailer to commits or a "Generated
  with Claude Code" footer to PRs in this project — the repo owner prefers no AI
  attribution.
