
package me.Sk8r2K10.Donate;

import java.util.logging.Logger;
import me.Sk8r2K10.Donate.util.ButtonListener;
import me.Sk8r2K10.Donate.util.SQLHandler;
import me.Sk8r2K10.Donate.util.Utilities;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class Donate extends JavaPlugin {
    
    public Location chestlocation;
	public final String pre = ChatColor.GRAY + "[" + ChatColor.GREEN + "Donate" + ChatColor.GRAY + "] " + ChatColor.GREEN; 
    public static final Logger log = Logger.getLogger("Minecraft");
    
    public SQLHandler SQL = new SQLHandler(this);
    public Utilities util = new Utilities(this);
	
	public boolean listen = false;
    
    @Override
    public void onDisable() {
        
    }
    
    @Override
    public void onEnable() {
        
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            
            log.severe("[Donate] Vault is missing! Disabling plugin.");
            this.getServer().getPluginManager().disablePlugin(this);
        }
        getConfig().options().copyDefaults(true);
		saveConfig();
		
		setupChestloc();
		SQL.loadSQLt();
		
		getCommand("deposit").setExecutor(new DonateCommand(this));
		getCommand("redeem").setExecutor(new DonateCommand(this));
		getCommand("vlist").setExecutor(new DonateCommand(this));
		if (listen) getServer().getPluginManager().registerEvents(new ButtonListener(this), this);
    }
	
    public void setupChestloc() {
        
        Server server = this.getServer();
        FileConfiguration config = this.getConfig();
        double chestx = config.getDouble("Donate.chestlocation.x");
        double chesty = config.getDouble("Donate.chestlocation.y");
        double chestz = config.getDouble("Donate.chestlocation.z");
        
        World world = server.getWorld(config.getString("Donate.chestlocation.world"));
        chestlocation = new Location(world, chestx, chesty, chestz);
        
        if (chestlocation.getBlock().getType() != Material.CHEST) {
            
            log.warning("[Donate] Chest location does not contain a chest block");
            log.warning("[Donate] Please set up a chest location");
            return;
        }
		listen = true;
    }
}
