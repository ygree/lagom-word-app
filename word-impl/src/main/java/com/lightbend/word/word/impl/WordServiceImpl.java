package com.lightbend.word.word.impl;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.japi.Pair;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import com.google.inject.Inject;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.persistence.*;
import com.lightbend.word.word.api.WordService;

import java.util.UUID;

public class WordServiceImpl implements WordService {
    private final PersistentEntityRegistry persistentEntityRegistry;

    @Inject
    public WordServiceImpl(PersistentEntityRegistry persistentEntityRegistry, ActorSystem system) {
        this.persistentEntityRegistry = persistentEntityRegistry;
        persistentEntityRegistry.register(WordEntity.class);

        Source<Pair<WordEvent.ProcessStarted, Offset>, NotUsed> source = persistentEntityRegistry.eventStream(WordEvent.WORD_EVENT_TAG, Offset.NONE)
                .filter(e -> e.first() instanceof WordEvent.ProcessStarted).map(p -> Pair.create((WordEvent.ProcessStarted) p.first(), p.second()));

        ActorMaterializer mat = ActorMaterializer.create(system);

        source.mapAsync(1, pair -> {

            PersistentEntityRef<WordCommand> ref = persistentEntityRegistry.refFor(WordEntity.class, pair.first().getUid());

            return ref.ask(new WordCommand.AddTranslation("HashCode", "T:" + pair.first().getWord().hashCode()));

        })
        .runWith(Sink.ignore(), mat);

    }

    @Override
    public ServiceCall<String, String> process() {
        return word -> {
            String id = UUID.randomUUID().toString();

            PersistentEntityRef<WordCommand> ref = persistentEntityRegistry.refFor(WordEntity.class, id);

            return ref.ask(new WordCommand.Process(id, word)).thenApply(done -> id);
        };
    }




}
