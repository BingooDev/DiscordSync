package nu.granskogen.spela.DiscordSync;

import java.util.UUID;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

public class UnPermbanCommand extends Command {
	Main pl = Main.getInstance();
	
	public UnPermbanCommand(String name) {
		super(name);
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if(!sender.hasPermission("DiscordSync.unpermban.use")) {
			pl.sendMessage("noPermission", sender);
			return;
		}
		
		if(args.length != 1) {
			pl.sendMessage("syntax.unpermban", sender);
			return;
		}
		
		UUID uuid;
		if((uuid = pl.getUUIDFromPlayername(args[0])) == null) {
			pl.sendMessage("noPlayer", sender);
			return;
		}
		
		if(!pl.isPlayerBanned(uuid)) {
			pl.sendMessage("notBanned", sender);
			return;
		}
		
		pl.removePermban(uuid);
		pl.sendMessage("removeBan", sender);
	}

}
