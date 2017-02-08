package com.lightbend.word.word.impl;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.lightbend.lagom.javadsl.persistence.AggregateEvent;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;
import com.lightbend.lagom.serialization.Jsonable;

import lombok.Value;

public interface WordEvent extends AggregateEvent<WordEvent>, Jsonable {

    AggregateEventTag<WordEvent> WORD_EVENT_TAG = AggregateEventTag.of(WordEvent.class);

    @Override
    default AggregateEventTag<WordEvent> aggregateTag() {
        return WORD_EVENT_TAG;
    }

    @SuppressWarnings("serial")
    @JsonDeserialize
    @Value
    class Translated implements WordEvent {
        String translation;
        String language;
    }

    @JsonDeserialize
    @Value
    class Init implements WordEvent {
        String uid;
        String word;
    }

}
