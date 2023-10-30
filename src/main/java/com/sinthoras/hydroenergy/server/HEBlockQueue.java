package com.sinthoras.hydroenergy.server;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.chunk.NibbleArray;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

import com.sinthoras.hydroenergy.HE;
import com.sinthoras.hydroenergy.HEUtil;
import com.sinthoras.hydroenergy.blocks.HEWater;
import com.sinthoras.hydroenergy.config.HEConfig;
import com.sinthoras.hydroenergy.network.packet.HEPacketChunkUpdate;
import com.sinthoras.hydroenergy.server.mytown2.HEMyTown2Integration;

public class HEBlockQueue {

    private static HashMap<Long, HEQueueChunk> chunks = new HashMap<Long, HEQueueChunk>();
    private static long timestampLastQueueTick = 0;

    public static void onTick() {
        final long currentTime = System.currentTimeMillis();
        if (currentTime - timestampLastQueueTick < HEConfig.delayBetweenSpreadingChunks) {
            return;
        }
        timestampLastQueueTick = currentTime;

        Iterator<Map.Entry<Long, HEQueueChunk>> it = chunks.entrySet().iterator();
        while (it.hasNext()) {
            final Map.Entry<Long, HEQueueChunk> entry = it.next();
            final HEQueueChunk chunk = entry.getValue();
            if (chunk.isLoaded()) {
                it.remove();
                final long key = entry.getKey();
                final int chunkX = (int) (key >> 32);
                final int chunkZ = (int) key;
                if (chunk.resolve()) {
                    final World world = chunk.chunk.worldObj;
                    addToChunk(world, chunkX - 1, chunkZ, chunk.neighborChunkWest);
                    addToChunk(world, chunkX, chunkZ - 1, chunk.neighborChunkNorth);
                    addToChunk(world, chunkX + 1, chunkZ, chunk.neighborChunkEast);
                    addToChunk(world, chunkX, chunkZ + 1, chunk.neighborChunkSouth);
                    return;
                }
            }
        }
    }

    private static void addToChunk(World world, int chunkX, int chunkZ, Stack<QueueEntry> stack) {
        if (!stack.isEmpty()) {
            long key = HEUtil.chunkCoordsToKey(chunkX, chunkZ);
            if (chunks.containsKey(key)) {
                chunks.get(key).blockStack.addAll(stack);
            } else {
                Chunk chunk = world.getChunkFromChunkCoords(chunkX, chunkZ);
                chunks.put(key, new HEQueueChunk(chunk, stack));
            }
        }
    }

    public static void enqueueBlock(World world, int blockX, int blockY, int blockZ, int waterId) {
        int chunkX = HEUtil.coordBlockToChunk(blockX);
        int chunkZ = HEUtil.coordBlockToChunk(blockZ);
        long key = HEUtil.chunkCoordsToKey(chunkX, chunkZ);
        if (chunks.containsKey(key)) {
            chunks.get(key).add(blockX, blockY, blockZ, HE.waterBlocks[waterId]);
        } else {
            chunks.put(
                    key,
                    new HEQueueChunk(
                            world.getChunkFromChunkCoords(chunkX, chunkZ),
                            HE.waterBlocks[waterId],
                            blockX,
                            blockY,
                            blockZ));
        }
    }
}

class HEQueueChunk {

    public Stack<QueueEntry> blockStack;
    public Stack<QueueEntry> neighborChunkWest = new Stack<QueueEntry>();
    public Stack<QueueEntry> neighborChunkNorth = new Stack<QueueEntry>();
    public Stack<QueueEntry> neighborChunkEast = new Stack<QueueEntry>();
    public Stack<QueueEntry> neighborChunkSouth = new Stack<QueueEntry>();
    public Chunk chunk;

    HEQueueChunk(Chunk chunk, Stack<QueueEntry> blockStack) {
        this.chunk = chunk;
        this.blockStack = blockStack;
    }

