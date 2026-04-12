# CS307 Database Project Part 1

## 项目简介
本项目是 CS307 课程 Project Part 1 的数据库系统实现，使用 PostgreSQL 存储数据，使用 Java 通过 JDBC 访问数据库并完成命令行交互。

项目围绕航班、机票、乘客与订单展开，完成了以下主要内容：

- 关系数据库表设计
- CSV 数据导入
- 机票查询与订单管理
- 登录认证
- 联系人管理
- 支持为自己或联系人订票

## 项目结构
```text
DB_project1/
├─ data/                      原始 CSV 数据
│  ├─ airline.csv
│  ├─ airport.csv
│  ├─ passenger.csv
│  ├─ region.csv
│  └─ tickets.csv
├─ lib/
│  └─ postgresql-42.2.5.jar   PostgreSQL JDBC 驱动
├─ sql/
│  ├─ create_tables.sql       建表脚本
│  └─ import_data.sql         数据导入脚本
├─ src/
│  ├─ main/
│  │  ├─ Connection.java      数据库连接管理
│  │  ├─ function.java        核心业务逻辑
│  │  └─ Main.java            程序主入口与命令行菜单
│  └─ test/                   数据查询测试代码
├─ out/                       编译输出目录
└─ README.md
```

## 开发环境
- Java: JDK 11 及以上
- PostgreSQL: 建议 14 及以上
- OS: Windows
- IDE: IntelliJ IDEA 或其他支持 Java 的 IDE

## 数据库配置
当前项目在代码中的默认数据库连接配置位于 [Connection.java](d:/code/ideaprogramms/DB_project1/src/main/Connection.java)：

```java
private static final String DB_URL = "jdbc:postgresql://localhost:5432/cs307project1";
private static final String DB_USER = "checker";
private static final String DB_PASSWORD = "123456";
```

如果本地数据库配置不同，需要先修改以上内容再运行程序。

## 初始化步骤

### 1. 创建数据库
在 PostgreSQL 中创建数据库，例如：

```sql
CREATE DATABASE cs307project1;
```

### 2. 执行建表脚本
运行：

```powershell
psql -U checker -d cs307project1 -f sql/create_tables.sql
```

### 3. 执行数据导入脚本
运行：

```powershell
psql -U checker -d cs307project1 -f sql/import_data.sql
```

说明：
- `import_data.sql` 中使用了本项目本地路径导入 CSV
- 如果项目目录位置变化，需要同步修改脚本中的文件路径

## 编译与运行

### 1. 编译
在项目根目录执行：

```powershell
javac -encoding UTF-8 -cp lib\postgresql-42.2.5.jar -d out\production\DB_project1 src\main\*.java src\test\*.java
```

### 2. 运行主程序
```powershell
java -cp "out\production\DB_project1;lib\postgresql-42.2.5.jar" main.Main
```

如果使用 IntelliJ IDEA，也可以直接运行 [Main.java](d:/code/ideaprogramms/DB_project1/src/main/Main.java)。

## 程序使用方法

### 1. 登录
程序启动后先进行登录：

- 输入 `passenger_id`
- 输入该乘客的 `mobile_number`

登录成功后进入主菜单。

### 2. 主菜单功能
当前系统支持以下功能：

1. 给定日期范围，基于航班信息生成机票
2. 按条件搜索机票
3. 模拟乘客预订机票
4. 管理我的订单
5. 联系人管理
0. 登出当前账号

### 3. 搜索机票
需要输入：

- 出发城市
- 到达城市
- 日期

可选输入：

- 航空公司
- 出发时间下限
- 到达时间上限

### 4. 预订机票
预订流程如下：

1. 先按条件搜索机票
2. 输入要预订的 `ticket_id`
3. 输入乘机人 `passenger_id`
   - 直接回车表示给自己订票
   - 输入联系人编号表示给联系人订票
4. 选择舱位 `Economy` 或 `Business`

预订成功后：

- 在 `ticket_order` 中新增一条订单
- 对应舱位余票减 1

### 5. 我的订单
支持：

- 查看当前登录乘客的全部订单
- 按 `order_id` 精确查询
- 删除自己的订单

删除订单后，对应舱位余票会恢复 1。

### 6. 联系人管理
联系人功能支持：

- 查看我的联系人
- 添加联系人

说明：

- 联系人必须是系统中已经存在的乘客
- 一个乘客不能把自己添加为联系人
- 不允许重复添加同一联系人
- 订票时只能为自己或自己的联系人订票

## 当前已实现功能

### 数据库部分
- `region`、`airline`、`airport`、`passenger`、`flight`、`ticket`、`ticket_order` 表设计
- 新增 `passenger_contact` 表用于维护联系人关系
- 支持数据导入与基础约束

### Java 程序部分
- 数据库连接初始化与测试
- 登录认证
- 机票查询
- 订单创建、查询、删除
- 联系人查看与添加
- 支持为联系人订票
- 支持单次程序启动后多用户轮流登录使用

## 测试代码说明
`src/test/` 目录下保留了若干查询测试类，用于数据检查与功能验证，例如：

- 查询地区对应城市
- 查询城市对应机场
- 查询航班
- 查询机票

这些测试类不属于主菜单系统，但可用于单项功能检查。

## 注意事项
- 本项目当前为命令行程序
- 运行前必须保证 PostgreSQL 服务已启动
- 如果数据库账号、密码、端口、库名不同，需要修改 [Connection.java](d:/code/ideaprogramms/DB_project1/src/main/Connection.java)
- 如果项目移动了目录位置，`import_data.sql` 中的 CSV 路径也需要同步修改
- 联系人关系表虽然会在程序启动时自动确保存在，但正式初始化数据库时仍建议执行 `sql/create_tables.sql`

## 后续可扩展方向
- 优化“生成机票”流程
- 增加图形界面或网页前端
- 增强订单权限与认证机制
- 扩展更完整的联系人订票流程
