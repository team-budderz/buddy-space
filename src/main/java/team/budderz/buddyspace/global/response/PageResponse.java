package team.budderz.buddyspace.global.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 페이징 응답 공통 DTO
 * @param <T> 응답 content로 포함될 객체 타입
 */
@Getter
@AllArgsConstructor
public class PageResponse<T> {

    /**
     * 현재 페이지에 포함될 데이터 목록
     */
    private List<T> content;

    /**
     * 현재 페이지 번호 (0부터 시작)
     */
    private int pageNumber;

    /**
     * 한 페이지에 포함될 항목 개수
     */
    private int pageSize;

    /**
     * 전체 데이터 개수
     */
    private long totalElements;

    /**
     * 전체 페이지 개수
     */
    private int totalPages;

    /**
     * 현재 페이지가 첫 번째 페이지인지 여부
     */
    private boolean first;

    /**
     * 현재 페이지가 마지막 페이지인지 여부
     */
    private boolean last;

    /**
     * 현재 페이지가 비어 있는지 여부
     */
    private boolean empty;

    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast(),
                page.isEmpty()
        );
    }
}
