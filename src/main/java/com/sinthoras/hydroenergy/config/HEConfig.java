package com.sinthoras.hydroenergy.config;

import java.util.ArrayList;
import java.util.List;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

import com.sinthoras.hydroenergy.HEUtil;

import cpw.mods.fml.common.Loader;
import gregtech.api.enums.GTValues;

public class HEConfig {

    private static class Defaults {

        public static final int maxDams = 16;
        public static final int minimalWaterUpdateInterval = 1000; // in milliseconds
        public static final int delayBetweenSpreadingChunks = 2000; // in milliseconds
        public static final int minLightUpdateTimePerSubChunk = 10; // in milliseconds
        public static final float clippingOffset = 0.05f; // in blocks
        public static final int maxWaterSpreadWest = 1000; // in blocks
        public static final int maxWaterSpreadDown = 1000; // in blocks
        public static final int maxWaterSpreadNorth = 1000; // in blocks
        public static final int maxWaterSpreadEast = 1000; // in blocks
        public static final int maxWaterSpreadUp = 24; // in blocks
        public static final int maxWaterSpreadSouth = 1000; // in blocks
        public static final int overworldId = 0;
        public static final int damDrainPerSecond = 2048; // in EU
        public static final float waterBonusPerSurfaceBlockPerRainTick = 1.0f; // in EU/block
        public static final int blockIdOffset = 17000;
        public static final float efficiencyLossPerTier = 0.03f;
        public static final float pressureIncreasePerTier = 2.0f;
        public static final float milliBucketPerEU = 1.0f;
        public static final String[] enabledTiers = new String[] { "lv", "mv", "hv", "ev", "iv" };
        public static final boolean useLimitedRendering = false;
        public static final boolean forceOpenGL = false;
    }

    private static class Categories {

        public static final String general = "General";
        public static final String spreading = "Spreading";
        public static final String energyBalance = "Energy Balance";
    }

    public static int maxDams = Defaults.maxDams;
    public static int minimalWaterUpdateInterval = Defaults.minimalWaterUpdateInterval;
    public static int delayBetweenSpreadingChunks = Defaults.delayBetweenSpreadingChunks;
    public static int minLightUpdateTimePerSubChunk = Defaults.minLightUpdateTimePerSubChunk;
    public static float clippingOffset = Defaults.clippingOffset;
    public static List<Integer> dimensionIdWhitelist = new ArrayList<>();
    public static int maxWaterSpreadWest = Defaults.maxWaterSpreadWest;
    public static int maxWaterSpreadDown = Defaults.maxWaterSpreadDown;
    public static int maxWaterSpreadNorth = Defaults.maxWaterSpreadNorth;
    public static int maxWaterSpreadEast = Defaults.maxWaterSpreadEast;
    public static int maxWaterSpreadUp = Defaults.maxWaterSpreadUp;
    public static int maxWaterSpreadSouth = Defaults.maxWaterSpreadSouth;
    public static int damDrainPerSecond = Defaults.damDrainPerSecond;
    public static float waterBonusPerSurfaceBlockPerRainTick = Defaults.waterBonusPerSurfaceBlockPerRainTick;
    public static int blockIdOffset = Defaults.blockIdOffset;
    public static float efficiencyLossPerTier = Defaults.efficiencyLossPerTier;
    public static float pressureIncreasePerTier = Defaults.pressureIncreasePerTier;
    public static float milliBucketPerEU = Defaults.milliBucketPerEU;
    public static float euPerMilliBucket = 1.0f / Defaults.milliBucketPerEU;
    public static boolean[] enabledTiers = new boolean[GTValues.VN.length - 1];
    public static boolean useLimitedRendering = Defaults.useLimitedRendering;
    public static boolean forceOpenGL = Defaults.forceOpenGL;

