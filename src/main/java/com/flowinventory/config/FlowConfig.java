package com.flowinventory.config;

public class FlowConfig {

    // ── Activity Detection ────────────────────────────────
    public boolean autoDetectActivity = true;
    public boolean autoApplyProfile = true;
    public int activitySwitchDelay = 30;
    public int combatDetectionRange = 8;

    // ── Sorting ───────────────────────────────────────────
    public String sortMode = "SMART";
    public boolean mergeStacks = true;

    // ── HUD ───────────────────────────────────────────────
    public boolean showHudOverlay = true;
    public String hudPosition = "TOP_RIGHT";
    public boolean showActivityMessages = true;
    public boolean showKeyHints = true;

    // ── Pattern Learning ──────────────────────────────────
    public boolean enablePatternLearning = true;
}