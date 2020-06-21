package org.projector.utils;

import java.util.List;

import org.projector.interfaces.Consumer;
import org.projector.interfaces.DeafConsumer;

public class Nullable {

    public static void checkNotNull(Object value, String name, String message) {
        if (value == null) {
            String info = String.format("%s can not be null%s%s", name,
                    message == null ? "" : ": ", message == null ? "" : message);
            throw new NullPointerException(info);
        }
    }

    public static <V, O> O ifNotNull(V value, Consumer<V, O> consumer) {
        checkNotNull(consumer, "Consumer", null);
        if (value != null) {
            return consumer.consume(value);
        }
        return null; //never
    }

    public static <V, O> O ifNull(V value, DeafConsumer<O> consumer) {
        checkNotNull(consumer, "Consumer", null);
        if (value == null) {
            return consumer.consume();
        }
        return null; //never
    }

    public static <V, O> O ifNullOrNot(V value, DeafConsumer<O> whenNull, Consumer<V, O> whenNotNull) {
        checkNotNull(whenNull, "Consumer for null value", null);
        checkNotNull(whenNotNull, "Consumer for not-null value", null);

        if (value == null) {
            return whenNull.consume();
        } else {
            return whenNotNull.consume(value);
        }
    }

    public static <V> V firstOrDefault(List<V> list, V defaultValue) {
        return ifNullOrNot(list, () -> defaultValue, (l) -> {
            for(V value : l) {
                if (value != null) {
                    return value;
                }
            }

            return defaultValue;
        });
    }

    public static <V> V lastOrDefault(List<V> list, V defaultValue) {
        return ifNullOrNot(list, () -> defaultValue, (l) -> {
            for (int i = l.size() - 1; i > -1; i--) {
                V value = l.get(i);
                if (value != null) {
                    return value;
                }
            }

            return defaultValue;
        });
    }
}