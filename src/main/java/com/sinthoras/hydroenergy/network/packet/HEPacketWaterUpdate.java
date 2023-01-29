package com.sinthoras.hydroenergy.network.packet;

import com.sinthoras.hydroenergy.client.HEClient;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

public class HEPacketWaterUpdate implements IMessage {

    public int waterId;
    public float waterLevel;

    public HEPacketWaterUpdate(int waterId, float waterLevel) {
        this.waterId = waterId;
        this.waterLevel = waterLevel;
    }

    public HEPacketWaterUpdate() {}

    @Override
    public void fromBytes(ByteBuf buf) {
        this.waterId = buf.readInt();
        this.waterLevel = buf.readFloat();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.waterId);
        buf.writeFloat(this.waterLevel);
    }

    public static class Handler implements IMessageHandler<HEPacketWaterUpdate, IMessage> {

        @Override
        public IMessage onMessage(HEPacketWaterUpdate message, MessageContext ctx) {
            HEClient.onWaterUpdate(message.waterId, message.waterLevel);
            return null;
        }
    }
}