    public static void syncronizeConfiguration(java.io.File configurationFile) {
        Configuration configuration = new Configuration(configurationFile);
        configuration.load();

        Property maxDamsProperty = configuration.get(
                Categories.general,
                "maxDams",
                Defaults.maxDams,
                "[SERVER] How many dams should the game support. At least as many as the server you want to connect"
                        + " to. Each dam will receive its own water block and it will also have a minuscule performance"
                        + " impact. Keep it only as long as you need. You can always just raise, but not shorten the value.");
        maxDams = maxDamsProperty.getInt();
        if (maxDams != Math.max(1, maxDams)) {
            maxDams = 1;
            maxDamsProperty.set(1);
        }

        Property minimalWaterUpdateIntervalProperty = configuration.get(
                Categories.general,
                "minimalWaterUpdateInterval",
                Defaults.minimalWaterUpdateInterval,
                "[SERVER] Minimum delay" + " in milliseconds between update packets from the server to ALL clients.");
        minimalWaterUpdateInterval = minimalWaterUpdateIntervalProperty.getInt();

        Property spreadingDelayBetweenPerChunksProperty = configuration.get(
                Categories.general,
                "delayBetweenSpreadingChunks",
                Defaults.delayBetweenSpreadingChunks,
                "[SERVER] Delay"
                        + " in milliseconds the game will wait between processing a chunk for water spreading. Keep in "
                        + "mind, that a single tick takes care of a whole chunk between y=0 and y=255 at once!");
        delayBetweenSpreadingChunks = spreadingDelayBetweenPerChunksProperty.getInt();

        Property minLightUpdateTimePerSubChunkProperty = configuration.get(
                Categories.general,
                "minLightUpdateTimePerSubChunk",
                Defaults.minLightUpdateTimePerSubChunk,
                "[CLIENT] Light "
                        + "calculation required all affected chunks to be rerendered. When a change in waterLevel induces "
                        + "rerendering it will also calculate a minimum delay before it can happen again. Light updates "
                        + "will not be lost, just delayed. For every subChunk (16 blocks high) that was rerendered because"
                        + " of this update event, the game will add the specified delay (in milliseconds) up for the actual delay. You "
                        + "should expect the number of rerendered subChunks to be in the low hundreds");
        minLightUpdateTimePerSubChunk = minLightUpdateTimePerSubChunkProperty.getInt();

        Property useLimitedRenderingProperty = configuration.get(
                Categories.general,
                "useLimitedRendering",
                Defaults.useLimitedRendering,
                "[CLIENT] Activate this if you have performance issues with the "
                        + "mod. But be warned: you will have limited render capabilities!");
        useLimitedRendering = useLimitedRenderingProperty.getBoolean();

        if (Loader.isModLoaded("angelica")) useLimitedRendering = true;

        Property forceOpenGLProperty = configuration.get(
                Categories.general,
                "forceOpenGL",
                Defaults.forceOpenGL,
                "[CLIENT] Some Macs may always report OpenGL 2.1 - activate this to disable the OpenGL 3.2 check; "
                        + "it will assume you have OpenGL 3.2 or greater. But be warned: it may crash!");
        forceOpenGL = forceOpenGLProperty.getBoolean();

        Property clippingOffsetProperty = configuration.get(
                Categories.general,
                "clippingOffset",
                Defaults.clippingOffset,
                "[SERVER + CLIENT] If water is sitting too narrow over a block there"
                        + " are graphical issues (Depth buffer resolution). To fix this, the game will not render a "
                        + "waterLevel that sits lower then the specified value over a block. This value is also used for "
                        + "physics calculation and is synced from the server all clients.");
        clippingOffset = (float) clippingOffsetProperty.getDouble();

        Property dimensionIdWhitelistProperties = configuration.get(
                Categories.general,
                "dimensionIdWhitelist",
                new int[] { Defaults.overworldId },
                "[SERVER] List of dimension a player is allowed to place a controller");
        dimensionIdWhitelist.clear();
        for (int id : dimensionIdWhitelistProperties.getIntList()) {
            dimensionIdWhitelist.add(id);
        }

        configuration.addCustomCategoryComment(
                Categories.spreading,
                "Water spreading will quickly get out of "
                        + "control if somebody mis-clicks their limits on their controllers. Here are game-wide limits for "
                        + "spreading.");

        Property maxWaterSpreadWestProperty = configuration
                .get(Categories.spreading, "maxWaterSpreadWest", Defaults.maxWaterSpreadWest, "[SERVER]");
        maxWaterSpreadWest = maxWaterSpreadWestProperty.getInt();
        Property maxWaterSpreadDownProperty = configuration
                .get(Categories.spreading, "maxWaterSpreadDown", Defaults.maxWaterSpreadDown, "[SERVER]");
        maxWaterSpreadDown = maxWaterSpreadDownProperty.getInt();
        Property maxWaterSpreadNorthProperty = configuration
                .get(Categories.spreading, "maxWaterSpreadNorth", Defaults.maxWaterSpreadNorth, "[SERVER]");
        maxWaterSpreadNorth = maxWaterSpreadNorthProperty.getInt();
        Property maxWaterSpreadEastProperty = configuration
                .get(Categories.spreading, "maxWaterSpreadEast", Defaults.maxWaterSpreadEast, "[SERVER]");
        maxWaterSpreadEast = maxWaterSpreadEastProperty.getInt();
        Property maxWaterSpreadUpProperty = configuration
                .get(Categories.spreading, "maxWaterSpreadUp", Defaults.maxWaterSpreadUp, "[SERVER]");
        maxWaterSpreadUp = maxWaterSpreadUpProperty.getInt();
        Property maxWaterSpreadSouthProperty = configuration
                .get(Categories.spreading, "maxWaterSpreadSouth", Defaults.maxWaterSpreadSouth, "[SERVER]");
        maxWaterSpreadSouth = maxWaterSpreadSouthProperty.getInt();

        Property damDrainPerSecondProperty = configuration.get(
                Categories.energyBalance,
                "damDrainPerSecond",
                Defaults.damDrainPerSecond,
                "[SERVER] How many EU a dam will provide as Pressurized Water for " + "turbines per tick.");
        damDrainPerSecond = damDrainPerSecondProperty.getInt();

        Property waterBonusPerSurfaceBlockPerRainTickProperty = configuration.get(
                Categories.energyBalance,
                "waterBonusPerSurfaceBlockPerRainTick",
                Defaults.waterBonusPerSurfaceBlockPerRainTick,
                "[SERVER] How many EU are added to a dam during rain for each water block on the"
                        + " highest Y coordinate aka water surface when full.");
        waterBonusPerSurfaceBlockPerRainTick = (float) waterBonusPerSurfaceBlockPerRainTickProperty.getDouble();

        Property blockIdOffsetProperty = configuration.get(
                Categories.general,
                "blockIdOffset",
                Defaults.blockIdOffset,
                "[SERVER + CLIENT] Offset of blockIds for GregTech block registration");
        blockIdOffset = blockIdOffsetProperty.getInt();

        Property efficiencyLossPerTierProperty = configuration.get(
                Categories.energyBalance,
                "efficiencyLossPerTier",
                Defaults.efficiencyLossPerTier,
                "[SERVER] Efficiency for Hydro Pump and Hydro Turbine in "
                        + "voltage variants and beginning from LV with '(1.0 - efficiencyLossPerTier)'.");
        efficiencyLossPerTier = (float) efficiencyLossPerTierProperty.getDouble();

        Property pressureIncreasePerTierProperty = configuration.get(
                Categories.energyBalance,
                "pressureIncreasePerTier",
                Defaults.pressureIncreasePerTier,
                "[SERVER] Hydro Pump height "
                        + "limit for voltage variants in blocks and beginning from LV with '1 * pressureIncreasePerTier'.");
        pressureIncreasePerTier = (float) pressureIncreasePerTierProperty.getDouble();

        Property milliBucketPerEUProperty = configuration.get(
                Categories.energyBalance,
                "milliBucketPerEU",
                Defaults.milliBucketPerEU,
                "[SERVER] Conversion ratio between Pressurized Water and EU on "
                        + "pressure 1. Affects the throughput on pipes between multi blocks and how much energy is "
                        + "stored in each Hydro Dam.");
        milliBucketPerEU = (float) milliBucketPerEUProperty.getDouble();
        euPerMilliBucket = 1.0f / milliBucketPerEU;

        Property enabledTiersProperty = configuration.get(
                Categories.energyBalance,
                "enabledTiers",
                Defaults.enabledTiers,
                "[SERVER] A list of all tiers that should have a Hydro Pump and Hydro Turbine generated. "
                        + "ULV is ignored since it is disabled.");
        String[] enableTierNames = enabledTiersProperty.getStringList();
        for (int i = 0; i < enableTierNames.length; i++) {
            final int tierId = HEUtil.voltageNameToTierId(enableTierNames[i]);
            // Catch -1 and disable ULV permanently
            if (tierId > 0) {
                enabledTiers[tierId] = true;
            }
        }

        if (configuration.hasChanged()) {
            configuration.save();
        }
    }
}
