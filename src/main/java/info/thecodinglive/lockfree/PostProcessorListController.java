package info.thecodinglive.lockfree;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@RestController
public class PostProcessorListController {
    private static Logger LOG = LoggerFactory.getLogger(PostProcessorListController.class);
    private final PostProcessorList postProcessorList = new PostProcessorList();

    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private static class PostResponse {
        private Integer id;
        private String title;
        private String writerName;
        private List<Comment> commentList;
        private List<AttachImage> attachImages;
    }

    private List<Post> fetchPostsFromDatabase() {
        // 실제 데이터베이스 조회 로직
        return List.of(
                new Post(1, "첫 번째 포스트"),
                new Post(2, "두 번째 포스트"),
                new Post(3, "세 번째 포스트")
        );
    }

    private PostResponse convert(Post post) {
        return PostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .writerName(post.getWriterName())
                .attachImages(post.getImages())
                .commentList(post.commentList)
                .build();
    }

    private List<PostResponse> convertToResponseList(List<Post> posts) {
        return posts.stream()
                .map(this::convert)
                .collect(Collectors.toList());
    }

    @GetMapping("/api/v1/posts")
    public CompletableFuture<List<PostResponse>> getPosts() {
        List<Post> posts = fetchPostsFromDatabase(); // 데이터베이스에서 포스트 조회
        List<Consumer<Post>> processors = List.of(
                postProcessorList::bindComment,
                postProcessorList::bindImage,
                postProcessorList::bindWriterProfile
        );

        return CompletableFuture.supplyAsync(() -> {
            postProcessorList.processWithDisruptor(posts, processors);
            return posts;
        }).thenApply(this::convertToResponseList)
                .exceptionally(ex -> {
                    LOG.error("error", ex);
                    return Collections.emptyList();
                });
    }
}
