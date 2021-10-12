package nu.granskogen.spela.DiscordSync;

import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class LuckPermsCheck {
	static Main pl = Main.getInstance();
	
	public static void addRanks(ProxiedPlayer player) {
		User user = pl.api.getUserManager().getUser(player.getUniqueId());
		for (Node node : user.getNodes()) {
			if(node.getKey().equals("group.omega")) {
				if(!node.hasExpired()) {
					if(node.getExpiry() == null) {
						pl.sendAddDiscordRank(player.getUniqueId(), "omega", null);
					} else {
						pl.sendAddDiscordRank(player.getUniqueId(), "omega", node.getExpiry().getEpochSecond());							
					}
				}
			}
			if(node.getKey().equals("group.decent")) {
				if(!node.hasExpired()) {
					pl.sendAddDiscordRank(player.getUniqueId(), "decent", null);
				}
			}
		}
	}
}
