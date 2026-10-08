package com.sinthoras.hydroenergy;

/** A section address without truncating any of the three coordinates. */
public final class HESectionPos {

    public final int x;
    public final int y;
    public final int z;

    public HESectionPos(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof HESectionPos)) return false;
        HESectionPos other = (HESectionPos) obj;
        return x == other.x && y == other.y && z == other.z;
    }

    @Override
    public int hashCode() {
        return (x * 31 + y) * 31 + z;
    }
}
