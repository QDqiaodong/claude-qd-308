-- 健身房 · 器械与团课
SET NAMES utf8mb4;

DROP TABLE IF EXISTS gym_class;
DROP TABLE IF EXISTS gym_member;
DROP TABLE IF EXISTS gym_machine;
DROP TABLE IF EXISTS gym_area;

CREATE TABLE gym_area (
  id         BIGINT      NOT NULL AUTO_INCREMENT,
  area_code  VARCHAR(20) NOT NULL,
  area_name  VARCHAR(60) NOT NULL,
  floor_size INT         NULL,
  capacity   INT         NULL,
  area_state VARCHAR(12) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_area_code (area_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE gym_machine (
  id            BIGINT      NOT NULL AUTO_INCREMENT,
  machine_code  VARCHAR(20) NOT NULL,
  machine_name  VARCHAR(60) NOT NULL,
  machine_type  VARCHAR(20) NULL,
  area_id       BIGINT      NULL,
  machine_state VARCHAR(12) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_machine_code (machine_code),
  KEY idx_machine_area (area_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE gym_class (
  id          BIGINT      NOT NULL AUTO_INCREMENT,
  class_code  VARCHAR(20) NOT NULL,
  class_name  VARCHAR(60) NOT NULL,
  coach_name  VARCHAR(32) NULL,
  class_date  DATE        NOT NULL,
  start_time  VARCHAR(8)  NULL,
  seat_total  INT         NOT NULL DEFAULT 0,
  seat_used   INT         NOT NULL DEFAULT 0,
  class_state VARCHAR(12) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_class_code (class_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE gym_member (
  id           BIGINT      NOT NULL AUTO_INCREMENT,
  member_code  VARCHAR(20) NOT NULL,
  member_name  VARCHAR(40) NOT NULL,
  phone        VARCHAR(11) NULL,
  card_level   VARCHAR(16) NULL,
  expire_date  DATE        NULL,
  member_state VARCHAR(12) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_member_code (member_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO gym_area (area_code, area_name, floor_size, capacity, area_state) VALUES
('AR-01', '有氧器械区', 220, 30, '开放'),
('AR-02', '自由力量区', 180, 20, '开放'),
('AR-03', '固定器械区', 150, 25, '开放'),
('AR-04', '私教区', 60, 6, '停用');

INSERT INTO gym_machine (machine_code, machine_name, machine_type, area_id, machine_state) VALUES
('GM-01', '跑步机 T1', '有氧', 1, '可用'),
('GM-02', '跑步机 T2', '有氧', 1, '占用'),
('GM-03', '椭圆机 E1', '有氧', 1, '维修'),
('GM-04', '史密斯架 S1', '力量', 2, '可用'),
('GM-05', '坐姿推胸 M1', '固定器械', 3, '可用');

INSERT INTO gym_class (class_code, class_name, coach_name, class_date, start_time, seat_total, seat_used, class_state) VALUES
('GC-01', '动感单车', '刘教练', '2026-09-18', '19:00', 20, 18, '待开课'),
('GC-02', '瑜伽初级', '孙教练', '2026-09-19', '10:00', 15, 15, '已约满'),
('GC-03', '搏击操', '刘教练', '2026-09-19', '20:00', 25, 9, '待开课'),
('GC-04', '普拉提', '孙教练', '2026-09-20', '15:00', 12, 3, '待开课'),
('GC-05', '核心训练', '陈教练', '2026-09-18', '12:00', 18, 18, '已完成');

INSERT INTO gym_member (member_code, member_name, phone, card_level, expire_date, member_state) VALUES
('MB-01', '张伟', '13800000001', '金卡', '2027-05-01', '正常'),
('MB-02', '李娜', '13800000002', '银卡', '2026-09-10', '正常'),
('MB-03', '王强', '13800000003', '普通卡', '2026-12-01', '已停卡'),
('MB-04', '赵敏', '13800000004', '银卡', '2027-01-15', '正常'),
('MB-05', '刘洋', '13800000005', '普通卡', '2026-09-25', '正常');
