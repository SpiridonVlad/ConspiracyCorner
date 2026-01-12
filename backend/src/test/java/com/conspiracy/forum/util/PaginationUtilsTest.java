package com.conspiracy.forum.util;

import com.conspiracy.forum.dto.PageInput;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import static org.junit.jupiter.api.Assertions.*;

class PaginationUtilsTest {

    @Test
    void createPageable_ShouldUseDefaults_WhenPageInputIsNull() {
        Pageable pageable = PaginationUtils.createPageable(null);

        assertEquals(0, pageable.getPageNumber());
        assertEquals(10, pageable.getPageSize());
    }

    @Test
    void createPageable_ShouldConvertPageNumber_FromOneBased() {
        PageInput pageInput = PageInput.builder()
                .page(1)
                .size(20)
                .build();

        Pageable pageable = PaginationUtils.createPageable(pageInput);

        assertEquals(0, pageable.getPageNumber()); // 1-based to 0-based
        assertEquals(20, pageable.getPageSize());
    }

    @Test
    void createPageable_ShouldHandlePageTwo() {
        PageInput pageInput = PageInput.builder()
                .page(2)
                .size(15)
                .build();

        Pageable pageable = PaginationUtils.createPageable(pageInput);

        assertEquals(1, pageable.getPageNumber());
        assertEquals(15, pageable.getPageSize());
    }

    @Test
    void createPageable_ShouldClampNegativePageToZero() {
        PageInput pageInput = PageInput.builder()
                .page(-1)
                .size(10)
                .build();

        Pageable pageable = PaginationUtils.createPageable(pageInput);

        assertEquals(0, pageable.getPageNumber());
    }

    @Test
    void createPageable_ShouldClampSizeToMinimumOne() {
        PageInput pageInput = PageInput.builder()
                .page(1)
                .size(0)
                .build();

        Pageable pageable = PaginationUtils.createPageable(pageInput);

        assertEquals(1, pageable.getPageSize());
    }

    @Test
    void createPageable_ShouldClampSizeToMaximum100() {
        PageInput pageInput = PageInput.builder()
                .page(1)
                .size(200)
                .build();

        Pageable pageable = PaginationUtils.createPageable(pageInput);

        assertEquals(100, pageable.getPageSize());
    }

    @Test
    void createPageable_ShouldUseDefaultSort() {
        Pageable pageable = PaginationUtils.createPageable(null);

        Sort sort = pageable.getSort();
        assertTrue(sort.isSorted());
        assertEquals("postedAt", sort.iterator().next().getProperty());
        assertEquals(Sort.Direction.DESC, sort.iterator().next().getDirection());
    }

    @Test
    void createPageable_ShouldUseCustomSort() {
        PageInput pageInput = PageInput.builder()
                .page(1)
                .size(10)
                .build();
        Sort customSort = Sort.by(Sort.Direction.ASC, "title");

        Pageable pageable = PaginationUtils.createPageable(pageInput, customSort);

        Sort sort = pageable.getSort();
        assertEquals("title", sort.iterator().next().getProperty());
        assertEquals(Sort.Direction.ASC, sort.iterator().next().getDirection());
    }
}
