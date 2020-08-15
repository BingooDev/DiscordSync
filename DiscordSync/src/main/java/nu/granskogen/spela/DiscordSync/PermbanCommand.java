package nu.granskogen.spela.DiscordSync;

import java.util.UUID;

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
		pl.getProxy().getScheduler().runAsync(pl, new Runnable() {
			public void run() {
				if (sender.hasPermission("DiscordSync.permban.use")) {
					if (args.length < 2) {
						pl.sendMessage("syntax.permban", sender);
						return;
					}
					String reason = "";
					for (int i = 1; i < args.length; i++) {
						reason += args[i] + " ";
					}
					UUID uuid;
					uuid = pl.getUUIDFromPlayername(args[0]);
					if (uuid == null) {
						pl.sendMessage("noPlayer", sender);
						return;
					}
					if (pl.addPermban(uuid, reason, sender.getName()))
						pl.sendMessage("addBan", sender);
					else
						pl.sendMessage("alreadyBanned", sender);

				} else {
					pl.sendMessage("noPermission", sender);
					return;
				}
			}
		});
	}

}
