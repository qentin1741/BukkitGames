package main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Color;
import org.bukkit.Difficulty;
import org.bukkit.FireworkEffect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import commands.BGConsole;
import commands.BGPlayer;

import threads.BGEndDB;
import threads.BGQuery;
import timers.GameTimer;
import timers.InvincibilityTimer;
import timers.PreGameTimer;
import timers.WorldBorderTimer;
import utilities.BGChat;
import utilities.BGCornucopia;
import utilities.BGDisguise;
import utilities.BGFiles;
import utilities.BGKit;
import utilities.BGReward;
import utilities.BGVanish;
import utilities.Border;
import utilities.Metrics;
import utilities.enums.BorderType;

import events.BGListener;

public class BGMain extends JavaPlugin {		
	public static  ReentrantLock lock = new ReentrantLock(true);

	public static String HELP_MESSAGE = null;
	public static String SERVER_FULL_MSG = "";
	public static String WORLD_BORDER_MSG = "";
	public static String GAME_IN_PROGRESS_MSG = "";
	public static String KIT_BUY_WEB = "";
	public static String NEW_WINNER = "";
	public static String MOTD_PROGRESS_MSG = "";
	public static String MOTD_COUNTDOWN_MSG = "";
	public static String NO_KIT_MSG = "";
	public static String SERVER_TITLE = null;
	public static Boolean ADV_CHAT_SYSTEM = true;
	public static Boolean KIT_PREFIX = true;
	public static Integer COUNTDOWN_SECONDS = Integer.valueOf(30);
	public static Integer FINAL_COUNTDOWN_SECONDS = Integer.valueOf(20);
	public static Integer MAX_GAME_RUNNING_TIME = Integer.valueOf(30);
	public static Integer MINIMUM_PLAYERS = Integer.valueOf(1);
	public static final Integer WINNER_PLAYERS = Integer.valueOf(1);
	public static Integer END_GAME_TIME = Integer.valueOf(25);
	public static final String WORLD_TEMPOARY_NAME = "world";
	public static Boolean REGEN_WORLD = false;
	public static Boolean RANDOM_START = false;
	public static Boolean SHOW_TIPS = true;
	public static Boolean DENY_CHECK_WORLDBORDER = Boolean.valueOf(false);
	public static Boolean DENY_LOGIN = false;
	public static Boolean DENY_BLOCKPLACE = false;
	public static Boolean DENY_BLOCKBREAK = false;
	public static Boolean DENY_ITEMPICKUP = false;
	public static Boolean DENY_ITEMDROP = false;
	public static Boolean DENY_DAMAGE_PLAYER = false;
	public static Boolean DENY_DAMAGE_ENTITY = false;
	public static Boolean DENY_SHOOT_BOW = false;
	public static Boolean QUIT_MSG = false;
	public static Boolean DEATH_MSG = false;
	public static Boolean COMPASS = true;
	public static Boolean AUTO_COMPASS = false;
	public static Boolean ADV_ABI = false;
	public static Boolean SIMP_REW = false;
	public static Boolean REW = false;
	public static Boolean DEATH_SIGNS = true;
	public static Boolean DEATH_SG_PROTECTED = true;
	public static Boolean END_GAME = true;
	public static Boolean DEFAULT_KIT = false;
	public static Boolean CORNUCOPIA = true;
	public static Boolean CORNUCOPIA_CHESTS = false;
	public static Boolean TEAM = true;
	public static Boolean GEN_MAPS = false;
	public static Boolean ITEM_MENU = true;
	public static Boolean CORNUCOPIA_ITEMS = true;
	public static Boolean CORNUCOPIA_PROTECTED = true;
	public static Boolean FEAST = true;
	public static Boolean FEAST_CHESTS = false;
	public static Boolean FEAST_PROTECTED = true;
	public static Boolean SPECTATOR_SYSTEM = false;
	public static Boolean SQL_DSC = false;
	public static Location spawn;
	public static String LAST_WINNER = "";
	public static ArrayList<Player> spectators = new ArrayList<Player>();
	public static ArrayList<Player> gamemakers = new ArrayList<Player>();
	public static Integer COUNTDOWN = Integer.valueOf(0);
	public static Integer FINAL_COUNTDOWN = Integer.valueOf(0);
	public static Integer GAME_RUNNING_TIME = Integer.valueOf(0);
	static HashMap<World, ArrayList<Border>> BORDERS = new HashMap<World, ArrayList<Border>>();
	public static Integer WORLDRADIUS = Integer.valueOf(250);
	public static Boolean SQL_USE = false;
	public static Integer FEAST_SPAWN_TIME = 30;
	public static Integer COINS_FOR_KILL = 1;
	public static Integer COINS_FOR_WIN = 5;

	public static Integer SQL_GAMEID = null;
	public static String SQL_HOST = null;
	public static String SQL_PORT = null;
	public static String SQL_USER = null;
	public static String SQL_PASS = null;
	public static String SQL_DATA = null;
	public static Connection con = null;
	
	public static BGMain instance;
	public static Logger log;
	
