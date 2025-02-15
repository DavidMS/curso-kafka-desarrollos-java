package com.imagina.kafka.broker.stream.feedback;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import com.imagina.kafka.broker.message.FeedbackMessage;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.support.serializer.JsonSerde;
import org.springframework.stereotype.Component;

//@Component
public class FeedbackTwoStream {

    private static final Set<String> GOOD_WORDS = java.util.Set.of("contento", "bueno", "amable");

    //@Autowired
    void kstreamFeedback(StreamsBuilder builder) {
        var stringSerde = Serdes.String();
        var feedbackSerde = new JsonSerde<>(FeedbackMessage.class);

        var goodFeedbackStream = builder
                .stream("t-commodity-feedback", Consumed.with(stringSerde, feedbackSerde))
                .flatMap(
                        (key, value) -> Arrays
                                .asList(value.getFeedback().replaceAll("[^a-zA-Z ]", "")
                                        .toLowerCase().split("\\s+"))
                                .stream()
                                .filter(word -> GOOD_WORDS.contains(word))
                                .distinct()
                                .map(goodWord -> KeyValue.pair(value.getLocation(),
                                        goodWord))
                                .collect(Collectors.toList()));

        goodFeedbackStream.to("t-commodity-feedback-two-good");
    }

}
