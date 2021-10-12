package nu.granskogen.spela.DiscordSync;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

public class ConfigManager {

	private Main pl = Main.getInstance();

	// Files and file configs here
	public Configuration languagecfg;
	public File languagefile;
	public Configuration configcfg;
	public File configfile;
	// ---------------------------

	public void setup() {
		if (!pl.getDataFolder().exists()) {
			pl.getDataFolder().mkdir();
		}

		languagefile = new File(pl.getDataFolder(), "language.yml");
		configfile = new File(pl.getDataFolder(), "config.yml");

		if (!languagefile.exists()) {
			InputStream stream = pl.getResourceAsStream("language.yml");
			File dest = new File(pl.getDataFolder(), "language.yml");
			copy(stream, dest);
		}
		if (!configfile.exists()) {
			InputStream stream = pl.getResourceAsStream("config.yml");
			File dest = new File(pl.getDataFolder(), "config.yml");
			copy(stream, dest);
		}

		try {
			languagecfg = ConfigurationProvider.getProvider(YamlConfiguration.class).load(languagefile);
			configcfg = ConfigurationProvider.getProvider(YamlConfiguration.class).load(configfile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void copy(InputStream in, File file) {
		try {
			OutputStream out = new FileOutputStream(file);
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			out.close();
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Configuration getLanguage() {
		return languagecfg;
	}

	public void saveLanguage() {
		try {
			ConfigurationProvider.getProvider(YamlConfiguration.class).save(languagecfg, languagefile);
		} catch (IOException e) {
			System.err.println("§4Could not save the config.yml file!");
		}
	}

	public void reloadLanguage() {
		try {
			languagecfg = ConfigurationProvider.getProvider(YamlConfiguration.class).load(languagefile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Configuration getConfig() {
		return configcfg;
	}

	public void saveConfig() {
		try {
			ConfigurationProvider.getProvider(YamlConfiguration.class).save(configcfg, configfile);
		} catch (IOException e) {
			System.err.println("§4Could not save the config.yml file!");
		}
	}

	public void reloadConfig() {
		try {
			configcfg = ConfigurationProvider.getProvider(YamlConfiguration.class).load(configfile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
