package com.imagina.kafka.broker.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DashboardMessage {

    private String symbol;
    private Double price;
    private int quantity;
}
