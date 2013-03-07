/*
 * @author     ucchy, owatakun
 * @license    GPLv3
 * @copyright  Copyright ucchy 2013
 */
package com.github.ucchyocean.bd;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

/**
 * @author ucchy
 * 弓の使用を抑制するプラグイン
 */
public class BowDisabler extends JavaPlugin implements Listener {

    private List<String> disRegList;

    /**
     * BowDisablerプラグインが有効になったときに呼び出されるメソッド
     * @see org.bukkit.plugin.java.JavaPlugin#onEnable()
     */
    @Override
    public void onEnable() {
        WorldGuardPlugin wg = getWorldGuard();
        if (wg == null) {
            getLogger().severe("WorldGuardがロードされていません");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        getServer().getPluginManager().registerEvents(this, this);
        saveDefaultConfig();
        disRegList = getConfig().getStringList("DisableRegions");
    }

    /**
     * @see org.bukkit.plugin.java.JavaPlugin#onCommand(org.bukkit.command.CommandSender, org.bukkit.command.Command, java.lang.String, java.lang.String[])
     */
    public boolean onCommand(
            CommandSender sender, Command command, String label, String[] args) {

        if ( args.length >= 1 ) {
            if ( args[0].equalsIgnoreCase("on") ) {
                getConfig().set("Enable", true);
                sender.sendMessage("弓の使用が無効になりました。");
                return true;
            } else if ( args[0].equalsIgnoreCase("off") ) {
                getConfig().set("Enable", false);
                sender.sendMessage("弓の使用が可能になりました。");
                return true;
            } else if ( args[0].equalsIgnoreCase("reload") ) {
                reloadConfig();
                disRegList = getConfig().getStringList("DisableRegions");
                sender.sendMessage("設定を再読み込みしました。");
                return true;
            } else if ( args[0].equalsIgnoreCase("world") ) {
                if ( args.length == 2 ) {
                    if ( args[1].equalsIgnoreCase("on") ) {
                        getConfig().set("DisableOverWorld", true);
                        saveConfig();
                        sender.sendMessage("ワールド全体で弓の使用が無効になりました。");
                        return true;
                    } else if ( args[1].equalsIgnoreCase("off") ) {
                        getConfig().set("DisableOverWorld", false);
                        saveConfig();
                        sender.sendMessage("ワールド全体で弓の使用が有効になりました。");
                        return true;
                    }
                }
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
        if ( !getConfig().getBoolean("Enable") ) {
            return;
        }
        Player player = event.getPlayer();
        List<String> locRegList = getWorldGuard().getRegionManager(player.getWorld()).getApplicableRegionsIDs(BukkitUtil.toVector(player.getLocation()));
        boolean insideDisReg = false;
        String lastHitReg = null;
        if (getConfig().getBoolean("DisableOverWorld")) {
            insideDisReg = true;
            lastHitReg = "World";
        }
        wgCheck:
            if (locRegList != null && !insideDisReg) {
                for (String locReg: locRegList) {
                    for (String disReg: disRegList) {
                        if (locReg.equalsIgnoreCase(disReg)) {
                            insideDisReg = true;
                            lastHitReg = disReg;
                            break wgCheck;
                        }
                    }
                }
            }
        if ( player.getItemInHand() != null &&
                player.getItemInHand().getType() == Material.BOW &&
                insideDisReg &&
                (event.getAction() == Action.RIGHT_CLICK_AIR ||
                 event.getAction() == Action.RIGHT_CLICK_BLOCK) ) {
            if (getConfig().getBoolean("PlayerSendMessage")) {
                player.sendMessage("ここでは弓は使用できません");
            }
            if (getConfig().getBoolean("ConsoleAlert")) {
                getLogger().warning(player.getName() + " が " + lastHitReg + " で弓を使用しようとしました");
            }
            event.setCancelled(true);
        }
    }

    /**
     * WorldGuardの呼び出し
     */
    private WorldGuardPlugin getWorldGuard() {
        Plugin plugin = getServer().getPluginManager().getPlugin("WorldGuard");
        if (plugin == null || !(plugin instanceof WorldGuardPlugin)) {
            return null;
        }
        return (WorldGuardPlugin) plugin;
    }
}