	public void onLoad() {
		instance = this;
		log = getPluginLogger();
		
		try {
			new BGFiles();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		log.info("Deleting old world.");
		Bukkit.getServer().unloadWorld("world", false);
		deleteDir(new File("world"));

		Random r = new Random();
		
		REGEN_WORLD = getConfig().getBoolean("REGEN_WORLD");
		GEN_MAPS = BGFiles.worldconf.getBoolean("GEN_MAPS");
		
		if (!REGEN_WORLD && !(GEN_MAPS && r.nextBoolean())) {
			List<String> mapnames = BGFiles.worldconf.getStringList("WORLDS");
			
			String map = mapnames.get(r.nextInt(mapnames.size()));
			String[] splitmap = map.split(",");
			
			log.info("Copying saved world. ("+splitmap[0]+")");
			try {
				copyDirectory(new File(getDataFolder(), splitmap[0]),
						new File("world"));
			} catch (IOException e) {
				log.warning("Error: " + e.toString());
			}
			if(splitmap.length == 2)
				BGMain.WORLDRADIUS = Integer.valueOf(Integer.parseInt(splitmap[1]));
			else
				BGMain.WORLDRADIUS = 300;
		} else {
			log.info("Generating new world.");
			BGMain.WORLDRADIUS = Integer.valueOf(getConfig().getInt("WORLD_BORDER_RADIUS"));
		}
	}

	private void registerEvents() {
		BGListener bl = new BGListener();
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(bl, this);
	}
		
	public void registerCommands() {
		ConsoleCommandSender console = Bukkit.getConsoleSender();

		if (getCommand("help") != null) 
			getCommand("help").setExecutor(new BGPlayer()); 
		else 
			console.sendMessage(ChatColor.RED+"getCommand help returns null");

		if (getCommand("kit") != null) 
			getCommand("kit").setExecutor(new BGPlayer()); 
		else 
			console.sendMessage(ChatColor.RED+"getCommand kit returns null");

		if (getCommand("kitinfo") != null) 
			getCommand("kitinfo").setExecutor(new BGPlayer()); 
		else 
			console.sendMessage(ChatColor.RED+"getCommand kitinfo returns null");

		if (getCommand("start") != null) 
			getCommand("start").setExecutor(new BGConsole()); 
		else 
			console.sendMessage(ChatColor.RED+"getCommand start returns null");

		if (getCommand("spawn") != null) 
			getCommand("spawn").setExecutor(new BGPlayer()); 
		else 
			console.sendMessage(ChatColor.RED+"getCommand spawn returns null");
		
		if (getCommand("coin") != null)
			getCommand("coin").setExecutor(new BGConsole());
		else
			console.sendMessage(ChatColor.RED+"getCommand coin returns null");
		
		if (getCommand("fbattle") != null)
			getCommand("fbattle").setExecutor(new BGConsole());
		else
			console.sendMessage(ChatColor.RED+"getCommand fbattle returns null");
		
		if (getCommand("bgversion") != null)
			getCommand("bgversion").setExecutor(new BGConsole());
		else
			console.sendMessage(ChatColor.RED+"getCommand bgversion returns null");
		if(getCommand("bgdownload") != null)
			getCommand("bgdownload").setExecutor(new BGConsole());
		else
			console.sendMessage(ChatColor.RED+"getCommand bgdownload returns null");
		if(getCommand("team") != null)
			getCommand("team").setExecutor(new BGPlayer());
		else
			console.sendMessage(ChatColor.RED+"getCommand team returns null");
		if(getCommand("gamemaker") != null)
			getCommand("gamemaker").setExecutor(new BGPlayer());
		else
			console.sendMessage(ChatColor.RED+"getCommand gamemaker returns null");
		if(getCommand("teleport") != null)
			getCommand("teleport").setExecutor(new BGPlayer());
		else
			console.sendMessage(ChatColor.RED+"getCommand teleport returns null");
	}
	
	public void onEnable() {
		instance = this;
		Bukkit.getServer().getWorlds().get(0).setDifficulty(Difficulty.PEACEFUL);
		
		ADV_ABI = Boolean.valueOf(getConfig().getBoolean("ADVANCED_ABILITIES"));
				
		//Metrics
		try{
			Metrics metrics = new Metrics(this);
			metrics.start();
			log.info("Starting Plugin Metrics...");
		}catch (IOException e) {
			log.info("Error with Plugin Metrics!");
		}
		
		registerEvents();
		registerCommands();
		new BGKit();
		new BGChat();
		
		log.info("Loading configuration options.");
		DEATH_SIGNS = Boolean.valueOf(getConfig().getBoolean("DEATH_SIGNS"));
		DEATH_SG_PROTECTED = Boolean.valueOf(BGFiles.dsign.getBoolean("PROTECTED"));
		KIT_BUY_WEB = getConfig().getString("MESSAGE.KIT_BUY_WEBSITE");
		SERVER_TITLE = getConfig().getString("MESSAGE.SERVER_TITLE");
		HELP_MESSAGE = getConfig().getString("MESSAGE.HELP_MESSAGE");
		RANDOM_START = Boolean.valueOf(getConfig().getBoolean("RANDOM_START"));
		DEFAULT_KIT = Boolean.valueOf(getConfig().getBoolean("DEFAULT_KIT"));
		SHOW_TIPS = Boolean.valueOf(getConfig().getBoolean("SHOW_TIPS"));
		REGEN_WORLD = Boolean.valueOf(getConfig().getBoolean("REGEN_WORLD"));
		CORNUCOPIA = Boolean.valueOf(getConfig().getBoolean("CORNUCOPIA"));
		CORNUCOPIA_ITEMS = Boolean.valueOf(BGFiles.cornconf.getBoolean("ITEM_SPAWN"));
		CORNUCOPIA_CHESTS = Boolean.valueOf(BGFiles.cornconf.getBoolean("CHESTS"));
		CORNUCOPIA_PROTECTED = Boolean.valueOf(BGFiles.cornconf.getBoolean("PROTECTED"));
		FEAST = Boolean.valueOf(getConfig().getBoolean("FEAST"));
		FEAST_CHESTS = Boolean.valueOf(BGFiles.feastconf.getBoolean("CHESTS"));
		FEAST_PROTECTED = Boolean.valueOf(BGFiles.feastconf.getBoolean("PROTECTED"));
		SPECTATOR_SYSTEM = Boolean.valueOf(getConfig().getBoolean("SPECTATOR_SYSTEM"));
		TEAM = Boolean.valueOf(getConfig().getBoolean("TEAM"));
		NO_KIT_MSG = getConfig().getString("MESSAGE.NO_KIT_PERMISSION");
		GAME_IN_PROGRESS_MSG = getConfig().getString("MESSAGE.GAME_PROGRESS");
		SERVER_FULL_MSG = getConfig().getString("MESSAGE.SERVER_FULL");
		WORLD_BORDER_MSG = getConfig().getString("MESSAGE.WORLD_BORDER");
		MOTD_PROGRESS_MSG = getConfig().getString("MESSAGE.MOTD_PROGRESS");
		MOTD_COUNTDOWN_MSG = getConfig().getString("MESSAGE.MOTD_COUNTDOWN");
		ADV_CHAT_SYSTEM = Boolean.valueOf(getConfig().getBoolean("ADVANCED_CHAT"));
		KIT_PREFIX = Boolean.valueOf(getConfig().getBoolean("KIT_PREFIX"));
		SQL_USE = Boolean.valueOf(getConfig().getBoolean("MYSQL"));
		SQL_HOST = getConfig().getString("HOST");
		SQL_PORT = getConfig().getString("PORT");
		SQL_USER = getConfig().getString("USERNAME");
		SQL_PASS = getConfig().getString("PASSWORD");
		SQL_DATA = getConfig().getString("DATABASE");
		SIMP_REW = getConfig().getBoolean("SIMPLE_REWARD");
		REW = Boolean.valueOf(getConfig().getBoolean("REWARD"));
		COINS_FOR_KILL = Integer.valueOf(getConfig().getInt("COINS_FOR_KILL"));
		COINS_FOR_WIN = Integer.valueOf(getConfig().getInt("COINS_FOR_WIN"));
		MINIMUM_PLAYERS = Integer.valueOf(getConfig().getInt("MINIMUM_PLAYERS_START"));	
		MAX_GAME_RUNNING_TIME = Integer.valueOf(getConfig().getInt("TIME.MAX_GAME-MIN"));
		COUNTDOWN_SECONDS = Integer.valueOf(getConfig().getInt("TIME.COUNTDOWN-SEC"));
		FINAL_COUNTDOWN_SECONDS = Integer.valueOf(getConfig().getInt("TIME.FINAL_COUNTDOWN-SEC"));
		END_GAME_TIME = Integer.valueOf(getConfig().getInt("TIME.INCREASE_DIFFICULTY-MIN"));
		COMPASS = Boolean.valueOf(getConfig().getBoolean("COMPASS"));
		AUTO_COMPASS = Boolean.valueOf(getConfig().getBoolean("AUTO_COMPASS"));
		ITEM_MENU = getConfig().getBoolean("ITEM_MENU");
		
		if (ADV_ABI) {
			log.info("Enableing the advanced abilities.");
			new BGDisguise();
		}
		
		if(REW && !SQL_USE) {
			log.warning("MySQL has to be enabled for advanced reward, turning it off.");
			REW = false;
		}
		
		if(FEAST) {
			FEAST_SPAWN_TIME = Integer.valueOf(BGFiles.feastconf.getInt("SPAWN_TIME"));
		}
		
		if(CORNUCOPIA) {
			BGCornucopia.createCorn();
		}
		
		if(BGMain.WORLDRADIUS < 60) {
			log.warning("Worldborder radius has to be 60 or higher!");
			getServer().getPluginManager().disablePlugin(this);
		}

		log.info("Getting winner of last game.");
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(new File(getDataFolder(),"leaderboard.yml")));
		} catch (FileNotFoundException e) {
			log.warning(e.toString());
		}

