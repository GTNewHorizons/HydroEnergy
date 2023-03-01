package com.sinthoras.hydroenergy.network.packet;

import net.minecraft.client.Minecraft;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.NibbleArray;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

import com.sinthoras.hydroenergy.HE;
import com.sinthoras.hydroenergy.HEUtil;
import com.sinthoras.hydroenergy.client.light.HELightSMPHooks;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class HEPacketChunkUpdate implements IMessage {

    private ByteBuf transmissionBuffer;
    private short flagsChunkY;
    private int chunkX;
    private int chunkZ;
    private ExtendedBlockStorage[] receivedChunk = null;

    public HEPacketChunkUpdate() {}

    public HEPacketChunkUpdate(Chunk chunk, short flagsChunkY) {
        transmissionBuffer = Unpooled.buffer();
        transmissionBuffer.writeShort(flagsChunkY);
        transmissionBuffer.writeInt(chunk.xPosition);
        transmissionBuffer.writeInt(chunk.zPosition);
        ExtendedBlockStorage[] blockStorages = chunk.getBlockStorageArray();
        for (int chunkY = 0; chunkY < HE.numChunksY; chunkY++) {
            if ((flagsChunkY & HEUtil.chunkYToFlag(chunkY)) > 0) {
                ExtendedBlockStorage subChunk = blockStorages[chunkY];

                transmissionBuffer.writeInt(subChunk.blockRefCount);
                transmissionBuffer.writeInt(subChunk.tickRefCount);

                byte[] lsb = subChunk.getBlockLSBArray();
                transmissionBuffer.writeBytes(lsb);

                NibbleArray msbArray = subChunk.getBlockMSBArray();
                transmissionBuffer.writeBoolean(msbArray == null);
                if (msbArray != null) {
                    transmissionBuffer.writeBytes(msbArray.data);
                }

                byte[] metadata = subChunk.getMetadataArray().data;
                transmissionBuffer.writeBytes(metadata);

                byte[] skylight = subChunk.getSkylightArray().data;
                transmissionBuffer.writeBytes(skylight);
            }
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBytes(transmissionBuffer);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        flagsChunkY = buf.readShort();
        chunkX = buf.readInt();
        chunkZ = buf.readInt();
        receivedChunk = new ExtendedBlockStorage[HE.numChunksY];
        for (int chunkY = 0; chunkY < HE.numChunksY; chunkY++) {
            if ((flagsChunkY & HEUtil.chunkYToFlag(chunkY)) > 0) {
                ExtendedBlockStorage subChunk = new ExtendedBlockStorage(chunkY << 4, false);

                subChunk.blockRefCount = buf.readInt();
                subChunk.tickRefCount = buf.readInt();

                byte[] lsb = buf.readBytes(HE.blockPerSubChunk).array();
                subChunk.setBlockLSBArray(lsb);

                if (!buf.readBoolean()) {
                    byte[] msb = buf.readBytes(HE.blockPerSubChunk / 2).array();
                    subChunk.setBlockMSBArray(new NibbleArray(msb, 4));
                }

                byte[] metadata = buf.readBytes(HE.blockPerSubChunk / 2).array();
                subChunk.setBlockMetadataArray(new NibbleArray(metadata, 4));

                byte[] skylight = buf.readBytes(HE.blockPerSubChunk / 2).array();
                subChunk.setSkylightArray(new NibbleArray(skylight, 4));

                receivedChunk[chunkY] = subChunk;
            }
        }
    }

    public boolean hasDataForSubChunk(int chunkY) {
        return (flagsChunkY & HEUtil.chunkYToFlag(chunkY)) > 0;
    }

    public static class Handler implements IMessageHandler<HEPacketChunkUpdate, IMessage> {

        @Override
        public IMessage onMessage(HEPacketChunkUpdate message, MessageContext ctx) {
            Chunk chunk = Minecraft.getMinecraft().theWorld.getChunkFromChunkCoords(message.chunkX, message.chunkZ);
            ExtendedBlockStorage[] chunkStorage = chunk.getBlockStorageArray();
            for (int chunkY = 0; chunkY < HE.numChunksY; chunkY++) {
                if (message.hasDataForSubChunk(chunkY)) {
                    if (chunkStorage[chunkY] == null) {
                        chunkStorage[chunkY] = new ExtendedBlockStorage(chunkY << 4, !chunk.worldObj.provider.hasNoSky);
                    }
                    chunkStorage[chunkY].blockRefCount = message.receivedChunk[chunkY].blockRefCount;
                    chunkStorage[chunkY].tickRefCount = message.receivedChunk[chunkY].tickRefCount;

                    chunkStorage[chunkY].setBlockLSBArray(message.receivedChunk[chunkY].getBlockLSBArray());
                    chunkStorage[chunkY].setBlockMSBArray(message.receivedChunk[chunkY].getBlockMSBArray());
                    chunkStorage[chunkY].setBlockMetadataArray(message.receivedChunk[chunkY].getMetadataArray());
                    chunkStorage[chunkY].setSkylightArray(message.receivedChunk[chunkY].getSkylightArray());
                }
            }
            HELightSMPHooks.onChunkDataLoad(chunk);
            return null;
        }
    }
}
