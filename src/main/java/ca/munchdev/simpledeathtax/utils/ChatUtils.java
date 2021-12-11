package ca.munchdev.simpledeathtax.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class ChatUtils {
    public static String parseColourCodes(String msg){
        msg = ChatColor.translateAlternateColorCodes('&', msg);
        return msg;
    }

    public static boolean sendMessageToPlayer(Player player, String message){
        player.sendMessage(parseColourCodes(message));

        return true;
    }

    public static void sendError(String error){
        Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.DARK_RED + error);
    }
}