		String line = null;
		String merke = null;
		try {
			while ((line = br.readLine()) != null)
				merke = line;
		} catch (IOException e) {
			log.warning(e.toString());
		}
		try {
			br.close();
		} catch (IOException e) {
			log.warning(e.toString());
		}

		LAST_WINNER = merke;

		World thisWorld = getServer().getWorlds().get(0);
		spawn = thisWorld.getSpawnLocation();

		Border newBorder1 = new Border(spawn.getX(), spawn.getZ(), BGMain.WORLDRADIUS, BorderType.STOP);
		Border newBorder2 = new Border(spawn.getX(), spawn.getZ(), BGMain.WORLDRADIUS - 10, BorderType.WARN);
		if (!BORDERS.containsKey(thisWorld)) {
			ArrayList<Border> newArray = new ArrayList<Border>();
			BORDERS.put(thisWorld, newArray);
		}
		((ArrayList<Border>) BORDERS.get(thisWorld)).add(newBorder1);
		((ArrayList<Border>) BORDERS.get(thisWorld)).add(newBorder2);

		COUNTDOWN = COUNTDOWN_SECONDS;
		FINAL_COUNTDOWN = FINAL_COUNTDOWN_SECONDS;
		GAME_RUNNING_TIME = Integer.valueOf(0);

