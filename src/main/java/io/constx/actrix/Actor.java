package io.constx.actrix;


import java.util.Objects;

public abstract class Actor {

    private String id;


    public Actor(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    void doReceive(ActorMessage msg) {
        receive(msg);
    }

    abstract void receive(ActorMessage actorMessage);


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
}
