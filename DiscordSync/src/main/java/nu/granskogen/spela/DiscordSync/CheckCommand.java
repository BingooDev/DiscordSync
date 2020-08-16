package nu.granskogen.spela.DiscordSync;

import java.util.UUID;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;

public class CheckCommand extends Command {
	Main pl = Main.getInstance();
	
	public CheckCommand(String name) {
		super(name);
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if(!sender.hasPermission("DiscordSync.check.use")) {
			pl.sendMessage("noPermission", sender);
			return;
		}
		
		if(args.length != 1) {
			pl.sendMessage("syntax.check", sender);
			return;
		}
		
		UUID uuid;
		if((uuid = pl.getUUIDFromPlayername(args[0])) == null) {
			pl.sendMessage("noPlayer", sender);
			return;
		}
		
		sender.sendMessage(new TextComponent(pl.getCheckMessage(uuid)));
	}

}
