package com.neusoft.mark.statistics.statistics.utils;

import java.sql.*;

public class DBUtil {
	
	public static Connection getConnection(){
		Connection connection=null;
		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
			String url="jdbc:oracle:thin:@(DESCRIPTION =(ADDRESS_LIST =(ADDRESS = (PROTOCOL = TCP)(HOST = 10.51.52.99)(PORT = 1521)))(CONNECT_DATA =(SID = xxdb1)))";
			String username="sipo_prosearch";
			String password="sipo_prosearch";
			connection=DriverManager.getConnection(url, username, password);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return connection;
	}
	public static void release(Connection con, Statement statement,ResultSet rs){
		if(null!=rs){
			try {
				rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}finally{
				rs=null;
			}
			if(null!=statement){
				try {
					statement.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}finally{
					statement=null;
				}
			}
			if(null!=con){
				try {
					con.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}finally{
					con=null;
				}
			}
		}
	}
}