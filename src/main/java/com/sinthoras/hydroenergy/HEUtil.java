package com.sinthoras.hydroenergy;

public class HEUtil {
	
	public static int coordBlockToChunk(int blockCoord) {
		return blockCoord < 0 ? -((-blockCoord - 1) >> 4) - 1 : blockCoord >> 4;
	}
	
	public static int coordChunkToBlock(int chunkCoord) {
		return chunkCoord < 0 ? -((-chunkCoord) << 4) : chunkCoord << 4;
	}
	
	public static long chunkCoordsToKey(int chunkX, int chunkZ) {
		return (((long)chunkX) << 32) | (chunkZ & 0xffffffffL);
	}

	public static float clamp(float value, float lowerLimit, float upperLimit) {
		return Math.min(Math.max(value, lowerLimit), upperLimit);
	}

	public static int clamp(int value, int lowerLimit, int upperLimit) {
		return Math.min(Math.max(value, lowerLimit), upperLimit);
	}

	public static long clamp(long value, long lowerLimit, long upperLimit) {
		return Math.min(Math.max(value, lowerLimit), upperLimit);
	}

	public static short chunkYToFlag(int chunkY) {
		return (short)(1 << chunkY);
	}

	public static class AveragedRingBuffer {
		private float[] values;
		private int pointer = 0;
		private float average = 0.0f;

		public AveragedRingBuffer(int averagedDurationInTicks) {
			values = new float[averagedDurationInTicks];
		}

		public void addValue(float newValue) {
			float oldValue = values[pointer];
			values[pointer] = newValue;
			average += newValue / values.length - oldValue / values.length;
			pointer++;
			pointer = pointer % values.length;
		}

		public float getAverage() {
			return average;
		}
	}
}
