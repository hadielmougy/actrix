package io.constx.actrix;

import java.util.*;
import java.util.concurrent.*;

public final class ActorSystem {

    private final Map<String, Actor> registry = new HashMap<>();
    private final Map<String, MailBox> mailboxRegistry = new HashMap<>();

    private final ExecutorService orchestrator = Executors.newSingleThreadExecutor();
    private final ExecutorService workers = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    private final ActorMessageMatcher matcher = new ActorMessageMatcher();
    private final ExecutorService dispatcher = Executors.newSingleThreadExecutor();

    private ActorSystem() {

        dispatcher.submit(() -> {
            while(true) {
                MatchingResult matching = matchingQueue.take();
                workers.submit(()-> {
                    registry.get(matching.actorId).doReceive(matching.msg);
                    orchestrator.submit(() -> matcher.addActor(registry.get(matching.actorId)));
                });
            }
        });
    }

    public static ActorSystem newSystem() {
        return new ActorSystem();
    }

    public ActorRef newActor(Actor actor)  {
        registry.put(actor.getId(), actor);
        orchestrator.submit(()-> matcher.addActor(actor));
        return new ActorRef(actor.getId(), this);
    }

    public void addToMailbox(String actorId, ActorMessage actorMessage) {
        orchestrator.submit(() -> matcher.addMessage(actorId, actorMessage));
    }

    private final BlockingQueue<MatchingResult> matchingQueue = new LinkedBlockingQueue<>();

    private class ActorMessageMatcher {
        private final Map<String, Actor> actorMap = new HashMap<>();
        private final Map<String, Queue<ActorMessage>> actorMailMap = new HashMap<>();

        public void addActor(Actor actor) {
            actorMap.put(actor.getId(), actor);
            if (actorMailMap.get(actor.getId()) == null) {
                return;
            }
            propagateMatching(actor.getId());
        }



        public void addMessage(String actorId, ActorMessage actorMessage) {
            actorMailMap.putIfAbsent(actorId, new LinkedList<>());
            actorMailMap.get(actorId).offer(actorMessage);
            propagateMatching(actorId);
        }

        private void propagateMatching(String actorId) {
            ActorMessage msg = match(actorId);
            if (msg != null) {
                matchingQueue.add(new MatchingResult(actorId, msg));
                actorMap.remove(actorId);
            }
        }

        public ActorMessage match(String actorId) {
            Actor actor = actorMap.get(actorId);
            if (actor == null) {
                return null;
            }
            return actorMailMap.get(actorId).poll();
        }
    }

    private static class MatchingResult {
        public final String actorId;
        public final ActorMessage msg;

        public MatchingResult(String actorId, ActorMessage msg) {
            this.actorId = actorId;
            this.msg = msg;
        }
    }
}
