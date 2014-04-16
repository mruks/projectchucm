package io.github.mruks.projectchucm;

import java.util.ArrayList;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public final class ProjectChucm extends JavaPlugin {
	private ConsoleCommandSender console;
	private Random rand = new Random();
	private int border = 0;
	private int plusBorder = 0;
	private int minBorder = 0;
	private World world;
	private int teams = 0;
	private Scoreboard sb;
	private ArrayList<Team> ready = new ArrayList<>();
	private ArrayList<Team> unready = new ArrayList<>();

	private static final String[] TEAM_COLORS = {
		"black", "dark_blue", "dark_green", "dark_aqua", "dark_red",
		"dark_purple", "gold", "gray", "dark_gray", "blue", "green",
		"aqua", "red", "light_purple", "yellow", "white"
	};

	@Override
	public void onEnable() {
		Bukkit.setDefaultGameMode(GameMode.ADVENTURE);
		console = Bukkit.getConsoleSender();
		world = Bukkit.getWorld("world");
		sb = Bukkit.getScoreboardManager().getMainScoreboard();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
//		Bukkit.dispatchCommand(console, "time set day");
//		Bukkit.dispatchCommand(console, "gamerule doDaylightCycle true");
//		Bukkit.dispatchCommand(console, "gamerule naturalRegeneration false");
//		Bukkit.dispatchCommand(console, "difficulty hard");
//		Bukkit.dispatchCommand(console, "gamemode survival");
//		Bukkit.dispatchCommand(console, "spread 0 0 100 500 true");
//		} else if (cmd.equals("")) {
		String cmd = command.getName().toLowerCase();
		if (cmd.equals("prep")) {
			if (args.length < 1) {
				sender.sendMessage("missing borderLength parameter");
			} else {
				if ((sender instanceof Player) && ((Player)sender).isOp()) {
					boolean isParseable = true;				
					try {
						border = Integer.parseInt(args[0]);
					} catch(NumberFormatException e) {
						isParseable = false;
					}
					if (isParseable && border > 100) {
						Bukkit.dispatchCommand(console, "gamerule doDaylightCycle false");
						Bukkit.dispatchCommand(console, "time set 0");
						Bukkit.dispatchCommand(console, "difficulty peaceful");
						Bukkit.dispatchCommand(console, "scoreboard objectives add sb health");
						Bukkit.dispatchCommand(console, "scoreboard objectives setdisplay list sb");
						Bukkit.dispatchCommand(console, "gamemode creative " + sender.getName());
						border += 2;
						minBorder = -(border/2);
						plusBorder = border + minBorder;
						((Player)sender).setFlying(true);
						Bukkit.dispatchCommand(console, "tp " + sender.getName() + " " + minBorder + " 200 " + minBorder);
						sender.sendMessage("now don't move and type /genmap");
					} else {
						sender.sendMessage("border is an invalid number or is to small (i.e. smaller than 100)");
						border = 0;
					}
				}
			}
		} else if (cmd.equals("teamup")) {
			if (args.length < 1) {
				sender.sendMessage("missing numberOfTeams parameter");
			} else {
				int temp = 0;
				boolean isParseable = true;				
				try {
					temp = Integer.parseInt(args[0]);
				} catch(NumberFormatException e) {
					isParseable = false;
					temp = 0;
				}
				if (isParseable) {
					Player[] players = Bukkit.getServer().getOnlinePlayers();
					if (temp < 2) {
						sender.sendMessage("You need at least 2 teams to have a good UHC");
					} else if (temp == players.length) {
						sender.sendMessage("If everyone is alone, no teams are needed ;)");
					} else if (temp > 16) {
						sender.sendMessage("You can't have more teams than colors(16) available");
					} else if (temp > players.length) {
						sender.sendMessage("You can't have more teams than players");
					} else {
						if (teams > 0) {
							ready.clear();
							unready.clear();
							for (int i = 1; i <= teams; i++) {
								Bukkit.dispatchCommand(console, "scoreboard teams empty team" + i);
								Bukkit.dispatchCommand(console, "scoreboard teams remove team" + i);
							}
						}
						teams = temp;
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
							teams--;
						} else {
							Bukkit.dispatchCommand(console, "scoreboard teams add team" + teams);
							Bukkit.dispatchCommand(console, "scoreboard teams option color "+getRandomColor(colors));
							Bukkit.dispatchCommand(console, "scoreboard teams join team" + teams + " @r[c=" + rest + ",team=]");
						}
						unready.addAll(sb.getTeams());
					}
				} else {
					sender.sendMessage("NumberOfTeams has to be a valid number");
				}
			}
		} else if (cmd.equals("genmap")) {
			if ((sender instanceof Player) && ((Player)sender).isOp()) {
				if (border == 0) {
					sender.sendMessage("map hasn't been prepped");
				} else {
					((Player)sender).setFlying(true);
					Bukkit.dispatchCommand(console, "tp " + sender.getName() + " " + minBorder + " 200 " + minBorder);
					if (world.getChunkAt(minBorder, minBorder).isLoaded()) {
						world.loadChunk(minBorder,minBorder);
						for (Player p : Bukkit.getServer().getOnlinePlayers()) {
							Bukkit.dispatchCommand(console, "kick " + p.getName() + " generating borders for UHC is very laginducing, you'll only make it worse :p\nPlease don't rejoin the server until it succesfully termimnates itself!");
						}
						console.sendMessage("Starting border generation: ...");
						int percent[] = {0,border*1024};
//						//terain gen
//						for (int i = minBorder; i <= plusBorder; i+=16) loadBorders(i,minBorder,percent);
//						for (int i = minBorder; i <= plusBorder; i+=16) loadBorders(plusBorder,i,percent);
//						for (int i = plusBorder; i >= minBorder; i-=16) loadBorders(i,plusBorder,percent);
//						for (int i = plusBorder; i >= minBorder; i-=16) loadBorders(minBorder,i,percent);
						//gen actual borders
						for (int i = minBorder; i <= plusBorder; i++) setBorders(i, minBorder, percent);
						for (int i = minBorder; i <= plusBorder; i++) setBorders(plusBorder,i, percent);
						for (int i = plusBorder; i >= minBorder; i--) setBorders(i, plusBorder, percent);
						for (int i = plusBorder; i >= minBorder; i--) setBorders(minBorder,i, percent);
						console.sendMessage("Server needs to restart");
						Bukkit.dispatchCommand(console, "stop");
					} else {
						sender.sendMessage("tp to " + minBorder + " 100 " + minBorder + "in order for this command to work! That chunk needs to be loaded!");
					}
				}
			} else {
				sender.sendMessage("This command has to be performed by a logged on OP in order to work");
			}
		} else if (cmd.equals("ready")) {
			if (sender instanceof Player) {
				Player player = (Player)sender;
				Team team = null;
				for (Team temp : unready) {
					if (temp.getPlayers().contains(player)) {
						unready.remove(temp);
						ready.add(temp);
						team = temp;
						break;
					}
				}
				if (team == null) sender.sendMessage("Your team is already ready, or you're not in a team");
				else console.sendMessage(team.getDisplayName() + " is now ready");
			} else {
				sender.sendMessage("This command has to be performed by a player in order to work");
			}
		} else if (cmd.equals("unready")) {
			if (sender instanceof Player) {
				Player player = (Player)sender;
				Team team = null;
				for (Team temp : ready) {
					if (temp.getPlayers().contains(player)) {
						ready.remove(temp);
						unready.add(temp);
						team = temp;
						break;
					}
				}
				if (team == null) sender.sendMessage("Your team wasn't yet ready, or you're not in a team");
				else console.sendMessage(team.getDisplayName() + " wussed out");
			} else {
				sender.sendMessage("This command has to be performed by a player in order to work");
			}
		} else if (cmd.equals("timer")) {
		}
		return super.onCommand(sender, command, label, args);
	}
	
//	private void loadBorders(int x, int z, int[] percent) {
//		world.loadChunk(x,z,true);
//		world.refreshChunk(x,z);
//		world.regenerateChunk(x,z);
//		world.refreshChunk(x,z);
//		setBorders(x, z, percent);
//		percentPrint(percent);
//	}
	
	private void setBorders(int x, int z, int[] percent) {
		world.loadChunk(x,z,true);
		for (int i = 0; i < 256; i++) {
			Block block = world.getBlockAt(x,i,z);
			block.getState().setData(new MaterialData(Material.BEDROCK));
			block.setType(Material.BEDROCK);
		}
		percentPrint(percent);
	}

	private void percentPrint(int[] percent) {
		percent[0]+=256;
		if (percent[0]%10000==0) {
			console.sendMessage("Border generation at: " + (int)(percent[0]*100/percent[1]) + "%");
		}
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