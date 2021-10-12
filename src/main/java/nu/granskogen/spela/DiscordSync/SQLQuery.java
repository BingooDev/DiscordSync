package nu.granskogen.spela.DiscordSync;

public enum SQLQuery {
	SELECT_USER_FROM_TOKEN("SELECT * FROM verifiedusers WHERE token=?;"),
	SELECT_USER_FROM_UUID("SELECT * FROM verifiedusers WHERE uuid=?;"),
	UPDATE_USER("UPDATE verifiedusers SET uuid=?,token='',minecraftName=? WHERE token=?;"),
	UPDATE_USER_OMEGA_TRUE("UPDATE verifiedusers SET isomega=1,token='' WHERE uuid=?;"),
	UPDATE_USER_DECENT_TRUE("UPDATE verifiedusers SET isdecent=1,token='' WHERE uuid=?;"),
	UPDATE_USER_OMEGA_FALSE("UPDATE verifiedusers SET isomega=0,token='' WHERE uuid=?;"),
	UPDATE_USER_DECENT_FALSE("UPDATE verifiedusers SET isdecent=0,token='' WHERE uuid=?;"),
	UPDATE_USERNAME("UPDATE verifiedusers SET minecraftname=? WHERE uuid=?"),
	PERMBANS_SELECT_USER_FROM_UUID("SELECT * FROM permbans WHERE uuid=?;"),
	PERMBANS_SELECT_ALL_USERS("SELECT * FROM permbans;"),
	PERMBANS_ADD_USER("INSERT INTO permbans (uuid, reason, operator, date) VALUES (?, ?, ?, ?);"),
	PERMBANS_REMOVE_USER("DELETE FROM permbans WHERE uuid=?;"),
	PERMBANS_CREATE_TABLE("CREATE TABLE IF NOT EXISTS permbans (id INT NOT NULL AUTO_INCREMENT PRIMARY KEY, uuid text, operator text, reason longtext, date timestamp, discordId text);");
	
	private String query;
	
	SQLQuery(String query) {
		this.query = query;
	}
	
	@Override
    public String toString() {
        return query;
    }
}
