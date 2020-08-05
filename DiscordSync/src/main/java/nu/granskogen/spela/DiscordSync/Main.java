package nu.granskogen.spela.DiscordSync;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.UUID;

import com.google.gson.JsonObject;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Plugin;

public class Main extends Plugin {
	private static Main instance;
	public ConfigManager cfgm;
	public boolean hasLuckperms = false;
	public LuckPerms api;
	
	public void onEnable() {
		instance = this;
		loadConfigManager();
		getProxy().getPluginManager().registerCommand(this, new DiscordCommand("discord"));
		getProxy().getPluginManager().registerListener(this, new EventListener());
		
		
		if(getProxy().getPluginManager().getPlugin("LuckPerms") == null) {
			System.err.println("Needs LuckPerms to manage ranks.");
		} else {
			hasLuckperms = true;
			api = LuckPermsProvider.get();
			new LuckPermsEventListener(api);
		}
	}
	
	public void loadConfigManager() {
		cfgm = new ConfigManager();
		cfgm.setup();
	}
	
	public static Main getInstance() {
		return instance;
	}
	
	public void sendMessage(String messagePath, CommandSender sender) {
		sender.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', cfgm.getLanguage().getString(messagePath))));
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
			if(expire != null) {
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
	
	public long getTimeInSeconds(String time) {
		String stringTimeWithSpace = "";
		long timeInSec = 0;
		for (String ch : time.split("")) {
			if (ch.equals("h") || ch.equals("m") || ch.equals("s")) {
				stringTimeWithSpace += ch + " ";
			} else {
				stringTimeWithSpace += ch;
			}
		}
		for (String timeWithUnit : stringTimeWithSpace.split(" ")) {
			if (timeWithUnit.contains("h")) {
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
}
