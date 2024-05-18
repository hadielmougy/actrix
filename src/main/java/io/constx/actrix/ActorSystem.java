package io.constx.actrix;

import java.util.*;
import java.util.concurrent.*;

public final class ActorSystem implements ActorContext {

    private final Map<String, Actor> registry = new HashMap<>();
    private final ExecutorService orchestrator = Executors.newSingleThreadExecutor();
    private final ExecutorService initiator = Executors.newSingleThreadExecutor();
    private final ExecutorService workers = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    private final ActorMessageMatcher matcher = new ActorMessageMatcher();

    private ActorSystem() {
        Executors.newSingleThreadExecutor().submit(() -> {
            while(true) {
                MatchingResult matching = matchingQueue.take();
                workers.submit(()-> {
                    Actor actor = registry.get(matching.actorId);
                    try {
                        actor.doReceive(this, matching.msg);
                    } finally {
                        orchestrator.submit(() -> matcher.addActor(registry.get(matching.actorId)));
                    }
                });
            }
        });
    }

    public static ActorSystem newSystem() {
        return new ActorSystem();
    }

    public void register(Actor actor)  {
        registry.put(actor.getId(), actor);
        actor.setActorSystem(this);
        orchestrator.submit(()-> matcher.addActor(actor));
        initiator.submit(actor::init);
    }

    public void addToMailbox(String toActorId, ContextedActorMessage msg) {
        orchestrator.submit(
                () -> matcher.addMessage(toActorId, msg));
    }

    private final BlockingQueue<MatchingResult> matchingQueue = new LinkedBlockingQueue<>();

    private class ActorMessageMatcher {
        private final Map<String, Actor> actorMap = new HashMap<>();
        private final Map<String, Queue<ContextedActorMessage>> actorMailMap = new HashMap<>();

        public void addActor(Actor actor) {
            actorMap.put(actor.getId(), actor);
            if (actorMailMap.get(actor.getId()) == null) {
                return;
            }
            tryMatching(actor.getId());
        }

        public void addMessage(String actorId, ContextedActorMessage msg) {
            if (registry.get(actorId) == null) {
                System.err.printf("Actor with id %s is not found%n", actorId);
                return;
            }
            actorMailMap.putIfAbsent(actorId, new LinkedList<>());
            actorMailMap.get(actorId).offer(msg);
            tryMatching(actorId);
        }

        private void tryMatching(String actorId) {
            ContextedActorMessage msg = match(actorId);
            if (msg != null) {
                matchingQueue.add(new MatchingResult(actorId, msg));
                actorMap.remove(actorId);
            }
        }

        public ContextedActorMessage match(String actorId) {
            Actor actor = actorMap.get(actorId);
            if (actor == null) {
                return null;
            }
            return actorMailMap.get(actorId).poll();
        }
    }

    private static class MatchingResult {
        public final String actorId;
        public final ContextedActorMessage msg;

        public MatchingResult(String actorId, ContextedActorMessage msg) {
            this.actorId = actorId;
            this.msg = msg;
        }
    }
}
