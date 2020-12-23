package com.interordi.ioservermonitor;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.interordi.ioservermonitor.utilities.Lag;

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
		String dbServer = this.getConfig().getString("mysql.server");
		String dbUsername = this.getConfig().getString("mysql.username");
		String dbPassword = this.getConfig().getString("mysql.password");
		String dbBase = this.getConfig().getString("mysql.base");
		String serverId = this.getConfig().getString("server-id");
		if (this.getConfig().contains("enable"))
			enable = this.getConfig().getBoolean("enable");
		
		//Save every minute
		if (enable) {
			data = new DataAccess(this, dbServer, dbUsername, dbPassword, dbBase, serverId);
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
