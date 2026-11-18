package com.atenea.biblion.util;

import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Utilería para el formateo consistente de moneda, fechas y números.
 */
public class FormatUtils {

    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(new Locale("es", "MX"));
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static final DateTimeFormatter SHORT_DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public static String formatoMoneda(double cantidad) {
        return CURRENCY_FORMAT.format(cantidad);
    }

    public static String formatoFechaHora(LocalDateTime fechaHora) {
        if (fechaHora == null) {
            return "-";
        }
        return fechaHora.format(DATE_TIME_FORMAT);
    }

    public static String formatoFechaCorta(LocalDateTime fechaHora) {
        if (fechaHora == null) {
            return "-";
        }
        return fechaHora.format(SHORT_DATE_FORMAT);
    }
}

