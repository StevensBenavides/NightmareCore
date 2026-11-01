package com.nightmare.enchant;

import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;

public final class EnchantContext {

    public final CustomEnchant enchant;
    public final EnchantTrigger trigger;
    public final Entity attacker;
    public final Entity victim;
    public final ItemStack item;
    public final int level;
    public final EntityDamageEvent event;
    public final double damage;

    public EnchantContext(CustomEnchant enchant, EnchantTrigger trigger, Entity attacker, Entity victim,
            ItemStack item, int level, EntityDamageEvent event, double damage) {
        this.enchant = enchant;
        this.trigger = trigger;
        this.attacker = attacker;
        this.victim = victim;
        this.item = item;
        this.level = level;
        this.event = event;
        this.damage = damage;
    }

}
