package test;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class get_region_by_region_code {
	public static void listCitiesByRegionCode(String regionCode) {
		String sql = """
				SELECT DISTINCT a.city
				FROM airport a
				JOIN region r ON a.region_name = r.region_name
				WHERE r.region_code = ?
				ORDER BY a.city
				""";

		try (java.sql.Connection conn = main.Connection.getConnection();
			 PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setString(1, regionCode);

			try (ResultSet rs = ps.executeQuery()) {
				boolean hasResult = false;
				System.out.println("查询结果：");

				while (rs.next()) {
					hasResult = true;
					System.out.println("- " + rs.getString("city"));
				}

				if (!hasResult) {
					System.out.println("未找到该地区代码对应的城市。");
				}
			}
		} catch (SQLException e) {
			System.err.println("查询失败: " + e.getMessage());
		}
	}
}
