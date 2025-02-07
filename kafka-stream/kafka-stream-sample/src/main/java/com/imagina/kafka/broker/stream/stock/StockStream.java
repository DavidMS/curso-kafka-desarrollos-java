package com.imagina.kafka.broker.stream.stock;

import com.imagina.kafka.broker.message.DashboardMessage;
import com.imagina.kafka.broker.message.StockMessage;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.support.serializer.JsonSerde;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class StockStream {

    @Autowired
    void stockStreamProcessor(StreamsBuilder builder) {

        var stockSerde = new JsonSerde<>(StockMessage.class);
        var dashboardSerde = new JsonSerde<>(DashboardMessage.class);
        var estadisticaSerde = new JsonSerde<>(EstadisticaBolsa.class);

        // Transformacion 1: Filtrado de operaciones precio > 180
        builder.stream("operaciones-bolsa", Consumed.with(Serdes.String(), stockSerde))
                .filter((key, stock) -> stock.getPrice() > 180.0)
                .to("notificaciones-bolsa", Produced.with(Serdes.String(), stockSerde));

        // Transformacion 2: Enviar datos transformados al dashboard
        builder.stream("operaciones-bolsa", Consumed.with(Serdes.String(), stockSerde))
                .mapValues(value -> DashboardMessage
                        .builder()
                        .symbol(value.getSymbol())
                        .price(value.getPrice())
                        .quantity(value.getQuantity())
                        .build())
                .to("dashboard-bolsa", Produced.with(Serdes.String(),dashboardSerde));

        // Transformacion 3: Estadisticas en ventanas temporales
        builder.stream("operaciones-bolsa", Consumed.with(Serdes.String(), stockSerde))
                .groupByKey()
                .windowedBy(TimeWindows.ofSizeWithNoGrace(Duration.ofMinutes(15)))
                .aggregate(
                        () -> new EstadisticaBolsa(0, 0, Double.MIN_VALUE, Double.MAX_VALUE),
                        (key, newValue, estadistica) -> estadistica.actualizar(newValue),
                        Materialized.with(Serdes.String(), estadisticaSerde)
                )
                .toStream()
                .to("estadisticas-bolsa", Produced.with(WindowedSerdes.timeWindowedSerdeFrom(String.class),estadisticaSerde));

    }
}
