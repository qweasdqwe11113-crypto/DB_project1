package test;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class listFlightsByIataCodes {
	public static void main(String[] args) {
		if (!main.Connection.initDriver()) {
			return;
		}

		if (!main.Connection.testConnection()) {
			return;
		}

		try (Scanner scanner = new Scanner(System.in)) {
			System.out.print("请输入出发地 iata_code（例如 JFK）: ");
			String sourceIataCode = scanner.nextLine().trim().toUpperCase();

			System.out.print("请输入到达地 iata_code（例如 LCY）: ");
			String destinationIataCode = scanner.nextLine().trim().toUpperCase();

			listFlightsByIataCodes(sourceIataCode, destinationIataCode);
		}
	}

	public static void listFlightsByIataCodes(String sourceIataCode, String destinationIataCode) {
		String sql = """
				SELECT DISTINCT
					SPLIT_PART(f.flight_number, '_', 1) AS flight_number,
					sa.city AS source_city,
					sr.region_name AS source_region,
					da.city AS destination_city,
					dr.region_name AS destination_region
				FROM flight f
				JOIN airport sa ON f.source_airport = sa.airport_name
				JOIN region sr ON sa.region_name = sr.region_name
				JOIN airport da ON f.destination_airport = da.airport_name
				JOIN region dr ON da.region_name = dr.region_name
				WHERE sa.airport_code = ?
				  AND da.airport_code = ?
				ORDER BY flight_number
				""";

		try (java.sql.Connection conn = main.Connection.getConnection();
			 PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setString(1, sourceIataCode);
			ps.setString(2, destinationIataCode);

			try (ResultSet rs = ps.executeQuery()) {
				boolean hasResult = false;
				System.out.println("查询结果：");

				while (rs.next()) {
					hasResult = true;
					System.out.println("- " + rs.getString("flight_number")
							+ " | " + rs.getString("source_city")
							+ " | " + rs.getString("source_region")
							+ " | " + rs.getString("destination_city")
							+ " | " + rs.getString("destination_region"));
				}

				if (!hasResult) {
					System.out.println("未找到对应 iata_code 组合的航班。");
				}
			}
		} catch (SQLException e) {
			System.err.println("查询失败: " + e.getMessage());
		}
	}
}
