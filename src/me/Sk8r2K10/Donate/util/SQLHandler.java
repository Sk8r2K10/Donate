package me.Sk8r2K10.Donate.util;

import java.sql.ResultSet;
import java.sql.SQLException;
import lib.PatPeter.SQLibrary.SQLite;
import me.Sk8r2K10.Donate.Donate;
import net.milkbowl.vault.item.Items;
import org.bukkit.inventory.ItemStack;

public class SQLHandler {

    public SQLite SQLt;
    private Donate plugin;

    public SQLHandler(Donate instance) {

        this.plugin = instance;
    }

    public void loadSQLt() {

        SQLt = new SQLite(plugin.log, "[Donate]", "Core", plugin.getDataFolder().getPath());

        SQLt.open();
        plugin.log.info("[Donate] SQLite Connection established.");

        String Vault = "CREATE TABLE IF NOT EXISTS `Vault` (`ID` int AUTO_INCREMENT, `item` VARCHAR(255) NOT NULL, `amount` int NOT NULL, PRIMARY KEY(`ID`))";

        SQLt.createTable(Vault);
    }

    public void addToVault(ItemStack[] items) throws SQLException {
        ItemStack item;

        for (int i = 0; i < items.length; i++) {

            item = items[i];

            if (item == null) {
                continue;
            }
            int am = item.getAmount();
            int newam;

            ResultSet result = this.selectItem(item);

            if (result.next()) {

                newam = result.getInt("amount");
				result.close();
				int finam = am + newam;
				
				SQLt.query("UPDATE `Vault` SET `item` = '" + Items.itemByStack(item).getName() + "', `amount` = '" + finam + "' WHERE `item` = '" + Items.itemByStack(item).getName() + "'");
				continue;
            }
			result.close();
			
            SQLt.query("INSERT INTO `Vault` (`item`, `amount`) VALUES ('" + Items.itemByStack(item).getName() + "', '" + am + "')");
        }
    }

    public void addToVault(ItemStack item) throws SQLException {
        int am = item.getAmount();
        int newam = 0;

        ResultSet result = this.selectItem(item);

        if (result.next()){

            newam = result.getInt("amount");
			result.close();
			int finam = am + newam;

			SQLt.query("UPDATE `Vault` SET `item` = '" + Items.itemByStack(item).getName() + "', `amount` = '" + finam + "' WHERE `item` = '" + Items.itemByStack(item).getName() + "'");
			return;
        }
        result.close();

        SQLt.query("INSERT INTO `Vault` (`item`, `amount`) VALUES ('" + Items.itemByStack(item).getName() + "', '" + am + "')");

    }

    public ResultSet getVaultContents() throws SQLException {

        ResultSet result = SQLt.query("SELECT * FROM `Vault` ORDER BY `item` ASC");
        return result;
    }

    public void removeFromVault(ItemStack item, int am) throws SQLException {
        int newam = 0;
		
        ResultSet result = this.selectItem(item);
        if (result.next()) {

            newam = result.getInt("amount");
        }
        result.close();
		
        int finam = newam - am;
		
        if (finam > 0) {
            SQLt.query("UPDATE `Vault` SET `item` = '" + Items.itemByStack(item).getName() + "', `amount` = '" + finam + "' WHERE `item` = '" + Items.itemByStack(item).getName() + "'");
        } else {
            SQLt.query("DELETE FROM `Vault` WHERE `item` = '" + Items.itemByStack(item).getName() + "'");
        }
    }

    public ResultSet selectItem(ItemStack item) throws SQLException {

        ResultSet result = SQLt.query("SELECT * FROM `Vault` WHERE `item` = '" + Items.itemByStack(item).getName() + "'");
        return result;
    }
}