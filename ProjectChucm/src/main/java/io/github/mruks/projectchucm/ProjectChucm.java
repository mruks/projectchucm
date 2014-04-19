package io.github.mruks.projectchucm;

import java.util.ArrayList;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

public final class ProjectChucm extends JavaPlugin {
	private ConsoleCommandSender console;
	private Random rand = new Random();
	private int border = 0;
	private int plusBorder = 0;
	private int minBorder = 0;
	private World world;
	private int teams = 0;
	private ArrayList<Team> ready = new ArrayList<>();
	private ArrayList<Team> unready = new ArrayList<>();
	private boolean uhc = false;
	private int mins = 20;

 	private static final String[] TEAM_COLORS = {

		"black", "dark_blue", "dark_green", "dark_aqua", "dark_red",
		"dark_purple", "gold", "gray", "dark_gray", "blue", "green",
		"aqua", "red", "light_purple", "yellow", "white"
	};
 	
 	private class UHCStarter extends BukkitRunnable {
 		private int counter = 5;
 		private JavaPlugin plugin;

		public UHCStarter(JavaPlugin plugin) {
			super();
			this.plugin = plugin;
		}

		@Override
 		public void run() {
			if (counter > 0) {
	 			ProjectChucm.sendAllMessage("[SERVER] UHC STARTS IN " + counter-- + " SECONDS");
			} else {
				for (Player p : getServer().getOnlinePlayers()) {
					Bukkit.dispatchCommand(console, "gamemode survival " + p.getDisplayName());
				}
				Bukkit.dispatchCommand(console, "difficulty hard");
				Bukkit.dispatchCommand(console, "time set day");
				Bukkit.dispatchCommand(console, "gamerule naturalRegeneration false");
				Bukkit.dispatchCommand(console, "gamerule doDaylightCycle true");
				if (mins > 0) {
					int time = mins*1200;
					new UHCTimer(mins).runTaskTimer(plugin, time, time);
				}
				this.cancel();
			}
 		}
 	}

	@Override
	public void onEnable() {
		Bukkit.setDefaultGameMode(GameMode.ADVENTURE);
		console = Bukkit.getConsoleSender();
		world = Bukkit.getWorld("world");
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		String cmd = command.getName().toLowerCase();
		if (uhc) sender.sendMessage("UHC has started, all commands from this plugin have been blocked");
		else {
			if (cmd.equals("prep")) return prep(sender, args);
			else if (cmd.equals("teamup")) return teamup(sender, args);
			else if (cmd.equals("genmap")) return genmap(sender);
			else if (cmd.equals("ready")) return ready(sender);
			else if (cmd.equals("unready")) return unready(sender);
			else if (cmd.equals("timer")) return timer(sender, args);
			else if (cmd.equals("uhcsetup")) return uhcsetup(sender);
		}
		return false;
	}
	
	private boolean prep(CommandSender sender, String[] args) {
		if (args.length < 1) {
			sender.sendMessage("missing borderLength parameter");
			return false;
		} else {
			if ((sender instanceof Player) && ((Player)sender).isOp()) {
				int[] temp = new int[1];
				if (parse(args[0],temp) && temp[0] > 100) {
					border = temp[0];
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
					return false;
				}
			} else {
				sender.sendMessage("this command has to be performed by an opped player");
				return false;
			}
		}
		return true;
	}

