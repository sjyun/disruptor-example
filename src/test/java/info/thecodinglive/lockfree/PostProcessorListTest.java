package info.thecodinglive.lockfree;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Consumer;

public class PostProcessorListTest {
    private static Logger LOG = LoggerFactory.getLogger(PostProcessorListTest.class);

    @Test
    void bindListTestWithDisruptor() {
        PostProcessorList dut = new PostProcessorList();

        List<Post> posts = List.of(
                new Post(1, "Article 1"),
                new Post(2, "Article 2"),
                new Post(3, "Article 3")
        );


        List<Consumer<Post>> processors = List.of(
                dut::bindWriterProfile,
                dut::bindComment,
                dut::bindImage
        );

        dut.processWithDisruptor(posts, processors);
    }
}
