package me.Sk8r2K10.Donate.util;

import java.sql.SQLException;
import me.Sk8r2K10.Donate.Donate;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;

public class ButtonListener implements Listener {

    private Donate plugin;

    public ButtonListener(Donate instance) {
        this.plugin = instance;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onButtonPress(PlayerInteractEvent e) throws SQLException {
        Block clicked = e.getClickedBlock();

        if (clicked.getType().equals(Material.STONE_BUTTON)) {

            double x = plugin.chestlocation.getX() - clicked.getLocation().getX();
            double y = plugin.chestlocation.getY() - clicked.getLocation().getY();
            double z = plugin.chestlocation.getZ() - clicked.getLocation().getZ();

            double dist = x * x + y * y + z * z;

            if (dist <= 9) {

                Chest chestblock = (Chest) plugin.chestlocation.getBlock().getState();
                Inventory chestinv = chestblock.getBlockInventory();
                
                if (chestinv != null) {
                    if (!plugin.util.isEmpty(chestinv)) {
                        plugin.SQL.addToVault(chestinv.getContents());

                        chestinv.clear();

                        e.getPlayer().sendMessage(plugin.pre + ChatColor.AQUA + "Thank you for donating!");

                    }
                }
            }
        }
    }
}
