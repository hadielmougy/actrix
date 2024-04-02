package io.constx.actrix;

import java.util.concurrent.ConcurrentLinkedDeque;

public final class MailBox {
    private final ConcurrentLinkedDeque<ActorMessage> queue = new ConcurrentLinkedDeque<>();

    public void add(ActorMessage msg) {
        queue.add(msg);
    }

    public ActorMessage take() {
        return queue.pop();
    }
}
