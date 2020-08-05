package nu.granskogen.spela.DiscordSync;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class EventListener implements Listener {
	Main pl = Main.getInstance();
	
	@EventHandler
	public void onLogin(final LoginEvent event) {
		pl.getProxy().getScheduler().runAsync(pl, new Runnable() {
			
			@Override
			public void run() {
				Connection con = null;
				PreparedStatement stat = null;
				try {
					UUID uuid = event.getConnection().getUniqueId();
					
					con = DataSource.getconConnection();
					stat = con.prepareStatement(SQLQueries.SELECT_USER_FROM_UUID.toString());
					stat.setString(1, uuid.toString());
					ResultSet rs = stat.executeQuery();

					if (!rs.next()) 
						return;
					
					String name = event.getConnection().getName();
					if(rs.getString("minecraftname") == null || !rs.getString("minecraftname").equals(name)) {
						PreparedStatement updateName = con.prepareStatement(SQLQueries.UPDATE_USERNAME.toString());
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
			}
		});
	}

}
