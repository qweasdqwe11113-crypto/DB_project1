package main;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class function {

	public static String authenticatePassenger(String passengerId, String mobileNumber) {
		if (isBlank(passengerId) || isBlank(mobileNumber)) {
			System.err.println("passenger_id 和手机号不能为空。");
			return null;
		}

		String sql = """
				SELECT passenger_name
				FROM passenger
				WHERE passenger_id = ?
				  AND mobile_number = ?
				""";

		try (Connection conn = main.Connection.getConnection();
			 PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, passengerId.trim());
			ps.setString(2, mobileNumber.trim());

			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return rs.getString("passenger_name");
				}
				return null;
			}
		} catch (SQLException e) {
			System.err.println("登录认证失败: " + e.getMessage());
			return null;
		}
	}

	public static int generateTicketsByDateRange(String startDateText, String endDateText) {
		LocalDate startDate;
		LocalDate endDate;

		try {
			startDate = LocalDate.parse(startDateText);
			endDate = LocalDate.parse(endDateText);
		} catch (DateTimeParseException e) {
			System.err.println("日期格式错误，请使用 YYYY-MM-DD。");
			return 0;
		}

		if (endDate.isBefore(startDate)) {
			System.err.println("结束日期不能早于开始日期。");
			return 0;
		}

		String selectFlightsSql = """
				SELECT f.flight_number, f.flight_date, f.source_airport, f.destination_airport,
				       f.departure_time, f.arrival_time, f.arrival_day_offset
				FROM flight f
				WHERE f.flight_date BETWEEN ? AND ?
				ORDER BY f.flight_date, f.flight_number
				""";

		int flightCount = 0;

		try (Connection conn = main.Connection.getConnection();
			 PreparedStatement selectPs = conn.prepareStatement(selectFlightsSql)) {

			selectPs.setDate(1, Date.valueOf(startDate));
			selectPs.setDate(2, Date.valueOf(endDate));

			System.out.println("查询结果：");
			System.out.println("航班号\t\t\t日期\t\t出发机场\t\t到达机场\t\t出发时间\t到达时间\t到达日偏移");
			System.out.println("------------------------------------------------------------------------------------------------------------------------");

			try (ResultSet rs = selectPs.executeQuery()) {
				while (rs.next()) {
					String flightNumber = rs.getString("flight_number");
					Date flightDate = rs.getDate("flight_date");
					String sourceAirport = rs.getString("source_airport");
					String destinationAirport = rs.getString("destination_airport");
					Time departureTime = rs.getTime("departure_time");
					Time arrivalTime = rs.getTime("arrival_time");
					int arrivalDayOffset = rs.getInt("arrival_day_offset");

					System.out.printf("%-20s\t%-10s\t%-15s\t%-15s\t%-10s\t%-10s\t%d%n",
							flightNumber, flightDate, sourceAirport, destinationAirport,
							departureTime, arrivalTime, arrivalDayOffset);
					flightCount++;
				}
			}

			System.out.println("------------------------------------------------------------------------------------------------------------------------");
			System.out.println("共查询到 " + flightCount + " 条航班记录。");
			return flightCount;
		} catch (SQLException e) {
			System.err.println("查询航班失败: " + e.getMessage());
			return 0;
		}
	}

	public static int searchTickets(
			String sourceCity,
			String destinationCity,
			String dateText,
			String airlineName,
			String departureTimeText,
			String arrivalTimeText
	) {
		if (isBlank(sourceCity) || isBlank(destinationCity) || isBlank(dateText)) {
			System.err.println("出发城市、到达城市、日期为必填项。");
			return 0;
		}

		Date flightDate;
		Time departureTime = null;
		Time arrivalTime = null;

		try {
			flightDate = Date.valueOf(LocalDate.parse(dateText.trim()));
		} catch (DateTimeParseException e) {
			System.err.println("日期格式错误，请使用 YYYY-MM-DD。");
			return 0;
		}

		try {
			if (!isBlank(departureTimeText)) {
				departureTime = Time.valueOf(LocalTime.parse(departureTimeText.trim()));
			}
			if (!isBlank(arrivalTimeText)) {
				arrivalTime = Time.valueOf(LocalTime.parse(arrivalTimeText.trim()));
			}
		} catch (DateTimeParseException e) {
			System.err.println("时间格式错误，请使用 HH:mm。");
			return 0;
		}

		StringBuilder sql = new StringBuilder("""
				SELECT
					t.ticket_id,
					f.flight_number,
					a.airline_name,
					f.flight_date,
					sa.city AS source_city,
					f.source_airport,
					da.city AS destination_city,
					f.destination_airport,
					f.departure_time,
					f.arrival_time,
					f.arrival_day_offset,
					t.business_price,
					t.business_remain,
					t.economy_price,
					t.economy_remain
				FROM ticket t
				JOIN flight f ON t.flight_number = f.flight_number
				JOIN airport sa ON f.source_airport = sa.airport_name
				JOIN airport da ON f.destination_airport = da.airport_name
				LEFT JOIN airline a
					ON a.airline_code = REGEXP_REPLACE(SPLIT_PART(f.flight_number, '_', 1), '[0-9]+$', '')
				WHERE sa.city = ?
				  AND da.city = ?
				  AND f.flight_date = ?
				""");

		List<Object> params = new ArrayList<>();
		params.add(sourceCity.trim());
		params.add(destinationCity.trim());
		params.add(flightDate);

		if (!isBlank(airlineName)) {
			sql.append(" AND a.airline_name = ? ");
			params.add(airlineName.trim());
		}

		if (departureTime != null) {
			sql.append(" AND f.departure_time >= ? ");
			params.add(departureTime);
		}

		if (arrivalTime != null) {
			sql.append(" AND f.arrival_time <= ? ");
			params.add(arrivalTime);
		}

		sql.append(" ORDER BY f.departure_time ASC, t.ticket_id ASC ");

		int ticketCount = 0;

		try (Connection conn = main.Connection.getConnection();
			 PreparedStatement ps = conn.prepareStatement(sql.toString())) {

			for (int i = 0; i < params.size(); i++) {
				ps.setObject(i + 1, params.get(i));
			}

			try (ResultSet rs = ps.executeQuery()) {
				System.out.println("机票查询结果：");

				while (rs.next()) {
					ticketCount++;
					printTicketRow(rs);
				}
			}

			if (ticketCount == 0) {
				System.out.println("未找到符合条件的机票。");
			} else {
				System.out.println("共查询到 " + ticketCount + " 条机票记录。");
			}

			return ticketCount;
		} catch (SQLException e) {
			System.err.println("查询机票失败: " + e.getMessage());
			return 0;
		}
	}

	public static boolean showTicketDetail(int ticketId) {
		String sql = """
				SELECT
					t.ticket_id,
					f.flight_number,
					a.airline_name,
					f.flight_date,
					sa.city AS source_city,
					f.source_airport,
					da.city AS destination_city,
					f.destination_airport,
					f.departure_time,
					f.arrival_time,
					f.arrival_day_offset,
					t.business_price,
					t.business_remain,
					t.economy_price,
					t.economy_remain
				FROM ticket t
				JOIN flight f ON t.flight_number = f.flight_number
				JOIN airport sa ON f.source_airport = sa.airport_name
				JOIN airport da ON f.destination_airport = da.airport_name
				LEFT JOIN airline a
					ON a.airline_code = REGEXP_REPLACE(SPLIT_PART(f.flight_number, '_', 1), '[0-9]+$', '')
				WHERE t.ticket_id = ?
				""";

		try (Connection conn = main.Connection.getConnection();
			 PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, ticketId);

			try (ResultSet rs = ps.executeQuery()) {
				if (!rs.next()) {
					System.out.println("未找到对应 ticket_id 的机票。");
					return false;
				}

				System.out.println("已选择机票：");
				printTicketRow(rs);
				return true;
			}
		} catch (SQLException e) {
			System.err.println("读取机票详情失败: " + e.getMessage());
			return false;
		}
	}

	public static boolean bookTicket(String passengerId, int ticketId, String cabinClass) {
		if (isBlank(passengerId) || isBlank(cabinClass)) {
			System.err.println("乘客编号和舱位类型不能为空。");
			return false;
		}

		String normalizedCabinClass = cabinClass.trim();
		if (!normalizedCabinClass.equalsIgnoreCase("Economy")
				&& !normalizedCabinClass.equalsIgnoreCase("Business")) {
			System.err.println("舱位类型只能是 Economy 或 Business。");
			return false;
		}
		normalizedCabinClass = capitalize(normalizedCabinClass);

		String checkPassengerSql = "SELECT 1 FROM passenger WHERE passenger_id = ?";
		String lockTicketSql = """
				SELECT ticket_id, business_price, business_remain, economy_price, economy_remain
				FROM ticket
				WHERE ticket_id = ?
				FOR UPDATE
				""";
		String updateEconomySql = """
				UPDATE ticket
				SET economy_remain = economy_remain - 1
				WHERE ticket_id = ?
				  AND economy_remain > 0
				""";
		String updateBusinessSql = """
				UPDATE ticket
				SET business_remain = business_remain - 1
				WHERE ticket_id = ?
				  AND business_remain > 0
				""";
		String insertOrderSql = """
				INSERT INTO ticket_order(passenger_id, ticket_id, cabin_class, order_time)
				VALUES (?, ?, ?, CURRENT_TIMESTAMP)
				RETURNING order_id, order_time
				""";

		try (Connection conn = main.Connection.getConnection()) {
			conn.setAutoCommit(false);

			try {
				if (!passengerExists(conn, checkPassengerSql, passengerId.trim())) {
					System.out.println("未找到该乘客编号。");
					conn.rollback();
					return false;
				}

				try (PreparedStatement lockPs = conn.prepareStatement(lockTicketSql)) {
					lockPs.setInt(1, ticketId);

					try (ResultSet rs = lockPs.executeQuery()) {
						if (!rs.next()) {
							System.out.println("未找到对应 ticket_id 的机票。");
							conn.rollback();
							return false;
						}

						if (normalizedCabinClass.equals("Economy")) {
							int remain = rs.getInt("economy_remain");
							if (remain <= 0) {
								System.out.println("经济舱余票不足，无法预订。");
								conn.rollback();
								return false;
							}
						} else {
							int remain = rs.getInt("business_remain");
							if (remain <= 0) {
								System.out.println("商务舱余票不足，无法预订。");
								conn.rollback();
								return false;
							}
						}
					}
				}

				String updateSql = normalizedCabinClass.equals("Economy") ? updateEconomySql : updateBusinessSql;
				try (PreparedStatement updatePs = conn.prepareStatement(updateSql)) {
					updatePs.setInt(1, ticketId);
					int updatedRows = updatePs.executeUpdate();
					if (updatedRows != 1) {
						System.out.println("扣减余票失败，预订未完成。");
						conn.rollback();
						return false;
					}
				}

				try (PreparedStatement insertPs = conn.prepareStatement(insertOrderSql)) {
					insertPs.setString(1, passengerId.trim());
					insertPs.setInt(2, ticketId);
					insertPs.setString(3, normalizedCabinClass);

					try (ResultSet rs = insertPs.executeQuery()) {
						if (rs.next()) {
							int orderId = rs.getInt("order_id");
							Timestamp orderTime = rs.getTimestamp("order_time");
							conn.commit();
							System.out.println("预订成功。order_id=" + orderId + " | order_time=" + orderTime);
							return true;
						}
					}
				}

				conn.rollback();
				System.out.println("创建订单失败，预订未完成。");
				return false;
			} catch (SQLException e) {
				conn.rollback();
				System.err.println("预订失败: " + e.getMessage());
				return false;
			} finally {
				conn.setAutoCommit(true);
			}
		} catch (SQLException e) {
			System.err.println("预订失败: " + e.getMessage());
			return false;
		}
	}

	public static int searchOrders(String orderIdText, String passengerId) {
		StringBuilder sql = new StringBuilder("""
				SELECT
					o.order_id,
					o.passenger_id,
					p.passenger_name,
					o.ticket_id,
					o.cabin_class,
					o.order_time,
					t.flight_number,
					f.flight_date,
					sa.city AS source_city,
					da.city AS destination_city,
					f.departure_time,
					f.arrival_time,
					f.arrival_day_offset
				FROM ticket_order o
				JOIN passenger p ON o.passenger_id = p.passenger_id
				JOIN ticket t ON o.ticket_id = t.ticket_id
				JOIN flight f ON t.flight_number = f.flight_number
				JOIN airport sa ON f.source_airport = sa.airport_name
				JOIN airport da ON f.destination_airport = da.airport_name
				WHERE 1 = 1
				""");

		List<Object> params = new ArrayList<>();

		if (!isBlank(orderIdText)) {
			try {
				params.add(Integer.parseInt(orderIdText.trim()));
				sql.append(" AND o.order_id = ? ");
			} catch (NumberFormatException e) {
				System.err.println("order_id 必须是整数。");
				return 0;
			}
		}

		if (!isBlank(passengerId)) {
			sql.append(" AND o.passenger_id = ? ");
			params.add(passengerId.trim());
		}

		sql.append(" ORDER BY o.order_time DESC, o.order_id DESC ");

		int orderCount = 0;

		try (Connection conn = main.Connection.getConnection();
			 PreparedStatement ps = conn.prepareStatement(sql.toString())) {
			for (int i = 0; i < params.size(); i++) {
				ps.setObject(i + 1, params.get(i));
			}

			try (ResultSet rs = ps.executeQuery()) {
				System.out.println("订单查询结果：");
				while (rs.next()) {
					orderCount++;
					printOrderRow(rs);
				}
			}

			if (orderCount == 0) {
				System.out.println("未找到符合条件的订单。");
			} else {
				System.out.println("共查询到 " + orderCount + " 条订单记录。");
			}

			return orderCount;
		} catch (SQLException e) {
			System.err.println("查询订单失败: " + e.getMessage());
			return 0;
		}
	}

	public static int searchOrdersForPassenger(String orderIdText, String passengerId) {
		return searchOrders(orderIdText, passengerId);
	}

	public static boolean deleteOrder(int orderId) {
		String lockOrderSql = """
				SELECT o.order_id, o.ticket_id, o.cabin_class
				FROM ticket_order o
				WHERE o.order_id = ?
				FOR UPDATE
				""";
		String deleteOrderSql = "DELETE FROM ticket_order WHERE order_id = ?";
		String restoreEconomySql = """
				UPDATE ticket
				SET economy_remain = economy_remain + 1
				WHERE ticket_id = ?
				""";
		String restoreBusinessSql = """
				UPDATE ticket
				SET business_remain = business_remain + 1
				WHERE ticket_id = ?
				""";

		try (Connection conn = main.Connection.getConnection()) {
			conn.setAutoCommit(false);

			try {
				int ticketId;
				String cabinClass;

				try (PreparedStatement lockPs = conn.prepareStatement(lockOrderSql)) {
					lockPs.setInt(1, orderId);
					try (ResultSet rs = lockPs.executeQuery()) {
						if (!rs.next()) {
							System.out.println("未找到对应 order_id 的订单。");
							conn.rollback();
							return false;
						}
						ticketId = rs.getInt("ticket_id");
						cabinClass = rs.getString("cabin_class");
					}
				}

				try (PreparedStatement deletePs = conn.prepareStatement(deleteOrderSql)) {
					deletePs.setInt(1, orderId);
					int deletedRows = deletePs.executeUpdate();
					if (deletedRows != 1) {
						System.out.println("删除订单失败。");
						conn.rollback();
						return false;
					}
				}

				String restoreSql = "Economy".equalsIgnoreCase(cabinClass) ? restoreEconomySql : restoreBusinessSql;
				try (PreparedStatement restorePs = conn.prepareStatement(restoreSql)) {
					restorePs.setInt(1, ticketId);
					int restoredRows = restorePs.executeUpdate();
					if (restoredRows != 1) {
						System.out.println("恢复余票失败。");
						conn.rollback();
						return false;
					}
				}

				conn.commit();
				System.out.println("订单删除成功，且对应舱位余票已恢复 1。");
				return true;
			} catch (SQLException e) {
				conn.rollback();
				System.err.println("删除订单失败: " + e.getMessage());
				return false;
			} finally {
				conn.setAutoCommit(true);
			}
		} catch (SQLException e) {
			System.err.println("删除订单失败: " + e.getMessage());
			return false;
		}
	}

	public static boolean deleteOrderForPassenger(int orderId, String passengerId) {
		String lockOrderSql = """
				SELECT o.order_id, o.ticket_id, o.cabin_class
				FROM ticket_order o
				WHERE o.order_id = ?
				  AND o.passenger_id = ?
				FOR UPDATE
				""";
		String deleteOrderSql = """
				DELETE FROM ticket_order
				WHERE order_id = ?
				  AND passenger_id = ?
				""";
		String restoreEconomySql = """
				UPDATE ticket
				SET economy_remain = economy_remain + 1
				WHERE ticket_id = ?
				""";
		String restoreBusinessSql = """
				UPDATE ticket
				SET business_remain = business_remain + 1
				WHERE ticket_id = ?
				""";

		try (Connection conn = main.Connection.getConnection()) {
			conn.setAutoCommit(false);

			try {
				int ticketId;
				String cabinClass;

				try (PreparedStatement lockPs = conn.prepareStatement(lockOrderSql)) {
					lockPs.setInt(1, orderId);
					lockPs.setString(2, passengerId);
					try (ResultSet rs = lockPs.executeQuery()) {
						if (!rs.next()) {
							System.out.println("未找到属于当前登录用户的该订单。");
							conn.rollback();
							return false;
						}
						ticketId = rs.getInt("ticket_id");
						cabinClass = rs.getString("cabin_class");
					}
				}

				try (PreparedStatement deletePs = conn.prepareStatement(deleteOrderSql)) {
					deletePs.setInt(1, orderId);
					deletePs.setString(2, passengerId);
					int deletedRows = deletePs.executeUpdate();
					if (deletedRows != 1) {
						System.out.println("删除订单失败。");
						conn.rollback();
						return false;
					}
				}

				String restoreSql = "Economy".equalsIgnoreCase(cabinClass) ? restoreEconomySql : restoreBusinessSql;
				try (PreparedStatement restorePs = conn.prepareStatement(restoreSql)) {
					restorePs.setInt(1, ticketId);
					int restoredRows = restorePs.executeUpdate();
					if (restoredRows != 1) {
						System.out.println("恢复余票失败。");
						conn.rollback();
						return false;
					}
				}

				conn.commit();
				System.out.println("订单删除成功，且对应舱位余票已恢复 1。");
				return true;
			} catch (SQLException e) {
				conn.rollback();
				System.err.println("删除订单失败: " + e.getMessage());
				return false;
			} finally {
				conn.setAutoCommit(true);
			}
		} catch (SQLException e) {
			System.err.println("删除订单失败: " + e.getMessage());
			return false;
		}
	}

	private static boolean passengerExists(Connection conn, String sql, String passengerId) throws SQLException {
		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, passengerId);
			try (ResultSet rs = ps.executeQuery()) {
				return rs.next();
			}
		}
	}

	private static void printTicketRow(ResultSet rs) throws SQLException {
		String arrivalDisplay = rs.getString("arrival_time");
		int dayOffset = rs.getInt("arrival_day_offset");
		if (dayOffset > 0) {
			arrivalDisplay = arrivalDisplay + "(+" + dayOffset + ")";
		}

		System.out.println(
				"- ticket_id=" + rs.getInt("ticket_id")
						+ " | flight_number=" + rs.getString("flight_number")
						+ " | airline_name=" + valueOrDefault(rs.getString("airline_name"), "未知")
						+ " | flight_date=" + rs.getDate("flight_date")
						+ " | source_city=" + rs.getString("source_city")
						+ " | source_airport=" + rs.getString("source_airport")
						+ " | destination_city=" + rs.getString("destination_city")
						+ " | destination_airport=" + rs.getString("destination_airport")
						+ " | departure_time=" + rs.getString("departure_time")
						+ " | arrival_time=" + arrivalDisplay
						+ " | business_price=" + rs.getBigDecimal("business_price")
						+ " | business_remain=" + rs.getInt("business_remain")
						+ " | economy_price=" + rs.getBigDecimal("economy_price")
						+ " | economy_remain=" + rs.getInt("economy_remain")
		);
	}

	private static void printOrderRow(ResultSet rs) throws SQLException {
		String arrivalDisplay = rs.getString("arrival_time");
		int dayOffset = rs.getInt("arrival_day_offset");
		if (dayOffset > 0) {
			arrivalDisplay = arrivalDisplay + "(+" + dayOffset + ")";
		}

		System.out.println(
				"- order_id=" + rs.getInt("order_id")
						+ " | passenger_id=" + rs.getString("passenger_id")
						+ " | passenger_name=" + rs.getString("passenger_name")
						+ " | ticket_id=" + rs.getInt("ticket_id")
						+ " | cabin_class=" + rs.getString("cabin_class")
						+ " | order_time=" + rs.getTimestamp("order_time")
						+ " | flight_number=" + rs.getString("flight_number")
						+ " | flight_date=" + rs.getDate("flight_date")
						+ " | source_city=" + rs.getString("source_city")
						+ " | destination_city=" + rs.getString("destination_city")
						+ " | departure_time=" + rs.getString("departure_time")
						+ " | arrival_time=" + arrivalDisplay
		);
	}

	private static String capitalize(String text) {
		String lower = text.toLowerCase();
		return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
	}

	private static boolean isBlank(String text) {
		return text == null || text.trim().isEmpty();
	}

	private static String valueOrDefault(String text, String defaultValue) {
		if (text == null || text.trim().isEmpty()) {
			return defaultValue;
		}
		return text;
	}
}
