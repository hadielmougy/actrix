# Actrix

Fault tolerance library for java

## Usage

```xml
<dependency>
    <groupId>io.constx</groupId>
    <artifactId>actrix</artifactId>
    <version>0.1.1</version>
</dependency>
```

## Usage

```java

ReceiverActor receiverActor = new ReceiverActor("receiver-actor");
ActorRef receiverRef = system.register(receiverActor);
ActorRef senderRef = system.register(new SenderActor(receiverRef));
senderRef.send(new IncrementMessage());

.......


class ReceiverActor extends Actor {

    public ReceiverActor(String id) {
        super(id);
    }

    @Override
    public void receive(ActorMessage actorMessage) {
        doSomething(actorMessage);
}
   
```
