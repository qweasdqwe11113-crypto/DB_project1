package main;

import test.get_region_by_region_code;
import java.util.Scanner;

public class Main {
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
			get_region_by_region_code.listCitiesByRegionCode(regionCode);
		}
	}
}
