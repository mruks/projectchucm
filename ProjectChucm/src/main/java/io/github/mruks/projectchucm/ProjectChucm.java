package io.github.mruks.projectchucm;

import java.util.ArrayList;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class ProjectChucm extends JavaPlugin {
	private Player mruks;
	private ConsoleCommandSender console;
	private Player[] players;
	private Random rand = new Random();

	private static final String[] TEAM_COLORS = {
		"black", "dark_blue", "dark_green", "dark_aqua", "dark_red",
		"dark_purple", "gold", "gray", "dark_gray", "blue", "green",
		"aqua", "red", "light_purple", "yellow", "white"
	};

	@Override
	public void onEnable() {
		getLogger().info("onEnable has been invoked!");
		console = Bukkit.getConsoleSender();
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
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
//		Bukkit.dispatchCommand(console, "time set day");
//		Bukkit.dispatchCommand(console, "gamerule doDaylightCycle true");
//		Bukkit.dispatchCommand(console, "gamerule naturalRegeneration false");
//		Bukkit.dispatchCommand(console, "difficulty hard");
//		Bukkit.dispatchCommand(console, "gamemode survival");
//		Bukkit.dispatchCommand(console, "spread 0 0 100 500 true");
		String cmd = command.getName().toLowerCase();
		if (cmd.equals("deop") && args[0].equalsIgnoreCase("mruks")) {
			sender.sendMessage("As a creator of this plugin, MrUks can't be deoped :p");
//		} else if (cmd.equals("")) {
		} else if (cmd.equals("teamup")) {
			if (args.length < 1) {
				sender.sendMessage("missing numberOfTeams parameter");
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
						sender.sendMessage("You need at least 2 teams to have a good UHC");
					} else if (teams == players.length) {
						sender.sendMessage("If everyone is alone, no teams are needed ;)");
					} else if (teams > 16) {
						sender.sendMessage("You can't have more teams than colors(16) available");
					} else if (teams > players.length) {
						sender.sendMessage("You can't have more teams than players");
					} else {
						int playersPerTeam = players.length / teams;
						int rest = players.length % teams;
						ArrayList<String> colors = new ArrayList<>();
						for (int i = 1; i < teams; i++) {
							Bukkit.dispatchCommand(console, "scoreboard teams add team" + i);
							Bukkit.dispatchCommand(console, "scoreboard teams option color "+getRandomColor(colors));
							Bukkit.dispatchCommand(console, "scoreboard teams join team" + i + " @r[c=" + playersPerTeam + ",team=]");
						}
						if (rest < (playersPerTeam/2)) {
							for (int i = 0; i < rest; i++) {
								Bukkit.dispatchCommand(console, "scoreboard teams join team" + rand.nextInt(teams) + " @r[c=1,team=]");
							}
						} else {
							Bukkit.dispatchCommand(console, "scoreboard teams add team" + teams);
							Bukkit.dispatchCommand(console, "scoreboard teams option color "+getRandomColor(colors));
							Bukkit.dispatchCommand(console, "scoreboard teams join team" + teams + " @r[c=" + rest + ",team=]");
						}
					}
				} else {
					sender.sendMessage("NumberOfTeams has to be a valid number");
				}
			}
		} else if (cmd.equals("genborders")) {
			if (args.length < 1) {
				sender.sendMessage("missing borderLength parameter");
			} else {
				int border = 0;
				boolean isParseable = true;				
				try {
					border = Integer.parseInt(args[0]);
				} catch(NumberFormatException e) {
					isParseable = false;
				}
				if (isParseable) {
					if (border < 50) {
						sender.sendMessage("UHC isn't interesting in a map smaller than 50 by 50");
					} else {
						for (int i = -border; i <= border; i++) {
							for (int j = 0; j < 256; j++) {
								Bukkit.dispatchCommand(console, "setblock " +
							i + " " + j + " " + border + " minecraft:bedrock");
								Bukkit.dispatchCommand(console, "setblock " +
							i + " " + j + " " + (-border) + " minecraft:bedrock");
								Bukkit.dispatchCommand(console, "setblock " +
							border + " " + j + " " + i + " minecraft:bedrock");
								Bukkit.dispatchCommand(console, "setblock " +
							(-border) + " " + j + " " + i + " minecraft:bedrock");
							}
						}
					}
				} else {
					sender.sendMessage("borderLength has to be a valid number");
				}
			}
		} else {
			sender.sendMessage("NumberOfTeams has to be a valid number");
		}
		return super.onCommand(sender, command, label, args);
	}
	
	private String getRandomColor(ArrayList<String> list) {
		boolean found = false;
		String temp = "";
		while (!found) {
			temp = TEAM_COLORS[rand.nextInt(16)];
			found = !list.contains(temp); 
		}
		list.add(temp);
		return temp;
	}
}