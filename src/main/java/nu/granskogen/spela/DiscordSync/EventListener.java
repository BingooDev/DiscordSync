package nu.granskogen.spela.DiscordSync;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class EventListener implements Listener {
	Main pl = Main.getInstance();
	
	@EventHandler
	public void onLogin(final PostLoginEvent event) {
		
		pl.getProxy().getScheduler().runAsync(pl, new Runnable() {
			
			@Override
			public void run() {
				// Kick Player if banned
				UUID uuid = event.getPlayer().getUniqueId();
				if(pl.isPlayerBanned(uuid)) {
					event.getPlayer().disconnect(new TextComponent(pl.getBannedMessageForBannedPlayer(uuid)));
					return;
				}

				// Update Nickname on Discord
				Connection con = null;
				PreparedStatement stat = null;
				try {
					con = DataSource.getconConnection();
					stat = con.prepareStatement(SQLQuery.SELECT_USER_FROM_UUID.toString());
					stat.setString(1, uuid.toString());
					ResultSet rs = stat.executeQuery();

					if (!rs.next()) 
						return;
					
					String name = event.getPlayer().getName();
					if(rs.getString("minecraftname") == null || !rs.getString("minecraftname").equals(name)) {
						PreparedStatement updateName = con.prepareStatement(SQLQuery.UPDATE_USERNAME.toString());
						updateName.setString(1, name);
						updateName.setString(2, uuid.toString());
						updateName.executeUpdate();
						pl.changeDiscordNickname(uuid, name);
					}


				} catch (SQLException e) {
					e.printStackTrace();
				} finally {
					DataSource.closeConnectionAndStatment(con, stat);
				}
				pl.addOnlineVerifiedPlayer(uuid);
			}
		});
	}
	
	@EventHandler
	public void onDisconnect(PlayerDisconnectEvent event) {
		pl.removeOnlineVerifiedPlayer(event.getPlayer().getUniqueId());
	}

}
