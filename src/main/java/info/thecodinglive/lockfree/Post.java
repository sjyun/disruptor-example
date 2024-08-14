package info.thecodinglive.lockfree;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Post {
    Integer id;
    String title;
    String writerName;
    List<Comment> commentList;
    List<AttachImage> images;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getWriterName() {
        return writerName;
    }

    public void setWriterName(String writerName) {
        this.writerName = writerName;
    }

    public List<Comment> getCommentList() {
        return commentList;
    }

    public void setCommentList(List<Comment> commentList) {
        this.commentList = commentList;
    }

    public List<AttachImage> getImages() {
        return images;
    }

    public void setImages(List<AttachImage> images) {
        this.images = images;
    }

    public Post(Integer id, String title) {
        this.id = id;
        this.title = title;
        this.commentList = new ArrayList<>();
        this.images = new ArrayList<>();
    }
}
