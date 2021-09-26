package com.interordi.ioservermonitor;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.time.LocalDateTime;

import org.bukkit.Bukkit;

public class DataAccess {

	@SuppressWarnings("unused")
	private IOServerMonitor plugin;
	private String database = "";
	private String tablePrefix = "stats_io_";
	private String serverId = "";
	

	DataAccess(IOServerMonitor plugin, String dbHost, int dbPort, String dbUsername, String dbPassword, String dbBase, String serverId) {
		this.plugin = plugin;
		this.serverId = serverId;
		
		database = "jdbc:mysql://" + dbHost + ":" + dbPort + "/" + dbBase + "?user=" + dbUsername + "&password=" + dbPassword + "&useSSL=false";
	}


	//Initialize the database
	public boolean init() {

		//Create or update the required database table
		//A failure indicates that the database wasn't configured properly
		Connection conn = null;
		PreparedStatement pstmt = null;
		String query = "";
		
		try {
			conn = DriverManager.getConnection(database);
			
			pstmt = conn.prepareStatement("" +
				"CREATE TABLE IF NOT EXISTS `stats_io_servers` ( " +
				"	`id` varchar(20) NOT NULL, " +
				"	`tps` decimal(4,2) NOT NULL, " +
				"	`nb_players` int(11) NOT NULL DEFAULT 0, " +
				"	`max_players` int(11) NOT NULL DEFAULT 0, " +
				"	`plugins` text NOT NULL DEFAULT '', " +
				"	`players` text NOT NULL DEFAULT '', " +
				"	`last_start` datetime NOT NULL, " +
				"	`last_check` datetime NOT NULL, " +
				"	PRIMARY KEY (`id`) " +
				") ENGINE=InnoDB DEFAULT CHARSET=latin1; "
			);
			pstmt.executeUpdate();


			//Check for the last_start field and add as needed
			pstmt = conn.prepareStatement("" +
				"SELECT * " +
				"FROM information_schema.COLUMNS " +
				"WHERE TABLE_NAME = 'stats_io_servers' " +
				"  AND COLUMN_NAME = 'last_start' "
			);
			ResultSet rs = pstmt.executeQuery();

			if (!rs.next()) {
				pstmt = conn.prepareStatement("" +
					"ALTER TABLE `stats_io_servers` " +
					"CHANGE `tps` `tps` decimal(4,2) NOT NULL DEFAULT '0' AFTER `id`, " +
					"CHANGE `last_check` `last_check` datetime NOT NULL DEFAULT '1970-01-01 00:00:00' AFTER `players`, " +
					"ADD `last_start` datetime NOT NULL DEFAULT '1970-01-01 00:00:00' AFTER `players` "
				);
				pstmt.executeUpdate();
			}


			//Set the server start time
			pstmt = conn.prepareStatement("" +
					"INSERT INTO " + this.tablePrefix + "servers (id, last_start)" + 
					"VALUES (?, ?) " +
					"ON DUPLICATE KEY UPDATE last_start = ?");

			pstmt.setString(1, serverId);
			pstmt.setString(2, LocalDateTime.now().toString());
			
			pstmt.setString(3, LocalDateTime.now().toString());
			
			@SuppressWarnings("unused")
			int res = pstmt.executeUpdate();

		} catch (SQLException ex) {
			Bukkit.getLogger().severe("Query: " + query);
			Bukkit.getLogger().severe("SQLException: " + ex.getMessage());
			Bukkit.getLogger().severe("SQLState: " + ex.getSQLState());
			Bukkit.getLogger().severe("VendorError: " + ex.getErrorCode());
			return false;
		}

		return true;
	}
	
	
	//Do this on a separate thread to avoid slowdown
	public void save(double tps, int nbPlayers, int maxPlayers, String plugins, String players) {

		Connection conn = null;
		String query = "";
		LocalDateTime dateTime = LocalDateTime.now();
		
		try {
			conn = DriverManager.getConnection(database);
			
			PreparedStatement pstmt = conn.prepareStatement("" +
					"INSERT INTO " + this.tablePrefix + "servers (id, tps, nb_players, max_players, plugins, players, last_check)" + 
					"VALUES (?, ?, ?, ?, ?, ?, ?) " +
					"ON DUPLICATE KEY UPDATE tps = ?, nb_players = ?, max_players = ?, plugins = ?, players = ?, last_check = ?");

			pstmt.setString(1, serverId);
			pstmt.setDouble(2, tps);
			pstmt.setInt(3, nbPlayers);
			pstmt.setInt(4, maxPlayers);
			pstmt.setString(5, plugins);
			pstmt.setString(6, players);
			pstmt.setString(7, dateTime.toString());
			
			pstmt.setDouble(8, tps);
			pstmt.setInt(9, nbPlayers);
			pstmt.setInt(10, maxPlayers);
			pstmt.setString(11, plugins);
			pstmt.setString(12, players);
			pstmt.setString(13, dateTime.toString());
			
			@SuppressWarnings("unused")
			int res = pstmt.executeUpdate();
			
			pstmt.close();
			conn.close();
			
		} catch (SQLException ex) {
			// handle any errors
			Bukkit.getLogger().severe("Query: " + query);
			Bukkit.getLogger().severe("SQLException: " + ex.getMessage());
			Bukkit.getLogger().severe("SQLState: " + ex.getSQLState());
			Bukkit.getLogger().severe("VendorError: " + ex.getErrorCode());
		}
	}
}
