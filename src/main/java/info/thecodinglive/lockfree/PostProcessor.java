package info.thecodinglive.lockfree;

import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.util.DaemonThreadFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadFactory;

public class PostProcessor {
    static class Post {
        Integer id;
        String title;
        String writerName;
        List<Comment> commentList;
        List<AttachImage> images;

        public Post(Integer id, String title) {
            this.id = id;
            this.title = title;
            this.commentList = new ArrayList<>();
            this.images = new ArrayList<>();
        }
    }

    static class Comment {
        Long id;
        String content;

        public Comment(Long id, String content) {
            this.id = id;
            this.content = content;
        }
    }

    static class AttachImage {
        Long id;
        String url;
        public AttachImage(Long id, String url) {
            this.id = id;
            this.url = url;
        }
    }

    static class PostEvent {
        private Post post;

        public Post getPost() {
            return post;
        }

        public void setPost(Post post) {
            this.post = post;
        }
    }

    static class PostEventFactory implements EventFactory<PostEvent> {
        @Override
        public PostEvent newInstance() {
            return new PostEvent();
        }
    }

    static class CommentBinder implements EventHandler<PostEvent> {
        @Override
        public void onEvent(PostEvent event, long sequence, boolean endOfBatch) {
            Post post = event.getPost();
            // db를 조회하거나 api 호출

            post.commentList = List.of(
                    new Comment(1L, "Comment 1 for article " + post.id),
                    new Comment(2L, "Comment 2 for article " + post.id)
            );
            System.out.println("댓글 로드 완료: Article ID " + post.id);
        }
    }



    static class ImageBinder implements EventHandler<PostEvent> {
        @Override
        public void onEvent(PostEvent event, long sequence, boolean endOfBatch) throws Exception {
            Post post = event.getPost();

            post.images = List.of(
                    new AttachImage(1L, "image1.jpg for article " + post.id),
                    new AttachImage(2L, "image2.jpg for article " + post.id)
            );
            System.out.println("이미지 로드 완료: Article ID " + post.id);
        }
    }

    static void processWithDisruptor(List<Post> posts) {
        int bufferSize = 1024;
        ThreadFactory threadFactory = DaemonThreadFactory.INSTANCE;
        Disruptor<PostEvent> disruptor = new Disruptor<>(new PostEventFactory(), bufferSize, threadFactory);

        // 핸들러 설정 (댓글 로드와 이미지 로드를 병렬로 처리)
        disruptor.handleEventsWith(new CommentBinder(), new ImageBinder());

        // 최종 결과 출력을 위한 핸들러
        disruptor.handleEventsWith((event, sequence, endOfBatch) -> {
            Post post = event.getPost();
            System.out.println("처리 완료: Article ID " + post.id +
                    ", 댓글 수: " + post.commentList.size() +
                    ", 이미지 수: " + post.images.size());
        });

        // Disruptor 시작
        disruptor.start();

        // 데이터 발행
        RingBuffer<PostEvent> ringBuffer = disruptor.getRingBuffer();
        for (Post article : posts) {
            ringBuffer.publishEvent((event, sequence, buffer) -> event.setPost(buffer), article);
        }

        // Disruptor 종료
        disruptor.shutdown();
    }

    public static void main(String[] ar) {
        List<Post> posts = List.of(
                new Post(1, "post1"),
                new Post(2, "post2"),
                new Post(3, "post3")
        );

        processWithDisruptor(posts);
    }

}
