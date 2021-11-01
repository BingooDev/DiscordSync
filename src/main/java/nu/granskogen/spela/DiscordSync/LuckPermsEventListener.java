package nu.granskogen.spela.DiscordSync;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.event.EventBus;
import net.luckperms.api.event.EventSubscription;
import net.luckperms.api.event.player.PlayerLoginProcessEvent;

public class LuckPermsEventListener {
	public EventSubscription<PlayerLoginProcessEvent> nodeListener;
	Main pl = Main.getInstance();
	
	public LuckPermsEventListener(LuckPerms api) {
		
		// get the LuckPerms event bus
        EventBus eventBus = api.getEventBus();
        
        // subscribe to an event using a lambda
        nodeListener = eventBus.subscribe(PlayerLoginProcessEvent.class, e -> {
			if(e.getUser() != null)
        		LuckPermsCheck.addRanks(e.getUser());
        });
	}

}
