package main;

import java.util.Scanner;

public class Main {
	private static String currentPassengerId;
	private static String currentPassengerName;

	private static void logout() {
		currentPassengerId = null;
		currentPassengerName = null;
	}

	private static void printMenu() {
		System.out.println("================ 航空票务系统菜单 ================");
		System.out.println("1. 给定日期范围，基于航班信息生成机票");
		System.out.println("2. 按条件搜索机票");
		System.out.println("3. 模拟乘客预订机票");
		System.out.println("4. 管理我的订单");
		System.out.println("5. 联系人管理");
		System.out.println("0. 登出当前账号");
		System.out.println("================================================");
		System.out.println("当前登录用户: " + currentPassengerName + " (" + currentPassengerId + ")");
		System.out.print("请选择操作: ");
	}

	private static boolean handleLogin(Scanner scanner) {
		System.out.println("请先登录后再使用系统。");

		while (true) {
			System.out.print("请输入 passenger_id: ");
			String passengerId = scanner.nextLine().trim();

			System.out.print("请输入手机号: ");
			String mobileNumber = scanner.nextLine().trim();

			String passengerName = function.authenticatePassenger(passengerId, mobileNumber);
			if (passengerName != null) {
				currentPassengerId = passengerId;
				currentPassengerName = passengerName;
				System.out.println("登录成功，欢迎你，" + currentPassengerName + "。\n");
				return true;
			}

			System.out.println("登录失败，passenger_id 或手机号不正确，请重试。\n");
		}
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

		System.out.println("当前登录用户: " + currentPassengerName + " (" + currentPassengerId + ")");
		System.out.print("请输入乘机人 passenger_id（直接回车表示为自己订票）: ");
		String travelerPassengerId = scanner.nextLine().trim();
		if (travelerPassengerId.isEmpty()) {
			travelerPassengerId = currentPassengerId;
		}

		System.out.print("请选择舱位（Economy/Business）: ");
		String cabinClass = scanner.nextLine().trim();

		boolean success = function.bookTicketForTraveler(
				currentPassengerId,
				travelerPassengerId,
				ticketId,
				cabinClass
		);
		if (success) {
			System.out.println("订单已写入 ticket_order，且余票已同步减少 1。");
		}
	}

	private static void printOrderMenu() {
		System.out.println("--------------- 我的订单 ---------------");
		System.out.println("1. 搜索我的机票订单");
		System.out.println("2. 删除我的机票订单");
		System.out.println("0. 返回上一级");
		System.out.println("----------------------------------------");
		System.out.print("请选择操作: ");
	}

	private static void handleOrderSearch(Scanner scanner) {
		System.out.println("当前登录用户的全部订单如下：");
		function.searchOrdersForPassenger("", currentPassengerId);
		System.out.println();

		System.out.print("请输入 order_id（可留空，直接回车表示查看全部）: ");
		String orderIdText = scanner.nextLine().trim();

		int resultCount = function.searchOrdersForPassenger(orderIdText, currentPassengerId);
		System.out.println("本次共返回 " + resultCount + " 条订单记录。");
	}

	private static void handleOrderDelete(Scanner scanner) {
		System.out.println("当前登录用户的全部订单如下：");
		int resultCount = function.searchOrdersForPassenger("", currentPassengerId);
		if (resultCount <= 0) {
			System.out.println("当前没有可删除的订单。");
			return;
		}
		System.out.println();

		System.out.print("请输入要删除的 order_id: ");
		String orderIdText = scanner.nextLine().trim();

		int orderId;
		try {
			orderId = Integer.parseInt(orderIdText);
		} catch (NumberFormatException e) {
			System.out.println("order_id 必须是整数。");
			return;
		}

		boolean success = function.deleteOrderForPassenger(orderId, currentPassengerId);
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

	private static void printContactMenu() {
		System.out.println("--------------- 联系人管理 ---------------");
		System.out.println("1. 查看我的联系人");
		System.out.println("2. 添加联系人");
		System.out.println("0. 返回上一级");
		System.out.println("------------------------------------------");
		System.out.print("请选择操作: ");
	}

	private static void handleContactList() {
		function.listContacts(currentPassengerId);
	}

	private static void handleAddContact(Scanner scanner) {
		System.out.print("请输入要添加的联系人 passenger_id: ");
		String contactPassengerId = scanner.nextLine().trim();

		function.addContact(currentPassengerId, contactPassengerId);
	}

	private static void handleContacts(Scanner scanner) {
		while (true) {
			printContactMenu();
			String choice = scanner.nextLine().trim();

			switch (choice) {
				case "1":
					handleContactList();
					break;
				case "2":
					handleAddContact(scanner);
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

		if (!function.ensurePassengerContactTable()) {
			return;
		}

		try (Scanner scanner = new Scanner(System.in)) {
			System.out.println("系统已启动。\n");

			while (true) {
				if (!handleLogin(scanner)) {
					return;
				}

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
						case "5":
							handleContacts(scanner);
							break;
						case "0":
							System.out.println("当前账号已登出，即将返回登录界面。\n");
							logout();
							break;
						default:
							System.out.println("无效输入，请输入 0-5。\n");
							continue;
					}

					if (currentPassengerId == null) {
						break;
					}

					System.out.println();
				}
			}
		}
	}
}
