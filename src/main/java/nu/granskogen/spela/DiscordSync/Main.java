package nu.granskogen.spela.DiscordSync;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.UUID;
import java.util.stream.Collectors;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;

public class Main extends Plugin {
	private static Main instance;
	public ConfigManager cfgm;
	public boolean hasLuckperms = false;
	public LuckPerms api;
	private ArrayList<UUID> bannedPlayers = new ArrayList<UUID>();
	private ArrayList<UUID> onlineVeriedPlayers = new ArrayList<UUID>();
	private Broadcast broadcastDiscord;

	public void onEnable() {
		instance = this;
		loadConfigManager();
		getProxy().getPluginManager().registerCommand(this, new DiscordCommand("discord"));
		getProxy().getPluginManager().registerCommand(this, new PermbanCommand("permban"));
		getProxy().getPluginManager().registerCommand(this, new UnPermbanCommand("unpermban"));
		getProxy().getPluginManager().registerCommand(this, new CheckCommand("checkpermban"));
		getProxy().getPluginManager().registerListener(this, new EventListener());
		createBannedPlayersTable(); // If table exists, it will not be created again.
		loadBannedPlayers();

		if (getProxy().getPluginManager().getPlugin("LuckPerms") == null) {
			System.err.println("Needs LuckPerms to manage ranks.");
		} else {
			hasLuckperms = true;
			api = LuckPermsProvider.get();
			new LuckPermsEventListener(api);
		}
		
		this.broadcastDiscord = new Broadcast("discordLink", "discordLinkHover", "discordUrl", 108000); //108000s = 0.5h
		this.broadcastDiscord.startLoop();
	}

	public void sendMessage(String messagePath, CommandSender sender) {
		sender.sendMessage(new TextComponent(
				ChatColor.translateAlternateColorCodes('&', cfgm.getLanguage().getString(messagePath))));
	}

	public void sendVerifiedPlayerToDiscord(String name, UUID uuid, String discordId) {
		int port = 3003;
		try (Socket socket = new Socket("localhost", port)) {

			OutputStream output = socket.getOutputStream();
			JsonObject obj = new JsonObject();

			obj.addProperty("type", "VerifyUser");
			obj.addProperty("discordId", discordId);
			obj.addProperty("minecraftName", name);
			obj.addProperty("uuid", uuid.toString());
			output.write(obj.toString().getBytes());

			socket.close();
		} catch (UnknownHostException ex) {

			System.out.println("Server not found: " + ex.getMessage());

		} catch (IOException ex) {

			System.out.println("I/O error: " + ex.getMessage());
		}
	}

	public void sendAddDiscordRank(UUID uuid, String rank, Long expire) {
		changeDiscordRank(uuid, rank, "AddRank", expire);
	}

	public void sendRemoveDiscordRank(UUID uuid, String rank) {
		changeDiscordRank(uuid, rank, "RemoveRank", null);
	}

	private void changeDiscordRank(UUID uuid, String rank, String addOrRemove, Long expire) {
		int port = 3003;
		try (Socket socket = new Socket("localhost", port)) {

			OutputStream output = socket.getOutputStream();
			JsonObject obj = new JsonObject();

			obj.addProperty("type", addOrRemove);
			obj.addProperty("uuid", uuid.toString());
			obj.addProperty("rank", rank);
			if (expire != null) {
				obj.addProperty("expire", expire);
			}
			output.write(obj.toString().getBytes());

			socket.close();
		} catch (UnknownHostException ex) {

			System.out.println("Server not found: " + ex.getMessage());

		} catch (IOException ex) {

			System.out.println("I/O error: " + ex.getMessage());
		}
	}

