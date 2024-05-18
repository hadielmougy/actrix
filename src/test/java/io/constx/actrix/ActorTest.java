package io.constx.actrix;

import org.junit.jupiter.api.RepeatedTest;
import java.time.Duration;

import static org.awaitility.Awaitility.*;

public class ActorTest {

    @RepeatedTest(2)
    public void testSendReceive() {
        ActorSystem system = ActorSystem.newSystem();
        ReceiverActor receiverActor = new ReceiverActor("receiver-actor");
        system.register(receiverActor);
        system.register(new SenderActor("sender-actor", true));
        waitAtMost(Duration.ofSeconds(1)).until(() -> receiverActor.counter == 1);
    }

    @RepeatedTest(2)
    public void testSendReceive100_000() {
        ActorSystem system = ActorSystem.newSystem();
        ReceiverActor receiverActor = new ReceiverActor("receiver-actor");
        system.register(receiverActor);
        system.register(new SenderActor("sender-actor", false));
        waitAtMost(Duration.ofSeconds(5)).until(() -> receiverActor.counter == 100_000);
    }

    @RepeatedTest(2)
    public void testSendReceive200_000() {
        ActorSystem system = ActorSystem.newSystem();
        ReceiverActor receiverActor01 = new ReceiverActor("receiver-actor-01");
        ReceiverActor receiverActor02 = new ReceiverActor("receiver-actor-02");
        system.register(receiverActor01);
        system.register(receiverActor02);
        system.register(new DualSenderActor("sender-actor"));
        waitAtMost(Duration.ofSeconds(5)).until(() -> receiverActor01.counter == 200_000);
        waitAtMost(Duration.ofSeconds(5)).until(() -> receiverActor02.counter == 200_000);
    }

    @RepeatedTest(2)
    public void testPingPong() {
        ActorSystem system = ActorSystem.newSystem();
        PingActor pingActor = new PingActor("ping-actor");
        PongActor pongActor = new PongActor("pong-actor");
        system.register(pingActor);
        system.register(pongActor);
        waitAtMost(Duration.ofSeconds(5)).until(() -> pingActor.counter == 100_000);
    }

    @RepeatedTest(2)
    public void testSelfCall() {
        ActorSystem system = ActorSystem.newSystem();
        var selfCallActor = new SelfCallActor();
        system.register(selfCallActor);
        waitAtMost(Duration.ofSeconds(5)).until(() -> selfCallActor.counter == 100_000);
    }

    private static final class SenderActor extends Actor {


        private final boolean singleSender;

        public SenderActor(String id, boolean singleSender) {
            super(id);
            this.singleSender = singleSender;
        }

        @Override
        protected void init() {
            if (singleSender) {
                send("receiver-actor", new IncrementMessage());
            } else {
                for (int i = 0; i < 100_000; i++) {
                    send("receiver-actor", new IncrementMessage());
                }
            }
        }

        @Override
        public void receive(ActorMessage actorMessage) {
        }
    }

    private static final class DualSenderActor extends Actor {


        public DualSenderActor(String id) {
            super(id);
        }

        @Override
        protected void init() {
            for (int i = 0; i < 200_000; i++) {
                send("receiver-actor-01", new IncrementMessage());
                send("receiver-actor-02", new IncrementMessage());
            }
        }

        @Override
        public void receive(ActorMessage actorMessage) {
        }
    }

    private static final class ReceiverActor extends Actor {

        public int counter = 0;

        public ReceiverActor(String id) {
            super(id);
        }

        @Override
        public void receive(ActorMessage actorMessage) {
            if (actorMessage instanceof IncrementMessage) {
                counter++;
            }
        }
    }

    private static final class PingActor extends Actor {

        public int counter;

        public PingActor(String id) {
            super(id);
        }

        @Override
        protected void init() {
            for (int i = 0; i < 100_000; i++) {
                send("pong-actor", new PingMessage());
            }
        }

        @Override
        public void receive(ActorMessage actorMessage) {
            if (actorMessage instanceof PongMessage) {
                counter++;
            }
        }
    }

    private static final class PongActor extends Actor {


        public PongActor(String id) {
            super(id);
        }

        @Override
        public void receive(ActorMessage actorMessage) {
            reply(new PongMessage());
        }
    }

    private static final class SelfCallActor extends Actor {

        public int counter = 0;

        public SelfCallActor() {
            super("SelfCallActor");
        }

        @Override
        protected void init() {
            send(new IncrementMessage());
        }

        @Override
        void receive(ActorMessage actorMessage) {
            if (counter == 100_000) {
                return;
            }
            if (actorMessage instanceof IncrementMessage) {
                counter++;
                send(actorMessage);
            }
        }
    }


    private static final class IncrementMessage implements ActorMessage {

    }

    private static final class PongMessage implements ActorMessage {

    }

    private static final class PingMessage implements ActorMessage {

    }
}
