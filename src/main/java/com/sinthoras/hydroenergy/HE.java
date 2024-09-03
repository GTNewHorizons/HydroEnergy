package com.sinthoras.hydroenergy;

import static net.minecraft.util.EnumChatFormatting.*;

import net.minecraft.item.ItemStack;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.sinthoras.hydroenergy.blocks.HEWaterStill;
import com.sinthoras.hydroenergy.client.gui.HEGuiHandler;
import com.sinthoras.hydroenergy.config.HEConfig;
import com.sinthoras.hydroenergy.fluids.HEPressurizedWater;

import cpw.mods.fml.common.network.IGuiHandler;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import gregtech.api.enums.GTValues;

public class HE {

    public static SimpleNetworkWrapper network;
    private static final Logger LOG = LogManager.getLogger(HETags.MODID);
    public static final int maxRenderDist = 16;
    public static final int numChunksY = 16;
    public static final int waterOpacity = 3;
    public static final int chunkWidth = 16;
    public static final int chunkHeight = 16;
    public static final int chunkDepth = 16;
    public static final int blockPerSubChunk = chunkWidth * chunkHeight * chunkDepth;
    public static final int underWaterSkylightDepth = (int) Math.ceil(16f / waterOpacity);
    public static final int controllerGuiUpdateDelay = 200;
    public static final int bucketToMilliBucket = 1_000;
    public static final int kiloBucketToMilliBucket = 1_000_000;

    public static boolean logicalClientLoaded = false;
    public static final String ERROR_serverIdsOutOfBounds = "Server uses invalid waterIds! Server message ignored. "
            + "Please make sure your config \"maxControllers\" is at least as big as the server you are connecting to!";
    public static final String WARN_clientConfigMissmatchDetected = "HydroEnergy: Configuration mismatch to the server "
            + "found! This might crash somewhat randomly. Please talk to your server admin!";
    public static final String blueprintHintTecTech = "To see the structure, use a " + BLUE
            + "Tec"
            + DARK_BLUE
            + "Tech"
            + GRAY
            + " Blueprint on the Controller!";

    public static HEPressurizedWater pressurizedWater = new HEPressurizedWater();
    public static final HEWaterStill[] waterBlocks = new HEWaterStill[HEConfig.maxDams];
    public static final int[] waterBlockIds = new int[HEConfig.maxDams];
    public static ItemStack hydroDamControllerBlock;
    public static ItemStack[] hydroPumpBlocks = new ItemStack[GTValues.VN.length - 1];
    public static ItemStack[] hydroTurbineBlocks = new ItemStack[GTValues.VN.length - 1];

    public static boolean DEBUGslowFill = false;
    public static final IGuiHandler guiHandler = new HEGuiHandler();

    // Texture locations
    public static String damBackgroundLocation = "textures/gui/he_water_config.png";
    public static String damLimitBackgroundLocation = "textures/gui/he_water_config_limits_popup.png";
    // To silence the water missing texture error. Points to a random but valid texture
    public static String dummyTexture = damBackgroundLocation;

    public static void debug(String message) {
        HE.LOG.debug(formatMessage(message));
    }

    public static void info(String message) {
        HE.LOG.info(formatMessage(message));
    }

    public static void warn(String message) {
        HE.LOG.warn(formatMessage(message));
    }

    public static void error(String message) {
        HE.LOG.error(formatMessage(message));
    }

    private static String formatMessage(String message) {
        return "[" + HETags.MODNAME + "] " + message;
    }

    public enum DamMode {

        DRAIN,
        DEBUG,
        SPREAD;

        public int getValue() {
            switch (this) {
                default:
                case DRAIN:
                    return 1;
                case DEBUG:
                    return 2;
                case SPREAD:
                    return 3;
            }
        }

        public static DamMode getMode(int mode) {
            switch (mode) {
                default:
                case 1:
                    return DRAIN;
                case 2:
                    return DEBUG;
                case 3:
                    return SPREAD;
            }
        }
    }
}
