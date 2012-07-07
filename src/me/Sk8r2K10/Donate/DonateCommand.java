package me.Sk8r2K10.Donate;

import java.sql.ResultSet;
import java.sql.SQLException;
import me.Sk8r2K10.Donate.util.InventoryManager;
import net.milkbowl.vault.item.Items;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class DonateCommand implements CommandExecutor {

	public Donate plugin;
	public Player player;

	public DonateCommand(Donate instance) {

		this.plugin = instance;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {

		if (sender instanceof Player) {

			player = (Player) sender;
		}

		if (sender instanceof Player) {
			if (commandLabel.equalsIgnoreCase("deposit")) {
                if (!plugin.util.getPerm(player, "deposit.deposit")) {
                    
                    return false;
                }
                // Donate hand
                if (args.length == 0) {
                    this.donate(player);
                    return true;
				}
                // Donate hand + amount
				if (args.length == 1 && plugin.util.isInt(args[0])) {
    				int am = plugin.util.getInt(args[0]);
                    this.donate(player, am);
                    return true;
				}
                // Help command
				if (args.length == 1 && args[0].equalsIgnoreCase("help")) {
					plugin.util.help(player);
					return true;
				}
                // Donate material + amount
				if (args.length == 2 && plugin.util.isInt(args[1])) {
                    if (!plugin.util.isMaterial(args[0])) {
                        player.sendMessage(plugin.pre + ChatColor.RED + "The material '" + ChatColor.YELLOW + args[0] + ChatColor.RED + "' is not recognised.");
                        return false;
                    }
                    if (!plugin.util.isInt(args[1])) {
                        player.sendMessage(plugin.pre + ChatColor.RED + "Amount specified is not a number, or is invalid.");
                        return false;
                    }
                    ItemStack i = Items.itemByString(args[0]).toStack();
                    if (!(new InventoryManager(player).contains(i))) {
                        player.sendMessage(plugin.pre + ChatColor.RED + "You don't have enough of that Item.");
                        return false;
                    }
                    i.setAmount(plugin.util.getInt(args[1]));                    
                    this.donate(player, i);
                    return true;
				}
			} 

			if (commandLabel.equalsIgnoreCase("redeem")) {
				if (!plugin.util.getPerm(player, "deposit.redeem")) {
                    
                    return false;
                }          
                // Redeem material + amount
                if (args.length == 2 && !args[0].equalsIgnoreCase("list")) {
                    if (!plugin.util.isMaterial(args[0])) {
                        player.sendMessage(plugin.pre + ChatColor.RED + "The material '" + ChatColor.YELLOW + args[0] + ChatColor.RED + "' is not recognised.");
                        return false;
                    }

                    if (!plugin.util.isInt(args[1])) {
                        player.sendMessage(plugin.pre + "Amount specified is not a number, or is invalid");
                        return false;
                    }
                    ItemStack i = Items.itemByString(args[0]).toStack();
                    int am = plugin.util.getInt(args[1]);
                    this.redeemItem(player, i, am);
                    return true;
				}
                // Redeem all material
				if (args.length == 1 && plugin.util.isMaterial(args[0])) {
                    ItemStack item = Items.itemByString(args[0]).toStack();
                    try {
                        ResultSet result = plugin.SQL.selectItem(item);
                        int am = 0;

                        if (result.next()) {

                            am = result.getInt("amount");
                        } else {
                            result.close();
                            player.sendMessage(plugin.pre + "That item has not yet been stored in the Vault!");
                            return false;
                        }
                        result.close();
                        
                        this.redeemItem(player, item, am);
                    } catch (SQLException e) {
                        plugin.log.severe("[Donate] Problem reading from SQL: " + e.getMessage());
                    }
					return true;
				}
                // List materials
				if (args.length >= 1 && args[0].equalsIgnoreCase("list")) {
					this.redeemList(player, args);
                    return true;
				}
				// Help command
				if (args.length == 1 && args[0].equalsIgnoreCase("help")) {
					plugin.util.help(player);
					return true;
				}
			}
            // Reached here, send invalid command usage message
            player.sendMessage(plugin.pre + ChatColor.RED + "Invalid command usage");
            plugin.util.help(player);
			return false;
		} else if (sender instanceof ConsoleCommandSender) {
			if (commandLabel.equalsIgnoreCase("vlist")) {
				this.consoleList(sender);
				return true;
			}
		}
		return false;
	}
    // Methods
    private void donate(Player player) {
        if (player.getItemInHand().clone().getType().equals(Material.AIR)) {
            player.sendMessage(plugin.pre + ChatColor.RED + "There's nothing in your hand.");
            return;
        }
        ItemStack i = player.getItemInHand().clone();
        this.donate(player, i);
    }
    
    private void donate(Player player, int am) {
        if (player.getItemInHand().clone().getType().equals(Material.AIR)) {
            player.sendMessage(plugin.pre + ChatColor.RED + "There's nothing in your hand.");
            return;
        }
        if (player.getItemInHand().clone().getAmount() < am) {
            player.sendMessage(plugin.pre + ChatColor.RED + "You don't have enough of that item in your hand");
            return;
        }
        ItemStack i = player.getItemInHand().clone();
        i.setAmount(am);
        this.donate(player, i);
    }
    
    private void donate(Player player, ItemStack i) {
        if (!plugin.util.isSupported(i)) {
            player.sendMessage(plugin.pre + ChatColor.RED + "That Item is not supported.");
            return;
        }
        
        if (plugin.util.isBlacklisted(Items.itemByStack(i).getName())) {
            player.sendMessage(plugin.pre + ChatColor.RED + "That Item is blacklisted for donation.");
            return;
        }                  
        if (i.getDurability() < i.getType().getMaxDurability()) {
            player.sendMessage(plugin.pre + ChatColor.RED + "Please don't donate used tools and items!");
            return;
        }
        if (!i.getEnchantments().isEmpty()) {
            player.sendMessage(plugin.pre + ChatColor.RED + "Please don't donate enchanted tools and items!");
            return;
        }

        new InventoryManager(player).remove(i);
        player.sendMessage(plugin.pre + "Thank you for donating to the Vault.");

        try {
            plugin.SQL.addToVault(i);
        } catch (SQLException e) {
            plugin.log.severe("[Donate] Problem adding to SQL: " + e.getMessage());
        }
    }
    
    private void redeemItem(Player player, ItemStack i, int am) {
        try {
            ResultSet result = plugin.SQL.selectItem(i);
            if (result.getInt("amount") < am) {
                result.close();
                player.sendMessage(plugin.pre + "Not enough of that item to redeem!");
                return;
            }
            result.close();

            i.setAmount(am);
            int slotsAvailable = plugin.util.countSlots(player);

            int slotsNeeded = (int) am / 64;

            if (slotsAvailable < slotsNeeded) {

                player.sendMessage(plugin.pre + ChatColor.RED + "You don't have enough inventory space for " + am + " " + Items.itemByStack(i).getName());
                am = slotsAvailable * 64;

                i.setAmount(am);
                player.getInventory().addItem(i);
                player.sendMessage(plugin.pre + "Adding " + (slotsAvailable * 64) + " " + Items.itemByStack(i).getName() + " instead");
            } else {
                player.getInventory().addItem(i);
                player.sendMessage(plugin.pre + am + " " + Items.itemByStack(i).getName() + "(s) have been redeemed from the Vault.");
            }

            try {

                plugin.SQL.removeFromVault(i, am);
            } catch (SQLException e) {

                plugin.log.severe("[Donate] Problem removing from SQL: " + e.getMessage());
            }
        } catch (SQLException e) {

            plugin.log.severe("[Donate] Problem reading from SQL: " + e.getMessage());
        }
    }
    
    private void redeemList(Player player, String[] args) {
        try {
            ResultSet result = plugin.SQL.getVaultContents();
            player.sendMessage(plugin.pre + "- Vault contents");

            int page = 0;
            int end = 10;

            if (args.length > 1 && plugin.util.isInt(args[1]) && !(plugin.util.getInt(args[1]) <= 1)) {
                page = (plugin.util.getInt(args[1]) * 8) - 8;
                end = plugin.util.getInt(args[1]) * 8;

                if (page != 0) {
                    int i = 0;

                    while (result.next() && i < (page - 1)) { //SQLite won't let me .absolute(int) >.>
                        i++;
                    }
                }
            }

            while (result.next() && page < end) {
                page++;
                String msg = ChatColor.AQUA + result.getString("item") + ChatColor.GRAY + " - " + ChatColor.AQUA + result.getInt("amount");

                player.sendMessage(msg);
            }

            if (args.length > 1) {
                player.sendMessage(plugin.pre + "Page: " + plugin.util.getInt(args[1]));
            }
            if (args.length == 1) {
                player.sendMessage(plugin.pre + "Page: 1");
            }
            result.close();

        } catch (SQLException e) {

            plugin.log.severe("[Donate] Problem reading SQL: " + e.getMessage());
        }
    }
    
    private void consoleList(CommandSender sender) {
        try {
            ResultSet result = plugin.SQL.getVaultContents();
            sender.sendMessage(plugin.pre + "- Vault contents");

            while (result.next()) {

                String msg = ChatColor.AQUA + result.getString("item") + ChatColor.GRAY + " - " + ChatColor.AQUA + result.getInt("amount");

                sender.sendMessage(msg);
            }
            result.close();

        } catch (SQLException e) {

            plugin.log.severe("[Donate] Problem reading SQL: " + e.getMessage());
        }
    }
}