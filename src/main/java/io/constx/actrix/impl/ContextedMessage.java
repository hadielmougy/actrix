package io.constx.actrix.impl;

import io.constx.actrix.ActorId;
import io.constx.actrix.ActorMessage;

public record ContextedMessage(ActorId actorId, ActorMessage msg) {
}
