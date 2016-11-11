// Copyright © 2016 Esko Luontola
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.luontola.cqrshotel.framework;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class EventStore {

    public static final int NEW_STREAM = 0;

    private final ConcurrentMap<UUID, List<Event>> streamsById = new ConcurrentHashMap<>();

    public List<Event> getEventsForStream(UUID streamId) {
        List<Event> events = streamsById.get(streamId);
        if (events == null) {
            throw new EventStreamNotFoundException(streamId);
        }
        return new ArrayList<>(events);
    }

    public void saveEvents(UUID streamId, List<Event> newEvents, int expectedVersion) {
        List<Event> events = streamsById.computeIfAbsent(streamId, uuid -> new ArrayList<>());
        synchronized (events) {
            int actualVersion = events.size();
            if (expectedVersion != actualVersion) {
                throw new OptimisticLockingException("expected version " + expectedVersion + " but was " + actualVersion + " for stream " + streamId);
            }
            events.addAll(newEvents);
        }
    }
}
