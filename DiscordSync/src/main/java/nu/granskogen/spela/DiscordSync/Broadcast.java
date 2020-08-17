package nu.granskogen.spela.DiscordSync;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.HoverEvent.Action;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.scheduler.ScheduledTask;

public class Broadcast {
	Main pl = Main.getInstance();

	private String messagePath;
	private String hoverMessagePath;
	private String clickUrlLanguagePath;
	private ScheduledTask task;
	private int interval;

	public Broadcast(String messagePath, String hoverMessagePath, String clickUrlLanguagePath, int interval) {
		super();
		this.messagePath = messagePath;
		this.hoverMessagePath = hoverMessagePath;
		this.clickUrlLanguagePath = clickUrlLanguagePath;
		this.interval = interval;
	}

	public void startLoop() {
		TextComponent msg = new TextComponent(pl.getFormatedMessageFromLanguagePath(this.messagePath));
		msg.setHoverEvent(new HoverEvent(Action.SHOW_TEXT,
				new ComponentBuilder(pl.getFormatedMessageFromLanguagePath(this.hoverMessagePath)).create()));
		msg.setClickEvent(new ClickEvent(net.md_5.bungee.api.chat.ClickEvent.Action.OPEN_URL,
				pl.cfgm.getLanguage().getString(this.clickUrlLanguagePath)));
		
		this.task = pl.getProxy().getScheduler().schedule(pl, new Runnable() {
			public void run() {
				Collection<UUID> verifiedPlayers = pl.getVerifiedPlayer();
				for (ProxiedPlayer player : pl.getProxy().getPlayers()) {
					if (!verifiedPlayers.contains(player.getUniqueId()))
						player.sendMessage(msg);
				}
			}
		}, 0, this.interval, TimeUnit.SECONDS);
	}

	public void stopLoop() {
		task.cancel();
	}

	public void restartLoop() {
		stopLoop();
		startLoop();
	}

	// Getters and setters

	public void setMessagePath(String messagePath) {
		this.messagePath = messagePath;

		// Need to restart loop to update message path
		restartLoop();
	}

	public String getMessagePath() {
		return this.messagePath;
	}

	public void setHoverMessagePath(String hoverMessagePath) {
		this.hoverMessagePath = hoverMessagePath;

		// Need to restart loop to update message path
		restartLoop();
	}

	public String getHoverMessagePath() {
		return this.hoverMessagePath;
	}

	public void setClickUrlLanguagePath(String clickUrlLanguagePath) {
		this.clickUrlLanguagePath = clickUrlLanguagePath;

		// Need to restart loop to update message path
		restartLoop();
	}

	public String getClickUrlLanguagePath() {
		return this.clickUrlLanguagePath;
	}
}
