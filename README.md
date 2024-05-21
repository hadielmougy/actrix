# actrix

´´´java
 ReceiverActor receiverActor = new ReceiverActor("receiver-actor");
 ActorRef receiverRef = system.register(receiverActor);
 ActorRef senderRef = system.register(new SenderActor(receiverRef));
 senderRef.send(new IncrementMessage());
 ´´´
