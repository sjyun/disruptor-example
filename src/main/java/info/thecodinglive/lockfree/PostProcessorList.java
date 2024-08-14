package info.thecodinglive.lockfree;

import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.util.DaemonThreadFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadFactory;
import java.util.function.Consumer;


public class PostProcessorList {
    private static Logger LOG = LoggerFactory.getLogger(PostProcessorList.class);


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


    void bindComment(Post post) {
        // db를 조회하거나 api 호출
        post.commentList = List.of(
                new Comment(1L, "Comment 1 for article " + post.id),
                new Comment(2L, "Comment 2 for article " + post.id)
        );
        LOG.debug("댓글 로드 완료: Post ID: {}", post.id);
    }


    void bindImage(Post post) {
        // db를 조회하거나 api 호출
        post.images = List.of(
                new AttachImage(1L, "image1.jpg for article " + post.id),
                new AttachImage(2L, "image2.jpg for article " + post.id)
        );
        LOG.debug("이미지 로드 완료: Post ID: {}", post.id);
    }

    void bindWriterProfile(Post post) {
        if (Objects.isNull(post)) {
            LOG.error("post is null");
        }
        post.setWriterName("홍길동" + post.id);
        LOG.debug("프로필 로드 완료: Post ID: {}", post.id);
    }

    static class GenericBinder implements EventHandler<PostEvent> {
        private final Consumer<Post> bindFunction;
        public GenericBinder(Consumer<Post> bindFunction) {
            this.bindFunction = bindFunction;
        }

        @Override
        public void onEvent(PostEvent event, long sequence, boolean endOfBatch) throws Exception {
            bindFunction.accept(event.getPost());
            LOG.debug("완료: Post ID {}", event.getPost().getId());
        }
    }


    public void processWithDisruptor(List<Post> posts, List<Consumer<Post>> processors) {
        int bufferSize = 1024;
        ThreadFactory threadFactory = DaemonThreadFactory.INSTANCE;
        Disruptor<PostEvent> disruptor = new Disruptor<>(new PostEventFactory(), bufferSize, threadFactory);


        //bind함수들을 핸들러로 변환
        @SuppressWarnings("unchecked")
        EventHandler<PostEvent>[] binders = processors.stream()
                .map(processor -> new GenericBinder(processor))
                .toArray(EventHandler[]::new);

        //모든 핸들러를 병렬로 실행
        disruptor.handleEventsWith(binders);


        // 최종 결과 출력을 위한 핸들러
        disruptor.handleEventsWith((event, sequence, endOfBatch) -> {
            Post post = event.getPost();
            LOG.debug("처리 완료: Article ID " + post.id +
                    ", 댓글 수: " + post.commentList.size() +
                    ", 이미지 수: " + post.images.size() +
                    ", 저자: " + post.getWriterName());
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
}
