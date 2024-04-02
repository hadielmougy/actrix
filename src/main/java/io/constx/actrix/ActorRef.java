package io.constx.actrix;

import io.constx.actrix.impl.ContextedMessage;

public final class ActorRef {
    private final String actorId;
    private final ActorSystem system;

    public ActorRef(String actorId, ActorSystem system) {
        this.actorId = actorId;
        this.system = system;
    }

    public void send(ActorMessage actorMessage) {
        system.addToMailbox(actorId, actorMessage);
    }
}
