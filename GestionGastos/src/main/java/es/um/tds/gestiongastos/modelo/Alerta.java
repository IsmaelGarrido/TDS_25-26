package es.um.tds.gestiongastos.modelo;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;


/**
 * Representa una alerta de gasto configurada por el usuario.
 * Las alertas pueden ser diarias, semanales o mensuales, y opcionalmente
 * estar vinculadas a una categoría específica.
 */
public class Alerta {

	private static int contadorID = 0;
	
	private int id;
	private double maxGasto;
	private TipoAlerta tipoAlerta;
	private Categoria categoria;
	private boolean activa;

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
		
		this.id = ++contadorID;
		this.maxGasto = _maxExpense;
		this.tipoAlerta = _typeAlert;
		this.categoria = _category;
		this.activa = true;
	}
	
	/**
	 * Constructor sin categoria
	 * @param _maxExpense Limite de gasto (positivo).
	 * @param _typeAlert Tipo de Alerta (no null).
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
		return maxGasto;
	}
	
	public TipoAlerta getTipoAlerta() {
		return tipoAlerta;
	}
	
	public Optional<Categoria> getCategoria(){
		return Optional.ofNullable(categoria);
	}
	
	public boolean getActiva() {
		return activa;
	}
	
	public boolean esGeneral() {
		return categoria == null;
	}
	
	/**
	 * Modifica el tope de gasto
	 * @param _maxGasto Nuevo tope (positivo)
	 * @throws IllegalArgumentException si _maxGasto <=0
	 */
	public void setMaxGasto(double _maxExpense) {
		validarMaxGasto(_maxExpense);
		this.maxGasto = _maxExpense;
	}
	
	/**
	 * Modifica el tipo de alerta
	 * @param _typeAlerta Nuevo tipo (no null)
	 * @throws IllegalArgumentException si _typeAlerta es null
	 */
	public void setTipoAlerta(TipoAlerta _typeAlert) {
		validarTypeAlerta(_typeAlert);
		this.tipoAlerta = _typeAlert;
	}

	public void setCategoria(Categoria _category) {
		this.categoria = _category;
	}
	
	public void activar() {
		this.activa = true;
	}
	
	public void desactivar() {
		this.activa = false;
	}
	
	protected void setID(int _ID) {
		this.id = _ID;
		if (_ID >= contadorID) {
			contadorID = _ID;
		}
	}

	protected void setActiva(boolean _active) {
		this.activa = _active;
	}
	/**
	 * Calcula el gasto acumulado en el periodo actual de esta alerta.
	 * Delega el cálculo del inicio en TipoAlerta (Strategy).
	 * Filtra por categoría si la alerta no es general.
	 * @param _expenses Lista de gastos a evaluar
	 * @return Total de gastos en el periodo actual
	 */
	public double calcularGastoActual(List<Gasto> _expenses) {
		if (_expenses == null || _expenses.isEmpty()) {
			return 0.0;
		}
		
		LocalDate today = LocalDate.now();
		LocalDate startPeriod = tipoAlerta.calcularInicioPeriodo(today);
		
		return _expenses.stream()
				.filter(g -> !g.getFecha().toLocalDate().isBefore(startPeriod))
				.filter(g -> !g.getFecha().toLocalDate().isAfter(today))
				.filter(g -> categoria == null || g.getCategoria().equals(categoria))
				.mapToDouble(Gasto::getCantidad)
				.sum();
	}
	
	/**
	 * Verifica si el gasto actual supera el tope configurado.
	 * Una alerta desactivada nunca dispara.
	 * @param _expenses Lista de gastos a evaluar
	 * @return true si el gasto supera el tope y la alerta está activa
	 */
	public boolean verificar(List<Gasto> _expenses) {
		if (!activa) {
			return false;
		}
		return calcularGastoActual(_expenses) > maxGasto;
	}
	
	/**
	 * Genera una notificación si el tope se supera.
	 * @param _expenses Lista de gastos a evaluar
	 * @return Optional con la notificación o vacío si no se supera el tope
	 */
	public Optional<Notificacion> generarNotificacion(List<Gasto> _expenses){
		if(!verificar(_expenses)) {
			return Optional.empty();
		}
		
		double currentGasto = calcularGastoActual(_expenses);
		String message = construirMensajeNotificacion(currentGasto);
		
		return Optional.of(new Notificacion(message));
	}
	
	/**
	 * Construye el mensaje de la notificación.
	 * @param _currentExpense Gasto actual calculado
	 * @return Mensaje formateado con tope y gasto actual
	 */
	public String construirMensajeNotificacion(double _currentExpense) {
		StringBuilder sb = new StringBuilder();
		sb.append("¡Alerta! Has superado el tope de gasto ");
		sb.append(tipoAlerta.getDescripcion().toLowerCase());
		
		getCategoria().ifPresent(cat -> sb.append(" en la categoria '").append(cat.getNombre()).append("'"));
	
		sb.append(String.format(". Tope: %.2f€, Gasto actual: %.2f€", maxGasto, _currentExpense));
		
		return sb.toString();
	}
	
	public static void resetContador() {
		contadorID = 0;
	}
	
	public static void setContador(int _counter) {
		contadorID = _counter;
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
				id, maxGasto, tipoAlerta, categoryStr, activa);
	}
}


