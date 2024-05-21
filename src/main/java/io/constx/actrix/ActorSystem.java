package io.constx.actrix;

import java.util.*;
import java.util.concurrent.*;

public final class ActorSystem implements ActorContext {

    private final Map<String, Actor> registry = new HashMap<>();
    private final ExecutorService orchestrator = Executors.newSingleThreadExecutor();
    private final ExecutorService initiator = Executors.newSingleThreadExecutor();
    private final ExecutorService workers = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    private final BlockingQueue<MatchingResult> matchingQueue = new LinkedBlockingQueue<>();
    private final ActorMessageMatcher matcher = new ActorMessageMatcher(matchingQueue);

    private final ThreadLocal<String> CURR_ID = new ThreadLocal<>();

    private ActorSystem() {
        Executors.newSingleThreadExecutor().submit(() -> {
            while(true) {
                MatchingResult matching = matchingQueue.take();
                workers.submit(()-> {
                    Actor actor = registry.get(matching.actorId());
                    try {
                        actor.doReceive(matching.msg());
                    } finally {
                        orchestrator.submit(() -> matcher.addActor(registry.get(matching.actorId())));
                    }
                });
            }
        });
    }

    public static ActorSystem newSystem() {
        return new ActorSystem();
    }

    public ActorRef ofActor(Actor actor)  {
        registry.put(actor.getId(), actor);
        actor.setActorSystem(this);
        orchestrator.submit(() -> matcher.addActor(actor));
        return new ActorRef(actor.getId(), this);
    }

    public void addToMailbox(String actorId, Message msg) {
        if (registry.get(actorId) == null) {
            System.err.printf("Actor with id %s is not found", actorId);
            return;
        }
        orchestrator.submit(() -> matcher.addMessage(actorId, msg));
    }
}