    HEQueueChunk(Chunk chunk, HEWater waterBlock, int blockX, int blockY, int blockZ) {
        this.chunk = chunk;
        blockStack = new Stack<QueueEntry>();
        blockStack.add(new QueueEntry(blockX, blockY, blockZ, waterBlock));
    }

    public boolean resolve() {
        boolean[] permissionsChecked = new boolean[HEConfig.maxDams];
        boolean[] hasPermissions = new boolean[HEConfig.maxDams];
        ExtendedBlockStorage[] chunkStorage = chunk.getBlockStorageArray();
        short subChunksHaveChanges = 0;
        while (!blockStack.isEmpty()) {
            QueueEntry entry = blockStack.pop();
            int waterId = entry.waterBlock.getWaterId();
            if (permissionsChecked[waterId] == false) {
                hasPermissions[waterId] = HEMyTown2Integration.getInstance().hasPlayerModificationRightsForChunk(
                        HEServer.instance.getOwnerName(waterId),
                        chunk.worldObj.provider.dimensionId,
                        chunk.xPosition,
                        chunk.zPosition);
                permissionsChecked[waterId] = true;
            }
            Block block = chunk.getBlock(entry.blockX & 15, entry.blockY, entry.blockZ & 15);
            boolean removeBlock = !HEServer.instance.canSpread(waterId)
                    || HEServer.instance.isBlockOutOfBounds(waterId, entry.blockX, entry.blockY, entry.blockZ)
                    || !hasPermissions[waterId];
            if (removeBlock) {
                if (block == entry.waterBlock) {
                    int chunkY = entry.blockY >> 4;
                    chunkStorage[chunkY]
                            .func_150818_a(entry.blockX & 15, entry.blockY & 15, entry.blockZ & 15, Blocks.air);
                    HEServer.instance.onWaterRemoved(waterId, entry.blockY);
                    subChunksHaveChanges |= HEUtil.chunkYToFlag(chunkY);

                    add(entry.blockX - 1, entry.blockY, entry.blockZ, entry.waterBlock);
                    add(entry.blockX, entry.blockY - 1, entry.blockZ, entry.waterBlock);
                    add(entry.blockX, entry.blockY, entry.blockZ - 1, entry.waterBlock);
                    add(entry.blockX + 1, entry.blockY, entry.blockZ, entry.waterBlock);
                    add(entry.blockX, entry.blockY + 1, entry.blockZ, entry.waterBlock);
                    add(entry.blockX, entry.blockY, entry.blockZ + 1, entry.waterBlock);
                }
            } else {
                if (entry.waterBlock.canFlowInto(chunk.worldObj, entry.blockX, entry.blockY, entry.blockZ)) {
                    int chunkY = entry.blockY >> 4;
                    if (chunkStorage[chunkY] == null) {
                        chunkStorage[chunkY] = new ExtendedBlockStorage(chunkY << 4, !chunk.worldObj.provider.hasNoSky);
                    }
                    chunkStorage[chunkY]
                            .func_150818_a(entry.blockX & 15, entry.blockY & 15, entry.blockZ & 15, entry.waterBlock);
                    // If the block is over all opague blocks aka can see the sky simply set light to 15.
                    // Else to the value of the first non HEWater block directly below
                    if (chunk.canBlockSeeTheSky(entry.blockX & 15, entry.blockY, entry.blockZ & 15)) {
                        chunkStorage[chunkY].getSkylightArray()
                                .set(entry.blockX & 15, entry.blockY & 15, entry.blockZ & 15, 15);
                    } else {
                        int highestOpaqueBlockY = chunk.heightMap[(entry.blockZ & 15) << 4 | (entry.blockX & 15)];
                        int highestOpaqueChunkY = HEUtil.coordBlockToChunk(highestOpaqueBlockY);
                        if (chunkStorage[highestOpaqueChunkY] == null) {
                            chunkStorage[highestOpaqueChunkY] = new ExtendedBlockStorage(
                                    highestOpaqueChunkY << 4,
                                    !chunk.worldObj.provider.hasNoSky);
                        }
                        NibbleArray skylightArray = chunkStorage[highestOpaqueChunkY].getSkylightArray();
                        if (skylightArray == null) {
                            skylightArray = new NibbleArray(HE.blockPerSubChunk, 4);
                            chunkStorage[highestOpaqueChunkY].setSkylightArray(skylightArray);
                        }
                        int lightValue = skylightArray
                                .get(entry.blockZ & 15, highestOpaqueBlockY & 15, entry.blockX & 15);
                        skylightArray.set(entry.blockX & 15, entry.blockY & 15, entry.blockZ & 15, lightValue);
                    }
                    HEServer.instance.onWaterPlaced(waterId, entry.blockY);
                    subChunksHaveChanges |= HEUtil.chunkYToFlag(chunkY);

                    add(entry.blockX - 1, entry.blockY, entry.blockZ, entry.waterBlock);
                    add(entry.blockX, entry.blockY - 1, entry.blockZ, entry.waterBlock);
                    add(entry.blockX, entry.blockY, entry.blockZ - 1, entry.waterBlock);
                    add(entry.blockX + 1, entry.blockY, entry.blockZ, entry.waterBlock);
                    add(entry.blockX, entry.blockY + 1, entry.blockZ, entry.waterBlock);
                    add(entry.blockX, entry.blockY, entry.blockZ + 1, entry.waterBlock);
                }
            }
        }
        boolean changedChunk = subChunksHaveChanges > 0;
        if (changedChunk) {

            chunk.setChunkModified();

            HEPacketChunkUpdate message = new HEPacketChunkUpdate(chunk, subChunksHaveChanges);
            for (EntityPlayerMP player : (List<EntityPlayerMP>) MinecraftServer.getServer()
                    .getConfigurationManager().playerEntityList) {
                if (chunk.worldObj.provider.dimensionId == player.worldObj.provider.dimensionId
                        && player.getServerForPlayer().getPlayerManager()
                                .isPlayerWatchingChunk(player, chunk.xPosition, chunk.zPosition)) {
                    HE.network.sendTo(message, player);
                }
            }
        }
        return changedChunk;
    }

