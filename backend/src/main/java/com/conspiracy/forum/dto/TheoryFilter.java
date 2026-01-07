package com.conspiracy.forum.dto;

import com.conspiracy.forum.enums.TheoryStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TheoryFilter {
    private TheoryStatus status;
    private String keyword;
    private Boolean hotOnly;
    private Integer minCommentCount;
}
