package io.constx.actrix;

import java.util.*;
import java.util.concurrent.*;

public final class ActorSystem implements ActorContext {

    private final Map<String, Actor> registry = new HashMap<>();
    private final ExecutorService orchestrator = Executors.newSingleThreadExecutor();
    private final ExecutorService workers = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    private final ExecutorService systemThread = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    private final BlockingQueue<MatchingResult> matchingQueue = new LinkedBlockingQueue<>();
    private final ActorMessageMatcher matcher = new ActorMessageMatcher(matchingQueue);
    private final Map<String, ExecutorService> schedulers = new HashMap<>();


    private ActorSystem() {
        systemThread.submit(() -> {
            while(true) {
                MatchingResult matching = matchingQueue.take();
                Actor actor = registry.get(matching.actorId());
                ExecutorService scheduler = schedulers.get(actor.getSchedulerName());
                Objects.requireNonNullElse(scheduler, workers).submit(() -> deliver(actor, matching));
            }
        });
    }

    private void deliver(Actor actor, MatchingResult matching) {
        try {
            actor.doReceive(matching.message());
        } finally {
            orchestrator.submit(() -> matcher.addActor(registry.get(matching.actorId())));
        }
    }

    public static ActorSystem newSystem() {
        return new ActorSystem();
    }

    public ActorRef register(Actor actor)  {
        registry.put(actor.getId(), actor);
        actor.setActorSystem(this);
        orchestrator.submit(() -> matcher.addActor(actor));
        return new ActorRef(actor.getId(), this);
    }

    public void registerScheduler(String name, ExecutorService executorService) {
        schedulers.put(name, executorService);
    }

    void addToMailbox(String actorId, ActorMessage message) {
        if (registry.get(actorId) == null) {
            System.err.printf("Actor with id %s is not found", actorId);
            return;
        }
        orchestrator.submit(() -> matcher.addMessage(actorId, message));
    }

    public void shutdown() {
        orchestrator.shutdown();
        workers.shutdown();
        systemThread.shutdown();
        schedulers.values().forEach(ExecutorService::shutdown);
    }

    public void shutdownNow() {
        orchestrator.shutdownNow();
        workers.shutdownNow();
        systemThread.shutdownNow();
        schedulers.values().forEach(ExecutorService::shutdownNow);
    }
}
