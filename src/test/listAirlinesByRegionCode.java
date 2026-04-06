package test;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class listAirlinesByRegionCode {
	public static void main(String[] args) {
		if (!main.Connection.initDriver()) {
			return;
		}

		if (!main.Connection.testConnection()) {
			return;
		}

		try (Scanner scanner = new Scanner(System.in)) {
			System.out.print("请输入地区代码（例如 CN）: ");
			String regionCode = scanner.nextLine().trim().toUpperCase();
			listAirlinesByRegionCode(regionCode);
		}
	}

	public static void listAirlinesByRegionCode(String regionCode) {
		String sql = """
				SELECT DISTINCT a.airline_code, a.airline_name
				FROM airline a
				JOIN region r ON a.region_name = r.region_name
				WHERE r.region_code = ?
				ORDER BY a.airline_code
				""";

		try (java.sql.Connection conn = main.Connection.getConnection();
			 PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setString(1, regionCode);

			try (ResultSet rs = ps.executeQuery()) {
				boolean hasResult = false;
				System.out.println("查询结果：");

				while (rs.next()) {
					hasResult = true;
					String code = rs.getString("airline_code");
					String name = rs.getString("airline_name");
					System.out.println("- " + code + " | " + name);
				}

				if (!hasResult) {
					System.out.println("未找到该地区代码对应的航空公司。");
				}
			}
		} catch (SQLException e) {
			System.err.println("查询失败: " + e.getMessage());
		}
	}
}
