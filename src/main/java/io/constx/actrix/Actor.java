package io.constx.actrix;


import java.util.Objects;
import java.util.UUID;

public abstract class Actor {

    private final String id;
    private ActorSystem actorSystem;
    private String refId;

    public Actor() {
        this(UUID.randomUUID().toString());
    }

    public Actor(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    final void doReceive(ActorContext ctx, ContextedActorMessage msg) {
        this.refId = msg.getReferenceActorId();
        receive(msg.getActorMessage());
    }

    protected void init() {}
    abstract void receive(ActorMessage actorMessage);

    void send(ActorMessage message) {
        Objects.requireNonNull(actorSystem);
        actorSystem.addToMailbox(getId(), new ContextedActorMessage(getId(), message));
    }
    void send(String dest, ActorMessage message) {
        Objects.requireNonNull(actorSystem);
        actorSystem.addToMailbox(dest, new ContextedActorMessage(getId(), message));
    }

    void reply(ActorMessage message) {
        send(refId, message);
    }

    @Override
    public String toString() {
        return "Actor{" +
                "id='" + id + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Actor actor = (Actor) o;
        return Objects.equals(id, actor.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    final void setActorSystem(ActorSystem actorSystem) {
        this.actorSystem = actorSystem;
    }
}
