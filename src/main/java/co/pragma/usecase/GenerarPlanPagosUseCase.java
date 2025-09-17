package co.pragma.usecase;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class GenerarPlanPagosUseCase {
    public List<String[]> execute(BigDecimal monto, BigDecimal tasaInteresAnual, int plazoEnMeses) {
        BigDecimal tasaMensual = tasaInteresAnual.divide(BigDecimal.valueOf(100 * 12L), 20, RoundingMode.HALF_UP);
        BigDecimal cuota = calcularCuota(monto, tasaInteresAnual, plazoEnMeses);

        List<String[]> plan = new ArrayList<>();
        BigDecimal saldo = monto;

        for (int mes = 1; mes <= plazoEnMeses; mes++) {
            BigDecimal interes = saldo.multiply(tasaMensual).setScale(2, RoundingMode.HALF_UP);
            BigDecimal capital = cuota.subtract(interes).setScale(2, RoundingMode.HALF_UP);
            saldo = saldo.subtract(capital).setScale(2, RoundingMode.HALF_UP);

            plan.add(new String[]{String.valueOf(mes), cuota.toString(), capital.toString(), interes.toString(), saldo.toString()});
        }
        return plan;
    }

    /**
     * Ref: HU7
     * Calcula la cuota mensual de un préstamo.
     * Fórmula de Cuota: P * (i * (1 + i)^n) / ((1 + i)^n - 1)
     * Donde:
     * - P: Monto del préstamo.
     * - i: Tasa de interés mensual.
     * - n: Plazo en meses.     *
     * @param tasaInteresAnual La tasa de interés anual en porcentaje.
     * @return La cuota mensual calculada con dos decimales.
     */
    private BigDecimal calcularCuota(BigDecimal monto, BigDecimal tasaInteresAnual, int plazoEnMeses) {
        MathContext mc = new MathContext(20, RoundingMode.HALF_UP);

        BigDecimal tasaMensual = tasaInteresAnual.divide(BigDecimal.valueOf(100), mc).divide(BigDecimal.valueOf(12), mc);

        if (tasaMensual.compareTo(BigDecimal.ZERO) == 0)
            return monto.divide(BigDecimal.valueOf(plazoEnMeses), 2, RoundingMode.HALF_UP);

        BigDecimal unoMasTasa = BigDecimal.ONE.add(tasaMensual, mc);
        BigDecimal potencia = unoMasTasa.pow(-plazoEnMeses, mc);

        BigDecimal numerador = monto.multiply(tasaMensual, mc);
        BigDecimal denominador = BigDecimal.ONE.subtract(potencia, mc);

        return numerador.divide(denominador, 2, RoundingMode.HALF_UP);
    }
}
