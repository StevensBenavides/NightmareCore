package com.nightmare;


import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import net.md_5.bungee.api.ChatColor;

public class CustomEnchants {
    
    public static ItemStack createStormBreakerEnchant() {
        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta meta = book.getItemMeta();

        if (meta == null) return book;

        ArrayList<String> lore = new ArrayList<>();

        meta.setItemName(ChatColor.translateAlternateColorCodes('&', "&e&l✴ StormBreaker I &e&l✴"));
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&e&l✴ StormBreaker I &e&l✴"));
        
        lore.add(ChatColor.translateAlternateColorCodes('&', ""));
        lore.add(ChatColor.translateAlternateColorCodes('&', "&c☠ &eElectrocute your enemies with"));
        lore.add(ChatColor.translateAlternateColorCodes('&', "&fa burst of lightning. &c☠"));
        lore.add(ChatColor.translateAlternateColorCodes('&', ""));
        lore.add(ChatColor.translateAlternateColorCodes('&', "&e&l• &fApplicable on the anvil."));

        meta.setLore(lore);

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(Main.getInstance(), "StormBreakerEnchant");

        pdc.set(key, PersistentDataType.STRING, "StormBreakerEnchant");
        book.setItemMeta(meta);

        return book;
    }

}
