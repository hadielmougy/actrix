package io.constx.actrix;

public final class ContextedActorMessage {
    private final String referenceActorId;
    private final ActorMessage actorMessage;

    public ContextedActorMessage(String referenceActorId, ActorMessage actorMessage) {
        this.referenceActorId = referenceActorId;
        this.actorMessage = actorMessage;
    }

    public String getReferenceActorId() {
        return referenceActorId;
    }

    public ActorMessage getActorMessage() {
        return actorMessage;
    }
}
