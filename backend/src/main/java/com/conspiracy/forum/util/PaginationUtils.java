package com.conspiracy.forum.util;

import com.conspiracy.forum.dto.PageInput;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * Utility class for common pagination operations.
 */
public final class PaginationUtils {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 10;
    private static final int MAX_SIZE = 100;

    private PaginationUtils() {
        // Utility class - prevent instantiation
    }

    /**
     * Creates a Pageable object from PageInput with default sorting by postedAt descending.
     *
     * @param pageInput the page input from the request
     * @return a configured Pageable object
     */
    public static Pageable createPageable(PageInput pageInput) {
        return createPageable(pageInput, Sort.by(Sort.Direction.DESC, "postedAt"));
    }

    /**
     * Creates a Pageable object from PageInput with custom sorting.
     *
     * @param pageInput the page input from the request
     * @param sort the sort configuration
     * @return a configured Pageable object
     */
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
