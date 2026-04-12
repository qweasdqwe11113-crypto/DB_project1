package main;

import java.util.Scanner;

public class Main {
	private static void printMenu() {
		System.out.println("================ 航空票务系统菜单 ================");
		System.out.println("1. 给定日期范围，基于航班信息生成机票");
		System.out.println("2. 按条件搜索机票");
		System.out.println("3. 模拟乘客预订机票");
		System.out.println("4. 搜索/删除订单");
		System.out.println("0. 退出");
		System.out.println("================================================");
		System.out.print("请选择操作: ");
	}

	private static void handleGenerateTickets(Scanner scanner) {
		System.out.print("请输入开始日期（YYYY-MM-DD）: ");
		String startDate = scanner.nextLine().trim();

		System.out.print("请输入结束日期（YYYY-MM-DD）: ");
		String endDate = scanner.nextLine().trim();

		int generated = function.generateTicketsByDateRange(startDate, endDate);
		System.out.println("本次查询到/生成对应航班记录数: " + generated);
	}

	private static void handleSearchTickets(Scanner scanner) {
		System.out.print("请输入出发城市: ");
		String sourceCity = scanner.nextLine().trim();

		System.out.print("请输入到达城市: ");
		String destinationCity = scanner.nextLine().trim();

		System.out.print("请输入日期（YYYY-MM-DD）: ");
		String dateText = scanner.nextLine().trim();

		System.out.print("请输入航空公司（可留空）: ");
		String airlineName = scanner.nextLine().trim();

		System.out.print("请输入出发时间下限（HH:mm，可留空）: ");
		String departureTime = scanner.nextLine().trim();

		System.out.print("请输入到达时间上限（HH:mm，可留空）: ");
		String arrivalTime = scanner.nextLine().trim();

		int resultCount = function.searchTickets(
				sourceCity,
				destinationCity,
				dateText,
				airlineName,
				departureTime,
				arrivalTime
		);

		System.out.println("本次共返回 " + resultCount + " 条机票记录。");
	}

	private static void handleBookTicket(Scanner scanner) {
		System.out.println("请先搜索机票，再选择要预订的 ticket_id。");

		System.out.print("请输入出发城市: ");
		String sourceCity = scanner.nextLine().trim();

		System.out.print("请输入到达城市: ");
		String destinationCity = scanner.nextLine().trim();

		System.out.print("请输入日期（YYYY-MM-DD）: ");
		String dateText = scanner.nextLine().trim();

		System.out.print("请输入航空公司（可留空）: ");
		String airlineName = scanner.nextLine().trim();

		System.out.print("请输入出发时间下限（HH:mm，可留空）: ");
		String departureTime = scanner.nextLine().trim();

		System.out.print("请输入到达时间上限（HH:mm，可留空）: ");
		String arrivalTime = scanner.nextLine().trim();

		int resultCount = function.searchTickets(
				sourceCity,
				destinationCity,
				dateText,
				airlineName,
				departureTime,
				arrivalTime
		);

		if (resultCount <= 0) {
			System.out.println("没有可预订的机票。");
			return;
		}

		System.out.print("请输入要预订的 ticket_id: ");
		String ticketIdText = scanner.nextLine().trim();

		int ticketId;
		try {
			ticketId = Integer.parseInt(ticketIdText);
		} catch (NumberFormatException e) {
			System.out.println("ticket_id 必须是整数。");
			return;
		}

		if (!function.showTicketDetail(ticketId)) {
			return;
		}

		System.out.print("请输入 passenger_id: ");
		String passengerId = scanner.nextLine().trim();

		System.out.print("请选择舱位（Economy/Business）: ");
		String cabinClass = scanner.nextLine().trim();

		boolean success = function.bookTicket(passengerId, ticketId, cabinClass);
		if (success) {
			System.out.println("订单已写入 ticket_order，且余票已同步减少 1。");
		}
	}

	private static void printOrderMenu() {
		System.out.println("--------------- 订单管理 ---------------");
		System.out.println("1. 搜索机票订单");
		System.out.println("2. 删除机票订单");
		System.out.println("0. 返回上一级");
		System.out.println("----------------------------------------");
		System.out.print("请选择操作: ");
	}

	private static void handleOrderSearch(Scanner scanner) {
		System.out.print("请输入 order_id（可留空）: ");
		String orderIdText = scanner.nextLine().trim();

		System.out.print("请输入 passenger_id（可留空）: ");
		String passengerId = scanner.nextLine().trim();

		int resultCount = function.searchOrders(orderIdText, passengerId);
		System.out.println("本次共返回 " + resultCount + " 条订单记录。");
	}

	private static void handleOrderDelete(Scanner scanner) {
		System.out.print("请输入要删除的 order_id: ");
		String orderIdText = scanner.nextLine().trim();

		int orderId;
		try {
			orderId = Integer.parseInt(orderIdText);
		} catch (NumberFormatException e) {
			System.out.println("order_id 必须是整数。");
			return;
		}

		boolean success = function.deleteOrder(orderId);
		if (success) {
			System.out.println("删除完成。");
		}
	}

	private static void handleOrders(Scanner scanner) {
		while (true) {
			printOrderMenu();
			String choice = scanner.nextLine().trim();

			switch (choice) {
				case "1":
					handleOrderSearch(scanner);
					break;
				case "2":
					handleOrderDelete(scanner);
					break;
				case "0":
					return;
				default:
					System.out.println("无效输入，请输入 0-2。\n");
			}

			System.out.println();
		}
	}

	public static void main(String[] args) {
		if (!Connection.initDriver()) {
			return;
		}

		if (!Connection.testConnection()) {
			return;
		}

		try (Scanner scanner = new Scanner(System.in)) {
			System.out.println("系统已启动。\n");

			while (true) {
				printMenu();
				String choice = scanner.nextLine().trim();

				switch (choice) {
					case "1":
						handleGenerateTickets(scanner);
						break;
					case "2":
						handleSearchTickets(scanner);
						break;
					case "3":
						handleBookTicket(scanner);
						break;
					case "4":
						handleOrders(scanner);
						break;
					case "0":
						System.out.println("程序已退出。");
						return;
					default:
						System.out.println("无效输入，请输入 0-4。\n");
				}

				System.out.println();
			}
		}
	}
}