		DENY_BLOCKBREAK = Boolean.valueOf(true);
		DENY_BLOCKPLACE = Boolean.valueOf(true);
		DENY_ITEMDROP = Boolean.valueOf(true);
		DENY_ITEMPICKUP = Boolean.valueOf(true);
		DENY_DAMAGE_PLAYER = Boolean.valueOf(true);
		DENY_DAMAGE_ENTITY = Boolean.valueOf(true);
		DENY_SHOOT_BOW = Boolean.valueOf(true);

		if (SQL_USE) {
			SQLconnect();
			SQLquery("CREATE TABLE IF NOT EXISTS `GAMES` (`ID` int(10) unsigned NOT NULL AUTO_INCREMENT, `STARTTIME` datetime NOT NULL, `ENDTIME` datetime, `REF_WINNER` int(10), PRIMARY KEY (`ID`)) ENGINE=InnoDB DEFAULT CHARSET=UTF8 AUTO_INCREMENT=1 ;");
			SQLquery("CREATE TABLE IF NOT EXISTS `PLAYERS` (`ID` int(10) unsigned NOT NULL AUTO_INCREMENT, `NAME` varchar(255) NOT NULL, PRIMARY KEY (`ID`)) ENGINE=InnoDB DEFAULT CHARSET=UTF8 AUTO_INCREMENT=1 ;");
			SQLquery("CREATE TABLE IF NOT EXISTS `PLAYS` (`ID` int(10) unsigned NOT NULL AUTO_INCREMENT, `REF_PLAYER` int(10), `REF_GAME` int(10), `KIT` varchar(255), `DEATHTIME` datetime, `REF_KILLER` int(10), `DEATH_REASON` varchar(255), PRIMARY KEY (`ID`)) ENGINE=InnoDB DEFAULT CHARSET=UTF8 AUTO_INCREMENT=1 ;");
			SQLquery("CREATE TABLE IF NOT EXISTS `REWARD` (`ID` int(10) unsigned NOT NULL AUTO_INCREMENT, `REF_PLAYER` int(10) NOT NULL, `COINS` int(10) NOT NULL, PRIMARY KEY (`ID`)) ENGINE=InnoDB DEFAULT CHARSET=UTF8 AUTO_INCREMENT=1 ;");
		}

		Location loc = randomLocation(spawn.getChunk()).add(0.0D, 30.0D,0.0D);
		Bukkit.getServer().getWorlds().get(0).loadChunk(loc.getChunk());
		new PreGameTimer();
		new WorldBorderTimer();
		
		PluginDescriptionFile pdfFile = getDescription();
		log.info("Plugin enabled");
		log.info("Author: " + pdfFile.getAuthors() + " | Version: " + pdfFile.getVersion());
		log.info("All rights reserved. This plugin is free to download. If you had to pay for it, contact us immediately!");
		
