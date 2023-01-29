package com.sinthoras.hydroenergy.network.packet;

import com.sinthoras.hydroenergy.HE;
import com.sinthoras.hydroenergy.client.HEClient;
import com.sinthoras.hydroenergy.config.HEConfig;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

public class HEPacketSynchronize implements IMessage {

    public int[] blocksX = new int[HEConfig.maxDams];
    public int[] blocksY = new int[HEConfig.maxDams];
    public int[] blocksZ = new int[HEConfig.maxDams];
    public float[] waterLevels = new float[HEConfig.maxDams];
    public HE.DamMode[] modes = new HE.DamMode[HEConfig.maxDams];
    public int[] limitsWest = new int[HEConfig.maxDams];
    public int[] limitsDown = new int[HEConfig.maxDams];
    public int[] limitsNorth = new int[HEConfig.maxDams];
    public int[] limitsEast = new int[HEConfig.maxDams];
    public int[] limitsUp = new int[HEConfig.maxDams];
    public int[] limitsSouth = new int[HEConfig.maxDams];
    public boolean[] enabledTiers;

    @Override
    public void fromBytes(ByteBuf buf) {
        int length = buf.readInt();
        blocksX = new int[length];
        blocksY = new int[length];
        blocksZ = new int[length];
        waterLevels = new float[length];
        modes = new HE.DamMode[length];
        limitsWest = new int[length];
        limitsDown = new int[length];
        limitsNorth = new int[length];
        limitsEast = new int[length];
        limitsUp = new int[length];
        limitsSouth = new int[length];
        for (int waterId = 0; waterId < length; waterId++) {
            blocksX[waterId] = buf.readInt();
            blocksY[waterId] = buf.readInt();
            blocksZ[waterId] = buf.readInt();
            waterLevels[waterId] = buf.readFloat();
            modes[waterId] = HE.DamMode.getMode(buf.readInt());
            limitsWest[waterId] = buf.readInt();
            limitsDown[waterId] = buf.readInt();
            limitsNorth[waterId] = buf.readInt();
            limitsEast[waterId] = buf.readInt();
            limitsUp[waterId] = buf.readInt();
            limitsSouth[waterId] = buf.readInt();
        }
        final int enabledTiersLength = buf.readInt();
        enabledTiers = new boolean[enabledTiersLength];
        for (int tierId = 0; tierId < enabledTiersLength; tierId++) {
            enabledTiers[tierId] = buf.readBoolean();
        }
        HEConfig.clippingOffset = buf.readFloat();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(HEConfig.maxDams);
        for (int waterId = 0; waterId < HEConfig.maxDams; waterId++) {
            buf.writeInt(blocksX[waterId]);
            buf.writeInt(blocksY[waterId]);
            buf.writeInt(blocksZ[waterId]);
            buf.writeFloat(waterLevels[waterId]);
            buf.writeInt(modes[waterId].getValue());
            buf.writeInt(limitsWest[waterId]);
            buf.writeInt(limitsDown[waterId]);
            buf.writeInt(limitsNorth[waterId]);
            buf.writeInt(limitsEast[waterId]);
            buf.writeInt(limitsUp[waterId]);
            buf.writeInt(limitsSouth[waterId]);
        }
        buf.writeInt(enabledTiers.length);
        for (boolean enabledTier : enabledTiers) {
            buf.writeBoolean(enabledTier);
        }
        buf.writeFloat(HEConfig.clippingOffset);
    }

    public static class Handler implements IMessageHandler<HEPacketSynchronize, IMessage> {

        @Override
        public IMessage onMessage(HEPacketSynchronize message, MessageContext ctx) {
            HEClient.onSynchronize(
                    message.blocksX,
                    message.blocksY,
                    message.blocksZ,
                    message.waterLevels,
                    message.modes,
                    message.limitsWest,
                    message.limitsDown,
                    message.limitsNorth,
                    message.limitsEast,
                    message.limitsUp,
                    message.limitsSouth,
                    message.enabledTiers);
            return null;
        }
    }
}
