package ca.munchdev.simpledeathtax.listeners;

import ca.munchdev.simpledeathtax.SimpleDeathTax;
import ca.munchdev.simpledeathtax.utils.ChatUtils;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PlayerDeathListener implements Listener {
    private SimpleDeathTax plugin;

    public PlayerDeathListener(SimpleDeathTax plugin){
        this.plugin = plugin;

        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    private void onPlayerDeath(PlayerDeathEvent event){
        Player player = event.getEntity();

        String taxReceiver = plugin.getConfig().getString("taxReceiver");

        if(player.isDead()) {
            // player.getKiller();
            // If it is null, they were not killed by a player, otherwise they were killed by a player
            if (player.getKiller() != null) {
                // Killed by a player

                if(taxReceiver == null){
                    ChatUtils.sendError("[SimpleDeathTax] Error: The taxReceiver category of the config.yml file is not properly set. Please set it to either KILLER or SERVER!");
                    return;
                }

                if(taxReceiver.equalsIgnoreCase("SERVER")){
                    ConfigurationSection deathTaxes = plugin.getConfig().getConfigurationSection("deathTaxes");

                    String taxType;
                    float taxValue = 0;

                    //      Permission, Weight
                    HashMap<String, Integer> permissionWeights = new HashMap<>();

                    for(String key : deathTaxes.getKeys(false)) {
                        String keyPermission = key.replace(";", ".");

                        if(player.hasPermission(keyPermission)) {
                            ConfigurationSection deathTaxInfo = plugin.getConfig().getConfigurationSection("deathTaxes." + key);

                            permissionWeights.put(key, plugin.getConfig().getInt("deathTaxes." + key + ".weight"));
                        }
                    }

                    Integer topWeight = 0;
                    Integer count = 0;
                    String topPerm = "";

                    ArrayList<String> equalWeightPermissions = new ArrayList<String>();

                    if(permissionWeights.size() != 0){
                        for(Map.Entry<String, Integer> entry : permissionWeights.entrySet()){
                            String permission = entry.getKey();
                            Integer weight = entry.getValue();

                            if(count == 0){
                                topWeight = weight;
                                topPerm = permission;
                            } else {
                                if(weight > topWeight) {
                                    topWeight = weight;
                                    topPerm = permission;

                                    if(equalWeightPermissions.size() != 0){
                                        equalWeightPermissions.clear();
                                    }
                                } else if (weight.equals(topWeight)){
                                    if(equalWeightPermissions.size() == 0) {
                                        equalWeightPermissions.add(topPerm);
                                        equalWeightPermissions.add(permission);
                                    } else {
                                        equalWeightPermissions.add(permission);
                                    }
                                }
                            }

                            count++;
                        }

                        if(equalWeightPermissions.size() != 0){
                            ChatUtils.sendError("[SimpleDeathTax] Error: There is a duplicated weight in the config file. Please ensure that each tax bracket has its own unique weight value to tax players who possess multiple perms.");
                            return;
                        }

                        ConfigurationSection permissionSection = plugin.getConfig().getConfigurationSection("deathTaxes." + topPerm);

                        // In here, this means they have the given permission and now we need to do the same logic as below, checking if they get a rewards and giving it.

                        taxType = permissionSection.getString("taxType");

                        String tempTaxVal = permissionSection.getString("taxValue");

                        if (tempTaxVal != null) {
                            taxValue = Float.parseFloat(tempTaxVal);
                        } else {
                            ChatUtils.sendError("[SimpleDeathTax] Error: The taxValue section of the config is undefined for the permission: " + topPerm);
                            return;
                        }

                        double currentBal = SimpleDeathTax.getEconomy().getBalance(player);

                        if (taxType.equalsIgnoreCase("PERCENTAGE")) {
                            double amountToRemove = currentBal * (taxValue / 100);

                            withdrawPlayerMoney(player, amountToRemove);
                        } else if (taxType.equalsIgnoreCase("AMOUNT")) {
                            double amountToRemove = taxValue;

                            if ((currentBal - amountToRemove) < 0){
                                amountToRemove = currentBal;
                            }

                            withdrawPlayerMoney(player, amountToRemove);
                        } else {
                            ChatUtils.sendError("[LoginStreak] ERROR: Tax Type was not set correctly for permission: " + topPerm);
                            return;
                        }
                    }
                } else if (taxReceiver.equalsIgnoreCase("KILLER")){
                    if (plugin.getConfig().getString("taxReceivedMoney").equals("-1")){
                        // The killer gets the money through the same system as the server.

                        ConfigurationSection deathTaxes = plugin.getConfig().getConfigurationSection("deathTaxes");

                        String taxType;
                        float taxValue = 0;

                        //      Permission, Weight
                        HashMap<String, Integer> permissionWeights = new HashMap<>();

                        for(String key : deathTaxes.getKeys(false)) {
                            String keyPermission = key.replace(";", ".");

                            if(player.hasPermission(keyPermission)) {
                                ConfigurationSection deathTaxInfo = plugin.getConfig().getConfigurationSection("deathTaxes." + key);

                                permissionWeights.put(key, plugin.getConfig().getInt("deathTaxes." + key + ".weight"));
                            }
                        }

                        Integer topWeight = 0;
                        Integer count = 0;
                        String topPerm = "";

                        ArrayList<String> equalWeightPermissions = new ArrayList<String>();

                        if(permissionWeights.size() != 0){
                            for(Map.Entry<String, Integer> entry : permissionWeights.entrySet()){
                                String permission = entry.getKey();
                                Integer weight = entry.getValue();

                                if(count == 0){
                                    topWeight = weight;
                                    topPerm = permission;
                                } else {
                                    if(weight > topWeight) {
                                        topWeight = weight;
                                        topPerm = permission;

                                        if(equalWeightPermissions.size() != 0){
                                            equalWeightPermissions.clear();
                                        }
                                    } else if (weight.equals(topWeight)){
                                        if(equalWeightPermissions.size() == 0) {
                                            equalWeightPermissions.add(topPerm);
                                            equalWeightPermissions.add(permission);
                                        } else {
                                            equalWeightPermissions.add(permission);
                                        }
                                    }
                                }

                                count++;
                            }

                            if(equalWeightPermissions.size() != 0){
                                ChatUtils.sendError("[SimpleDeathTax] Error: There is a duplicated weight in the config file. Please ensure that each tax bracket has its own unique weight value to tax players who possess multiple perms.");
                                return;
                            }

                            ConfigurationSection permissionSection = plugin.getConfig().getConfigurationSection("deathTaxes." + topPerm);

                            // In here, this means they have the given permission and now we need to do the same logic as below, checking if they get a rewards and giving it.

                            taxType = permissionSection.getString("taxType");

                            String tempTaxVal = permissionSection.getString("taxValue");

                            if (tempTaxVal != null) {
                                taxValue = Float.parseFloat(tempTaxVal);
                            } else {
                                ChatUtils.sendError("[SimpleDeathTax] Error: The taxValue section of the config is undefined for the permission: " + topPerm);
                                return;
                            }

                            double currentBal = SimpleDeathTax.getEconomy().getBalance(player);

                            if (taxType.equalsIgnoreCase("PERCENTAGE")) {
                                double amountToRemove = currentBal * (taxValue / 100);

                                withdrawPlayerMoney(player, amountToRemove);
                                depositPlayerMoney(player.getKiller(), amountToRemove);
                            } else if (taxType.equalsIgnoreCase("AMOUNT")) {
                                double amountToRemove = taxValue;

                                if ((currentBal - amountToRemove) < 0){
                                    amountToRemove = currentBal;
                                }

                                withdrawPlayerMoney(player, amountToRemove);
                                depositPlayerMoney(player.getKiller(), amountToRemove);
                            } else {
                                ChatUtils.sendError("[LoginStreak] ERROR: Tax Type was not set correctly for permission: " + topPerm);
                                return;
                            }
                        }
                    } else {
                        // The killer gets the special tax rate.

                        String tempTaxVal = plugin.getConfig().getString("taxReceivedMoney");
                        double taxValue = 0;

                        if (tempTaxVal != null) {
                            taxValue = Float.parseFloat(tempTaxVal);
                        } else {
                            ChatUtils.sendError("[SimpleDeathTax] Error: The taxValue section of the config is undefined for the killer.");
                            return;
                        }

                        double currentBal = SimpleDeathTax.getEconomy().getBalance(player);
                        double amountToRemove = currentBal * (taxValue / 100);


                        withdrawPlayerMoney(player, amountToRemove);
                        depositPlayerMoney(player.getKiller(), amountToRemove);
                    }
                } else {
                    ChatUtils.sendError("[SimpleDeathTax] Error: The taxReceiver category of the config.yml file is not properly set. Please set it to either KILLER or SERVER!");
                    return;
                }
            } else {
                // Killed by something other than a player
                ConfigurationSection deathTaxes = plugin.getConfig().getConfigurationSection("deathTaxes");

                String taxType;
                float taxValue = 0;

                //      Permission, Weight
                HashMap<String, Integer> permissionWeights = new HashMap<>();

                for(String key : deathTaxes.getKeys(false)) {
                    String keyPermission = key.replace(";", ".");

                    if(player.hasPermission(keyPermission)) {
                        ConfigurationSection deathTaxInfo = plugin.getConfig().getConfigurationSection("deathTaxes." + key);

                        permissionWeights.put(key, plugin.getConfig().getInt("deathTaxes." + key + ".weight"));
                    }
                }

                Integer topWeight = 0;
                Integer count = 0;
                String topPerm = "";

                ArrayList<String> equalWeightPermissions = new ArrayList<String>();

                if(permissionWeights.size() != 0){
                    for(Map.Entry<String, Integer> entry : permissionWeights.entrySet()){
                        String permission = entry.getKey();
                        Integer weight = entry.getValue();

                        if(count == 0){
                            topWeight = weight;
                            topPerm = permission;
                        } else {
                            if(weight > topWeight) {
                                topWeight = weight;
                                topPerm = permission;

                                if(equalWeightPermissions.size() != 0){
                                    equalWeightPermissions.clear();
                                }
                            } else if (weight.equals(topWeight)){
                                if(equalWeightPermissions.size() == 0) {
                                    equalWeightPermissions.add(topPerm);
                                    equalWeightPermissions.add(permission);
                                } else {
                                    equalWeightPermissions.add(permission);
                                }
                            }
                        }

                        count++;
                    }

                    if(equalWeightPermissions.size() != 0){
                        ChatUtils.sendError("[SimpleDeathTax] Error: There is a duplicated weight in the config file. Please ensure that each tax bracket has its own unique weight value to tax players who possess multiple perms.");
                        return;
                    }

                    ConfigurationSection permissionSection = plugin.getConfig().getConfigurationSection("deathTaxes." + topPerm);

                    // In here, this means they have the given permission and now we need to do the same logic as below, checking if they get a rewards and giving it.

                    taxType = permissionSection.getString("taxType");

                    String tempTaxVal = permissionSection.getString("taxValue");

                    if (tempTaxVal != null) {
                        taxValue = Float.parseFloat(tempTaxVal);
                    } else {
                        ChatUtils.sendError("[SimpleDeathTax] Error: The taxValue section of the config is undefined for the permission: " + topPerm);
                        return;
                    }

                    double currentBal = SimpleDeathTax.getEconomy().getBalance(player);

                    if (taxType.equalsIgnoreCase("PERCENTAGE")) {
                        double amountToRemove = currentBal * (taxValue / 100);

                        withdrawPlayerMoney(player, amountToRemove);
                    } else if (taxType.equalsIgnoreCase("AMOUNT")) {
                        double amountToRemove = taxValue;

                        if ((currentBal - amountToRemove) < 0) {
                            amountToRemove = currentBal;
                        }

                        withdrawPlayerMoney(player, amountToRemove);
                    } else {
                        ChatUtils.sendError("[LoginStreak] ERROR: Tax Type was not set correctly for permission: " + topPerm);
                        return;
                    }
                }
            }
        }
    }

    private boolean withdrawPlayerMoney(Player p, double amount){
        EconomyResponse r = SimpleDeathTax.getEconomy().withdrawPlayer(p, amount);

        if (r.transactionSuccess()) {
            // Main.getEconomy().format(r.amount)
            if (!plugin.getConfig().getString("deathMoneyLostMessage").equals("")) {
                String message = plugin.getConfig().getString("deathMoneyLostMessage");
                message = message.replace("%amount%", SimpleDeathTax.getEconomy().format(r.amount));
                message = ChatUtils.parseColourCodes(message);
                p.sendMessage(message);
            }

            return true;
        } else {
            p.sendMessage(String.format("An error occured: %s", r.errorMessage));
            return false;
        }
    }

    private boolean depositPlayerMoney(Player p, double amount){
        EconomyResponse r = SimpleDeathTax.getEconomy().depositPlayer(p, amount);

        if (r.transactionSuccess()) {
            // Main.getEconomy().format(r.amount)
            return true;
        } else {
            p.sendMessage(String.format("An error occured: %s", r.errorMessage));
            return false;
        }
    }
}
