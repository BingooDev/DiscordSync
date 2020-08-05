package nu.granskogen.spela.DiscordSync;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.event.EventBus;
import net.luckperms.api.event.EventSubscription;
import net.luckperms.api.event.player.PlayerLoginProcessEvent;
import net.luckperms.api.node.Node;

public class LuckPermsEventListener {
	public EventSubscription<PlayerLoginProcessEvent> nodeListener;
	Main pl = Main.getInstance();
	
	public LuckPermsEventListener(LuckPerms api) {
		
		// get the LuckPerms event bus
        EventBus eventBus = api.getEventBus();
        
        // subscribe to an event using a lambda
        nodeListener = eventBus.subscribe(PlayerLoginProcessEvent.class, e -> {
        	for (Node node : e.getUser().getNodes()) {
				if(node.getKey().equals("group.omega")) {
					if(!node.hasExpired()) {
						System.err.println(e.getUniqueId());
						if(node.getExpiry() == null) {
							pl.sendAddDiscordRank(e.getUniqueId(), "omega", null);
						} else {
							pl.sendAddDiscordRank(e.getUniqueId(), "omega", node.getExpiry().getEpochSecond());							
						}
					}
				}
				if(node.getKey().equals("group.decent")) {
					if(!node.hasExpired()) {
						pl.sendAddDiscordRank(e.getUniqueId(), "decent", null);
					}
				}
			}
        });
	}
}
