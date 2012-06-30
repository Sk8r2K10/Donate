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
			if (commandLabel.equalsIgnoreCase("donate")) {
                if (!plugin.util.getPerm(player, "donate.donate")) {
                    
                    return false;
                }
                
                if (args.length == 0) {

					ItemStack item = player.getItemInHand().clone();
                    if (plugin.util.isBlacklisted(Items.itemByStack(item).getName())) {
                        player.sendMessage(plugin.pre + "That Item is blacklisted for donation.");
                        return false;
                    }                    
                    if (item.getType().equals(Material.AIR)) {
                        player.sendMessage(plugin.pre + "There's no Item in your hand!");
                        return false;
                    }                    
					if (item.getDurability() < item.getType().getMaxDurability()) {
						player.sendMessage(plugin.pre + "Please don't donate used tools and items!");
						return false;
					}
					if (!item.getEnchantments().isEmpty()) {
						player.sendMessage(plugin.pre + "Please don't donate enchanted tools and items!");
						return false;
					}

					new InventoryManager(player).remove(item);
					player.sendMessage(plugin.pre + "Thank you for donating to the Vault.");

					try {
						plugin.SQL.addToVault(item);
					} catch (SQLException e) {
						plugin.log.severe("[Donate] Problem adding to SQL: " + e.getMessage());
					}
					return true;
				}

				if (args.length == 1 && plugin.util.isInt(args[0])) {
					try {
						ItemStack item = player.getItemInHand().clone();
						int am = plugin.util.getInt(args[0]);
                        if (plugin.util.isBlacklisted(Items.itemByStack(item).getName())) {
                            player.sendMessage(plugin.pre + "That Item is blacklisted for donation.");
                            return false;
                        }                        
						if (!(item.getAmount() >= am)) {
							player.sendMessage(plugin.pre + "You don't have enough of that item");
							return false;
						}

						if (!item.getEnchantments().isEmpty()) {
							player.sendMessage(plugin.pre + "Please don't donate enchanted tools and items!");
							return false;
						}
						item.setAmount(am);

						new InventoryManager(player).remove(item);

						player.sendMessage(plugin.pre + "Thank you for donating " + am + " " + Items.itemByStack(item).getName());
						plugin.SQL.addToVault(item);
					} catch (SQLException e) {
						plugin.log.severe("[Donate] Problem adding to SQL: " + e.getMessage());
					}
					return true;
				}

				if (args.length == 1 && args[0].equalsIgnoreCase("help")) {
					plugin.util.help(player);
					return true;
				}

				if (args.length == 2 && plugin.util.isInt(args[1])) {
					try {
                        if (!plugin.util.isMaterial(args[0], player)) {
							return false;
						}

						if (!plugin.util.isInt(args[1])) {
							player.sendMessage(plugin.pre + "Amount specified is not a number, or is invalid");
							return false;
						}
                        if (plugin.util.isBlacklisted(args[0])) {
                            player.sendMessage(plugin.pre + "That Item is blacklisted for donation.");
                            return false;
                        } 

						ItemStack item = Items.itemByName(args[0]).toStack();
						int am = plugin.util.getInt(args[1]);

						item.setAmount(am);
						if (!(new InventoryManager(player).contains(item))) {
							player.sendMessage(plugin.pre + "You don't have enough of that item");
							return false;
						}

						new InventoryManager(player).remove(item);
						player.sendMessage(plugin.pre + "Thank you for donating " + am + " " + Items.itemByStack(item).getName());
						plugin.SQL.addToVault(item);
					} catch (SQLException e) {
						plugin.log.severe("[Donate] Problem adding to SQL: " + e.getMessage());
					}
					return true;
				}
			} 

			if (commandLabel.equalsIgnoreCase("redeem")) {
				if (!plugin.util.getPerm(player, "donate.redeem")) {
                    
                    return false;
                }                
                if (args.length == 2 && !args[0].equalsIgnoreCase("list")) {
					try {
						if (!plugin.util.isMaterial(args[0], player)) {
							return false;
						}

						if (!plugin.util.isInt(args[1])) {
							player.sendMessage(plugin.pre + "Amount specified is not a number, or is invalid");
							return false;
						}
						ItemStack item = Items.itemByString(args[0]).toStack();
						int am = plugin.util.getInt(args[1]);

						ResultSet result = plugin.SQL.selectItem(item);
						if (result.getInt("amount") < am) {
							result.close();
							player.sendMessage(plugin.pre + "Not enough of that item to redeem!");
							return false;
						}
						result.close();

						item.setAmount(am);
						int slotsAvailable = plugin.util.countSlots(player);
						
						int slotsNeeded = (int) am / 64;
						
						if (slotsAvailable < slotsNeeded) {
							
							player.sendMessage(plugin.pre + ChatColor.RED + "You don't have enough inventory space for " + am + " " + Items.itemByStack(item).getName());
							am = slotsAvailable * 64;
							
							item.setAmount(am);
							player.getInventory().addItem(item);
							player.sendMessage(plugin.pre + "Adding " + (slotsAvailable * 64) + " " + Items.itemByStack(item).getName() + " instead");
						} else {
							player.getInventory().addItem(item);
							player.sendMessage(plugin.pre + am + " " + Items.itemByStack(item).getName() + "(s) have been redeemed from the Vault.");
						}
						
						try {

							plugin.SQL.removeFromVault(item, am);
						} catch (SQLException e) {

							plugin.log.severe("[Donate] Problem removing from SQL: " + e.getMessage());
						}
						return true;
					} catch (SQLException e) {

						plugin.log.severe("[Donate] Problem reading from SQL: " + e.getMessage());
						return false;
					}
				}

				if (args.length == 1 && plugin.util.isMaterial(args[0], player)) {
					if (plugin.util.isMaterial(args[0], player)) {
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

							item.setAmount(am);
							player.getInventory().addItem(item);
							player.sendMessage(plugin.pre + am + " " + Items.itemByStack(item).getName() + " redeemed from the Vault");
							
							try {
								plugin.SQL.removeFromVault(item, am);
							} catch (SQLException e) {
								plugin.log.severe("[Donate] Problem removing from SQL: " + e.getMessage());
							}
						} catch (SQLException e) {
							plugin.log.severe("[Donate] Problem reading from SQL: " + e.getMessage());
						}
					}
					return true;
				}

				if (args.length >= 1 && args[0].equalsIgnoreCase("list")) {
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
					return true;
				}
				
				if (args.length == 1 && args[0].equalsIgnoreCase("help")) {
					plugin.util.help(player);
					return true;
				}
			}
            player.sendMessage(plugin.pre + ChatColor.RED + "Invalid command usage");
            plugin.util.help(player);
			return false;
		} else if (sender instanceof ConsoleCommandSender) {
			if (commandLabel.equalsIgnoreCase("vlist")) {
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
				return true;
			}
		}
		return false;
	}
}