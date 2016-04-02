package me.tmods.serveraddons;

import java.io.File;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import me.tmods.serverutils.Methods;

public class DoubleJump extends JavaPlugin implements Listener{
	public static File maincfgfile = new File("plugins/TModsServerUtils", "config.yml");
	public static FileConfiguration maincfg = YamlConfiguration.loadConfiguration(maincfgfile);
	@Override
	public void onEnable() {
		Bukkit.getPluginManager().registerEvents(this, this);
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		try {
		if (event.getPlayer().hasPermission("ServerAddons.denyFallDamage") && !maincfg.getBoolean("allowFallDamage")) {
			event.getPlayer().setFallDistance(0f);
		}
		if (event.getPlayer().getGameMode() != GameMode.CREATIVE && event.getPlayer().isFlying() && event.getPlayer().hasPermission("ServerAddons.doubleJump") && maincfg.getBoolean("allowDoubleJump")) {
			if (maincfg.getBoolean("twoStageDoubleJump")) {
				Vector v1 = new Vector(0,maincfg.getDouble("doubleJumpUp"),0).multiply(2);
				event.getPlayer().setVelocity(v1);
				Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
					@Override
					public void run() {
						Vector v2 = event.getPlayer().getLocation().getDirection().multiply(maincfg.getDouble("doubleJumpForward")).setY(maincfg.getDouble("doubleJumpUp") / 2);
						event.getPlayer().setVelocity(v2);
					}
				},10);
			} else {
				Vector v = event.getPlayer().getLocation().getDirection().multiply(maincfg.getDouble("doubleJumpForward")).setY(maincfg.getDouble("doubleJumpUp"));
				event.getPlayer().setVelocity(v);
			}
			Methods.playEffect(event.getPlayer().getLocation(), "Cloud", 0.5f, 200, false);
			Methods.playSound("Enderdragon_Flap", event.getPlayer().getLocation(), event.getPlayer());
			event.getPlayer().setAllowFlight(false);
		}
		if (event.getPlayer().getGameMode() != GameMode.CREATIVE && maincfg.getBoolean("allowDoubleJump") && !event.getPlayer().getAllowFlight() && event.getPlayer().hasPermission("ServerAddons.doubleJump")) {
			Methods.playEffect(event.getPlayer().getLocation(), "Firework_Spark", 0, 1, false);
		}
		if (event.getPlayer().isOnGround() && event.getPlayer().hasPermission("ServerAddons.doubleJump") && maincfg.getBoolean("allowDoubleJump")) {
			event.getPlayer().setAllowFlight(true);
		}
		} catch(Exception e) {
			Methods.log(e);
		}
	}
}