		log.info("Game phase: 1 - Waiting");
	}

	public void onDisable() {
		PluginDescriptionFile pdfFile = getDescription();
		
		Bukkit.getServer().getScheduler().cancelAllTasks();
		
		if (SQL_USE) {
			if (SQL_GAMEID != null) {
				Integer PL_ID = getPlayerID(NEW_WINNER);
				SQLquery("UPDATE `GAMES` SET `ENDTIME` = NOW(), `REF_WINNER` = "
						+ PL_ID + " WHERE `ID` = " + SQL_GAMEID + " ;");
				SQL_DSC = true;
				SQLdisconnect();
			}
		}

		for (Player p : getPlayers()) {
			p.kickPlayer(ChatColor.YELLOW + "Server is restarting!");
		}
		
		Bukkit.getServer().unloadWorld(Bukkit.getServer().getWorlds().get(0), false);
		
		log.info("Plugin disabled");
		log.info("Author: " + pdfFile.getAuthors() + " | Version: " + pdfFile.getVersion());
		
		Bukkit.getServer().shutdown();
	}

	private void copyDirectory(File sourceLocation, File targetLocation)
			throws IOException {

		if (sourceLocation.isDirectory()) {
			if (!targetLocation.exists()) {
				targetLocation.mkdir();
			}

			String[] children = sourceLocation.list();
			for (int i = 0; i < children.length; i++) {
				copyDirectory(new File(sourceLocation, children[i]), new File(
						targetLocation, children[i]));
			}
		} else {

			InputStream in = new FileInputStream(sourceLocation);
			OutputStream out = new FileOutputStream(targetLocation);
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			in.close();
			out.close();
		}
	}

	public static void copy(InputStream in, File file) {
		try {
			OutputStream out = new FileOutputStream(file);
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			out.close();
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static boolean inStopBorder(Location checkHere) {
		if (!BORDERS.containsKey(checkHere.getWorld()))
			return true;
		
		for (Border amIHere : BORDERS.get(checkHere.getWorld())) {
			if(amIHere.type != BorderType.STOP)
				continue;
			int X = (int) Math.abs(amIHere.centerX - checkHere.getBlockX());
			int Z = (int) Math.abs(amIHere.centerZ - checkHere.getBlockZ());
			if ((X < amIHere.definiteSq) && (Z < amIHere.definiteSq))
				return true;
			if ((X > amIHere.radius) || (Z > amIHere.radius))
				continue;
			if (X * X + Z * Z < amIHere.radiusSq)
				return true;
		}
		return false;
	}
	
	public static boolean inBorder(Location c, BorderType t) {
		if(t == BorderType.STOP) {
			return inStopBorder(c);
		}
		
		if(t == BorderType.WARN) {
			if(!inWarnBorder(c) && inStopBorder(c))
				return false;
			else
				return true;
		}
		return true;
	}
	
	public static boolean inWarnBorder(Location checkHere) {
		if (!BORDERS.containsKey(checkHere.getWorld()))
			return true;
		
		for (Border amIHere : BORDERS.get(checkHere.getWorld())) {
			if(amIHere.type != BorderType.WARN)
				continue;
			int X = (int) Math.abs(amIHere.centerX - checkHere.getBlockX());
			int Z = (int) Math.abs(amIHere.centerZ - checkHere.getBlockZ());
			if ((X < amIHere.definiteSq) && (Z < amIHere.definiteSq))
				return true;
			if ((X > amIHere.radius) || (Z > amIHere.radius))
				continue;
			if (X * X + Z * Z < amIHere.radiusSq)
				return true;
		}
		return false;
	}
		
	public static Location getSpawn() {
		Location loc = Bukkit.getWorlds().get(0).getSpawnLocation();
		loc.setY(Bukkit.getWorlds().get(0).getHighestBlockYAt(Bukkit.getWorlds().get(0).getSpawnLocation()) + 1.5);
		return loc;
	}

	public static Player[] getGamers() {
		ArrayList<Player> gamers = new ArrayList<Player>();
		Player[] list = Bukkit.getOnlinePlayers();
		for (Player p : list) {
			if (!BGMain.isSpectator(p) && !BGMain.isGameMaker(p)) {
				gamers.add(p);
			}
		}
		return (Player[]) gamers.toArray(new Player[0]);
	}

	public static Player[] getPlayers() {
		ArrayList<Player> players = new ArrayList<Player>();
		Player[] onlineplayers = Bukkit.getOnlinePlayers();
		for (int i = 0; i < onlineplayers.length; i++) {
			players.add(onlineplayers[i]);
		}
		return (Player[]) players.toArray(new Player[0]);
	}

	public static void startgame() {
		log.info("Game phase: 2 - Starting");
		PreGameTimer.cancel();
		new InvincibilityTimer();

		DENY_LOGIN = true;
		DENY_BLOCKBREAK = false;
		DENY_BLOCKPLACE = false;
		DENY_ITEMDROP = false;
		DENY_ITEMPICKUP = false;
		DENY_DAMAGE_ENTITY = false;
		DENY_SHOOT_BOW = false;
		QUIT_MSG = true;

		if(CORNUCOPIA_ITEMS && CORNUCOPIA)
			BGCornucopia.spawnItems();
		
		if (SQL_USE) {
			PreparedStatement statement = null;
			ResultSet generatedKeys = null;

			try {
				statement = con.prepareStatement(
						"INSERT INTO `GAMES` (`STARTTIME`) VALUES (NOW()) ;",
						Statement.RETURN_GENERATED_KEYS);

				int affectedRows = statement.executeUpdate();
				if (affectedRows == 0) {
					throw new SQLException("Error!");
				}

				generatedKeys = statement.getGeneratedKeys();
				if (generatedKeys.next()) {
					SQL_GAMEID = (int) generatedKeys.getLong(1);
				} else {
					throw new SQLException("Error!");
				}
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				if (generatedKeys != null)
					try {
						generatedKeys.close();
					} catch (SQLException logOrIgnore) {
					}
				if (statement != null)
					try {
						statement.close();
					} catch (SQLException logOrIgnore) {
					}
			}

		}

		DENY_CHECK_WORLDBORDER = Boolean.valueOf(true);
		Bukkit.getServer().getWorlds().get(0).loadChunk(getSpawn().getChunk());

		Bukkit.getWorlds().get(0).setDifficulty(Difficulty.HARD);
		for (Player p : getPlayers()) {
			if(isGameMaker(p) || isSpectator(p))
				continue;
			if (RANDOM_START == false) {
				Random r = new Random();
				Location startFrom = getSpawn();
				Location loc = startFrom.clone();
				int addx;
				int addy;
				do {
					
					addx = (r.nextBoolean() ? 1 : -1) * r.nextInt(7);
					addy = (r.nextBoolean() ? 1 : -1) * r.nextInt(7);
				}while((Math.abs(addx)+Math.abs(addy)) < 5);
				loc.add(addx, 60, addy);
				loc.setY(Bukkit.getServer().getWorlds().get(0).getHighestBlockYAt(loc) + 1.5);
				p.teleport(loc);
			} else {
				Location tploc = getRandomLocation();
				while(!inBorder(tploc,BorderType.WARN)) {
					tploc = getRandomLocation();
				}
				tploc.setY(Bukkit.getServer().getWorlds().get(0).getHighestBlockYAt(tploc) + 1.5);
				p.teleport(tploc);
			}
			p.setHealth(20);
			p.setFoodLevel(20);
			p.setExp(0);
			if(p.getOpenInventory() != null)
				p.getOpenInventory().close();
			
			BGKit.giveKit(p);

			if (SQL_USE & !BGMain.isSpectator(p)) {
				Integer PL_ID = getPlayerID(p.getName());
				SQLquery("INSERT INTO `PLAYS` (`REF_PLAYER`, `REF_GAME`, `KIT`) VALUES ("
						+ PL_ID
						+ ","
						+ SQL_GAMEID
						+ ",'"
						+ BGKit.getKit(p)
						+ "') ;");
			}
		}

		Bukkit.getServer().getWorlds().get(0).setTime(0L);
		Bukkit.getServer().getWorlds().get(0).setStorm(false);
		Bukkit.getServer().getWorlds().get(0).setThundering(false);
		DENY_CHECK_WORLDBORDER = Boolean.valueOf(false);
		if (ADV_CHAT_SYSTEM) {
			BGChat.printInfoChat(" --- The games have begun! ---");
			BGChat.printDeathChat("�e\"May the odds be ever in your favor!\"");
		} else {
			BGChat.printTimeChat("");
			BGChat.printTimeChat("The games have begun!");
		}
		BGChat.printTimeChat("Everyone is invincible for "
				+ TIME(FINAL_COUNTDOWN_SECONDS) + ".");
	}

	public static Location randomLocation(Chunk c) {
		Random random = new Random();
		Location startFrom = Bukkit.getWorlds().get(0).getSpawnLocation();
		Location loc = startFrom.clone();
		loc.add((random.nextBoolean() ? 1 : -1) * random.nextInt(WORLDRADIUS),
				60,
				(random.nextBoolean() ? 1 : -1) * random.nextInt(WORLDRADIUS));
		int newY = Bukkit.getWorlds().get(0).getHighestBlockYAt(loc);
		loc.setY(newY);
		return loc;
	}

	public static Location getRandomLocation() {
		Random random = new Random();
		Location startFrom = Bukkit.getWorlds().get(0).getSpawnLocation();
		Location loc;
		do{
			loc = startFrom.clone();
			loc.add((random.nextBoolean() ? 1 : -1) * random.nextInt(WORLDRADIUS),
				60,
				(random.nextBoolean() ? 1 : -1) * random.nextInt(WORLDRADIUS));
			int newY = Bukkit.getWorlds().get(0).getHighestBlockYAt(loc);
			loc.setY(newY);
		} while(!BGMain.inBorder(loc, BorderType.WARN));
		return loc;
	}

	public static void checkwinner() {
		if (getGamers().length <= WINNER_PLAYERS)
			if (getGamers().length == 0) {
				GameTimer.cancel();
				Bukkit.getServer().shutdown();
			} else {
				GameTimer.cancel();
				String winnername = getGamers()[0].getName();
				NEW_WINNER = winnername;
				try {
					String contents = winnername;
					BufferedWriter writer = new BufferedWriter(new FileWriter(
							new File(instance.getDataFolder(), "leaderboard.yml"), true));
					writer.newLine();
					writer.write(contents);
					writer.flush();
					writer.close();
				} catch (Exception ex) {
					log.warning(ex.toString());
				}
				
				final Player pl = getGamers()[0];
				pl.playSound(pl.getLocation(), Sound.LEVEL_UP, 1.0F, (byte) 1);
				DENY_DAMAGE_PLAYER = true;
				
				if(SQL_USE) {
					Integer PL_ID = getPlayerID(winnername);
					SQLquery("UPDATE `PLAYS` SET deathtime = NOW(), `DEATH_REASON` = 'WINNER' WHERE `REF_PLAYER` = "
							+ PL_ID
							+ " AND `REF_GAME` = "
							+ SQL_GAMEID + " ;");
				}
				
				if(REW) {
					if (getPlayerID(winnername) == null) {
						BGReward.createUser(winnername);
						BGReward.giveCoins(winnername, BGMain.COINS_FOR_WIN);
					} else {
						BGReward.giveCoins(winnername, BGMain.COINS_FOR_WIN);
					}
				}
				final Boolean R = REW;
				final Integer CFW = COINS_FOR_WIN;
				String text = "";
				
				if(R && CFW != 0) {
					text = "You got ";
					if(CFW == 1)
						text += "1 coin for winning the game!";
					else
						text += CFW+" coins for winning the game!";
				} 
				BGChat.printPlayerChat(pl, "�6�lYOU HAVE WON THIS GAME!" + text);
				
				final Integer s = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(BGMain.instance, new Runnable() {
					
					@Override
					public void run() {
						Random r = new Random();
						spawnRandomFirework(Bukkit.getServer().getWorlds().get(0).getHighestBlockAt(pl.getLocation().add(0, 0, r.nextInt(5) + 5).add(0, 5, 0)).getLocation());
						spawnRandomFirework(Bukkit.getServer().getWorlds().get(0).getHighestBlockAt(pl.getLocation().add(r.nextInt(5) + 5, 0, 0).add(0, 5, 0)).getLocation());
						spawnRandomFirework(Bukkit.getServer().getWorlds().get(0).getHighestBlockAt(pl.getLocation().add(r.nextInt(5) + 5, 0, r.nextInt(5) + 5).add(0, 5, 0)).getLocation());
						spawnRandomFirework(Bukkit.getServer().getWorlds().get(0).getHighestBlockAt(pl.getLocation().add(-r.nextInt(5) - 5, 0, 0).add(0, 5, 0)).getLocation());
						spawnRandomFirework(Bukkit.getServer().getWorlds().get(0).getHighestBlockAt(pl.getLocation().add(0, 0, -r.nextInt(5) - 5).add(0, 5, 0)).getLocation());
						spawnRandomFirework(Bukkit.getServer().getWorlds().get(0).getHighestBlockAt(pl.getLocation().add(-r.nextInt(5) - 5, 0, -r.nextInt(5) - 5).add(0, 5, 0)).getLocation());
						spawnRandomFirework(Bukkit.getServer().getWorlds().get(0).getHighestBlockAt(pl.getLocation().add(-r.nextInt(5) - 5, 0, r.nextInt(5) + 5).add(0, 5, 0)).getLocation());
						spawnRandomFirework(Bukkit.getServer().getWorlds().get(0).getHighestBlockAt(pl.getLocation().add(r.nextInt(5) + 5, 0, -r.nextInt(5) - 5).add(0, 5, 0)).getLocation());
					}
					
				}, 0, 20);
				
				Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(BGMain.instance, new Runnable() {
					
					@Override
					public void run() {
						Bukkit.getServer().getScheduler().cancelTask(s);
						//TODO: When doing this, the client will crash
						//pl.kickPlayer("�6�lYOU HAVE WON THIS GAME!");
						Bukkit.getServer().shutdown();
					}
					
				}, 20*10);
			}
	}

	public static void spawnRandomFirework(Location loc) {
        Firework fw = (Firework) loc.getWorld().spawnEntity(loc, EntityType.FIREWORK);
        FireworkMeta fwm = fw.getFireworkMeta();
        Random r = new Random();   

        int rt = r.nextInt(4) + 1;
        Type type = Type.BALL;       
        if (rt == 1) type = Type.BALL;
        if (rt == 2) type = Type.BALL_LARGE;
        if (rt == 3) type = Type.BURST;
        if (rt == 4) type = Type.CREEPER;
        if (rt == 5) type = Type.STAR;
        
        Color c1 = Color.RED;
        Color c2 = Color.YELLOW;
        Color c3 = Color.ORANGE;
       
        FireworkEffect effect = FireworkEffect.builder().flicker(r.nextBoolean()).withColor(c1).withColor(c2).withFade(c3).with(type).trail(r.nextBoolean()).build();
        fwm.addEffect(effect);
       
        int rp = r.nextInt(2) + 1;
        fwm.setPower(rp);
        
        fw.setFireworkMeta(fwm);           
	}
	
	public static void deleteDir(File dir) {
		if (dir.isDirectory()) {
			String[] children = dir.list();
			for (int i = 0; i < children.length; i++) {
				deleteDir(new File(dir, children[i]));
			}
		}
		dir.delete();
	}

	public static String TIME(Integer i) {
		if (i.intValue() >= 60) {
			Integer time = Integer.valueOf(i.intValue() / 60);
			String add = "";
			if (time > 1) {
				add = "s";
			}
			return time + " minute" + add;
		}
		Integer time = i;
		String add = "";
		if (time > 1) {
			add = "s";
		}
		return time + " second" + add;
	}

	public static boolean winner(Player p) {
		if (LAST_WINNER == null) {
			return false;
		}
		if (LAST_WINNER.equals(p.getName())) {
			return true;
		} else {
			return false;
		}
	}

	public static Integer getPlayerID(String playername) {
		try {
			Statement stmt = con.createStatement();
			ResultSet r = stmt
					.executeQuery("SELECT `ID`, `NAME` FROM `PLAYERS` WHERE `NAME` = '"
							+ playername + "' ;");
			r.last();
			if (r.getRow() == 0) {
				stmt.close();
				r.close();
				return null;
			}
			Integer PL_ID = r.getInt("ID");
			stmt.close();
			r.close();
			return PL_ID;
		} catch (SQLException ex) {
			System.err.println("Error with following query: "
					+ "SELECT `ID`, `NAME` FROM `PLAYERS` WHERE `NAME` = '"
					+ playername + "' ;");
			System.err.println("MySQL-Error: " + ex.getMessage());
			return null;
		} catch (NullPointerException ex) {
			System.err
					.println("Error while performing a query. (NullPointerException)");
			return null;
		}
	}

	public static void SQLconnect() {
		try {
			log.info("Connecting to MySQL database...");
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			String conn = "jdbc:mysql://" + SQL_HOST + ":" + SQL_PORT + "/"
					+ SQL_DATA;
			con = DriverManager.getConnection(conn, SQL_USER, SQL_PASS);
		} catch (ClassNotFoundException ex) {
			log.warning("No MySQL driver found!");
		} catch (SQLException ex) {
			log.warning("Error while fetching MySQL connection!");
		} catch (Exception ex) {
			log.warning("Unknown error while fetchting MySQL connection.");
		}
	}

	public static Connection SQLgetConnection() {
		return con;
	}

	public static void SQLquery(String sql) {
		BGQuery bq = new BGQuery(sql, log, con, instance);
		ExecutorService executor = Executors.newCachedThreadPool();
		executor.execute(bq);
		executor.shutdown();
	}

	public static void SQLdisconnect() {
		BGEndDB end = new BGEndDB(instance, log, con);
		
		ExecutorService executor = Executors.newCachedThreadPool();
		
		executor.execute(end);
		
		executor.shutdown();
	}
	
	public static Integer getCoins(Integer playerID) {
		try {
			Statement stmt = con.createStatement();
			ResultSet r = stmt
					.executeQuery("SELECT `COINS`, `REF_PLAYER` FROM `REWARD` WHERE `REF_PLAYER` = "
							+ playerID + " ;");
			r.last();
			if (r.getRow() == 0) {
				stmt.close();
				r.close();
				return null;
			}
			Integer PL_ID = r.getInt("COINS");
			stmt.close();
			r.close();
			return PL_ID;
		} catch (SQLException ex) {
			log.warning("Error with following query: "
					+ "SELECT `COINS`, `REF_PLAYER` FROM `REWARD` WHERE `REF_PLAYER` = "
					+ playerID + " ;");
			System.err.println("MySQL-Error: " + ex.getMessage());
			return null;
		} catch (NullPointerException ex) {
			log.warning("Error while performing a query. (NullPointerException)");
			return null;
		}
	}
	
	public static void addGameMaker(Player p) {
		spectators.remove(p);
		gamemakers.add(p);
		
		p.setGameMode(GameMode.CREATIVE);
		BGVanish.makeVanished(p);
		BGChat.printPlayerChat(p, "�eYou are now a gamemaker!");
	}
	
	public static void remGameMaker(Player p) {
		gamemakers.remove(p);
		p.setGameMode(GameMode.SURVIVAL);
		BGVanish.makeVisible(p);
	}
	
	public static Boolean isGameMaker(Player p) {
		return gamemakers.contains(p);
	}
	
	public static Boolean isSpectator(Player p) {
		if(isGameMaker(p))
			return false;
		
		return spectators.contains(p);
	}
	
	public static ArrayList<Player> getSpectators() {
		return spectators;
	}
	
	public static ArrayList<Player> getGamemakers() {
		return gamemakers;
	}
	
	public static void addSpectator(Player p) {
		if(isGameMaker(p))
			return;
			
		spectators.add(p);
		p.setGameMode(GameMode.CREATIVE);
		BGVanish.makeVanished(p);
		for(int i=0;i<=8;i++) {
			p.getInventory().setItem(i, new ItemStack(Material.CROPS, 1));
		}
		BGChat.printPlayerChat(p, "�eYou are now a spectator!");
	}
	
	public static void remSpectator(Player p) {
		spectators.remove(p);
		p.setGameMode(GameMode.SURVIVAL);
		p.getInventory().clear();
		BGVanish.makeVisible(p);
	}
	
	public static Logger getPluginLogger() {
		return instance.getLogger();
	}
	
	public static File getPFile() {
		return instance.getFile();
	}
}
