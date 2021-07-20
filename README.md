# IOServerMonitor

![Logo](https://www.interordi.com/images/plugins/ioservermonitor-96.png)

Monitor the status of a Minecraft server by saving it every minute in a MySQL table named `stats_io_servers`. In a Bungee environment, each server can be identified with a different name to get a quick view of the status of your network. The data can then be displayed in a frontend of your choice (not included).

The data stored is similar to that of a server query, which includes the following:
* Number of players online
* Max amount of players allowed
* List of online players
* List of plugins installed

To this are added:
* Current TPS (Ticks Per Second)
* Time of last server start
* Time of the last check

This is designed to let server admins see the status of their network at a glance. If the last check time isn't updating anymore, it can be a useful clue to warn someone that either a server is offline, or it crashed!

Saving this information to a database lets you use multiple clients to check on the results without bogging down the servers themselves with constant queries.


## Installation

1. Download the plugin and place it in your plugins/ directory.
2. Start and stop the server to generate the configuration file.
3. Edit config.yml with your MySQL database information and a unique server ID (string).
4. Start your server. After a minute, the MySQL table will start getting results.


## Configuration

`database.host`: Database host  
`database.port`: Database port  
`database.base`: Database name  
`database.username`: Database username  
`database.password`: Database password  
`server-id`: A unique ID for this server


## Commands

None


## Permissions

None