	public void changeDiscordNickname(UUID uuid, String newName) {
		int port = 3003;
		try (Socket socket = new Socket("localhost", port)) {

			OutputStream output = socket.getOutputStream();
			JsonObject obj = new JsonObject();

			obj.addProperty("type", "UpdateNickname");
			obj.addProperty("uuid", uuid.toString());
			obj.addProperty("minecraftName", newName);
			output.write(obj.toString().getBytes());

			socket.close();
		} catch (UnknownHostException ex) {

			System.out.println("Server not found: " + ex.getMessage());

		} catch (IOException ex) {

			System.out.println("I/O error: " + ex.getMessage());
		}
	}

	public boolean addPermban(UUID uuid, String reason, String operator, String nameOfBanPlayer) {
		Connection con = null;
		PreparedStatement stat = null;
		PreparedStatement stat2 = null;
		final String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
		try {
			con = DataSource.getconConnection();
			stat = con.prepareStatement(SQLQuery.PERMBANS_SELECT_USER_FROM_UUID.toString());
			stat.setString(1, uuid.toString());
			ResultSet rs = stat.executeQuery();

			if (rs.next())
				return false;

			stat2 = con.prepareStatement(SQLQuery.PERMBANS_ADD_USER.toString());
			stat2.setString(1, uuid.toString());
			stat2.setString(2, reason);
			stat2.setString(3, operator);
			stat2.setString(4, date);
			stat2.execute();

		} catch (SQLException e) {
			e.printStackTrace();
			DataSource.closeConnectionAndStatment(con, stat);
			return false;
		} finally {
			DataSource.closeConnectionAndStatment(con, stat);
		}

		ProxiedPlayer player = getProxy().getPlayer(uuid);
		if (player != null)
			player.disconnect(new TextComponent(getBanMessage(operator, reason, date)));
		sendBanNotification(operator, nameOfBanPlayer, reason);
		if (!bannedPlayers.contains(uuid))
			bannedPlayers.add(uuid);

		int port = 3003;
		try (Socket socket = new Socket("localhost", port)) {

			OutputStream output = socket.getOutputStream();
			JsonObject obj = new JsonObject();

			obj.addProperty("type", "AddBan");
			obj.addProperty("uuid", uuid.toString());
			obj.addProperty("reason", reason);
			obj.addProperty("operator", operator);
			obj.addProperty("name", nameOfBanPlayer);
			obj.addProperty("date", date);
			
			output.write(obj.toString().getBytes());

			socket.close();
		} catch (UnknownHostException ex) {

			System.out.println("Server not found: " + ex.getMessage());

		} catch (IOException ex) {
			System.out.println("I/O error: " + ex.getMessage());
		}
		return true;
	}

	public boolean removePermban(UUID uuid) {
		Connection con = null;
		PreparedStatement stat = null;
		PreparedStatement stat2 = null;
		String discordId = null;
		try {
			con = DataSource.getconConnection();
			
			stat = con.prepareStatement(SQLQuery.PERMBANS_SELECT_USER_FROM_UUID.toString());
			stat.setString(1, uuid.toString());
			ResultSet rs = stat.executeQuery();
			
			if(!rs.next())
				return false;
			
			discordId = rs.getString("discordId");

			stat2 = con.prepareStatement(SQLQuery.PERMBANS_REMOVE_USER.toString());
			stat2.setString(1, uuid.toString());
			stat2.executeUpdate();

		} catch (SQLException e) {
			e.printStackTrace();
			DataSource.closeConnectionAndStatment(con, stat);
			return false;
		} finally {
			DataSource.closeConnectionAndStatment(con, stat);
		}
		if (bannedPlayers.contains(uuid))
			bannedPlayers.remove(uuid);

		if(discordId != null) {
			int port = 3003;
			try (Socket socket = new Socket("localhost", port)) {
				
				OutputStream output = socket.getOutputStream();
				JsonObject obj = new JsonObject();
				
				obj.addProperty("type", "RemoveBan");
				obj.addProperty("uuid", uuid.toString());
				obj.addProperty("discordId", discordId);
				output.write(obj.toString().getBytes());
				
				socket.close();
			} catch (UnknownHostException ex) {
				
				System.out.println("Server not found: " + ex.getMessage());
				
			} catch (IOException ex) {
				System.out.println("I/O error: " + ex.getMessage());
			}			
		}
		return true;
	}

