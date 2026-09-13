package com.censera.tpc;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.IntSupplier;

final class RequestManager {
    enum RequestType { TPA, TPH }

    enum SendOutcome { SENT, SELF, TARGET_HAS_OTHER_REQUEST, ALREADY_PENDING_FOR_TARGET }

    record IncomingRequest(UUID requesterId, RequestType type) { }

    record AcceptResult(UUID requesterId, RequestType type) { }

    private final JavaPlugin plugin;
    private final IntSupplier requestExpirationSeconds;
    private final BiConsumer<UUID, UUID> onExpire;

    private final Map<UUID, Request> incoming = new HashMap<>();
    private final Map<UUID, Request> outgoing = new HashMap<>();

    RequestManager(JavaPlugin plugin, IntSupplier requestExpirationSeconds, BiConsumer<UUID, UUID> onExpire) {
        this.plugin = plugin;
        this.requestExpirationSeconds = requestExpirationSeconds;
        this.onExpire = onExpire;
    }

    Optional<IncomingRequest> incoming(UUID targetId) {
        Request request = incoming.get(targetId);
        return request == null ? Optional.empty() : Optional.of(new IncomingRequest(request.requester(), request.type()));
    }

    Optional<RequestType> outgoingType(UUID requesterId) {
        Request request = outgoing.get(requesterId);
        return request == null ? Optional.empty() : Optional.of(request.type());
    }

    SendOutcome send(UUID requesterId, UUID targetId, RequestType type) {
        if (requesterId.equals(targetId)) return SendOutcome.SELF;
        Request existingIncoming = incoming.get(targetId);
        if (existingIncoming != null && !existingIncoming.requester().equals(requesterId)) {
            return SendOutcome.TARGET_HAS_OTHER_REQUEST;
        }
        Request existingOutgoing = outgoing.get(requesterId);
        if (existingOutgoing != null) {
            if (existingOutgoing.target().equals(targetId)) return SendOutcome.ALREADY_PENDING_FOR_TARGET;
            cancelOutgoing(requesterId);
        }
        BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin,
                () -> expire(requesterId), secondsToTicks(requestExpirationSeconds.getAsInt()));
        Request request = new Request(requesterId, targetId, type, task);
        outgoing.put(requesterId, request);
        incoming.put(targetId, request);
        return SendOutcome.SENT;
    }

    Optional<AcceptResult> accept(UUID targetId) {
        return takeIncoming(targetId).map(request -> new AcceptResult(request.requester(), request.type()));
    }

    Optional<UUID> decline(UUID targetId) {
        return takeIncoming(targetId).map(Request::requester);
    }

    boolean cancelOutgoing(UUID requesterId) {
        return takeOutgoing(requesterId).isPresent();
    }

    /** Call when {@code playerId} disconnects and was the target of a request. */
    Optional<UUID> removeIncomingFor(UUID playerId) {
        return takeIncoming(playerId).map(Request::requester);
    }

    /** Call when {@code playerId} disconnects and was the requester of a request. */
    Optional<UUID> removeOutgoingFor(UUID playerId) {
        return takeOutgoing(playerId).map(Request::target);
    }

    void shutdown() {
        for (Request request : outgoing.values()) {
            request.expirationTask().cancel();
        }
        incoming.clear();
        outgoing.clear();
    }

    private Optional<Request> takeIncoming(UUID playerId) {
        Request request = incoming.remove(playerId);
        if (request == null) return Optional.empty();
        outgoing.remove(request.requester(), request);
        request.expirationTask().cancel();
        return Optional.of(request);
    }

    private Optional<Request> takeOutgoing(UUID playerId) {
        Request request = outgoing.remove(playerId);
        if (request == null) return Optional.empty();
        incoming.remove(request.target(), request);
        request.expirationTask().cancel();
        return Optional.of(request);
    }

    private void expire(UUID requesterId) {
        Request request = outgoing.remove(requesterId);
        if (request == null) return;
        incoming.remove(request.target(), request);
        onExpire.accept(request.requester(), request.target());
    }

    private static long secondsToTicks(int seconds) {
        return seconds * 20L;
    }

    private record Request(UUID requester, UUID target, RequestType type, BukkitTask expirationTask) { }
}
