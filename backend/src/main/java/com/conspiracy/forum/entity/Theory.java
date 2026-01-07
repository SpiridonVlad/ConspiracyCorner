package com.conspiracy.forum.entity;

import com.conspiracy.forum.enums.TheoryStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "theories")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Theory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private TheoryStatus status = TheoryStatus.UNVERIFIED;

    @ElementCollection
    @CollectionTable(name = "theory_evidence_urls", joinColumns = @JoinColumn(name = "theory_id"))
    @Column(name = "url")
    @Builder.Default
    private List<String> evidenceUrls = new ArrayList<>();

    @Column(name = "posted_at")
    @Builder.Default
    private LocalDateTime postedAt = LocalDateTime.now();

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "is_anonymous_post")
    @Builder.Default
    private boolean isAnonymousPost = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @OneToMany(mappedBy = "theory", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Comment> comments = new ArrayList<>();

    @Column(name = "comment_count")
    @Builder.Default
    private int commentCount = 0;

    public void incrementCommentCount() {
        this.commentCount++;
    }

    public void decrementCommentCount() {
        if (this.commentCount > 0) {
            this.commentCount--;
        }
    }
}