    public void add(int blockX, int blockY, int blockZ, HEWater waterBlock) {
        final QueueEntry entry = new QueueEntry(blockX, blockY, blockZ, waterBlock);
        int chunkX = HEUtil.coordBlockToChunk(blockX);
        int chunkZ = HEUtil.coordBlockToChunk(blockZ);
        Block block = chunk.getBlock(blockX & 15, blockY, blockZ & 15);
        if (chunkX < chunk.xPosition) {
            neighborChunkWest.push(entry);
        } else if (chunkZ < chunk.zPosition) {
            neighborChunkNorth.push(entry);
        } else if (chunkX > chunk.xPosition) {
            neighborChunkEast.push(entry);
        } else if (chunkZ > chunk.zPosition) {
            neighborChunkSouth.push(entry);
        } else if (block == waterBlock || waterBlock.canFlowInto(chunk.worldObj, blockX, blockY, blockZ)) {
            this.blockStack.push(entry);
        }
    }

    public boolean isLoaded() {
        final IChunkProvider chunkProvider = chunk.worldObj.getChunkProvider();
        return chunkProvider.chunkExists(chunk.xPosition, chunk.zPosition)
                && chunkProvider.chunkExists(chunk.xPosition - 1, chunk.zPosition)
                && chunkProvider.chunkExists(chunk.xPosition, chunk.zPosition - 1)
                && chunkProvider.chunkExists(chunk.xPosition + 1, chunk.zPosition)
                && chunkProvider.chunkExists(chunk.xPosition, chunk.zPosition + 1);
    }
}

class QueueEntry {

    public int blockX;
    public int blockY;
    public int blockZ;
    public HEWater waterBlock;

    public QueueEntry(int blockX, int blockY, int blockZ, HEWater waterBlock) {
        this.blockX = blockX;
        this.blockY = blockY;
        this.blockZ = blockZ;
        this.waterBlock = waterBlock;
    }
}
