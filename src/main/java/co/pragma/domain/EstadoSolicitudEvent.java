package co.pragma.domain;

import java.math.BigDecimal;

public record EstadoSolicitudEvent(
        String codigoSolicitud,
        String emailCliente,
        String nombreCliente,
        BigDecimal monto,
        String estado,
        BigDecimal tasaInteres,
        Integer plazoEnMeses
) {}
