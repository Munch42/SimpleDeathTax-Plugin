package ca.munchdev.simpledeathtax;

import ca.munchdev.simpledeathtax.listeners.PlayerDeathListener;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public final class SimpleDeathTax extends JavaPlugin {

    private static final Logger log = Logger.getLogger("Minecraft");
    private static Economy econ = null;

    private SimpleDeathTax plugin;

    @Override
    public void onEnable() {
        // Plugin startup logic
        plugin = this;

        if (!setupEconomy() ) {
            log.severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        saveDefaultConfig();

        if(setupEconomy()){
            if(!checkEconomy() && getConfig().getBoolean("economy")){
                // In here this will be run if they have vault but no economy plugin. Can add && to the check to see if a option in the plugin.yml is set.
                log.severe(String.format("[%s] - Disabled due to no Economy plugin found!", getDescription().getName()));
                getServer().getPluginManager().disablePlugin(this);
                return;
            }
        }

        new PlayerDeathListener(this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        log.info(String.format("[%s] Disabled Version %s", getDescription().getName(), getDescription().getVersion()));
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        return true;
    }

    private boolean checkEconomy(){
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    public static Economy getEconomy() {
        return econ;
    }

    public SimpleDeathTax getPlugin(){
        return plugin;
    }
}
