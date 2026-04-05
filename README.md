# CS307 数据库项目 Part1

## 项目简介

本项目是2026年春季CS307课程的数据库设计与实现任务，基于提供的航班相关CSV数据，完成E-R图设计、数据库搭建、数据导入和CRUD操作实现。

## 项目结构

```Java
DB_project1/
├── src/                  # Java源代码
│   ├── main/             # 主代码（CRUD操作实现）
│   └── test/             # 测试代码
├── sql/                  # SQL脚本
│   ├── create_tables.sql # 创建表结构的DDL语句
│   └── import_data.sql   # 数据导入脚本
├── data/                 # 原始CSV数据文件
│   ├── airline.csv       # 航空公司信息
│   ├── airport.csv       # 机场信息
│   ├── passenger.csv     # 乘客信息
│   ├── region.csv        # 地区信息
│   └── tickets.csv       # 机票信息（需拆分）
├── docs/                 # 项目文档
│   ├── report.pdf        # 项目报告
│   └── er_diagram.png    # E-R图截图
├── .gitignore            # Git忽略文件配置
└── README.md             # 项目说明
```

## 环境依赖

- **数据库**：PostgreSQL 14+
- **Java**：JDK 11+
- **IDE**：IntelliJ IDEA（或其他Java IDE）
- **数据库客户端**：DataGrip（用于可视化E-R图）

## 快速开始

### 1. 数据库搭建

1. 启动PostgreSQL服务，创建数据库（例如 `cs307_project`）：
   ```sql
   CREATE DATABASE cs307_project;
   ```
2. 连接到数据库，执行 `sql/create_tables.sql` 创建表结构：
   ```bash
   psql -U username -d cs307_project -f sql/create_tables.sql
   ```

### 2. 数据导入

1. 将 `data/` 目录下的CSV文件放入PostgreSQL可访问的路径。
2. 执行 `sql/import_data.sql` 导入数据：
   ```bash
   psql -U username -d cs307_project -f sql/import_data.sql
   ```

### 3. 运行Java程序

1. 在IDE中导入项目，配置依赖（如PostgreSQL JDBC驱动）。
2. 运行 `src/main/` 目录下的主类（如 `Main.java`），执行CRUD操作。

## 功能说明

- **E-R图设计**：基于CSV数据绘制实体-关系图，包含航空公司、机场、乘客、地区、机票等实体。
- **数据库设计**：遵循三大范式，设计表结构并添加主键、外键约束。
- **数据导入**：通过SQL脚本将CSV数据批量导入数据库。
- **CRUD操作**：
  - 生成机票（基于日期范围）。
  - 搜索机票（按出发/到达城市、日期等条件）。
  - 模拟预订流程（记录订单、减少座位数）。
  - 管理订单（搜索、删除）。

## 技术栈

- **数据库**：PostgreSQL
- **后端**：Java
- **SQL**：DDL（数据定义语言）、DML（数据操作语言）

## 注意事项

- `tickets.csv` 需要拆分为 `flight` 和 `ticket` 表。
- 地区数据可能存在重复，需特殊处理（如香港）。
- 到达时间可能跨天，查询时需注意处理逻辑。

## 提交说明

- 项目报告（PDF格式）放入 `docs/` 目录。
- SQL脚本和Java源代码按目录结构组织。
- 所有文件压缩为 `.zip` 格式后提交。

