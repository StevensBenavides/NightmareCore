package com.nightmare.tier;

public enum MobTier {

    A(0, 0, 100, 0, 1.00, 1.00, 0, 1.00, 0.000, false),
    B(1, 9, 80, 0, 1.15, 1.05, 2, 1.00, 0.000, false),
    C(2, 18, 65, 0, 1.30, 1.10, 4, 1.00, 0.000, false),
    D(3, 27, 50, 1, 1.50, 1.15, 6, 1.02, 0.020, false),
    E(4, 36, 40, 1, 1.70, 1.20, 8, 1.04, 0.030, false),
    F(5, 45, 32, 1, 1.90, 1.25, 10, 1.06, 0.040, false),
    G(6, 54, 25, 2, 2.20, 1.30, 12, 1.08, 0.050, false),
    H(7, 63, 20, 2, 2.50, 1.35, 14, 1.10, 0.060, false),
    I(8, 72, 16, 3, 2.80, 1.40, 16, 1.12, 0.070, false),
    J(9, 81, 12, 4, 3.20, 1.50, 18, 1.14, 0.080, false),
    K(10, 90, 6, 5, 5.00, 1.70, 20, 1.18, 0.250, true);

    public final int index;
    public final int unlockDay;
    public final int baseWeight;
    public final int enchantLevel;
    public final double healthMultiplier;
    public final double damageMultiplier;
    public final int armorBonus;
    public final double speedMultiplier;
    public final double dropChance;
    public final boolean elite;

    MobTier(int index, int unlockDay, int baseWeight, int enchantLevel, double healthMultiplier,
            double damageMultiplier, int armorBonus, double speedMultiplier, double dropChance, boolean elite) {
        this.index = index;
        this.unlockDay = unlockDay;
        this.baseWeight = baseWeight;
        this.enchantLevel = enchantLevel;
        this.healthMultiplier = healthMultiplier;
        this.damageMultiplier = damageMultiplier;
        this.armorBonus = armorBonus;
        this.speedMultiplier = speedMultiplier;
        this.dropChance = dropChance;
        this.elite = elite;
    }

}
