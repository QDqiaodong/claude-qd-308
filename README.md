# 健身房 · 器械与团课

健身房的日常台账：**训练区**、**器械**、**团课**、**会员**。

业务重点：
- **团课名额是硬的**：已报名不能超过名额，页面上的「加一位」满了会被后端拦住；
- 训练区停用前要求先把器械挪走；器械只能放进「开放」的训练区；
- 会员卡到期没到期由后端算好（`expired` 字段）连同数据一起给前端。

## 技术栈

- 后端：Spring Boot 3.3 / Java 17、**JdbcTemplate + Map**（SQL 里用别名直接取成前端要的字段名，
  不建实体类、不写 RowMapper）、MySQL 8、Redis 7
- 前端：Vue 3（Composition API + **不用 axios，直接 fetch**）+ Element Plus + Vite
- 一键起：`./start.sh`

## 业务模块

1. **训练区**（`gym_area`）—— 编号名称、面积与容纳人数、开放与停用
2. **器械**（`gym_machine`）—— 编号名称类型、归属训练区、可用/占用/维修
3. **团课**（`gym_class`）—— 编号名称教练、日期时间、名额与已报名、待开课/已约满/已完成
4. **会员**（`gym_member`）—— 编号姓名电话、卡种、到期日、正常与已停卡

## 本地跑起来

| | 地址 |
| --- | --- |
| 前端页面 | http://127.0.0.1:8238/ |
| 后端接口 | http://127.0.0.1:8338/api/members |
| MySQL | 127.0.0.1:3538（库 `gym_center`） |
| Redis | 127.0.0.1:6538 |

容器名统一是 `claude-qd-308-{mysql,redis,backend,frontend}`。

```bash
./start.sh              # 起容器
docker compose ps       # 看状态
docker compose down -v  # 停掉并清数据
```
