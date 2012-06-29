package me.Sk8r2K10.Donate.util;

import me.Sk8r2K10.Donate.Donate;
import net.milkbowl.vault.item.Items;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class Utilities {

    public Donate plugin;

    public Utilities(Donate instance) {

        this.plugin = instance;
    }

    public boolean isEmpty(Inventory inv) {
        for (ItemStack item : inv.getContents()) {
            if (item != null) {
                return false;
            }
        }
        return true;
    }

    public boolean isMaterial(String args, Player player) {
        if (Items.itemByString(args) != null) { //check if item is null

            return true;
        } else if (Material.matchMaterial(args) != null) { //check if Vault is just out of date
            
			plugin.log.info("[Donate] Vault may be out of date");
            plugin.log.info("[Donate] Please update Vault to the latest version");
        }
		if (!(args.equalsIgnoreCase("help") || args.equalsIgnoreCase("list"))) {player.sendMessage(plugin.pre + ChatColor.RED + "The material '" + ChatColor.YELLOW + args + ChatColor.RED + "' is not recognised.");}
        return false;
    }
    
    public boolean isInt(String arg) {
        try {
            Integer.parseInt(arg);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }
    
    public int getInt(String arg) {
        if (this.isInt(arg)) {
            int i = Integer.parseInt(arg);
            return i;
        }
        return -1;
    }
	
	public void help(Player player) {
		
		player.sendMessage(plugin.pre + " - Help Menu - " + plugin.pre);
		if (player.hasPermission("donate.donate")) player.sendMessage(ChatColor.AQUA + "/donate [item] [amount] - " + ChatColor.GRAY + "Donates item specifed, or item in hand if not specified.");
		if (player.hasPermission("donate.redeem")) player.sendMessage(ChatColor.AQUA + "/redeem [item] [amount] OR /redeem <list> [page] - " + ChatColor.GRAY + "Redeems, or lists items in Vault.");
		//Super-happy-modularness
	}
	
	public boolean getPerm(Player player, String perm) {
		if (player.hasPermission(perm)) {
			
			return true;
		}
		player.sendMessage(plugin.pre + ChatColor.RED + "You don't have permission for that command.");
		return false;
	}
	
	public int countSlots(Player player) {
		int slots = 0;
		Inventory inv = player.getInventory();
		ItemStack[] invslot = inv.getContents();
		
		for (int i = 0; i < inv.getSize(); i++) {

			if (invslot[i] == null) {
				slots++;
				continue;
			}
		}
		return slots;
	}
}
