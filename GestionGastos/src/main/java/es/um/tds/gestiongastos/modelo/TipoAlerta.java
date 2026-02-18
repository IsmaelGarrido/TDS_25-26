package es.um.tds.gestiongastos.modelo;

/**
 * Enumeración que define los tipos de alerta disponibles.
 * Cada tipo determina el periodo de tiempo sobre el que calcula el gasto.
 */
public enum TipoAlerta {
	DIARIO("Diario", 1),
	SEMANAL("Semanal", 7),
	MENSUAL("Mensual", 30);
	
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
