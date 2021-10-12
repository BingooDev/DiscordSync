package nu.granskogen.spela.DiscordSync;

import java.util.Locale;
import java.util.UUID;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

public class PermbanCommand extends Command implements TabExecutor {
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
					if (pl.addPermban(uuid, reason, sender.getName(), args[0]))
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
	
	@Override
	public Iterable<String> onTabComplete(CommandSender sender, String[] args)
    {
        final String lastArg = ( args.length > 0 ) ? args[args.length - 1].toLowerCase( Locale.ROOT ) : "";
        return Iterables.transform( Iterables.filter( ProxyServer.getInstance().getPlayers(), new Predicate<ProxiedPlayer>()
        {
            @Override
            public boolean apply(ProxiedPlayer player)
            {
                return player.getName().toLowerCase( Locale.ROOT ).startsWith( lastArg );
            }
        } ), new Function<ProxiedPlayer, String>()
        {
            @Override
            public String apply(ProxiedPlayer player)
            {
                return player.getName();
            }
        } );
    }

}
