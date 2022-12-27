package com.jay.practice.spring.security.notice;

import org.springframework.data.jpa.repository.JpaRepository;

public interface NoticeRepository extends JpaRepository<com.jay.practice.spring.security.notice.Notice, Long> {
}