package com.conspiracy.forum.util;

import com.conspiracy.forum.dto.PageInput;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public final class PaginationUtils {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 10;
    private static final int MAX_SIZE = 100;

    private PaginationUtils() {
    }

    public static Pageable createPageable(PageInput pageInput) {
        return createPageable(pageInput, Sort.by(Sort.Direction.DESC, "postedAt"));
    }

    public static Pageable createPageable(PageInput pageInput, Sort sort) {
        int page = DEFAULT_PAGE;
        int size = DEFAULT_SIZE;

        if (pageInput != null) {
            page = Math.max(pageInput.getPage() - 1, 0);
            size = Math.max(1, Math.min(pageInput.getSize(), MAX_SIZE));
        }

        return PageRequest.of(page, size, sort);
    }
}
