package io.constx.actrix;


public class ActorRef {

    private final String actorId;
    private final ActorSystem actorSystem;

    public ActorRef(String id, ActorSystem actorSystem) {
        this.actorId = id;
        this.actorSystem = actorSystem;
    }

    public final void send(ActorMessage message) {
        actorSystem.addToMailbox(actorId, message);
    }
}