	public long getTimeInSeconds(String time) {
		String stringTimeWithSpace = "";
		long timeInSec = 0;
		for (String ch : time.split("")) {
			if (ch.equals("d") || ch.equals("h") || ch.equals("m") || ch.equals("s")) {
				stringTimeWithSpace += ch + " ";
			} else {
				stringTimeWithSpace += ch;
			}
		}
		for (String timeWithUnit : stringTimeWithSpace.split(" ")) {
			if (timeWithUnit.contains("d")) {
				String days = timeWithUnit.replace("d", "");
				if (days.isEmpty()) {
					days = "0";
				}
				timeInSec += Double.parseDouble(days) * 24 * 60 * 60;
			} else if (timeWithUnit.contains("h")) {
				String hours = timeWithUnit.replace("h", "");
				if (hours.isEmpty()) {
					hours = "0";
				}
				timeInSec += Double.parseDouble(hours) * 60 * 60;
			} else if (timeWithUnit.contains("m")) {
				String minutes = timeWithUnit.replace("m", "");
				if (minutes.isEmpty()) {
					minutes = "0";
				}
				timeInSec += Double.parseDouble(minutes) * 60;
			} else if (timeWithUnit.contains("s")) {
				String sec = timeWithUnit.replace("s", "");
				if (sec.isEmpty()) {
					sec = "0";
				}
				timeInSec += Double.parseDouble(sec);
			}
		}
		return timeInSec;
	}

	public String getBanMessage(String operator, String reason, String date) {
		String message = cfgm.getLanguage().getStringList("banMessage.layout").stream()
				.collect(Collectors.joining("\n"));
		message = ChatColor.translateAlternateColorCodes('&',
				message.replace("%OPERATOR%", operator).replace("%REASON%", reason).replace("%DATE%", date));
		return message;
	}

	public String getBanNotification(String operator, String bannedPlayerName, String reason) {
		String message = cfgm.getLanguage().getStringList("banMessage.notification.layout").stream()
				.collect(Collectors.joining("\n"));
		message = ChatColor.translateAlternateColorCodes('&',
				message.replace("%OPERATOR%", operator).replace("%REASON%", reason).replace("%NAME%", bannedPlayerName));
		return message;
	}

	public String getCheckMessage(UUID uuid) {
		Connection con = null;
		PreparedStatement stat = null;
		boolean banned = false;
		String reason = "";
		try {
			con = DataSource.getconConnection();

			stat = con.prepareStatement(SQLQuery.PERMBANS_SELECT_USER_FROM_UUID.toString());
			stat.setString(1, uuid.toString());
			ResultSet rs = stat.executeQuery();

			if (!rs.next()) {
				banned = false;
			} else {
				banned = true;
				reason = rs.getString("reason");
			}

		} catch (SQLException e) {
			e.printStackTrace();
			DataSource.closeConnectionAndStatment(con, stat);
		} finally {
			DataSource.closeConnectionAndStatment(con, stat);
		}

		String message = cfgm.getLanguage().getStringList("check.layout").stream().collect(Collectors.joining("\n"));
		String banMsg = "";
		banMsg = cfgm.getLanguage().getString("check.banned." + banned);
		if (banned) {
			message = message.replace("%UUID%", uuid.toString()).replace("%BANNED%",
					banMsg + "\n  " + cfgm.getLanguage().getString("check.reason").replace("%REASON%", reason));
		} else if (!banned) {
			message = message.replace("%UUID%", uuid.toString()).replace("%BANNED%", banMsg);
		}

		return ChatColor.translateAlternateColorCodes('&', message);
	}

