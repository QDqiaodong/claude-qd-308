-- 健身房 · 器械与团课
SET NAMES utf8mb4;

DROP TABLE IF EXISTS gym_trial_ticket;
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

-- 更衣柜：locker_state 只记「空闲 / 故障」；「占用」由未离场的入场单推出来，不另存
CREATE TABLE gym_locker (
  id           BIGINT      NOT NULL AUTO_INCREMENT,
  locker_code  VARCHAR(20) NOT NULL,
  locker_state VARCHAR(12) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_locker_code (locker_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 体验入场单：一张单同时钉住一个训练区和一个更衣柜；
-- 介绍人只是备注（referrer_id 指向会员，停卡/过期都行），开单不动会员档案
CREATE TABLE gym_trial_ticket (
  id              BIGINT      NOT NULL AUTO_INCREMENT,
  ticket_code     VARCHAR(20) NOT NULL,
  guest_name      VARCHAR(40) NOT NULL,
  area_id         BIGINT      NOT NULL,
  locker_id       BIGINT      NOT NULL,
  visit_date      DATE        NOT NULL,
  time_slot       VARCHAR(8)  NOT NULL,
  referrer_id     BIGINT      NULL,
  ticket_state    VARCHAR(12) NOT NULL,
  key_returned    TINYINT(1)  NOT NULL DEFAULT 0,
  created_at      DATETIME    NOT NULL,
  key_returned_at DATETIME    NULL,
  left_at         DATETIME    NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_ticket_code (ticket_code),
  KEY idx_ticket_area (area_id),
  KEY idx_ticket_locker (locker_id),
  KEY idx_ticket_state (ticket_state)
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

INSERT INTO gym_class (class_code, class_name, coach_name, class_date, start_time, area_id, seat_total, seat_used, class_state) VALUES
('GC-01', '动感单车', '刘教练', '2026-09-18', '19:00', 1, 20, 18, '待开课'),
('GC-02', '瑜伽初级', '孙教练', '2026-09-19', '10:00', 3, 15, 15, '已约满'),
('GC-03', '搏击操', '刘教练', '2026-09-19', '20:00', 2, 25, 9, '待开课'),
('GC-04', '普拉提', '孙教练', '2026-09-20', '15:00', 3, 12, 3, '待开课'),
('GC-05', '核心训练', '陈教练', '2026-09-18', '12:00', 2, 18, 18, '已完成');

INSERT INTO gym_member (member_code, member_name, phone, card_level, expire_date, member_state) VALUES
('MB-01', '张伟', '13800000001', '金卡', '2027-05-01', '正常'),
('MB-02', '李娜', '13800000002', '银卡', '2026-09-10', '正常'),
('MB-03', '王强', '13800000003', '普通卡', '2026-12-01', '已停卡'),
('MB-04', '赵敏', '13800000004', '银卡', '2027-01-15', '正常'),
('MB-05', '刘洋', '13800000005', '普通卡', '2026-09-25', '正常');

INSERT INTO gym_locker (locker_code, locker_state) VALUES
('LK-01', '空闲'),
('LK-02', '空闲'),
('LK-03', '空闲'),
('LK-04', '空闲'),
('LK-05', '空闲'),
('LK-06', '空闲'),
('LK-07', '空闲'),
('LK-08', '空闲'),
('LK-09', '故障'),
('LK-10', '空闲');

INSERT INTO gym_trial_ticket
  (ticket_code, guest_name, area_id, locker_id, visit_date, time_slot, referrer_id,
   ticket_state, key_returned, created_at, key_returned_at, left_at) VALUES
-- 在馆：占着 LK-01，钥匙还没还；介绍人王强是已停卡会员，照样能当介绍人
('TK-0001', '王先生', 1, 1, '2026-09-19', '晚上', 3, '在馆', 0, '2026-09-19 18:30:00', NULL, NULL),
-- 已离场：钥匙还了才走的；介绍人李娜卡已过期，也只是备注
('TK-0002', '刘女士', 2, 3, '2026-09-18', '下午', 2, '已离场', 1, '2026-09-18 14:05:00', '2026-09-18 16:50:00', '2026-09-18 17:00:00');
