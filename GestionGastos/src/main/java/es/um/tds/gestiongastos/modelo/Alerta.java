package es.um.tds.gestiongastos.modelo;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Objects;
import java.util.Optional;


/**
 * Representa una alerta de gasto configurada por el usuario.
 * Las alertas pueden ser diarias, semanales o mensuales, y opcionalmente
 * estar vinculadas a una categoría específica.
 */
public class Alerta {

	private static int counterID = 0;
	
	private int id;
	private double maxExpense;
	private TipoAlerta typeAlert;
	private Categoria category;
	private boolean active;

	/**
	 * Constructor completo de Alerta
	 * @param _maxGasto Limite de gasto (positivo).
	 * @param _typeAlerta Tipo de Alerta (no null)
	 * @param _category Categoria asociada
	 * @throws IllegalArgumentException si _maxGasto <=0 o typeAlerta es null
	**/
	public Alerta(double _maxExpense, TipoAlerta _typeAlert, Categoria _category) {
		validarMaxGasto(_maxExpense);
		validarTypeAlerta(_typeAlert);
		
		this.id = ++counterID;
		this.maxExpense = _maxExpense;
		this.typeAlert = _typeAlert;
		this.category = _category;
		this.active = true;
	}
	
	/**
	 * Constructor sin categoria
	 * @param _maxGasto Limite de gasto (positivo).
	 * @param _typeAlerta Tipo de Alerta (no null).
	 */
	public Alerta(double _maxExpense, TipoAlerta _typeAlert) {
		this(_maxExpense, _typeAlert, null);
	}
	
	protected Alerta() {
	}
	
	private void validarMaxGasto(double _maxExpense) {
		if (_maxExpense <= 0) {
			throw new IllegalArgumentException("El tope de gasto debe ser mayor que 0");
		}
	}
	
	private void validarTypeAlerta(TipoAlerta _typeAlert) {
		if (_typeAlert == null) {
			throw new IllegalArgumentException("El tipo de alerta no puede ser null");
		}
	}
	
	public int getID() {
		return id;
	}
	
	public double getMaxGasto() {
		return maxExpense;
	}
	
	public TipoAlerta getTipoAlerta() {
		return typeAlert;
	}
	
	public Optional<Categoria> getCategoria(){
		return Optional.ofNullable(category);
	}
	
	public boolean isActiva() {
		return active;
	}
	
	public boolean esGeneral() {
		return category == null;
	}
	
	/**
	 * Modifica el tope de gasto
	 * @param _maxGasto Nuevo tope (positivo)
	 * @throws IllegalArgumentException si _maxGasto <=0
	 */
	public void setMaxGasto(double _maxExpense) {
		validarMaxGasto(_maxExpense);
		this.maxExpense = _maxExpense;
	}
	
	/**
	 * Modifica el tipo de alerta
	 * @param _typeAlerta Nuevo tipo (no null)
	 * @throws IllegalArgumentException si _typeAlerta es null
	 */
	public void setTypeAlerta(TipoAlerta _typeAlert) {
		validarTypeAlerta(_typeAlert);
		this.typeAlert = _typeAlert;
	}

	public void setCategoria(Categoria _category) {
		this.category = _category;
	}
	
	public void activar() {
		this.active = true;
	}
	
	public void desactivar() {
		this.active = false;
	}
	
	protected void setID(int _ID) {
		this.id = _ID;
		if (_ID >= counterID) {
			counterID = _ID;
		}
	}

	protected void setActiva(boolean _active) {
		this.active = _active;
	}
	
	public double calcularGastoActual(List<Gasto> _expenses) {
		if (_expenses == null || _expenses.isEmpty()) {
			return 0.0;
		}
		
		LocalDate today = LocalDate.now();
		LocalDate startPeriod = calcularInicioPeriodo(today);
		
		return _expenses.stream()
				.filter(g -> !g.getFecha().toLocalDate().isBefore(startPeriod))
				.filter(g -> !g.getFecha().toLocalDate().isAfter(today))
				.filter(g -> category == null || g.getCategoria().equals(category))
				.mapToDouble(Gasto::getCantidad)
				.sum();
	}
	
	private LocalDate calcularInicioPeriodo(LocalDate _currentDate) {
		switch (typeAlert) {
			case SEMANAL:
				return _currentDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
			case MENSUAL:
				return _currentDate.withDayOfMonth(1);
			default:
				return _currentDate;
		}
	}
	
	public boolean verificar(List<Gasto> _expenses) {
		if (!active) {
			return false;
		}
		return calcularGastoActual(_expenses) > maxExpense;
	}
	
	public Optional<Notificacion> generarNotificacion(List<Gasto> _expenses){
		if(!verificar(_expenses)) {
			return Optional.empty();
		}
		
		double currentGasto = calcularGastoActual(_expenses);
		String message = construirMensajeNotificacion(currentGasto);
		
		return Optional.of(new Notificacion(message));
	}
	
	public String construirMensajeNotificacion(double _currentExpense) {
		StringBuilder sb = new StringBuilder();
		sb.append("¡Alerta! Has superado el tope de gasto ");
		sb.append(typeAlert.getDescripcion().toLowerCase());
		
		getCategoria().ifPresent(cat -> sb.append(" en la cateogria '").append(cat.getNombre()).append("'"));
	
		sb.append(String.format(". Tope: %.€, Gasto actual: %.2f€", maxExpense, _currentExpense));
		
		return sb.toString();
	}
	
	public static void resetContador() {
		counterID = 0;
	}
	
	public static void setContador(int _counter) {
		counterID = _counter;
	}
	
	@Override
	public boolean equals(Object _obj) {
		if (this == _obj) return true;
		if (_obj == null || getClass() != _obj.getClass()) return false;
		Alerta alerta = (Alerta) _obj;
		return id == alerta.id;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(id);
	}
	
	@Override
	public String toString() {
		String categoryStr = getCategoria()
						.map(Categoria::getNombre)
						.orElse("Todas");
		return String.format("Alerta[id=%d, tope=%.2f€, tipo=%s, categoria=%s, activa=%s]", 
				id, maxExpense, typeAlert, categoryStr, active);
	}
}


