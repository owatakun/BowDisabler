/*
 * @author     ucchy
 * @license    GPLv3
 * @copyright  Copyright ucchy 2013
 */
package com.github.ucchyocean.bd;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * @author ucchy
 * 弓の使用を抑制するプラグイン
 */
public class BowDisabler extends JavaPlugin implements Listener {

    private boolean isEnable = true;

    /**
     * BowDisablerプラグインが有効になったときに呼び出されるメソッド
     * @see org.bukkit.plugin.java.JavaPlugin#onEnable()
     */
    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
    }

    /**
     * @see org.bukkit.plugin.java.JavaPlugin#onCommand(org.bukkit.command.CommandSender, org.bukkit.command.Command, java.lang.String, java.lang.String[])
     */
    public boolean onCommand(
            CommandSender sender, Command command, String label, String[] args) {

        if ( args.length >= 1 ) {
            if ( args[0].equalsIgnoreCase("on") ) {
                isEnable = true;
                sender.sendMessage("弓の使用が無効になりました。");
                return true;
            } else if ( args[0].equalsIgnoreCase("off") ) {
                isEnable = false;
                sender.sendMessage("弓の使用が可能になりました。");
                return true;
            }
        }
        return false;
    }

    /**
     * クライアントが何かキーを押したイベント
     * @param event
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if ( !isEnable ) {
            return;
        }
        Player player = event.getPlayer();
        if ( player.getItemInHand() != null &&
                player.getItemInHand().getType() == Material.BOW &&
                (event.getAction() == Action.RIGHT_CLICK_AIR ||
                 event.getAction() == Action.RIGHT_CLICK_BLOCK) ) {
            //player.sendMessage("弓なんてひかせねーぜ！(＊へωへ＊)");
            event.setCancelled(true);
        }
    }
}
