package com.sinthoras.hydroenergy.network.packet;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.NibbleArray;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

import com.falsepattern.endlessids.mixin.helpers.SubChunkBlockHook;
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

                if (HE.EID_LOADED) {
                    SubChunkBlockHook hook = (SubChunkBlockHook) subChunk;
                    transmissionBuffer.writeBytes(hook.eid$getB1());
                    // Blocks
                    // Subsequent arrays only exist if the previous one does
                    if (writeNullable(hook.eid$getB2Low(), transmissionBuffer)) {
                        if (writeNullable(hook.eid$getB2High(), transmissionBuffer)) {
                            writeNullable(hook.eid$getB3(), transmissionBuffer);
                        }
                    }
                    // Metadata
                    transmissionBuffer.writeBytes(hook.eid$getM1Low().data);
                    if (writeNullable(hook.eid$getM1High(), transmissionBuffer)) {
                        writeNullable(hook.eid$getM2(), transmissionBuffer);
                    }
                } else {
                    byte[] lsb = subChunk.getBlockLSBArray();
                    transmissionBuffer.writeBytes(lsb);

                    writeNullable(subChunk.getBlockMSBArray(), transmissionBuffer);

                    byte[] metadata = subChunk.getMetadataArray().data;
                    transmissionBuffer.writeBytes(metadata);
                }

                byte[] skylight = subChunk.getSkylightArray().data;
                transmissionBuffer.writeBytes(skylight);
            }
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBytes(transmissionBuffer);
        transmissionBuffer.release();
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        try {
            // Minimum packet size: 2 (short flagsChunkY) + 4 (int chunkX) + 4 (int chunkZ) = 10 bytes
            if (buf.readableBytes() < 10) {
                return;
            }
            flagsChunkY = buf.readShort();
            chunkX = buf.readInt();
            chunkZ = buf.readInt();
            receivedChunk = new ExtendedBlockStorage[HE.numChunksY];
            for (int chunkY = 0; chunkY < HE.numChunksY; chunkY++) {
                if ((flagsChunkY & HEUtil.chunkYToFlag(chunkY)) > 0) {
                    ExtendedBlockStorage subChunk = new ExtendedBlockStorage(chunkY << 4, false);

                    subChunk.blockRefCount = buf.readInt();
                    subChunk.tickRefCount = buf.readInt();
                    if (HE.EID_LOADED) {
                        SubChunkBlockHook hook = (SubChunkBlockHook) subChunk;
                        byte[] b1 = buf.readBytes(HE.blockPerSubChunk).array();
                        hook.eid$setB1(b1);

                        // Blocks
                        NibbleArray b2Low = readNullableNibbleArray(buf);
                        if (b2Low != null) {
                            hook.eid$setB2Low(b2Low);
                            NibbleArray b2High = readNullableNibbleArray(buf);
                            if (b2High != null) {
                                hook.eid$setB2High(b2Low);
                                hook.eid$setB3(readNullableArray(buf));
                            }
                        }
                        // Metadata
                        byte[] m1Low = buf.readBytes(HE.blockPerSubChunk / 2).array();
                        hook.eid$setM1Low(new NibbleArray(m1Low, 4));
                        NibbleArray m1High = readNullableNibbleArray(buf);
                        if (m1High != null) {
                            hook.eid$setM1High(m1High);
                            hook.eid$setM2(readNullableArray(buf));
                        }
                    } else {
                        byte[] lsb = buf.readBytes(HE.blockPerSubChunk).array();
                        subChunk.setBlockLSBArray(lsb);

                        subChunk.setBlockMSBArray(readNullableNibbleArray(buf));

                        byte[] metadata = buf.readBytes(HE.blockPerSubChunk / 2).array();
                        subChunk.setBlockMetadataArray(new NibbleArray(metadata, 4));
                    }

                    byte[] skylight = buf.readBytes(HE.blockPerSubChunk / 2).array();
                    subChunk.setSkylightArray(new NibbleArray(skylight, 4));

                    receivedChunk[chunkY] = subChunk;
                }
            }
        } catch (IndexOutOfBoundsException e) {
            // Packet was malformed or truncated - ignore to prevent crash
            receivedChunk = null;
        }
    }

    public boolean hasDataForSubChunk(int chunkY) {
        return (flagsChunkY & HEUtil.chunkYToFlag(chunkY)) > 0;
    }

    private static boolean writeNullable(@Nullable NibbleArray array, ByteBuf buf) {
        buf.writeBoolean(array == null);
        if (array != null) {
            buf.writeBytes(array.data);
            return true;
        }
        return false;
    }

    private static boolean writeNullable(@Nullable byte[] array, ByteBuf buf) {
        buf.writeBoolean(array == null);
        if (array != null) {
            buf.writeBytes(array);
            return true;
        }
        return false;
    }

    private static @Nullable NibbleArray readNullableNibbleArray(ByteBuf buf) {
        if (!buf.readBoolean()) {
            byte[] arr = buf.readBytes(HE.blockPerSubChunk / 2).array();
            return new NibbleArray(arr, 4);
        }
        return null;
    }

    private static @Nullable byte[] readNullableArray(ByteBuf buf) {
        if (!buf.readBoolean()) {
            return buf.readBytes(HE.blockPerSubChunk).array();
        }
        return null;
    }

    public static class Handler implements IMessageHandler<HEPacketChunkUpdate, IMessage> {

        @Override
        public IMessage onMessage(HEPacketChunkUpdate message, MessageContext ctx) {
            // Guard against malformed packets
            if (message.receivedChunk == null) {
                return null;
            }
            Chunk chunk = Minecraft.getMinecraft().theWorld.getChunkFromChunkCoords(message.chunkX, message.chunkZ);
            ExtendedBlockStorage[] chunkStorage = chunk.getBlockStorageArray();
            for (int chunkY = 0; chunkY < HE.numChunksY; chunkY++) {
                if (message.hasDataForSubChunk(chunkY)) {
                    if (chunkStorage[chunkY] == null) {
                        chunkStorage[chunkY] = new ExtendedBlockStorage(chunkY << 4, !chunk.worldObj.provider.hasNoSky);
                    }
                    ExtendedBlockStorage writeChunk = chunkStorage[chunkY];
                    ExtendedBlockStorage readChunk = message.receivedChunk[chunkY];
                    writeChunk.blockRefCount = readChunk.blockRefCount;
                    writeChunk.tickRefCount = readChunk.tickRefCount;

                    if (HE.EID_LOADED) {
                        SubChunkBlockHook hookWrite = (SubChunkBlockHook) writeChunk;
                        SubChunkBlockHook hookRead = (SubChunkBlockHook) readChunk;

                        // Blocks
                        hookWrite.eid$setB1(hookRead.eid$getB1());
                        hookWrite.eid$setB2Low(hookRead.eid$getB2Low());
                        hookWrite.eid$setB2High(hookRead.eid$getB2High());
                        hookWrite.eid$setB3(hookRead.eid$getB3());

                        // Metadata
                        hookWrite.eid$setM1Low(hookRead.eid$getM1Low());
                        hookWrite.eid$setM1High(hookRead.eid$getM1High());
                        hookWrite.eid$setM2(hookRead.eid$getM2());
                    } else {
                        writeChunk.setBlockLSBArray(readChunk.getBlockLSBArray());
                        writeChunk.setBlockMSBArray(readChunk.getBlockMSBArray());
                        writeChunk.setBlockMetadataArray(readChunk.getMetadataArray());
                    }
                    writeChunk.setSkylightArray(readChunk.getSkylightArray());

                }
            }
            HELightSMPHooks.onChunkDataLoad(chunk);
            return null;
        }
    }
}