	private boolean teamup(CommandSender sender, String[] args) {
		if (args.length < 1) {
			sender.sendMessage("missing numberOfTeams parameter");
			return false;
		} else {
			int[] temp = new int[1];
			if (parse(args[0],temp)) {
				Player[] players = Bukkit.getServer().getOnlinePlayers();
				if (temp[0] < 2) {
					sender.sendMessage("You need at least 2 teams to have a good UHC");
					return false;
				} else if (temp[0] > 16) {
					sender.sendMessage("You can't have more teams than colors(16) available");
					return false;
				} else if (temp[0] > players.length) {
					sender.sendMessage("You can't have more teams than players");
					return false;
				} else {
					if (teams > 0) {
						ready.clear();
						unready.clear();
						for (int i = 0; i < teams; i++) {
							Bukkit.dispatchCommand(console, "scoreboard teams empty team" + i);
							Bukkit.dispatchCommand(console, "scoreboard teams remove team" + i);
						}
					}
					teams = temp[0];
					int playersPerTeam = players.length / teams;
					int rest = players.length % teams;
					if (teams == players.length) {
						playersPerTeam = 1;
						rest = 0;
					}
					ArrayList<String> colors = new ArrayList<>();
					for (int i = 0; i < teams; i++) {
						Bukkit.dispatchCommand(console, "scoreboard teams add team" + i);
						Bukkit.dispatchCommand(console, "scoreboard teams option team" + i + " color " + getRandomColor(colors));
						if ((rest--) > 0) populateTeam(i, playersPerTeam + 1);
						else populateTeam(i, playersPerTeam);
					}
					Scoreboard sb = getServer().getScoreboardManager().getMainScoreboard();
					unready.addAll(sb.getTeams());
					for (Player p : getServer().getOnlinePlayers()) {
						Team team = sb.getPlayerTeam(p);
						StringBuilder str = new StringBuilder();
						Object[] tp = team.getPlayers().toArray();
						if (tp.length == 1) str.append(" completely alone :(");
						else str.append(" with: ");
						for (int i = 0; i < tp.length; i++) {
							if (!tp[i].equals(p)) {
								str.append(((Player)tp[i]).getName());
								if (i > 0) {
									if (i < tp.length - 1) str.append(" and ");
									else str.append(", ");
								}
							}
						}
						p.sendMessage("You've been added to " + team.getName() + str.toString());
					}
				}
			} else {
				sender.sendMessage("NumberOfTeams has to be a valid number");
				return false;
			}
		}
		return true;
	}
	
