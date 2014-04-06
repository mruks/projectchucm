package io.github.mruks.projectchucm;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class ProjectChucm extends JavaPlugin {
	private static Player mruks;
	private static Server server;
	private static Player[] players;

	private static final String[] TEAM_COLORS = {
		"black", "dark_blue", "dark_green", "dark_aqua", "dark_red",
		"dark_purple", "gold", "gray", "dark_gray", "blue", "green",
		"aqua", "red", "light_purple", "yellow", "white"
	};

	@Override
	public void onEnable() {
		getLogger().info("onEnable has been invoked!");
		ConsoleCommandSender console = Bukkit.getConsoleSender();
		Bukkit.dispatchCommand(console, "gamerule doDaylightCycle false");
		Bukkit.dispatchCommand(console, "difficulty peaceful");
		Bukkit.dispatchCommand(console, "gamemode adventure");
		Bukkit.dispatchCommand(console, "scoreboard objectives add sb health");
		Bukkit.dispatchCommand(console, "scoreboard objectives setdisplay list sb");
		players = Bukkit.getServer().getOnlinePlayers();
		mruks = null;
		for (Player player : players) {
			player.setFlying(true);
			if (player.getName().equalsIgnoreCase("mruks")) {
				mruks = player;
				mruks.setOp(true);
			}
		}
	}
 
	@Override
	public void onDisable() {
		getLogger().info("onDisable has been invoked!");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {
//		Bukkit.dispatchCommand(console, "time set day");
//		Bukkit.dispatchCommand(console, "gamerule doDaylightCycle true");
//		Bukkit.dispatchCommand(console, "gamerule naturalRegeneration false");
//		Bukkit.dispatchCommand(console, "difficulty hard");
//		Bukkit.dispatchCommand(console, "gamemode survival");
//		Bukkit.dispatchCommand(console, "scoreboard teams add <name>");
//		Bukkit.dispatchCommand(console, "scoreboard teams option color <color>");
//		Bukkit.dispatchCommand(console, "scoreboard teams join <name> @r[c=2,team=]");
//		Bukkit.dispatchCommand(console, "spread 0 0 100 500 true");
		String cmd = command.getName().toLowerCase();
		if (cmd.equals("deop") && args[0].equalsIgnoreCase("mruks")) {
			sender.sendMessage("As a creator of this plugin, MrUks can't be deoped :p");
//		} else if (cmd.equals("")) {
		} else if (cmd.equals("teamup")) {
			if (args.length < 1) {
				sender.sendMessage("missing numberOfTeams parameter!");
			} else {
				int teams = 0;
				boolean isParseable = true;
				try {
					teams = Integer.parseInt(args[0]);
				} catch(NumberFormatException e) {
					isParseable = false;
				}
				if (isParseable) {
					if (teams < 2) {
						sender.sendMessage("You need at least 2 teams to have a good UHC!");
					} else if (teams == players.length) {
						sender.sendMessage("If everyone is alone, no teams are needed ;)");
					} else if (teams > players.length) {
						sender.sendMessage("You can't have more teams than players!");
					} else {
					}
				} else {
					sender.sendMessage("NumberOfTeams has to be a valid number!");
				}
			}
		}
		return super.onCommand(sender, command, label, args);
	}
}