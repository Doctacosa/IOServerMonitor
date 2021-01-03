package com.interordi.ioservermonitor;

import java.util.HashSet;
import java.util.Set;

import com.interordi.ioservermonitor.utilities.Lag;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class StatusMonitor implements Runnable {

	Plugin plugin;
	DataAccess db;

	public StatusMonitor(Plugin plugin, DataAccess db) {
		this.plugin = plugin;
		this.db = db;
	}


	public void run() {
		double tps = Lag.getTPS();
		int maxPlayers = plugin.getServer().getMaxPlayers();
		Set< String > plugins = new HashSet< String >();
		for (Plugin plugin : plugin.getServer().getPluginManager().getPlugins()) {
			plugins.add(plugin.getName());
		}
		Set< String > players = new HashSet< String >();
		for (Player player : plugin.getServer().getOnlinePlayers()) {
			players.add(player.getDisplayName());
		}

		Bukkit.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {

			@Override
			public void run() {
				String myPlugins = plugins.toString().substring(1, plugins.toString().length() - 1);
				String myPlayers = players.toString().substring(1, players.toString().length() - 1);
				db.save(tps, players.size(), maxPlayers, myPlugins, myPlayers);
			}
		});
	}
	
}
