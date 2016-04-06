package me.tmods.serveraddons;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import me.tmods.serverutils.Methods;

public class DoubleJump extends JavaPlugin implements Listener{
	public static File maincfgfile = new File("plugins/TModsServerUtils", "config.yml");
	public static FileConfiguration maincfg = YamlConfiguration.loadConfiguration(maincfgfile);
	public static File maindatafile = new File("plugins/TModsServerUtils", "data.yml");
	public static FileConfiguration maindata = YamlConfiguration.loadConfiguration(maindatafile);
	public List<Player> jumping = new ArrayList<Player>();
	@Override
	public void onEnable() {
		Bukkit.getPluginManager().registerEvents(this, this);
		List<String> uuids = maindata.getStringList("DoubleJump.players");
		if (uuids.size() > 0) {
			for (String s:uuids) {
				if (Bukkit.getPlayer(UUID.fromString(s)) != null) {
					jumping.add(Bukkit.getPlayer(UUID.fromString(s)));
				}
			}
		}
		if (Bukkit.getOnlinePlayers().size() > 0) {
			for (Player p:Bukkit.getOnlinePlayers()) {
				if (!jumping.contains(p)) {
					p.setAllowFlight(false);
				}
			}
		}
		if (!Bukkit.getAllowFlight()) {
			Methods.print("You should enable allowFlight in your server.properties to make doublejump work correctly", false, ChatColor.RED + "");
		}
	}
	@Override
	public void onDisable() {
		List<String> uuids = new ArrayList<String>();
		if (jumping.size() > 0) {
			for (Player p:jumping) {
				uuids.add(p.getUniqueId().toString());
			}
		}
		maindata.set("DoubleJump.players", uuids);
		try {
			maindata.save(maindatafile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		try {
		if (event.getPlayer().hasPermission("ServerAddons.denyFallDamage") && !maincfg.getBoolean("allowFallDamage")) {
			event.getPlayer().setFallDistance(0f);
		}
		if (jumping.contains(event.getPlayer()) && event.getPlayer().isFlying() && event.getPlayer().hasPermission("ServerAddons.doubleJump") && maincfg.getBoolean("allowDoubleJump")) {
			event.getPlayer().setFlying(false);
			event.getPlayer().setAllowFlight(false);
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
		}
		if (maincfg.getBoolean("allowDoubleJump") && !event.getPlayer().getAllowFlight() && event.getPlayer().hasPermission("ServerAddons.doubleJump") && jumping.contains(event.getPlayer())) {
			Methods.playEffect(event.getPlayer().getLocation(), "Firework_Spark", 0, 1, false);
		}
		if (jumping.contains(event.getPlayer()) && event.getPlayer().getLocation().add(0,-1,0).getBlock().getType().isSolid() && event.getPlayer().hasPermission("ServerAddons.doubleJump") && maincfg.getBoolean("allowDoubleJump")) {
			event.getPlayer().setAllowFlight(true);
		}
		} catch(Exception e) {
			Methods.log(e);
		}
	}
	/*
	@EventHandler
	public void onFlyToggle(PlayerToggleFlightEvent event) {
		if (jumping.contains(event.getPlayer()) && event.isFlying() && event.getPlayer().hasPermission("ServerAddons.doubleJump") && maincfg.getBoolean("allowDoubleJump")) {
			event.getPlayer().setFlying(false);
			event.getPlayer().setAllowFlight(false);
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
		}
	}
	*/
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("toggleJump")) {
			if (sender instanceof Player && sender.hasPermission("ServerAddons.doubleJump")) {
				if (jumping.contains(sender)) {
					jumping.remove((Player)sender);
					if (((Player) sender).getGameMode() != GameMode.CREATIVE && ((Player) sender).getGameMode() != GameMode.SPECTATOR) {
						((Player) sender).setAllowFlight(false);
					}
				} else {
					jumping.add((Player)sender);
					((Player) sender).setAllowFlight(true);
				}
				sender.sendMessage("Your DoubleJump mode was set to " + jumping.contains((Player)sender));
				return true;
			} else {
				sender.sendMessage(Methods.getLang("permdeny"));
				return true;
			}
		}
		return false;
	}
}
