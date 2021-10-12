package nu.granskogen.spela.DiscordSync;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class DiscordCommand extends Command {
	Main pl = Main.getInstance();

	public DiscordCommand(String name) {
		super(name);
	}

	@Override
	public void execute(CommandSender sender, final String[] args) {
		if (args.length == 0) {
			TextComponent text = new TextComponent(
					ChatColor.translateAlternateColorCodes('&', pl.cfgm.getLanguage().getString("discordLink")));
			text.setClickEvent(new ClickEvent(Action.OPEN_URL, pl.cfgm.getLanguage().getString("discordUrl")));
			text.setHoverEvent(new HoverEvent(net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT,
					new ComponentBuilder(ChatColor.translateAlternateColorCodes('&',
							pl.cfgm.getLanguage().getString("discordLinkHover"))).create()));
			sender.sendMessage(text);

		} else if (args.length == 1) {
			if (args[0].equalsIgnoreCase("addrank")) {
				pl.sendMessage("syntax.addrank", sender);
			} else if (args[0].equalsIgnoreCase("removerank")) {
				pl.sendMessage("syntax.removerank", sender);
			} else {
				if (!(sender instanceof ProxiedPlayer)) {
					pl.sendMessage("onlyPlayers", sender);
					return;
				}
				final ProxiedPlayer player = (ProxiedPlayer) sender;

				pl.getProxy().getScheduler().runAsync(pl, new Runnable() {

					public void run() {
						Connection con = null;
						PreparedStatement stat = null;
						PreparedStatement stat2 = null;
						String discordId = "";
						try {
							con = DataSource.getconConnection();
							stat = con.prepareStatement(SQLQuery.SELECT_USER_FROM_UUID.toString());
							stat.setString(1, player.getUniqueId().toString());
							ResultSet rs = stat.executeQuery();

							if (rs.next()) {
								pl.sendMessage("alreadyLinked", player);
								return;
							}
							
							stat2 = con.prepareStatement(SQLQuery.SELECT_USER_FROM_TOKEN.toString());
							stat2.setString(1, args[0]);
							rs = stat2.executeQuery();

							if (!rs.next()) {
								pl.sendMessage("notInDatabase", player);
								return;
							}

							PreparedStatement addToDatabase = con.prepareStatement(SQLQuery.UPDATE_USER.toString());
							addToDatabase.setString(1, player.getUniqueId().toString());
							addToDatabase.setString(2, player.getName());
							addToDatabase.setString(3, args[0]);
							addToDatabase.executeUpdate();
							discordId = rs.getString("discordId");

						} catch (SQLException e) {
							e.printStackTrace();
						} finally {
							DataSource.closeConnectionAndStatment(con, stat);
						}
						pl.sendVerifiedPlayerToDiscord(player.getName(), player.getUniqueId(), discordId);
						if(pl.hasLuckperms) {
							LuckPermsCheck.addRanks(player);
						}
						
						pl.addOnlineVerifiedPlayer(player.getUniqueId());
						pl.sendMessage("added", player);
					}
				});
			}
		} else if (args.length == 3) {
			if (args[0].equalsIgnoreCase("addrank") || args[0].equalsIgnoreCase("removerank")) {
				if (!sender.hasPermission("DiscordSync.changeRanks")) {
					pl.sendMessage("noPermission", sender);
					return;
				}
				ProxiedPlayer player = pl.getProxy().getPlayer(args[1]);
				if (player == null) {
					pl.sendMessage("noPlayer", sender);
					return;
				}
				List<String> ranks = Arrays.asList("omega", "decent");
				if (!ranks.contains(args[2])) {
					pl.sendMessage("notARank", sender);
					return;
				}

				if (args[0].equalsIgnoreCase("addrank")) {
					pl.sendAddDiscordRank(player.getUniqueId(), args[2], null);
					pl.sendMessage("rankAdded", sender);
				} else if (args[0].equalsIgnoreCase("removerank")) {
					pl.sendRemoveDiscordRank(player.getUniqueId(), args[2]);
					pl.sendMessage("rankRemoved", sender);
				}
			}
		} else if (args.length == 4) {
			if (args[0].equalsIgnoreCase("addrank")) {
				if (!sender.hasPermission("DiscordSync.changeRanks")) {
					pl.sendMessage("noPermission", sender);
					return;
				}
				ProxiedPlayer player = pl.getProxy().getPlayer(args[1]);
				if (player == null) {
					pl.sendMessage("noPlayer", sender);
					return;
				}
				List<String> ranks = Arrays.asList("omega", "decent");
				if (!ranks.contains(args[2])) {
					pl.sendMessage("notARank", sender);
					return;
				}
				if (!args[3].matches("^[0-9dhms]*$")) {
					pl.sendMessage("wrongTimeFormat", sender);
					return;
				}

				pl.sendAddDiscordRank(player.getUniqueId(), args[2],
						(new Date()).getTime()/1000 + pl.getTimeInSeconds(args[3]));
				pl.sendMessage("rankAdded", sender);

			}
		} else {
			if (args[0].equalsIgnoreCase("addrank")) {
				pl.sendMessage("syntax.addrank", sender);
			} else if (args[0].equalsIgnoreCase("removerank")) {
				pl.sendMessage("syntax.removerank", sender);
			}
		}
	}

}
