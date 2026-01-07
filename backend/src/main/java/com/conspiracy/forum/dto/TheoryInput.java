package com.conspiracy.forum.dto;

import com.conspiracy.forum.enums.TheoryStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TheoryInput {
    private String title;
    private String content;
    private TheoryStatus status;
    private List<String> evidenceUrls;
    private Boolean anonymousPost;
}
