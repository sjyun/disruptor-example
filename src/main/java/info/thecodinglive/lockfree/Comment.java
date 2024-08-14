package info.thecodinglive.lockfree;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class Comment {
    Long id;
    String content;

    public Comment(Long id, String content) {
        this.id = id;
        this.content = content;
    }
}
