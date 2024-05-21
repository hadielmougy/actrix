package io.constx.actrix;

import org.junit.jupiter.api.RepeatedTest;
import java.time.Duration;

import static org.awaitility.Awaitility.*;

public class ActorsWithLoadTest {

    @RepeatedTest(2)
    public void testSendReceive() {
        ActorSystem system = ActorSystem.newSystem();
        ReceiverActor receiverActor = new ReceiverActor("receiver-actor");
        ActorRef receiverRef = system.register(receiverActor);
        ActorRef senderRef = system.register(new SenderActor(receiverRef));
        senderRef.send(new IncrementMessage());
        waitAtMost(Duration.ofSeconds(1)).until(() -> receiverActor.counter == 1);
    }

    @RepeatedTest(2)
    public void testSendReceive100_000() {
        ActorSystem system = ActorSystem.newSystem();
        ReceiverActor receiverActor = new ReceiverActor("receiver-actor");
        ActorRef receiverRef = system.register(receiverActor);
        ActorRef senderRef = system.register(new SenderActor(receiverRef));
        for (int i = 0; i < 100_000; i++) {
            senderRef.send(new IncrementMessage());
        }
        waitAtMost(Duration.ofSeconds(5)).until(() -> receiverActor.counter == 100_000);
    }

    @RepeatedTest(2)
    public void testSendReceive200_000() {
        ActorSystem system = ActorSystem.newSystem();
        ReceiverActor receiverActor01 = new ReceiverActor("receiver-actor-01");
        ReceiverActor receiverActor02 = new ReceiverActor("receiver-actor-02");
        var receiverActor01Ref = system.register(receiverActor01);
        var receiverActor02Ref = system.register(receiverActor02);
        var dualSenderActorRef = system.register(new DualSenderActor(receiverActor01Ref, receiverActor02Ref, "sender-actor"));
        for (int i = 0; i < 200_000; i++) {
            dualSenderActorRef.send(new IncrementMessage());
        }
        waitAtMost(Duration.ofSeconds(5)).until(() -> receiverActor01.counter == 200_000);
        waitAtMost(Duration.ofSeconds(5)).until(() -> receiverActor02.counter == 200_000);
    }

    @RepeatedTest(2)
    public void testPingPong() {
        ActorSystem system    = ActorSystem.newSystem();
        PongActor pongActor   = new PongActor("pong-actor");
        ActorRef pongActorRef = system.register(pongActor);
        PingActor pingActor   = new PingActor(pongActorRef, "ping-actor");
        ActorRef pingActorRef = system.register(pingActor);

        for (int i = 0; i < 100_000; i++) {
            pingActorRef.send(new PingMessage());
        }

        waitAtMost(Duration.ofSeconds(5)).until(() -> pingActor.counter == 100_000);
    }

    @RepeatedTest(2)
    public void testSelfCall() {
        ActorSystem system = ActorSystem.newSystem();
        var selfCallActor = new SelfCallActor();
        var selfCallActorRef = system.register(selfCallActor);
        selfCallActorRef.send(new IncrementMessage());
        waitAtMost(Duration.ofSeconds(5)).until(() -> selfCallActor.counter == 100_000);
    }

    private static final class SenderActor extends Actor {


        private final ActorRef receiverRef;

        public SenderActor(ActorRef receiverRef) {
            super();
            this.receiverRef = receiverRef;
        }

        @Override
        public void receive(ActorMessage actorMessage) {
            receiverRef.send(actorMessage);
        }
    }

    private static final class DualSenderActor extends Actor {


        private final ActorRef receiverActor01Ref;
        private final ActorRef receiverActor02Ref;

        public DualSenderActor(ActorRef receiverActor01Ref, ActorRef receiverActor02Ref, String id) {
            super(id);
            this.receiverActor01Ref = receiverActor01Ref;
            this.receiverActor02Ref = receiverActor02Ref;
        }

        @Override
        public void receive(ActorMessage actorMessage) {
            receiverActor01Ref.send(actorMessage);
            receiverActor02Ref.send(actorMessage);
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

        private final ActorRef pongActorRef;
        public int counter;

        public PingActor(ActorRef pongActorRef, String id) {
            super(id);
            this.pongActorRef = pongActorRef;
        }

        @Override
        public void receive(ActorMessage actorMessage) {

            if (actorMessage instanceof PingMessage) {
                ((PingMessage) actorMessage).responseTo = getRef();
                pongActorRef.send(actorMessage);
            }

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
            send(((PingMessage)actorMessage).responseTo, new PongMessage());
        }
    }

    private static final class SelfCallActor extends Actor {

        public int counter = 0;

        public SelfCallActor() {
            super("SelfCallActor");
        }

        @Override
        public void receive(ActorMessage actorMessage) {
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
        ActorRef responseTo;

    }
}
