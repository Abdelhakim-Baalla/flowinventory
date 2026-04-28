package com.flowinventory.core;

import com.flowinventory.FlowInventoryMod;
import com.flowinventory.profiles.ActivityType;
import net.minecraft.client.network.ClientPlayerEntity;

public class ActivityDetector {

    private ActivityType currentActivity = ActivityType.GENERAL;

    public ActivityType getCurrentActivity() {
        return currentActivity;
    }

    public void forceSetActivity(ActivityType activity) {
        currentActivity = activity;
    }
}