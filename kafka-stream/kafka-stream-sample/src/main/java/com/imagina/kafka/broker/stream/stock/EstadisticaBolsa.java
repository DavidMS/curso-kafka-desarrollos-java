package com.imagina.kafka.broker.stream.stock;

import com.imagina.kafka.broker.message.StockMessage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EstadisticaBolsa {
    private long transacciones;
    private double promedio;
    private double min;
    private double max;

    public EstadisticaBolsa actualizar(StockMessage accion) {
        this.transacciones++;
        this.promedio = (this.promedio * (this.transacciones - 1) + accion.getPrice()) / this.transacciones;
        this.min = Math.min(this.min, accion.getPrice());
        this.max = Math.max(this.max, accion.getPrice());
        return this;
    }
}
