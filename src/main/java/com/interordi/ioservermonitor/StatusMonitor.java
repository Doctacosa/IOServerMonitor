package com.interordi.ioservermonitor;

import com.interordi.ioservermonitor.utilities.Lag;

import org.bukkit.Bukkit;
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

		Bukkit.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {

			@Override
			public void run() {
				db.save(tps);
			}
		});
	}
	
}
