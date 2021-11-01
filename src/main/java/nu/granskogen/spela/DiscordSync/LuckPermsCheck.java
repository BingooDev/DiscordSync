package nu.granskogen.spela.DiscordSync;

import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.UUID;

public class LuckPermsCheck {
	static Main pl = Main.getInstance();
	
	public static void addRanks(ProxiedPlayer player) {
		User user = pl.api.getUserManager().getUser(player.getUniqueId());
		addRanks(user);
	}

	public static void addRanks(User user) {
		for (Node node : user.getNodes()) {
			if(node.getKey().equals("group.omegaplus")) {
				giveRankOnDiscord(user.getUniqueId(), node, "omegaplus");
			}
			if(node.getKey().equals("group.omega") || node.getKey().equals("group.omegalite")) {
				giveRankOnDiscord(user.getUniqueId(), node, "omega");
			}
			if(node.getKey().equals("group.decent")) {
				if(!node.hasExpired()) {
					pl.sendAddDiscordRank(user.getUniqueId(), "decent", null);
				}
			}
		}
	}

	private static void giveRankOnDiscord(UUID uuid, Node node, String rank) {
		if (!node.hasExpired()) {
			if (node.getExpiry() == null) {
				pl.sendAddDiscordRank(uuid, rank, null);
			} else {
				pl.sendAddDiscordRank(uuid, rank, node.getExpiry().getEpochSecond());
			}
		}
	}
}
