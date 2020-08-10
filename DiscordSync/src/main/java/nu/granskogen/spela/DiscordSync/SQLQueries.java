package nu.granskogen.spela.DiscordSync;

public enum SQLQueries {
	SELECT_USER_FROM_TOKEN("SELECT * FROM verifiedusers WHERE token=?;"),
	SELECT_USER_FROM_UUID("SELECT * FROM verifiedusers WHERE uuid=?;"),
	UPDATE_USER("UPDATE verifiedusers SET uuid=?,token='',minecraftName=? WHERE token=?;"),
	UPDATE_USER_OMEGA_TRUE("UPDATE verifiedusers SET isomega=1,token='' WHERE uuid=?;"),
	UPDATE_USER_DECENT_TRUE("UPDATE verifiedusers SET isdecent=1,token='' WHERE uuid=?;"),
	UPDATE_USER_OMEGA_FALSE("UPDATE verifiedusers SET isomega=0,token='' WHERE uuid=?;"),
	UPDATE_USER_DECENT_FALSE("UPDATE verifiedusers SET isdecent=0,token='' WHERE uuid=?;"),
	UPDATE_USERNAME("UPDATE verifiedusers SET minecraftname=? WHERE uuid=?"),
	PERMBANS_SELECT_USER_FROM_UUID("SELECT * FROM permbans WHERE uuid=?;"),
	PERMBANS_ADD_USER("INSERT INTO permbans (uuid, reason, time) VALUES (?, ?, ?);"),
	PERMBANS_REMOVE_USER("DELETE FROM permbans WHERE uuid=?;");
	
	private String query;
	
	SQLQueries(String query) {
		this.query = query;
	}
	
	@Override
    public String toString() {
        return query;
    }
}
