package test;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class listAirportsByCityName {
	public static void main(String[] args) {
		if (!main.Connection.initDriver()) {
			return;
		}

		if (!main.Connection.testConnection()) {
			return;
		}

		try (Scanner scanner = new Scanner(System.in)) {
			System.out.print("请输入城市名称（例如 London）: ");
			String cityName = scanner.nextLine().trim();
			listAirportsByCityName(cityName);
		}
	}

	public static void listAirportsByCityName(String cityName) {
		String sql = """
				SELECT DISTINCT a.airport_name, a.airport_code
				FROM airport a
				WHERE a.city = ?
				ORDER BY a.airport_name
				""";

		try (java.sql.Connection conn = main.Connection.getConnection();
			 PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setString(1, cityName);

			try (ResultSet rs = ps.executeQuery()) {
				boolean hasResult = false;
				System.out.println("查询结果：");

				while (rs.next()) {
					hasResult = true;
					String airportName = rs.getString("airport_name");
					String airportCode = rs.getString("airport_code");
					if (airportCode == null || airportCode.isBlank()) {
						airportCode = "N/A";
					}
					System.out.println("- " + airportName + " (iata_code=" + airportCode + ")");
				}

				if (!hasResult) {
					System.out.println("未找到该城市对应的机场。");
				}
			}
		} catch (SQLException e) {
			System.err.println("查询失败: " + e.getMessage());
		}
	}
}
