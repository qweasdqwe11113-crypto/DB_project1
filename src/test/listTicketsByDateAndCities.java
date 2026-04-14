package test;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
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
					f.destination_airport
				FROM flight f
				JOIN ticket t ON t.flight_number = f.flight_number
				JOIN airport sa ON sa.airport_name = f.source_airport
				JOIN airport da ON da.airport_name = f.destination_airport
				WHERE f.flight_date = ?
				  AND LOWER(sa.city) = LOWER(?)
				  AND LOWER(da.city) = LOWER(?)
				ORDER BY t.economy_price ASC, f.departure_time ASC
				""";

		Date flightDate;
		try {
			flightDate = Date.valueOf(LocalDate.parse(dateText));
		} catch (DateTimeParseException e) {
			System.err.println("日期格式错误，请使用 YYYY-MM-DD，例如 2026-02-05。");
			return;
		}

		try (java.sql.Connection conn = main.Connection.getConnection();
			 PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setDate(1, flightDate);
			ps.setString(2, sourceCity);
			ps.setString(3, destinationCity);

			try (ResultSet rs = ps.executeQuery()) {
				boolean hasResult = false;
				System.out.println("查询结果（按 economy_price 升序）：");

				while (rs.next()) {
					hasResult = true;
					String departureTime = rs.getString("departure_time");
					String arrivalTime = rs.getString("arrival_time");
					short dayOffset = rs.getShort("arrival_day_offset");
					String arrivalDisplay = dayOffset > 0 ? arrivalTime + "(+" + dayOffset + ")" : arrivalTime;

					System.out.println(
							"- d_time=" + departureTime
									+ " | a_time=" + arrivalDisplay
									+ " | ap1.name=" + rs.getString("source_airport")
									+ " | ap2.name=" + rs.getString("destination_airport")
					);
				}

				if (!hasResult) {
					System.out.println("未找到符合条件的机票。");
					printCitySuggestion(conn, "出发城市", sourceCity);
					printCitySuggestion(conn, "到达城市", destinationCity);
				}
			}
		} catch (SQLException e) {
			System.err.println("查询失败: " + e.getMessage());
		}
	}

	private static void printCitySuggestion(java.sql.Connection conn, String label, String inputCity) throws SQLException {
		if (cityExists(conn, inputCity)) {
			return;
		}

		List<String> candidates = getClosestCities(conn, inputCity, 3);
		if (candidates.isEmpty()) {
			System.out.println(label + " `" + inputCity + "` 不存在于数据库中。");
			return;
		}

		System.out.println(label + " `" + inputCity + "` 可能拼写有误，你是不是想输入: " + String.join(", ", candidates));
	}

	private static boolean cityExists(java.sql.Connection conn, String city) throws SQLException {
		String sql = """
				SELECT 1
				FROM airport
				WHERE LOWER(city) = LOWER(?)
				LIMIT 1
				""";

		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, city);
			try (ResultSet rs = ps.executeQuery()) {
				return rs.next();
			}
		}
	}

	private static List<String> getClosestCities(java.sql.Connection conn, String inputCity, int limit) throws SQLException {
		String sql = """
				SELECT DISTINCT city
				FROM airport
				WHERE city IS NOT NULL
				""";

		List<String> allCities = new ArrayList<>();
		try (PreparedStatement ps = conn.prepareStatement(sql);
			 ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				allCities.add(rs.getString("city"));
			}
		}

		String normalizedInput = inputCity.toLowerCase();
		allCities.sort((a, b) -> {
			int da = levenshtein(normalizedInput, a.toLowerCase());
			int db = levenshtein(normalizedInput, b.toLowerCase());
			if (da != db) {
				return Integer.compare(da, db);
			}
			return a.compareToIgnoreCase(b);
		});

		List<String> result = new ArrayList<>();
		for (String city : allCities) {
			if (!result.contains(city)) {
				result.add(city);
			}
			if (result.size() == limit) {
				break;
			}
		}
		return result;
	}

	private static int levenshtein(String a, String b) {
		int[][] dp = new int[a.length() + 1][b.length() + 1];

		for (int i = 0; i <= a.length(); i++) {
			dp[i][0] = i;
		}
		for (int j = 0; j <= b.length(); j++) {
			dp[0][j] = j;
		}

		for (int i = 1; i <= a.length(); i++) {
			for (int j = 1; j <= b.length(); j++) {
				int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
				dp[i][j] = Math.min(
						Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
						dp[i - 1][j - 1] + cost
				);
			}
		}

		return dp[a.length()][b.length()];
	}
}
