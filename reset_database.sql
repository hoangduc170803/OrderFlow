-- Script để reset database hoàn toàn
-- CẢNH BÁO: Script này sẽ xóa toàn bộ dữ liệu trong database

-- Xóa database hiện tại
DROP DATABASE IF EXISTS flower_shop;

-- Tạo database mới
CREATE DATABASE flower_shop 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

-- Sử dụng database mới
USE flower_shop;

-- Database sẽ được tạo tự động bởi Hibernate với cấu hình create-drop
