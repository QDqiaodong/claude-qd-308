-- 健身房 · 器械与团课
SET NAMES utf8mb4;

DROP TABLE IF EXISTS gym_visit;
DROP TABLE IF EXISTS gym_locker;
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
  end_time    VARCHAR(8)  NULL,
  area_id     BIGINT      NULL,
  seat_total  INT         NOT NULL DEFAULT 0,
  seat_used   INT         NOT NULL DEFAULT 0,
  class_state VARCHAR(12) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_class_code (class_code),
  KEY idx_class_area (area_id)
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

-- 更衣柜台账：柜子自己只记「正常 / 故障」，
-- 「空不空闲」由 gym_visit 里没离场的单子推出来，不在这里冗余一份。
CREATE TABLE gym_locker (
  id           BIGINT      NOT NULL AUTO_INCREMENT,
  locker_code  VARCHAR(20) NOT NULL,
  locker_name  VARCHAR(60) NULL,
  locker_state VARCHAR(12) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_locker_code (locker_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 体验入场单：开单时训练区容纳和更衣柜占用在同一个动作里校验落单。
-- active_locker 是生成列：单子在馆时等于 locker_id，离场后变 NULL，
-- 给它加唯一键，数据库层面保证同一个柜子同时只被一张在馆单占着。
CREATE TABLE gym_visit (
  id            BIGINT      NOT NULL AUTO_INCREMENT,
  visit_code    VARCHAR(24) NOT NULL,
  guest_name    VARCHAR(40) NOT NULL,
  area_id       BIGINT      NOT NULL,
  locker_id     BIGINT      NOT NULL,
  referrer_id   BIGINT      NULL,
  visit_state   VARCHAR(12) NOT NULL,
  key_returned  TINYINT(1)  NOT NULL DEFAULT 0,
  enter_time    DATETIME    NOT NULL,
  leave_time    DATETIME    NULL,
  active_locker BIGINT GENERATED ALWAYS AS (IF(visit_state = '在馆', locker_id, NULL)) STORED,
  PRIMARY KEY (id),
  UNIQUE KEY uk_visit_code (visit_code),
  UNIQUE KEY uk_visit_active_locker (active_locker),
  KEY idx_visit_area (area_id),
  KEY idx_visit_locker (locker_id)
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

INSERT INTO gym_class (class_code, class_name, coach_name, class_date, start_time, end_time, area_id, seat_total, seat_used, class_state) VALUES
('GC-01', '动感单车', '刘教练', '2026-09-18', '19:00', '19:45', 1, 20, 18, '待开课'),
('GC-02', '瑜伽初级', '孙教练', '2026-09-19', '10:00', '10:50', 3, 15, 15, '已约满'),
('GC-03', '搏击操', '刘教练', '2026-09-19', '20:00', '20:50', 2, 25, 9, '待开课'),
('GC-04', '普拉提', '孙教练', '2026-09-20', '15:00', '15:50', 3, 12, 3, '待开课'),
('GC-05', '核心训练', '陈教练', '2026-09-18', '12:00', '12:45', 2, 18, 18, '已完成');

INSERT INTO gym_member (member_code, member_name, phone, card_level, expire_date, member_state) VALUES
('MB-01', '张伟', '13800000001', '金卡', '2027-05-01', '正常'),
('MB-02', '李娜', '13800000002', '银卡', '2026-09-10', '正常'),
('MB-03', '王强', '13800000003', '普通卡', '2026-12-01', '已停卡'),
('MB-04', '赵敏', '13800000004', '银卡', '2027-01-15', '正常'),
('MB-05', '刘洋', '13800000005', '普通卡', '2026-09-25', '正常');

INSERT INTO gym_locker (locker_code, locker_name, locker_state) VALUES
('LK-01', '一层 A 排 01 柜', '正常'),
('LK-02', '一层 A 排 02 柜', '正常'),
('LK-03', '一层 A 排 03 柜', '正常'),
('LK-04', '一层 B 排 01 柜', '正常'),
('LK-05', '一层 B 排 02 柜', '正常'),
('LK-06', '一层 B 排 03 柜', '故障'),
('LK-07', '二层 C 排 01 柜', '正常'),
('LK-08', '二层 C 排 02 柜', '正常');

INSERT INTO gym_visit (visit_code, guest_name, area_id, locker_id, referrer_id, visit_state, key_returned, enter_time, leave_time) VALUES
('VS-0001', '周先生', 1, 1, 1, '在馆', 0, NOW(), NULL),
('VS-0002', '吴女士', 2, 4, 3, '在馆', 0, NOW(), NULL),
('VS-0003', '郑先生', 1, 2, 2, '已离场', 1, DATE_SUB(NOW(), INTERVAL 3 HOUR), DATE_SUB(NOW(), INTERVAL 1 HOUR));
