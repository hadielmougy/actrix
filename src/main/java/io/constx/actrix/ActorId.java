package io.constx.actrix;

import java.util.Objects;

public record ActorId(String actorId) {
    public static ActorId of(String actorId) {
        return new ActorId(Objects.requireNonNull(actorId));
    }
}
