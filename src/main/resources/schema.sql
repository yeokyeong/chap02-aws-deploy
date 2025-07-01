-- --------------------------------------------------------
-- db 생성 및 유저 권한 할당 (필요하면 실행)
-- --------------------------------------------------------
-- 1. 유저 생성 (root 계정인 postgres로 진행)
-- CREATE USER ohgiraffers PASSWORD 'ohgiraffers' CREATEDB;

-- 2. 데이터베이스 생성
-- CREATE DATABASE ohgi_restaurant
--     WITH
--     OWNER = ohgiraffers
--     ENCODING = 'UTF8'
--     LC_COLLATE = 'en_US.utf8'
--     LC_CTYPE = 'en_US.utf8'
--     TEMPLATE template0;

-- 3. 스키마 생성 (ohgiraffers 계정으로 진행)
-- CREATE SCHEMA IF NOT EXISTS ohgi_restaurant;

-- 4. 권한 할당
-- GRANT ALL PRIVILEGES ON SCHEMA ohgi_restaurant TO ohgiraffers;
-- GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA ohgi_restaurant TO ohgiraffers;

-- 5. 검색 경로 설정
-- ALTER ROLE ohgiraffers SET search_path TO ohgi_restaurant;
-- SHOW search_path;


-- --------------------------------------------------------
-- PostgreSQL 스키마 정의 (chap02-aws-deploy)
-- --------------------------------------------------------

-- 카테고리 테이블
DROP TABLE IF EXISTS tbl_category CASCADE;
CREATE TABLE tbl_category (
    category_code SERIAL PRIMARY KEY,
    category_name VARCHAR(255) NOT NULL
);

-- 메뉴 테이블
DROP TABLE IF EXISTS tbl_menu CASCADE;
CREATE TABLE tbl_menu (
    menu_code SERIAL PRIMARY KEY,
    menu_name VARCHAR(255) NOT NULL,
    menu_price INTEGER NOT NULL,
    menu_description TEXT,
    menu_orderable CHAR(1) NOT NULL DEFAULT 'Y',
    category_code INTEGER REFERENCES tbl_category(category_code),
    menu_image_url VARCHAR(255),
    menu_stock INTEGER NOT NULL DEFAULT 0
); 