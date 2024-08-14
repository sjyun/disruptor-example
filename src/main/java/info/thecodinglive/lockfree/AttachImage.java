package info.thecodinglive.lockfree;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AttachImage {
    Long id;
    String url;

    public AttachImage(Long id, String url) {
        this.id = id;
        this.url = url;
    }
}
