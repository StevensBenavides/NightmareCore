package com.nightmare.gui;

import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import com.nightmare.tier.MobTier;

public final class NightmareGuiHolder implements InventoryHolder {

    public final GuiType type;
    public final String enchantId;
    public final int level;
    public final int page;
    public final String category;
    public final MobTier tier;
    public final EquipmentSlot slot;
    public final boolean legendary;
    public final int abyssalStep;

    private Inventory inventory;

    public NightmareGuiHolder(GuiType type, String enchantId, int level, int page, String category,
            MobTier tier, EquipmentSlot slot, boolean legendary, int abyssalStep
    ) {
        this.type = type;
        this.enchantId = enchantId;
        this.level = level;
        this.page = page;
        this.category = category;
        this.tier = tier;
        this.slot = slot;
        this.legendary = legendary;
        this.abyssalStep = abyssalStep;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

}
