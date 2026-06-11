package es.um.tds.gestiongastos.modelo;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

/**
 * Enumeración que define los tipos de alerta disponibles.
 * Cada tipo determina el periodo de tiempo sobre el que calcula el gasto.
 */
public enum TipoAlerta {
	SEMANAL("Semanal", 7){
		@Override
		public LocalDate calcularInicioPeriodo(LocalDate fecha) {
			return fecha.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
		}
	},
	MENSUAL("Mensual", 30){
		@Override
		public LocalDate calcularInicioPeriodo(LocalDate fecha) {
			return fecha.withDayOfMonth(1);
		}
	};
	
	private final String description;
	private final int days;
	
	/**
	 * Constructor del enum
	 * @param description Descripción legible del tipo de alerta
	 * @param days Número aprox. de días del periodo.
	 */
	TipoAlerta(String _description, int _days){
		this.description = _description;
		this.days = _days;
	}
	
	/**
	 * Calcula la fecha de inicio del periodo de esta alerta.
	 * Cada valor del enum implementa su propia estratégia de cálculo.
	 * @param fechaActual Fecha desde la que calcular el inicio del periodo
	 * @return Fecha de inicio del periodo correspondiente
	 */
	public abstract LocalDate calcularInicioPeriodo(LocalDate fechaActual);
	
	/**
	 * Obtiene la descripción del tipo de alerta
	 * @return Descripción del tipo
	 */
	public String getDescripcion() {
		return description;
	}
	
	/**
	 * Obtiene el número aprox. de dias
	 * @return Número de días
	 */
	public int getDias() {
		return days;
	}
	
	@Override
	public String toString() {
		return description;
	}
}
