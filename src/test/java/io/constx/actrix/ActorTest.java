package io.constx.actrix;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.RepeatedTest;

import java.time.Duration;

public class ActorTest {

    @RepeatedTest(2)
    public void testSendReceive() {
        ActorSystem system = ActorSystem.newSystem();
        ReceiverActor receiverActor = new ReceiverActor("receiver-actor");
        ActorRef receiverActorRef = system.newActor(receiverActor);
        ActorRef senderActor = system.newActor(new SenderActor("sender-actor", receiverActorRef));
        senderActor.send(new IncrementMessage());
        Awaitility.waitAtMost(Duration.ofSeconds(1)).until(() -> receiverActor.counter == 1);
    }

    @RepeatedTest(2)
    public void testSendReceive100_000() {
        ActorSystem system = ActorSystem.newSystem();
        ReceiverActor receiverActor = new ReceiverActor("receiver-actor");
        ActorRef receiverActorRef = system.newActor(receiverActor);
        ActorRef senderActor = system.newActor(new SenderActor("sender-actor", receiverActorRef));
        for (int i = 0; i < 100_000; i++) {
            senderActor.send(new IncrementMessage());
        }
        Awaitility.waitAtMost(Duration.ofSeconds(5)).until(() -> receiverActor.counter == 100_000);
    }

    @RepeatedTest(2)
    public void testSendReceive200_000() {
        ActorSystem system = ActorSystem.newSystem();
        ReceiverActor receiverActor01 = new ReceiverActor("receiver-actor-01");
        ReceiverActor receiverActor02 = new ReceiverActor("receiver-actor-02");
        ActorRef receiverActorRef01 = system.newActor(receiverActor01);
        ActorRef receiverActorRef02 = system.newActor(receiverActor02);
        ActorRef senderActor = system.newActor(new DualSenderActor("sender-actor", receiverActorRef01,receiverActorRef02));
        for (int i = 0; i < 200_000; i++) {
            senderActor.send(new IncrementMessage());
        }
        Awaitility.waitAtMost(Duration.ofSeconds(5)).until(() -> receiverActor01.counter == 200_000);
        Awaitility.waitAtMost(Duration.ofSeconds(5)).until(() -> receiverActor02.counter == 200_000);
    }

    @RepeatedTest(2)
    public void testPingPong() {
        ActorSystem system = ActorSystem.newSystem();
        PingActor pingActor = new PingActor("ping-actor");
        PongActor pongActor = new PongActor("pong-actor");
        ActorRef pingRef = system.newActor(pingActor);
        ActorRef pongRef = system.newActor(pongActor);
        pingActor.setPongActor(pongRef);
        pongActor.setPingActor(pingRef);
        for (int i = 0; i < 100_000; i++) {
            pingRef.send(new PingMessage());
        }
        Awaitility.waitAtMost(Duration.ofSeconds(5)).until(() -> pingActor.counter == 100_000);
    }

    private static final class SenderActor extends Actor {

        private final ActorRef receiverActor;

        public SenderActor(String id, ActorRef receiverActor) {
            super(id);
            this.receiverActor = receiverActor;
        }

        @Override
        public void receive(ActorMessage actorMessage) {
            receiverActor.send(actorMessage);
        }
    }

    private static final class DualSenderActor extends Actor {

        private final ActorRef receiverActor01;
        private final ActorRef receiverActor02;

        public DualSenderActor(String id, ActorRef receiverActor01, ActorRef receiverActor02) {
            super(id);
            this.receiverActor01 = receiverActor01;
            this.receiverActor02 = receiverActor02;
        }

        @Override
        public void receive(ActorMessage actorMessage) {
            receiverActor01.send(actorMessage);
            receiverActor02.send(actorMessage);
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

        private ActorRef pongActor;
        public int counter;

        public PingActor(String id) {
            super(id);
        }

        public void setPongActor(ActorRef pongActor) {
            this.pongActor = pongActor;
        }

        @Override
        public void receive(ActorMessage actorMessage) {
            if (actorMessage instanceof PingMessage) {
                pongActor.send(actorMessage);
            }

            if (actorMessage instanceof PongMessage) {
                counter++;
            }
        }
    }

    private static final class PongActor extends Actor {

        private ActorRef pingActor;

        public PongActor(String id) {
            super(id);
        }

        public void setPingActor(ActorRef pingActor) {
            this.pingActor = pingActor;
        }

        @Override
        public void receive(ActorMessage actorMessage) {
            pingActor.send(new PongMessage());
        }
    }


    private static final class IncrementMessage implements ActorMessage {

    }

    private static final class PongMessage implements ActorMessage {

    }

    private static final class PingMessage implements ActorMessage {

    }
}
