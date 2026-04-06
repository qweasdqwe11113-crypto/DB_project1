package main;

import java.sql.DriverManager;
import java.sql.SQLException;

public class Connection {
	private static final String DB_URL = "jdbc:postgresql://localhost:5432/cs307project1";
	private static final String DB_USER = "checker";
	private static final String DB_PASSWORD = "123456";

	private Connection() {
	}

	public static boolean initDriver() {
		try {
			Class.forName("org.postgresql.Driver");
			return true;
		} catch (ClassNotFoundException e) {
			System.err.println("未找到 PostgreSQL JDBC 驱动，请确认已将 postgresql-42.2.5.jar 加入当前项目模块依赖。");
			return false;
		}
	}

	public static boolean testConnection() {
		try (java.sql.Connection conn = getConnection()) {
			System.out.println("数据库连接成功。");
			return true;
		} catch (SQLException e) {
			System.err.println("数据库连接失败: " + e.getMessage());
			return false;
		}
	}

	public static java.sql.Connection getConnection() throws SQLException {
		return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
	}
}
