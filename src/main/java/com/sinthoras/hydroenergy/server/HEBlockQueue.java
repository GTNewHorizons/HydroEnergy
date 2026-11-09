package com.sinthoras.hydroenergy.server;

import java.util.ArrayDeque;
import java.util.BitSet;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

import com.sinthoras.hydroenergy.HE;
import com.sinthoras.hydroenergy.HESectionPos;
import com.sinthoras.hydroenergy.HEWorld;
import com.sinthoras.hydroenergy.blocks.HEWater;
import com.sinthoras.hydroenergy.config.HEConfig;
import com.sinthoras.hydroenergy.server.mytown2.HEMyTown2Integration;

public class HEBlockQueue {

    private static final Map<World, Map<HESectionPos, SectionQueue>> worlds = new IdentityHashMap<>();
    private static long timestampLastQueueTick;
    private static World activeWorld;
    private static int activeX, activeZ;
    private static boolean activeChanged;

    public static void onTick() {
        long now = System.currentTimeMillis();
        if (activeWorld == null && now - timestampLastQueueTick < HEConfig.delayBetweenSpreadingChunks) return;
        // Preserve the configured delay between columns, but finish their vertical sections across ticks.
        // Native world updates do lighting work, so both the work count and elapsed time are bounded.
        int remaining = 256;
        long deadline = System.nanoTime() + 4_000_000L;
        while (remaining > 0 && System.nanoTime() < deadline) {
            SectionQueue ready = findReady();
            if (ready == null) {
                if (activeWorld == null) return;
                activeWorld = null;
                if (activeChanged) {
                    return;
                }
                continue;
            }
            if (activeWorld == null) {
                activeWorld = ready.world;
                activeX = ready.pos.x;
                activeZ = ready.pos.z;
                activeChanged = false;
            }
            remaining -= ready.resolve(remaining, deadline);
            if (ready.changed && !activeChanged) timestampLastQueueTick = now;
            activeChanged |= ready.changed;
            if (ready.entries.isEmpty()) worlds.get(ready.world).remove(ready.pos);
            else return;
        }
    }

    private static SectionQueue findReady() {
        for (Map.Entry<World, Map<HESectionPos, SectionQueue>> world : worlds.entrySet()) {
            if (activeWorld != null && world.getKey() != activeWorld) continue;
            for (SectionQueue section : world.getValue().values()) {
                if (activeWorld != null && (section.pos.x != activeX || section.pos.z != activeZ)) continue;
                if (section.isLoaded()) return section;
            }
        }
        return null;
    }

    public static void enqueueBlock(World world, int x, int y, int z, int waterId) {
        if (world == null || world.isRemote
                || waterId < 0
                || waterId >= HE.waterBlocks.length
                || y < HEWorld.minHeight(world)
                || y >= HEWorld.maxHeight(world))
            return;
        HESectionPos pos = new HESectionPos(x >> 4, y >> 4, z >> 4);
        Map<HESectionPos, SectionQueue> sections = worlds.computeIfAbsent(world, ignored -> new LinkedHashMap<>());
        sections.computeIfAbsent(pos, ignored -> new SectionQueue(world, pos)).add(x, y, z, waterId);
    }

    public static void onWorldUnload(World world) {
        worlds.remove(world);
        if (activeWorld == world) activeWorld = null;
    }

    public static void clear() {
        worlds.clear();
        activeWorld = null;
        activeChanged = false;
        timestampLastQueueTick = 0;
    }

    private static final class SectionQueue {

        private final World world;
        private final HESectionPos pos;
        private final Deque<Entry> entries = new ArrayDeque<>();
        // Deduplicate pending entries only: edits and mode changes can revisit a processed position.
        private final BitSet[] pending = new BitSet[HEConfig.maxDams];
        private boolean changed;

        private SectionQueue(World world, HESectionPos pos) {
            this.world = world;
            this.pos = pos;
        }

        private void add(int x, int y, int z, int waterId) {
            if (y < HEWorld.minHeight(world) || y >= HEWorld.maxHeight(world)) return;
            if ((x >> 4) != pos.x || (y >> 4) != pos.y || (z >> 4) != pos.z) {
                enqueueBlock(world, x, y, z, waterId);
                return;
            }
            BitSet flags = pending[waterId];
            if (flags == null) pending[waterId] = flags = new BitSet(4096);
            int address = ((y & 15) << 8) | ((z & 15) << 4) | (x & 15);
            if (!flags.get(address)) {
                flags.set(address);
                entries.push(new Entry(x, y, z, waterId));
            }
        }

        private boolean isLoaded() {
            if (!HEWorld.isLoaded(world, pos.x, pos.y, pos.z) || !HEWorld.isLoaded(world, pos.x - 1, pos.y, pos.z)
                    || !HEWorld.isLoaded(world, pos.x + 1, pos.y, pos.z)
                    || !HEWorld.isLoaded(world, pos.x, pos.y, pos.z - 1)
                    || !HEWorld.isLoaded(world, pos.x, pos.y, pos.z + 1))
                return false;
            return (pos.y <= (HEWorld.minHeight(world) >> 4) || HEWorld.isLoaded(world, pos.x, pos.y - 1, pos.z))
                    && (pos.y >= ((HEWorld.maxHeight(world) - 1) >> 4)
                            || HEWorld.isLoaded(world, pos.x, pos.y + 1, pos.z));
        }

        private int resolve(int budget, long deadline) {
            int processed = 0;
            changed = false;
            boolean[] checked = new boolean[HEConfig.maxDams];
            boolean[] allowed = new boolean[HEConfig.maxDams];
            while (!entries.isEmpty() && processed < budget && System.nanoTime() < deadline) {
                processed++;
                Entry entry = entries.pop();
                int id = entry.waterId;
                pending[id].clear(((entry.y & 15) << 8) | ((entry.z & 15) << 4) | (entry.x & 15));
                if (!checked[id]) {
                    checked[id] = true;
                    allowed[id] = HEMyTown2Integration.getInstance().hasPlayerModificationRightsForChunk(
                            HEServer.instance.getOwnerName(id),
                            world.provider.dimensionId,
                            pos.x,
                            pos.z);
                }
                HEWater water = HE.waterBlocks[id];
                Block old = world.getBlock(entry.x, entry.y, entry.z);
                boolean remove = !allowed[id] || !HEServer.instance.canSpread(id)
                        || HEServer.instance.isBlockOutOfBounds(id, entry.x, entry.y, entry.z);
                if (remove ? old != water : !water.canFlowInto(world, entry.x, entry.y, entry.z)) continue;
                // Normal world updates maintain cube storage, dirty state, lighting and watcher packets.
                // Neighbours are pending below; notifying them synchronously would recursively flood the queue.
                if (!world.setBlock(entry.x, entry.y, entry.z, remove ? Blocks.air : water, 0, 2)) continue;
                changed = true;
                if (remove) HEServer.instance.onWaterRemoved(id, entry.y);
                else HEServer.instance.onWaterPlaced(id, entry.y);
                add(entry.x - 1, entry.y, entry.z, id);
                add(entry.x + 1, entry.y, entry.z, id);
                add(entry.x, entry.y - 1, entry.z, id);
                add(entry.x, entry.y + 1, entry.z, id);
                add(entry.x, entry.y, entry.z - 1, id);
                add(entry.x, entry.y, entry.z + 1, id);
            }
            return processed;
        }
    }

    private static final class Entry {

        private final int x, y, z, waterId;

        private Entry(int x, int y, int z, int waterId) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.waterId = waterId;
        }
    }
}
