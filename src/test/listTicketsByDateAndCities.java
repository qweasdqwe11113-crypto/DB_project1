package test;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

public class listTicketsByDateAndCities {
	public static void main(String[] args) {
		if (!main.Connection.initDriver()) {
			return;
		}

		if (!main.Connection.testConnection()) {
			return;
		}

		try (Scanner scanner = new Scanner(System.in)) {
			System.out.print("请输入日期（YYYY-MM-DD）: ");
			String dateText = scanner.nextLine().trim();
			System.out.print("请输入出发城市名称（例如 New York）: ");
			String sourceCity = scanner.nextLine().trim();
			System.out.print("请输入到达城市名称（例如 London）: ");
			String destinationCity = scanner.nextLine().trim();

			listTicketsByDateAndCities(dateText, sourceCity, destinationCity);
		}
	}

	public static void listTicketsByDateAndCities(String dateText, String sourceCity, String destinationCity) {
		String sql = """
				SELECT
					f.departure_time,
					f.arrival_time,
					f.arrival_day_offset,
					f.source_airport,
					f.destination_airport,
					t.economy_price
				FROM flight f
				JOIN ticket t ON t.flight_number = f.flight_number
				JOIN airport sa ON sa.airport_name = f.source_airport
				JOIN airport da ON da.airport_name = f.destination_airport
				WHERE f.flight_date = ?
				  AND sa.city = ?
				  AND da.city = ?
				ORDER BY f.departure_time ASC
				""";

		Date flightDate;
		try {
			flightDate = Date.valueOf(LocalDate.parse(dateText));
		} catch (DateTimeParseException e) {
			System.err.println("日期格式错误，请使用 YYYY-MM-DD，例如 2026-02-01。");
			return;
		}

		try (java.sql.Connection conn = main.Connection.getConnection();
			 PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setDate(1, flightDate);
			ps.setString(2, sourceCity);
			ps.setString(3, destinationCity);

			try (ResultSet rs = ps.executeQuery()) {
				boolean hasResult = false;
				System.out.println("查询结果（按 departure_time 升序）：");

				while (rs.next()) {
					hasResult = true;
					String departureTime = rs.getString("departure_time");
					String arrivalTime = rs.getString("arrival_time");
					short dayOffset = rs.getShort("arrival_day_offset");
					String arrivalDisplay = dayOffset > 0 ? arrivalTime + "(+" + dayOffset + ")" : arrivalTime;

					System.out.println(
							"- arrive_time=" + arrivalDisplay
									+ " | departure_airport=" + rs.getString("source_airport")
									+ " | destination_airport=" + rs.getString("destination_airport")
									+ " | economy_price=" + rs.getBigDecimal("economy_price")
									+ " | departure_time=" + departureTime
					);
				}

				if (!hasResult) {
					System.out.println("未找到符合条件的机票。");
				}
			}
		} catch (SQLException e) {
			System.err.println("查询失败: " + e.getMessage());
		}
	}
}
