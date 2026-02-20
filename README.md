# 听书平台 (TingShu Platform)

> 基于 Spring Cloud 微服务架构的有声书平台系统

## 📖 项目简介

听书平台是一个基于 Spring Cloud 微服务架构的有声书内容服务平台，提供用户管理、内容管理、订单支付、搜索推荐等核心功能。系统采用前后端分离架构，支持高并发、高可用的业务场景。

## 🏗️ 技术架构

### 核心技术栈

- **Java**: 17
- **Spring Boot**: 3.0.5
- **Spring Cloud**: 2022.0.2
- **Spring Cloud Alibaba**: 2022.0.0.0-RC1
- **MyBatis Plus**: 3.5.3.1
- **MySQL**: 8.0.30
- **MongoDB**: Spring Data MongoDB (文档数据库)
- **Elasticsearch**: Spring Data Elasticsearch (搜索引擎)
- **Redis**: Redisson 3.20.0
- **Nacos**: 服务注册与配置中心
- **Gateway**: Spring Cloud Gateway
- **Kafka**: 3.1.0 (消息队列)
- **XXL-Job**: 2.4.0 (分布式任务调度)
- **Knife4j**: 4.1.0 (API 文档)

### 中间件与工具

- **MinIO**: 8.2.0 (对象存储)
- **腾讯云 VOD**: 2.1.4 (视频点播)
- **微信支付**: 0.0.3
- **FastJSON**: 1.2.29
- **Guava**: 23.0
- **Pinyin4j**: 2.5.0 (拼音工具)

## 📦 项目结构

```
tingshu-parent/
├── common/                    # 公共模块
│   ├── common-util/          # 通用工具类
│   ├── service-util/         # 服务工具类
│   └── kafka-util/           # Kafka 工具类
├── model/                     # 数据模型模块
├── server-gateway/            # API 网关服务
├── service/                   # 业务服务模块
│   ├── service-user/         # 用户服务
│   ├── service-album/        # 专辑服务
│   ├── service-order/        # 订单服务
│   ├── service-payment/      # 支付服务
│   ├── service-account/      # 账户服务
│   ├── service-search/       # 搜索服务
│   ├── service-dispatch/     # 调度服务
│   ├── service-cdc/          # CDC 数据变更服务
│   └── service-agent/        # 代理服务
└── service-client/            # 服务客户端模块
    ├── service-user-client/
    ├── service-album-client/
    ├── service-order-client/
    ├── service-search-client/
    └── service-account-client/
```

## 🚀 快速开始

### 环境要求

- JDK 17+
- Maven 3.6+
- MySQL 8.0+
- MongoDB 4.4+ (文档数据库)
- Elasticsearch 7.x+ (搜索引擎)
- Redis 6.0+
- Nacos 2.0+
- Kafka 3.0+ (可选)

### 配置说明

1. **数据库配置**
   - 创建数据库并导入初始化脚本
   - 在各服务的 `bootstrap.properties` 中配置数据库连接

2. **Nacos 配置**
   - 启动 Nacos 服务
   - 在各服务的 `bootstrap.properties` 中配置 Nacos 地址
   - 在 Nacos 控制台配置各服务的配置文件

3. **Redis 配置**
   - 启动 Redis 服务
   - 在 Nacos 配置中心配置 Redis 连接信息

4. **MongoDB 配置**
   - 启动 MongoDB 服务
   - 在 Nacos 配置中心配置 MongoDB 连接信息

5. **Elasticsearch 配置**
   - 启动 Elasticsearch 服务
   - 在 Nacos 配置中心配置 Elasticsearch 连接信息
   - 搜索服务依赖 Elasticsearch 进行全文检索

### 运行项目

```bash
# 1. 克隆项目
git clone <repository-url>
cd tingshu-parent

# 2. 编译项目
mvn clean install

# 3. 启动服务（按顺序启动）
# 启动网关服务
cd server-gateway
mvn spring-boot:run

# 启动业务服务
cd ../service/service-user
mvn spring-boot:run
```

### JVM 参数建议

为避免内存不足问题，建议在启动时配置以下 JVM 参数：

```bash
-Xms512m -Xmx1024m -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=256m -Xss256k
```

## 🔧 功能模块

### 核心功能

- ✅ **用户管理**: 用户注册、登录、个人信息管理
- ✅ **内容管理**: 专辑管理、音频管理、分类管理
- ✅ **订单系统**: 订单创建、支付、退款
- ✅ **支付系统**: 微信支付集成
- ✅ **搜索服务**: 全文搜索、推荐算法
- ✅ **账户系统**: 账户余额、充值、消费记录
- ✅ **任务调度**: 基于 XXL-Job 的分布式任务调度
- ✅ **数据同步**: 基于 CDC 的数据变更捕获

### API 文档

启动服务后，访问 Knife4j 文档地址：
- 网关文档: `http://localhost:网关端口/doc.html`

## 📝 开发规范

### 代码规范

- 遵循阿里巴巴 Java 开发手册
- 使用统一的代码格式化配置
- 提交前进行代码检查

### 分支管理

- `main`: 主分支，生产环境代码
- `develop`: 开发分支
- `feature/*`: 功能分支
- `hotfix/*`: 热修复分支

### 提交规范

```
feat: 新功能
fix: 修复bug
docs: 文档更新
style: 代码格式调整
refactor: 代码重构
test: 测试相关
chore: 构建/工具链相关
```

## 🐛 问题排查

### 常见问题

1. **内存不足错误**
   - 降低 JVM 堆内存配置
   - 检查系统可用内存
   - 关闭不必要的服务

2. **服务注册失败**
   - 检查 Nacos 服务是否启动
   - 检查网络连接
   - 检查配置文件中的 Nacos 地址

3. **数据库连接失败**
   - 检查数据库服务是否启动
   - 检查数据库连接配置
   - 检查数据库用户权限

## 📄 许可证

本项目采用私有许可证，未经授权不得使用。

## 👤 作者

**方毅**

- 项目维护者
- 微信: W3250848293
- 如有问题，请提交 Issue 或通过微信联系

## 🙏 致谢

感谢所有为本项目做出贡献的开发者。

---

**注意**: 本项目为商业项目，请勿用于商业用途。

