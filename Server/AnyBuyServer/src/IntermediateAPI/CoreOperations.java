package IntermediateAPI;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import SQLControl.SQLOperation;

public class CoreOperations {

	static String register (String[] str) throws SQLException {
		writeLog("Register");
		if (str.length < 1) return "0x1A06";
		String[] str2 = str[0].split("\\?");
		String[] uInfo = str2[0].split("\\@");
		if (str2.length != 2 || uInfo.length != 2) return "0x1A01";
		if (uInfo[0].charAt(0) == '0' && uInfo[0].charAt(1) == 'x' && uInfo[0].length() == 6) return "0x1A01";
		Connection c = SQLControl.SQLOperation.getConnect("userInfo");
		String emailDomainCode = SQLControl.SQLOperation.readDatabase(c, "select code from domainCode"
				+ " where emailDomain='" + uInfo[1] + "'");
		if (emailDomainCode == null) {
			emailDomainCode = UserManage.createDomainCode(c, uInfo[1]);
		}
		if (emailDomainCode == "0x1A07") return "0x1A07";
		emailDomainCode = SQLControl.SQLOperation.readDatabase(c, "select code from domainCode"
				+ " where emailDomain='" + uInfo[1] + "'");
		String usr = SQLControl.SQLOperation.readDatabase(c, "select psc from " + emailDomainCode + " where name='" + uInfo[0] + "'");
		if (usr != null) return "0x1A08";
		int uid = SQLOperation.countLine(c, emailDomainCode) + 10000;
		String sql = "INSERT INTO " + emailDomainCode + "(name,psc,id) VALUES('" + uInfo[0] + "','" + str2[1] + "','" + uid + "');";
		SQLControl.SQLOperation.updateData(c, sql);
		SQLOperation.creatProfile(c, emailDomainCode + "" + uid);
		c.close();
		return "0x01";
	}
	
	static String login (String[] str) throws SQLException {
		writeLog("Login");
		String[] str2 = str[0].split("\\?");
		String[] uInfo = str2[0].split("\\@");
		Connection c = SQLControl.SQLOperation.getConnect("userInfo");
		String sql = "select code from domainCode where emailDomain='" + uInfo[1] + "'";
		String emailCode = SQLControl.SQLOperation.readDatabase(c, sql);
		sql = "select id from " + emailCode + " where name='" + uInfo[0] + "'";
		String uid = SQLControl.SQLOperation.readDatabase(c, sql);
		if (emailCode == null) return "0x1C01";
		sql = "select psc from " + emailCode + " where name='" + uInfo[0] + "'";
		System.out.println(sql);
		if (str2[1].equals(SQLControl.SQLOperation.readDatabase(c, sql)) ) {
			c.close();
			int authToken = (int) (Math.random() * 10 * 0xFFFF);
			// TODO improve authToken algorithm to make it has a high security level.
			c = SQLControl.SQLOperation.getConnect("accessLog");
			sql = "select token from authLog where uid='" + emailCode + uid + "'";
			String usrStatus = SQLControl.SQLOperation.readDatabase(c, sql);
			if (usrStatus == null) {
				sql = "insert into authLog (uid, authTime, token) values ('" + emailCode + uid + "','" + System.currentTimeMillis() + "','" + authToken + "');";
				SQLControl.SQLOperation.updateData(c, sql);
			}
			else {
				sql = "update authLog set authTime='" + System.currentTimeMillis() + "' where uid='" + emailCode + uid + "';" ;
				SQLControl.SQLOperation.updateData(c, sql);
				sql = "update authLog set token='" + authToken + "' where uid='" + emailCode + uid + "';" ;
				SQLControl.SQLOperation.updateData(c, sql);
			}
			String sessionID = emailCode + uid + "?" + authToken;
			System.out.println(authToken);
			return sessionID;
		} else {
			c.close();
			return "0x1C02";
		}
	}
	
	static String placeOrder (String[] str) {
		writeLog("Place Order");
		return null;
	}
	
	static String giveRate (String[] str) {
		writeLog("Give Rate");
		return null;
	}
	
	static String cancelOrder (String[] str) {
		writeLog("Cancel Order");
		return null;
	}
	
	static String acceptRate (String[] str) {
		writeLog("Accept Rate");
		return null;
	}
	
	static String addCard (String[] str) throws SQLException {
		//adc&snok10000?538847&yoona?lim&amex=375987654321001&1220?95064
		
		String uid = sessionVerify(str[0]);
		if (uid.length() == 6 && uid.charAt(0) == '0' && uid.charAt(1) == 'x') return uid;
		
		
		String[] name = str[1].split("\\?");
		String[] cardNum = str[2].split("\\=");
		String[] expInfo = str[3].split("\\?");
		
		Connection c = SQLOperation.getConnect(uid);
		String cardStatus = SQLControl.SQLOperation.readDatabase(c, "select issuer from payment where cardNumber='" + cardNum[1] + "'");
		if (cardStatus != null) {
			c.close();
			return "0x1E01";
		}
		
		cardStatus = validateCardInfo(name, cardNum, expInfo);
		if ( !cardStatus.equals("0x01") ) {
			c.close();
			return cardStatus;
		}
		
		String value = "'" + name[0] + "','" + name[1] + "','" + cardNum[0] + "','" + cardNum[1] + "','" + expInfo[0] + "','" + expInfo[1] + "'";
		String sql = "INSERT INTO payment(fn, ln, issuer, cardNumber, exp, zip) VALUES(" + value + ");"; 
		System.out.println(SQLOperation.updateData(c, sql));
		c.close();
		return "0x01";
	}
	
