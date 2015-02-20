package se.bylenny.savings;

import android.os.Handler;
import android.os.Looper;

import com.squareup.otto.Bus;

public class MessageBus extends Bus {

    private static final MessageBus BUS = new MessageBus();
    private static final String MESSAGE_BUS = "MESSAGE_BUS";

    public static MessageBus getInstance() {
        return BUS;
    }

    private MessageBus() {
        super(MESSAGE_BUS);
        // No instances.
    }

    public static void send(final Object event) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                BUS.post(event);
            }
        });
    }

    public static void subscribe(Object object) {
        BUS.register(object);
    }

    public static void unsubscribe(Object object) {
        BUS.unregister(object);
    }
}
