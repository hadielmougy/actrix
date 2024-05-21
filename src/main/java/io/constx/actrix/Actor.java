package io.constx.actrix;


import java.util.Objects;
import java.util.UUID;

public abstract class Actor {

    protected final String id;
    private ActorSystem actorSystem;
    public Actor() {
        this(UUID.randomUUID().toString());
    }

    public Actor(String id) {
        this.id = id;
    }

    public String getScheduler() {
        return null;
    }

    public String getId() {
        return id;
    }

    final void doReceive(ActorMessage message) {
        receive(message);
    }

    protected abstract void receive(ActorMessage actorMessage);

    protected final void send(ActorMessage message) {
        Objects.requireNonNull(actorSystem);
        actorSystem.addToMailbox(getId(), message);
    }

    protected final void send(ActorRef to, ActorMessage message) {
        to.send(message);
    }

    protected final ActorRef getRef() {
        return new ActorRef(getId(), actorSystem);
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
