package nu.granskogen.spela.DiscordSync;

import java.sql.*;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class DataSource {
	private static Main pl = Main.getInstance();
	private static HikariConfig config = new HikariConfig();
	private static HikariDataSource ds;

	
	static {
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			String host = pl.cfgm.getConfig().getString("database.host");
			String database = pl.cfgm.getConfig().getString("database.database");
			String username = pl.cfgm.getConfig().getString("database.username");
			String password = pl.cfgm.getConfig().getString("database.password");
			int port = 3306;
			String url = "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false";
			config.setJdbcUrl(url);
			config.setUsername(username);
			config.setPassword(password);
			config.addDataSourceProperty("cachePrepStmts", "true");
			config.addDataSourceProperty("prepStmtCacheSize", "250");
			config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
			ds = new HikariDataSource(config);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	private DataSource() {}
	
	public static Connection getconConnection() throws SQLException {
		return ds.getConnection();
	}
	
	public static void closeConnectionAndStatment(Connection connection, PreparedStatement statment) {
		if(connection != null) {
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		if(statment != null) {
			try {
				statment.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
}
