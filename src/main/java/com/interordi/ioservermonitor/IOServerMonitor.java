package com.interordi.ioservermonitor;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.interordi.ioservermonitor.utilities.Lag;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;


public class IOServerMonitor extends JavaPlugin {

	public DataAccess data;
	public StatusMonitor monitor;
	int lagTask;
	private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	private static ScheduledFuture<?> updaterHandle;


	public void onEnable() {
		//Always ensure we've got a copy of the config in place (does not overwrite existing)
		this.saveDefaultConfig();

		boolean enable = true;

		//Configuration file use (config.yml): http://wiki.bukkit.org/Configuration_API_Reference
		String dbHost = this.getConfig().getString("database.host", null);
		int dbPort = this.getConfig().getInt("database.port", 3306);
		String dbUsername = this.getConfig().getString("database.username", null);
		String dbPassword = this.getConfig().getString("database.password", null);
		String dbBase = this.getConfig().getString("database.base", null);

		//Old config format
		if (dbHost == null)
			dbHost = this.getConfig().getString("mysql.server");
		if (dbUsername == null)
			dbUsername = this.getConfig().getString("mysql.username");
		if (dbPassword == null)
			dbPassword = this.getConfig().getString("mysql.password");
		if (dbBase == null)
			dbBase = this.getConfig().getString("mysql.base");
		
		String serverId = this.getConfig().getString("server-id");
		if (this.getConfig().contains("enable"))
			enable = this.getConfig().getBoolean("enable");
		
		//Save every minute
		if (enable) {
			data = new DataAccess(this, dbHost, dbPort, dbUsername, dbPassword, dbBase, serverId);
			if (!data.init()) {
				Bukkit.getLogger().severe("---------------------------------");
				Bukkit.getLogger().severe("Failed to initialize the database");
				Bukkit.getLogger().severe("Make sure to configure config.yml");
				Bukkit.getLogger().severe("---------------------------------");
				enable = false;
				return;
			}
			
			monitor = new StatusMonitor(this, data);
			
			lagTask = getServer().getScheduler().scheduleSyncRepeatingTask(this, new Lag(), 100L, 1L);

			//Use Java scheduler instead of Bukkit so the data keeps being saved despite the server slowing down
			updaterHandle = scheduler.scheduleAtFixedRate(monitor, 60, 60, TimeUnit.SECONDS);
		}
		
		getLogger().info("IOServerMonitor enabled");
	}
	
	
	public void onDisable() {
		if (updaterHandle != null)
			updaterHandle.cancel(true);
		getServer().getScheduler().cancelTask(lagTask);
		getLogger().info("IOServerMonitor disabled");
	}
	
	
}
