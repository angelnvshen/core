package own.jucAction;

import java.util.Map;
import java.util.concurrent.*;

public class Memorize2<A, V> implements Computable<A, V> {

    private final Map<A, Future<V>> cache = new ConcurrentHashMap<>();
    private final Computable<A, V> c;

    public Memorize2(Computable<A, V> c) {
        this.c = c;
    }

    @Override
    public V compute(A arg) throws InterruptedException {
        Future<V> f = cache.get(arg);
        if (f == null) {
            Callable<V> eval = () -> c.compute(arg);
            FutureTask<V> ft = new FutureTask<>(eval);
            f = (Future<V>) ft;
            cache.put(arg, (Future<V>) ft);
            ft.run();
        }
        try {
            return f.get();
        } catch (ExecutionException e) {
            throw Util.launderThrowable(e.getCause());
        }
    }
}
