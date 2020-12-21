package com.interordi.ioservermonitor;

import com.interordi.ioservermonitor.utilities.Lag;

import org.bukkit.plugin.java.JavaPlugin;


public class IOServerMonitor extends JavaPlugin {

	public DataAccess data;
	public StatusMonitor monitor;


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
			
			getServer().getScheduler().scheduleSyncRepeatingTask(this, new Lag(), 100L, 1L);
			getServer().getScheduler().runTaskTimer(this, monitor, 60*20L, 60*20L);	//Run every minute
		}
		
		getLogger().info("IOServerMonitor enabled");
	}
	
	
	public void onDisable() {
		if (monitor != null)
			monitor.run();	//Save the current data before stopping
		getLogger().info("IOServerMonitor disabled");
	}
	
	
}
