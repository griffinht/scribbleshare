package canvas;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.stzups.scribbleshare.data.objects.canvas.Canvas;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;

@Fork(1)
@Warmup(iterations = 3)
@Measurement(iterations = 3)
public class CanvasBenchmark {
    @org.openjdk.jmh.annotations.State(Scope.Benchmark)
    public static class State {
        ByteBuf byteBuf;
        Canvas canvas;

        @Setup
        public void setup() {
            byteBuf = Unpooled.buffer();

            canvas = new Canvas();
            canvas.serialize(byteBuf);
            byteBuf.resetReaderIndex();
            byteBuf.resetWriterIndex();
        }

        @TearDown
        public void teardown() {
            byteBuf.release();
        }
    }

    @Benchmark
    public void serialize(State state) {
        state.canvas.serialize(state.byteBuf);
    }
}
