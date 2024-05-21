package io.constx.actrix.examples.scheduler;

import io.constx.actrix.Actor;
import io.constx.actrix.ActorMessage;
import io.constx.actrix.ActorRef;
import io.constx.actrix.ActorSystem;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.awaitility.Awaitility.waitAtMost;

public class SchedulerTest {

    private static final String SCHEDULER_NAME = "non-default-executor";

    @Test
    public void run4ParallelTasksInARegisteredScheduler() {
        ActorSystem system = ActorSystem.newSystem();
        ExecutorService exe = Executors.newFixedThreadPool(4);
        system.registerScheduler(SCHEDULER_NAME, exe);
        ActorRef workerActor1 = system.register(new WorkerActor());
        ActorRef workerActor2 = system.register(new WorkerActor());
        ActorRef workerActor3 = system.register(new WorkerActor());
        ActorRef workerActor4 = system.register(new WorkerActor());
        OrchestratorActor orchestratorActor = new OrchestratorActor(workerActor1, workerActor2, workerActor3, workerActor4);
        ActorRef orchestratorActorRef = system.register(orchestratorActor);
        orchestratorActorRef.send(new Signal());

        waitAtMost(Duration.ofSeconds(1)).until(() -> orchestratorActor.counter == 4);

    }

    private static final class OrchestratorActor extends Actor {

        private final ActorRef[] workerActors;
        public int counter;

        public OrchestratorActor(ActorRef... workerActors) {
            this.workerActors = workerActors;
        }

        @Override
        public String getScheduler() {
            // runs in the default scheduler
            return null;
        }

        @Override
        protected void receive(ActorMessage actorMessage) {
            switch (actorMessage) {
                case Signal signal -> dispatch();
                case Reply reply -> aggregate();
                default -> throw new IllegalStateException();
            };
        }

        private void aggregate() {
            counter++;
        }

        private void dispatch() {
            AtomicInteger counter = new AtomicInteger(1);
            Stream.of(workerActors)
                    .forEach(worker -> worker.send(new WorkAssignment(counter.getAndIncrement(), getRef())));
        }
    }

    public static final class WorkerActor extends Actor {

        @Override
        public String getScheduler() {
            // runs in externally registered scheduler
            return SCHEDULER_NAME;
        }

        @Override
        protected void receive(ActorMessage actorMessage) {
            if (Objects.requireNonNull(actorMessage) instanceof WorkAssignment assignment) {
                doExecuteAndReply(assignment);
            } else {
                throw new IllegalStateException("Unexpected value: " + actorMessage);
            }
        }

        private void doExecuteAndReply(WorkAssignment assignment) {
            System.out.println("Execute assignment number : "+ assignment.workUnit);
            try {
                Thread.sleep(900);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            send(assignment.responseTo, new Reply());
        }
    }

    record WorkAssignment(int workUnit, ActorRef responseTo) implements ActorMessage {
    }

    record Signal() implements ActorMessage{}
    record Reply() implements ActorMessage{}
}