	static String loadCard (String[] str) throws SQLException {
		String uid = sessionVerify(str[0]);
		if (uid.length() == 6 && uid.charAt(0) == '0' && uid.charAt(1) == 'x') return uid;
		Connection c = SQLOperation.getConnect(uid);
		String sql = "SELECT * FROM payment";
		ResultSet rs = SQLOperation.readDatabaseRS(c, sql);
		String res = generateResWithRS(rs, 6);
		c.close();
		if (res.equals("")) return "0x1E04";
		else return res;
	}
	
	static String deleteCard(String[] str) throws SQLException {
		//dlc&sid&card#
		String uid = sessionVerify(str[0]);
		if (uid.length() == 6 && uid.charAt(0) == '0' && uid.charAt(1) == 'x') return uid;
		String sql = "delete from payment where cardNumber=" + str[1] + ";";
		Connection c = SQLControl.SQLOperation.getConnect(uid);
		String cardStatus = SQLControl.SQLOperation.readDatabase(c, "select issuer from payment where cardNumber='" + str[1] + "'");
		if (cardStatus == null) {
			c.close();
			return "0x1E04";
		}
		String res = SQLControl.SQLOperation.updateData(c, sql);
		c.close();
		if (res != "UPS") return res;
		else return "0x01";
	}
	
	static String addAddress(String[] str) throws SQLException {
		//<veri>&yoona?lim&SM Ent'l?Yeongdong-daero 513?Gangnam-gu?Seoul?KR?00000
		String uid = sessionVerify(str[0]);
		if (uid.length() == 6 && uid.charAt(0) == '0' && uid.charAt(1) == 'x') return uid;
		String[] name = str[1].split("\\?");
		String[] info = str[2].split("\\?");
		
		Connection c = SQLOperation.getConnect(uid);
		String addStatus = SQLControl.SQLOperation.readDatabase(c, "select line2 from address where line1='" + info[1] + "'");
		if (addStatus != null) {
			c.close();
			return "0x1E06";
		}
		
		String value = "('" + name[0] + "','" + name[1] + "','" + info[0] + "','" + info[1] + "','" + info[2] + "','" + info[3] + "','" + info[4] + "','" + info[5] + "')";
		String sql = "INSERT INTO address(fn, ln, company, line1, line2, city, state, zip) VALUES" + value + ";";
		System.out.println(SQLOperation.updateData(c, sql));
		c.close();
		return "0x01";
	}
	
	static String loadAddress (String[] str) throws SQLException {
		String uid = sessionVerify(str[0]);
		if (uid.length() == 6 && uid.charAt(0) == '0' && uid.charAt(1) == 'x') return uid;
		Connection c = SQLOperation.getConnect(uid);
		String sql = "SELECT * FROM address";
		ResultSet rs = SQLOperation.readDatabaseRS(c, sql);
		String res = generateResWithRS(rs, 8);
		c.close();
		if (res.equals("")) return "0x1E05";
		else return res;
	}
	
	private static String generateResWithRS(ResultSet rs, int len) throws SQLException {
		String res = "";
		while (rs.next()) {
			if (res != "") res += "&";
			for (int i = 1; i <= len; i++) {
				res  += rs.getString(i);
				if (i < len) res += "?";
			}
		}
		return res;
	}
	
	private static String validateCardInfo(String[] name, String[] card, String[] exp) {
		return "0x01";
	}
	
	static String sessionVerify (String sessionID) throws SQLException {
		String[] veri = sessionID.split("\\?");
		Connection c = SQLControl.SQLOperation.getConnect("accessLog");
		String sql = "select token from authLog where uid='" + veri[0] + "'";
		String res = SQLOperation.readDatabase(c, sql);
		if (!veri[1].equals(res)) {
			c.close();
			return "0x1D01";
		}
		sql = "select authTime from authLog where uid='" + veri[0] + "'";
		Long l = Long.parseLong(SQLOperation.readDatabase(c, sql));
		if (System.currentTimeMillis() - l > 0x927C0 || System.currentTimeMillis() < l) return "0x1D02";
		sql = "update authLog set authTime='" + System.currentTimeMillis() + "' where uid='" + veri[0] + "';" ;
		SQLControl.SQLOperation.updateData(c, sql);
		c.close();
		return veri[0];
	}
	
	static String illegalInput() {
		writeLog("Illegal Input.");
		return null;
	}
	
	static void writeLog (String str) {
		System.out.println(str);
	}
	
	
}
