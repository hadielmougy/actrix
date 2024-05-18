package io.constx.actrix;

public final class Message<T> {
    public final int messageType;
    public final T payload;
    public final ActorId sender;
    public final ActorId receiver;

    public Message(int messageType, T payload, ActorId sender, ActorId receiver) {
        this.messageType = messageType;
        this.payload = payload;
        this.sender = sender;
        this.receiver = receiver;
    }
}
