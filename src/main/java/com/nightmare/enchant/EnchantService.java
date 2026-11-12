package com.nightmare.enchant;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.nightmare.Main;
import com.nightmare.relic.AbyssalTendrilsService;
import com.nightmare.relic.TendrilsEffects;

public final class EnchantService implements Listener {

    public static final Set<UUID> PROCESSING_HIT = new HashSet<>();

    public static final Set<UUID> PROCESSING_DAMAGED = new HashSet<>();

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof LivingEntity victim)) {
            return;
        }

        Entity attacker = null;

        if (event instanceof EntityDamageByEntityEvent byEntity) {
            attacker = byEntity.getDamager();

            if (attacker instanceof Projectile projectile && projectile.getShooter() instanceof Entity shooter) {
                attacker = shooter;
            }
        }

        if (!PROCESSING_DAMAGED.add(victim.getUniqueId())) {
            return;
        }

        try {
            Map<String, Integer> levels = new HashMap<>();
            ItemStack mainHand = null;

            if (victim.getEquipment() != null) {
                mainHand = victim.getEquipment().getItemInMainHand();
                ItemStack[] armor = {
                        victim.getEquipment().getHelmet(),
                        victim.getEquipment().getChestplate(),
                        victim.getEquipment().getLeggings(),
                        victim.getEquipment().getBoots(),
                        mainHand
                };

                for (ItemStack piece : armor) {
                    if (piece == null || piece.getType() == Material.AIR || piece.getItemMeta() == null) {
                        continue;
                    }

                    PersistentDataContainer container = piece.getItemMeta().getPersistentDataContainer();

                    for (Map.Entry<String, NamespacedKey> entry : EnchantKeys.KEYS.entrySet()) {
                        Integer level = container.get(entry.getValue(), PersistentDataType.INTEGER);

                        if (level != null && level > levels.getOrDefault(entry.getKey(), 0)) {
                            levels.put(entry.getKey(), level);
                        }
                    }
                }
            }

            for (Map.Entry<String, Integer> entry : levels.entrySet()) {
                CustomEnchant enchant = EnchantRegistry.ENCHANTS.get(entry.getKey());

                if (enchant == null) {
                    continue;
                }

                if (enchant.playerOnly && !(victim instanceof Player)) {
                    continue;
                }

                enchant.apply(new EnchantContext(enchant, EnchantTrigger.DAMAGED, attacker, victim, mainHand,
                        entry.getValue(), event, event.getDamage()));
            }
        } finally {
            PROCESSING_DAMAGED.remove(victim.getUniqueId());
        }
    }

    public static void tick() {
        AbyssalCoreService.tick();
        AbyssalTendrilsService.tick();

        for (Player player : Bukkit.getOnlinePlayers()) {
            ItemStack item = player.getInventory().getItemInMainHand();
            boolean holdingAbyssal = false;

            if (item != null && item.getType() != Material.AIR && item.getItemMeta() != null) {
                Integer level = item.getItemMeta().getPersistentDataContainer()
                        .get(EnchantKeys.KEYS.get("abyssal_howl"), PersistentDataType.INTEGER);

                holdingAbyssal = level != null && level > 0;
            }

            ItemStack chestplate = player.getInventory().getChestplate();
            boolean wearingAbyssalCore = false;

            if (chestplate != null && chestplate.getType() != Material.AIR && chestplate.getItemMeta() != null) {
                Integer level = chestplate.getItemMeta().getPersistentDataContainer()
                        .get(EnchantKeys.KEYS.get("abyssal_core"), PersistentDataType.INTEGER);

                wearingAbyssalCore = level != null && level > 0;
            }

            if (holdingAbyssal || wearingAbyssalCore) {
                int strengthAmplifier = Main.enchants.getInt("enchants.abyssal_howl.strength-amplifier", 2);
                int speedAmplifier = Main.enchants.getInt("enchants.abyssal_howl.speed-amplifier", 1);

                player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, PotionEffect.INFINITE_DURATION, strengthAmplifier, true, false, false));
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, PotionEffect.INFINITE_DURATION, speedAmplifier, true, false, false));
                player.getPersistentDataContainer().set(EnchantKeys.ABYSSAL_ACTIVE, PersistentDataType.BOOLEAN, true);

                Location center = player.getLocation().add(0.0, 1.0, 0.0);

                int dust = wearingAbyssalCore ? 3 : 8;
                int ink = wearingAbyssalCore ? 2 : 4;
                int soul = wearingAbyssalCore ? 1 : 2;

                player.getWorld().spawnParticle(Particle.DUST, center, dust, 0.6, 0.8, 0.6, 0.01,
                        new Particle.DustOptions(Color.BLACK, 1.2F));
                player.getWorld().spawnParticle(Particle.SQUID_INK, center, ink, 0.5, 0.7, 0.5, 0.01);
                player.getWorld().spawnParticle(Particle.SCULK_SOUL, center, soul, 0.4, 0.6, 0.4, 0.01);

                TendrilsEffects.passive(player, wearingAbyssalCore);

                continue;
            }

            Boolean active = player.getPersistentDataContainer().get(EnchantKeys.ABYSSAL_ACTIVE, PersistentDataType.BOOLEAN);

            if (active == null || !active) {
                continue;
            }

            PotionEffect strength = player.getPotionEffect(PotionEffectType.STRENGTH);
            PotionEffect speed = player.getPotionEffect(PotionEffectType.SPEED);

            if (strength != null && strength.getDuration() == PotionEffect.INFINITE_DURATION
                    && strength.getAmplifier() == Main.enchants.getInt("enchants.abyssal_howl.strength-amplifier", 2)) {
                player.removePotionEffect(PotionEffectType.STRENGTH);
            }

            if (speed != null && speed.getDuration() == PotionEffect.INFINITE_DURATION
                    && speed.getAmplifier() == Main.enchants.getInt("enchants.abyssal_howl.speed-amplifier", 1)) {
                player.removePotionEffect(PotionEffectType.SPEED);
            }

            player.getPersistentDataContainer().remove(EnchantKeys.ABYSSAL_ACTIVE);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof LivingEntity victim)) {
            return;
        }

        Entity attacker = event.getDamager();

        if (attacker instanceof Projectile projectile && projectile.getShooter() instanceof Entity shooter) {
            attacker = shooter;
        }

        if (attacker == null) {
            return;
        }

        ItemStack item = null;

        if (attacker instanceof Player player) {
            item = player.getInventory().getItemInMainHand();
        } else if (attacker instanceof Mob mob && mob.getEquipment() != null) {
            item = mob.getEquipment().getItemInMainHand();
        }

        if (item == null || item.getType() == Material.AIR || item.getItemMeta() == null) {
            return;
        }

        if (!PROCESSING_HIT.add(victim.getUniqueId())) {
            return;
        }

        try {
            PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();

            for (Map.Entry<String, NamespacedKey> entry : EnchantKeys.KEYS.entrySet()) {
                Integer level = container.get(entry.getValue(), PersistentDataType.INTEGER);

                if (level == null || level <= 0) {
                    continue;
                }

                CustomEnchant enchant = EnchantRegistry.ENCHANTS.get(entry.getKey());

                if (enchant == null || !enchant.applicable.contains(item.getType())) {
                    continue;
                }

                if (enchant.playerOnly && !(attacker instanceof Player)) {
                    continue;
                }

                enchant.apply(new EnchantContext(enchant, EnchantTrigger.HIT, attacker, victim, item, level,
                        event, event.getDamage()));
            }
        } finally {
            PROCESSING_HIT.remove(victim.getUniqueId());
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDeath(EntityDeathEvent event) {
        Entity dead = event.getEntity();
        Entity attacker = dead instanceof LivingEntity living ? living.getKiller() : null;

        if (attacker == null && dead.getLastDamageCause() instanceof EntityDamageByEntityEvent byEntity) {
            attacker = byEntity.getDamager();

            if (attacker instanceof Projectile projectile && projectile.getShooter() instanceof Entity shooter) {
                attacker = shooter;
            }
        }

        if (attacker == null) {
            return;
        }

        ItemStack item = null;

        if (attacker instanceof Player player) {
            item = player.getInventory().getItemInMainHand();
        } else if (attacker instanceof Mob mob && mob.getEquipment() != null) {
            item = mob.getEquipment().getItemInMainHand();
        }

        if (item == null || item.getType() == Material.AIR || item.getItemMeta() == null) {
            return;
        }

        PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();

        for (Map.Entry<String, NamespacedKey> entry : EnchantKeys.KEYS.entrySet()) {
            Integer level = container.get(entry.getValue(), PersistentDataType.INTEGER);

            if (level == null || level <= 0) {
                continue;
            }

            CustomEnchant enchant = EnchantRegistry.ENCHANTS.get(entry.getKey());

            if (enchant == null || !enchant.applicable.contains(item.getType())) {
                continue;
            }

            if (enchant.playerOnly && !(attacker instanceof Player)) {
                continue;
            }

            enchant.apply(new EnchantContext(enchant, EnchantTrigger.KILL, attacker, dead, item, level, null, 0.0));
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onRegainHealth(EntityRegainHealthEvent event) {
        Entity entity = event.getEntity();

        if (!(entity instanceof LivingEntity)) {
            return;
        }

        PersistentDataContainer container = entity.getPersistentDataContainer();

        Long until = container.get(EnchantKeys.ANTI_HEAL_UNTIL, PersistentDataType.LONG);
        Double percent = container.get(EnchantKeys.ANTI_HEAL_PERCENT, PersistentDataType.DOUBLE);

        if (until == null || percent == null || System.currentTimeMillis() >= until) {
            return;
        }

        event.setAmount(event.getAmount() * (1.0 - percent));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        EnchantService.PROCESSING_HIT.remove(event.getPlayer().getUniqueId());
        EnchantService.PROCESSING_DAMAGED.remove(event.getPlayer().getUniqueId());
    }

}


