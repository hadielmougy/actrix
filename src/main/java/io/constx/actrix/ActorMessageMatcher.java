package io.constx.actrix;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;

final class ActorMessageMatcher {
    private final Map<String, Actor> actorMap = new HashMap<>();
    private final Map<String, Queue<ContextedActorMessage>> actorMailMap = new HashMap<>();
    private final BlockingQueue<MatchingResult> matchingQueue;

    ActorMessageMatcher(BlockingQueue<MatchingResult> matchingQueue) {
        this.matchingQueue = matchingQueue;
    }

    public void addActor(Actor actor) {
        actorMap.put(actor.getId(), actor);
        if (actorMailMap.get(actor.getId()) == null) {
            return;
        }
        tryMatching(actor.getId());
    }

    public void addMessage(String actorId, ContextedActorMessage msg) {
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