	public void sendBanNotification(String operator, String bannedPlayerName, String reason) {
		String notification = getBanNotification(operator, bannedPlayerName, reason);
		for (ProxiedPlayer player : getProxy().getPlayers()) {
			if (player.hasPermission("DiscordSync.permban.notify"))
				player.sendMessage(new TextComponent(notification));
		}
	}

	public void loadConfigManager() {
		cfgm = new ConfigManager();
		cfgm.setup();
	}

	public static Main getInstance() {
		return instance;
	}

	public Collection<UUID> getBannedPlayers() {
		return Collections.unmodifiableCollection(bannedPlayers);
	}
	
	public Collection<UUID> getVerifiedPlayer() {
		return Collections.unmodifiableCollection(onlineVeriedPlayers);
	}
	
	public void addOnlineVerifiedPlayer(UUID uuid) {
		if(!onlineVeriedPlayers.contains(uuid))
			onlineVeriedPlayers.add(uuid);
	}
	
	public void removeOnlineVerifiedPlayer(UUID uuid) {
		if(onlineVeriedPlayers.contains(uuid))
			onlineVeriedPlayers.remove(uuid);
	}

	private void createBannedPlayersTable() {
		Connection con = null;
		PreparedStatement stat = null;
		try {
			con = DataSource.getconConnection();
			stat = con.prepareStatement(SQLQuery.PERMBANS_CREATE_TABLE.toString());
			stat.executeUpdate();

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DataSource.closeConnectionAndStatment(con, stat);
		}
	}

	private void loadBannedPlayers() {
		Connection con = null;
		PreparedStatement stat = null;
		try {
			con = DataSource.getconConnection();
			stat = con.prepareStatement(SQLQuery.PERMBANS_SELECT_ALL_USERS.toString());
			ResultSet rs = stat.executeQuery();

			while (rs.next()) {
				bannedPlayers.add(UUID.fromString(rs.getString("uuid")));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DataSource.closeConnectionAndStatment(con, stat);
		}
	}

	public boolean isPlayerBanned(ProxiedPlayer player) {
		if (bannedPlayers.contains(player.getUniqueId()))
			return true;
		return false;
	}

	public boolean isPlayerBanned(UUID uuid) {
		if (bannedPlayers.contains(uuid))
			return true;
		return false;
	}

	public String getBannedMessageForBannedPlayer(UUID uuid) {
		Connection con = null;
		PreparedStatement stat = null;
		try {
			con = DataSource.getconConnection();
			stat = con.prepareStatement(SQLQuery.PERMBANS_SELECT_USER_FROM_UUID.toString());
			stat.setString(1, uuid.toString());
			ResultSet rs = stat.executeQuery();

			if (rs.next()) {
				return getBanMessage(rs.getString("operator"), rs.getString("reason"), rs.getString("date"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DataSource.closeConnectionAndStatment(con, stat);
		}
		return null;
	}

	public UUID getUUIDFromPlayername(String name) {
		URL url;
		try {
			url = new URL("https://api.mojang.com/users/profiles/minecraft/" + name);
			BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"));
			String line = reader.readLine();
			if (line != null) {
				JsonObject json = (JsonObject) (new JsonParser()).parse(line);
				return getUuidFromUuidWithoutDashes(json.get("id").getAsString());
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public UUID getUuidFromUuidWithoutDashes(String uuid) {
		// To get a UUID from a string, it requires dashes, so we have to format it with
		// regex
		String uuidWithDashes = uuid.replaceFirst(
				"([0-9a-fA-F]{8})([0-9a-fA-F]{4})([0-9a-fA-F]{4})([0-9a-fA-F]{4})([0-9a-fA-F]+)", "$1-$2-$3-$4-$5");
		return UUID.fromString(uuidWithDashes);
	}
	
	public String getFormatedMessageFromLanguagePath(String path) {
		return ChatColor.translateAlternateColorCodes('&', cfgm.getLanguage().getString(path));
	}
}
