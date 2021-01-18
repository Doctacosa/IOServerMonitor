package com.interordi.ioservermonitor;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import java.time.LocalDateTime;

public class DataAccess {

	 @SuppressWarnings("unused")
	private IOServerMonitor plugin;
	private String database = "";
	private String tablePrefix = "stats_io_";
	private String serverId = "";
	

	DataAccess(IOServerMonitor plugin, String dbServer, String dbUsername, String dbPassword, String dbBase, String serverId) {
		this.plugin = plugin;
		this.serverId = serverId;
		
		database = "jdbc:mysql://" + dbServer + "/" + dbBase + "?user=" + dbUsername + "&password=" + dbPassword + "&useSSL=false";
	}


	//Initialize the database
	public boolean init() {

		//Create the required database table
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
				"	`last_check` datetime NOT NULL, " +
				"	PRIMARY KEY (`id`) " +
				") ENGINE=InnoDB DEFAULT CHARSET=latin1; "
			);
			pstmt.executeUpdate();
		} catch (SQLException ex) {
			System.out.println("Query: " + query);
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
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
			System.out.println("Query: " + query);
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
		}
	}
}