	private boolean genmap(CommandSender sender) {
		if ((sender instanceof Player) && ((Player)sender).isOp()) {
			if (border == 0) {
				sender.sendMessage("map hasn't been prepped");
				return false;
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
					//gen actual borders
					for (int i = minBorder; i <= plusBorder; i++) setBorders(i, minBorder, percent);
					for (int i = minBorder; i <= plusBorder; i++) setBorders(plusBorder,i, percent);
					for (int i = plusBorder; i >= minBorder; i--) setBorders(i, plusBorder, percent);
					for (int i = plusBorder; i >= minBorder; i--) setBorders(minBorder,i, percent);
					console.sendMessage("Server needs to restart");
					Bukkit.dispatchCommand(console, "stop");
				} else {
					sender.sendMessage("tp to " + minBorder + " 100 " + minBorder + "in order for this command to work! That chunk needs to be loaded!");
					return false;
				}
			}
		} else {
			sender.sendMessage("This command has to be performed by a logged on OP in order to work");
			return false;
		}
		return true;
	}
	
	private boolean ready(CommandSender sender) {
		if (sender instanceof Player) {
			Player player = (Player)sender;
			Team team = getServer().getScoreboardManager().getMainScoreboard().getPlayerTeam(player);
			boolean found = false;
			for (Team temp : unready) {
				if (temp.equals(team)) {
					unready.remove(temp);
					ready.add(temp);
					found = true;
					break;
				}
			}
			if (found) sendAllMessage(team.getDisplayName() + " is now ready");
			else {
				sender.sendMessage("Your team is already ready, or you're not in a team");
				return false;
			}
		} else {
			sender.sendMessage("This command has to be performed by a player in order to work");
			return false;
		}
		if (unready.isEmpty()) {
			uhc = true;
			UHCStarter starter = new UHCStarter(this);
			starter.runTaskTimer(this, 20, 20);
		}
		return true;
	}

	private boolean unready(CommandSender sender) {
		if (sender instanceof Player) {
			Player player = (Player)sender;
			Team team = getServer().getScoreboardManager().getMainScoreboard().getPlayerTeam(player);
			boolean found = false;
			for (Team temp : ready) {
				if (temp.equals(team)) {
					ready.remove(temp);
					unready.add(temp);
					found = true;
					break;
				}
			}
			if (found) {
				sender.sendMessage("Your team wasn't yet ready, or you're not in a team");
				return false;
			} else sendAllMessage(team.getDisplayName() + " wussed out");
		} else {
			sender.sendMessage("This command has to be performed by a player in order to work");
			return false;
		}
		return true;
	}

	private boolean timer(CommandSender sender, String[] args) {
		if (args.length < 1) {
			sender.sendMessage("missing minuteLength parameter");
			return false;
		} else {
			if ((sender instanceof Player) && ((Player)sender).isOp()) {
				int[] temp = new int[1];
				if (parse(args[0],temp) && (temp[0] > 0)) {
					mins = temp[0];
					sender.sendMessage("minuteLength for the timer has been changed to " + mins + " minutes");
				} else {
					sender.sendMessage("minuteLength is an invalid number or to small (i.e. less then 1)");
					return false;
				}
			} else {
				sender.sendMessage("this command has to be performed by an opped player");
				return false;
			}
		}
		return true;
	}

	private boolean uhcsetup(CommandSender sender) {
		if(sender instanceof Player && ((Player)sender).isOp() && (teams > 0)) {
			StringBuilder str = new StringBuilder();
			str.append("spreadplayers 0 0 " + (border/5) + " " + (-minBorder) + " true");
			for (Player p : getServer().getOnlinePlayers()) {
				str.append(" " + p.getDisplayName());
				Bukkit.dispatchCommand(console, "effect " + p.getDisplayName() + " 17 5 126");
				p.setFlying(false);
			}
			Bukkit.dispatchCommand(console, str.toString());
			new BukkitRunnable() {
				@Override
				public void run() {
				console.sendMessage("started task");
					for (Player p : getServer().getOnlinePlayers()) {
						Bukkit.dispatchCommand(console, "effect " + p.getDisplayName() + " 23 1 19");
						Bukkit.dispatchCommand(console, "effect " + p.getDisplayName() + " 10 1 126");
					}
					console.sendMessage("finished task");
				}
			}.runTaskLater(this, 100);
		} else {
			sender.sendMessage("This command has to be performed by an opped player in order to work and the teamup command has to be used correctly");
			return false;
		}
		return true;
	}

	private void setBorders(int x, int z, int[] percent) {
		Chunk chunk = world.getChunkAt(x,z);
		chunk.load(true);
		for (int i = 0; i < 256; i++) {
			Block block = world.getBlockAt(x,i,z);
			block.setType(Material.BEDROCK);
		}
		percent[0]+=256;
		if (percent[0]%10000==0) {
			console.sendMessage("Border generation at: " + (int)(percent[0]*100/percent[1]) + "%");
		}
	}
	
	private String getRandomColor(ArrayList<String> list) {
		boolean found = false;
		String temp = "";
		do {
			temp = TEAM_COLORS[rand.nextInt(16)];
			found = !list.contains(temp); 
		} while (!found);
		list.add(temp);
		return temp;
	}

	public static void sendAllMessage(String msg) {
		for (Player p : Bukkit.getServer().getOnlinePlayers()) {
			p.sendMessage(msg);
		}
	}

	private boolean parse(String str, int[] number) {
		try {
			number[0] = Integer.parseInt(str);
		} catch(NumberFormatException e) {
			number[0] = 0;
			return false;
		}
		return true;
	}
	
	private void populateTeam(int teamnum, int amount) {
		Scoreboard sb = getServer().getScoreboardManager().getMainScoreboard();
		int count = 0;
		Player[] players = getServer().getOnlinePlayers();
		while (count < amount) {
			Player p = players[rand.nextInt(players.length)];
			if (sb.getPlayerTeam(p) == null) {
				Bukkit.dispatchCommand(console, "scoreboard teams join team" + teamnum + " " + p.getDisplayName());
				count++;
			}
			if (count > amount) return;
		}
	}
}