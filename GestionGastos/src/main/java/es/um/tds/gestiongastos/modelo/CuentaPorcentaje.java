package es.um.tds.gestiongastos.modelo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Cuenta compartida con reparto por porcentajes.
 * Cada participante asume un porcentaje específico del gasto.
 * La suma de los porcentajes debe ser 100%.
 */
public class CuentaPorcentaje extends CuentaCompartida{
	
	private Map<Persona, Double> porcentajes;
	
	/**
	 * Contructor con la lista de personas y porcentajes.
	 * @param _name Nombre de la cuenta.
	 * @param _people Lista de personas (mínimo 2)
	 * @param _percentages Mapa de persona a porcentaje (Debe sumar 100)
	 * @throws IllegalArgumentException si los porcentajes no suman 100 o faltan personas
	 */
	public CuentaPorcentaje(String _name, List<Persona> _people, Map<Persona, Double> _percentages) {
		super(_name, _people);
		validarPorcentajes(_percentages);
		this.porcentajes = new HashMap<>(_percentages);
	}
	
	/**
	 * Constructor que inicializa con reparto equitativo.
	 * Los porcentajes pueden modificarse posteriormente.
	 * @param _name Nombre de la cuenta
	 * @param _people Lista de personas (minimo 2)
	 */
	public CuentaPorcentaje(String _name, List<Persona> _people) {
		super(_name, _people);
		this.porcentajes = new HashMap<>();
		double percentage = 100.0/_people.size();
		for (Persona p: this.personas) {
			porcentajes.put(p, percentage);
		}
	}
	
	/**
	 * Constructor con varargs.
	 * @param _name Nombre de la cuenta.
	 * @param _people Personas de la cuenta (minimo 2).
	 */
	public CuentaPorcentaje(String _name, Map<Persona, Double> _percentages, Persona... _people) {
		this(_name, List.of(_people), _percentages);
	}
	
	/**
	 * Constructor con varargs (reparto equitativo inicial).
	 * @param _name Nombre de la cuenta.
	 * @param _people Personas de la cuenta (minimo 2).
	 */
	public CuentaPorcentaje(String _name, Persona... _people) {
		this(_name, List.of(_people));
	}
	
	protected CuentaPorcentaje() {
		super();
		this.porcentajes = new HashMap<>();
	}
	
	private void validarPorcentajes(Map<Persona, Double> _percentages) {
		if (_percentages == null || _percentages.isEmpty()) {
			throw new IllegalArgumentException("Los porcentajes no pueden ser null ni vacíos");
		}
		
		for (Persona p : getPersonas()) {
			if (!_percentages.containsKey(p)) {
				throw new IllegalArgumentException("Falta el porcentaje para: " + p.getNombre());
			}
		}
			
		for (Double percentage : _percentages.values()) {
			if (percentage < 0) {
				throw new IllegalArgumentException("Los porcentajes no pueden ser igual a negativos");
			}
		}
		
		double suma = _percentages.values().stream()
				.mapToDouble(Double::doubleValue)
				.sum();
		
		if (Math.abs(suma- 100.0) > 0.01) {
			throw new IllegalArgumentException(String.format("Los porcentajes deben sumar 100%%. Suma actual: %.2f%%", suma));
		}
	}
		
	public double getPorcentaje(Persona _person) {
		validarPersonaEnCuenta(_person);
		return porcentajes.getOrDefault(_person, 0.0);
	}
	
	public Map<Persona, Double> getPorcentajes(){
		return new HashMap<>(porcentajes);
	}
	
	/**
	 * Establece los porcentajes para todas las personas.
	 * @param _percentages Nuevo mapa de porcentajes (debe sumar 100).
	 * @throw IllegalArgumentException si los porcentajes son invalidos.
	 */
	public void setPorcentajes(Map<Persona, Double> _percentages) {
		validarPorcentajes(_percentages);
		this.porcentajes = new HashMap<>(_percentages);
		recalcularSaldos();
	}
	
	@Override
	public double calcularProporcion(Persona _person) {
		validarPersonaEnCuenta(_person);
		return porcentajes.getOrDefault(_person, 0.0) / 100.0;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(String.format("CuentaPorcentaje[id=%d, nombre=%s, gastoTotal=%.2f€, ", 
				getID(), getNombre(), calcularGastoTotal()));
		sb.append("porcentajes={");
		
		boolean first = true;
		for(Persona p: getPersonas()) {
			if(!first) sb.append(", ");
			sb.append(String.format("%s=%.1f%%", p.getNombre(), getPorcentaje(p)));
			first = false;
		}
		
		sb.append("}]");
		return sb.toString();
	}
}
