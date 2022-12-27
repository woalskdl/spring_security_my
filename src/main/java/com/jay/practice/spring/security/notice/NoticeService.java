package com.jay.practice.spring.security.notice;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class NoticeService {

    private final com.jay.practice.spring.security.notice.NoticeRepository noticeRepository;

    /**
     * 모든 공지사항 조회
     *
     * @return 모든 공지사항 List
     */
    @Transactional(readOnly = true)
    public List<com.jay.practice.spring.security.notice.Notice> findAll() {
        return noticeRepository.findAll(Sort.by(Direction.DESC, "id"));
    }

    /**
     * 공지사항 저장
     *
     * @param title   제목
     * @param content 내용
     * @return 저장된 공지사항
     */
    public com.jay.practice.spring.security.notice.Notice saveNotice(String title, String content) {
        return noticeRepository.save(new com.jay.practice.spring.security.notice.Notice(title, content));
    }

    /**
     * 공지사항 삭제
     *
     * @param id ID
     */
    public void deleteNotice(Long id) {
        noticeRepository.findById(id).ifPresent(noticeRepository::delete);
    }
}
