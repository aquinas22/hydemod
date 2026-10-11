package com.hydemod.item;

import net.minecraft.util.StringRepresentable;

/**
 * Kyber crystal colours. The harder a crystal is to obtain, the harder its blade hits:
 * green (overworld stone) < blue (deep overworld) < red (nether) < purple (end).
 */
public enum KyberColor implements StringRepresentable {
    GREEN("green", 8.0F, 0x55FF55),
    BLUE("blue", 9.5F, 0x5599FF),
    RED("red", 12.0F, 0xFF4444),
    PURPLE("purple", 15.0F, 0xCC66FF);

    private final String name;
    private final float attackDamage;
    private final int rgb;

    KyberColor(String name, float attackDamage, int rgb) {
        this.name = name;
        this.attackDamage = attackDamage;
        this.rgb = rgb;
    }

    /** Total attack damage of a lit, powered blade (hearts x2). */
    public float attackDamage() {
        return attackDamage;
    }

    public int rgb() {
        return rgb;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
