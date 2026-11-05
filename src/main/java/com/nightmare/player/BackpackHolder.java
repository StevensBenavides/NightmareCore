package com.nightmare.player;

import java.util.UUID;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public final class BackpackHolder implements InventoryHolder {

    private final UUID owner;
    private Inventory inventory;

    public BackpackHolder(UUID owner) {
        this.owner = owner;
    }

    public UUID owner() {
        return owner;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }
}
