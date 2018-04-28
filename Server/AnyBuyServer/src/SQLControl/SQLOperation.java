package SQLControl;

import java.sql.*;

public class SQLOperation {
	
	public static void main(String args[]) {
		
	}
	
	public static Connection getConnect (String base, String user, String psc) {
		return new SQL(base, user, psc).connect();
	}
	
	public static String readDatabase(Connection c, String sql) {
		try {
			ResultSet rst = c.createStatement().executeQuery(sql);
			if (rst.next()) return rst.getString(1);
		} catch (SQLException e) {
			return null;
		}
		return null;
	}
	
	public static String writeData(Connection c, String sql) {
		try {
			if (c.createStatement().executeUpdate(sql) != 0) return "WTS";
			else return "0x1B01";
			// WTS = Write Success
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			return "0x1B02";
		}
	}
	
	public static String makeTable (Connection c, String str) {
		String sql = "create table " + str + "( name Char(40),psc Char(50), id int(8));";
		try {
			c.createStatement().executeUpdate(sql);
		} catch (SQLException e) {
			return "0x1A04";
		}
		return "0x1A05";
	}
	
	public static String creatProfile(Connection c, String userId) {
		try {
			String sql = "CREATE DATABASE " + userId;
			c.createStatement().executeUpdate(sql);
			c = SQLControl.SQLOperation.getConnect(userId, "anybuy", "CMPS115.");
			sql = "create table payment ( issuer Char(4), cardNumber int(16), exp Char(4), zip Char(5) );";
			c.createStatement().executeUpdate(sql);
			sql = "create table address ( line1 Char(255), line2 Char(255), state Char(2), zip Char(5) );";
			c.createStatement().executeUpdate(sql);
		} catch (SQLException e) {
			return "0x1A04";
		}
		return "0x1A05";
	}
	
	public static int countLine(Connection c, String tableName) throws SQLException {
		String sql = "select count(*) as rowCount from " + tableName;
		ResultSet rset = c.createStatement().executeQuery(sql);
		rset.next();
		int rtn = rset.getInt("rowCount");
//		int rtn = rset.getMetaData().
		return rtn;
	}
	
}
