package nu.granskogen.spela.DiscordSync;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

public class PermbanCommand extends Command {
	Main pl = Main.getInstance();
	
	public PermbanCommand(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if(sender.hasPermission("DiscordSync.permban")) {
			
		} else {
			pl.sendMessage("noPermission", sender);
			return;
		}
	}

}
