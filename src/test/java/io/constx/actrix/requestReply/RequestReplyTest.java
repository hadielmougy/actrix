package io.constx.actrix.requestReply;

import io.constx.actrix.Actor;
import io.constx.actrix.ActorMessage;
import io.constx.actrix.ActorRef;
import io.constx.actrix.ActorSystem;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.UUID;

import static org.awaitility.Awaitility.waitAtMost;

public class RequestReplyTest {

    @Test
    public void testRequestReply() {
        ActorSystem system = ActorSystem.newSystem();
        ActorRef receiverRef = system.register(new Receiver());
        Sender sender = new Sender(receiverRef);
        ActorRef senderRef = system.register(sender);
        senderRef.send(new Signal());


        waitAtMost(Duration.ofSeconds(1)).until(() -> sender.content != null);
    }

    private static final class Sender extends Actor {

        private final ActorRef receiverRef;
        public String content;

        private Sender(ActorRef receiverRef) {
            this.receiverRef = receiverRef;
        }

        @Override
        protected void receive(ActorMessage actorMessage) {
            switch (actorMessage) {
                case Signal signal -> send(receiverRef, new Request(getRef()));
                case Response response -> applyResponse(response);
                default -> throw new IllegalStateException();
            };
        }

        private void applyResponse(Response response) {
            this.content = response.content();
        }
    }

    private static final class Receiver extends Actor {

        @Override
        protected void receive(ActorMessage actorMessage) {
            send(((Request)actorMessage).responseTo(), new Response(UUID.randomUUID().toString()));
        }
    }

    record Request(ActorRef responseTo) implements ActorMessage {}
    record Response(String content) implements ActorMessage {}
    record Signal() implements ActorMessage {}


}
