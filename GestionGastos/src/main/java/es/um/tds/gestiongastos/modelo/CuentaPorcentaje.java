package es.um.tds.gestiongastos.modelo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CuentaPorcentaje extends CuentaCompartida {

	private Map<Persona, Double> porcentajes;

	public CuentaPorcentaje(String _name, List<Persona> _people, Map<Persona, Double> _percentages) {
		super(_name, _people);
		validarPorcentajes(_percentages);
		this.porcentajes = new HashMap<>(_percentages);
	}

	public CuentaPorcentaje(String _name, List<Persona> _people) {
		super(_name, _people);
		this.porcentajes = new HashMap<>();
		double pct = 100.0 / _people.size();
		for (Persona p : this.personas) porcentajes.put(p, pct);
	}

	public CuentaPorcentaje(String _name, Map<Persona, Double> _percentages, Persona... _people) {
		this(_name, List.of(_people), _percentages);
	}

	public CuentaPorcentaje(String _name, Persona... _people) { this(_name, List.of(_people)); }

	protected CuentaPorcentaje() { super(); this.porcentajes = new HashMap<>(); }

	private void validarPorcentajes(Map<Persona, Double> _percentages) {
		if (_percentages == null || _percentages.isEmpty())
			throw new IllegalArgumentException("Los porcentajes no pueden ser null ni vacíos");
		for (Persona p : getPersonas())
			if (!_percentages.containsKey(p))
				throw new IllegalArgumentException("Falta el porcentaje para: " + p.getNombre());
		for (Double pct : _percentages.values())
			if (pct < 0)
				throw new IllegalArgumentException("Los porcentajes no pueden ser negativos");
		double suma = _percentages.values().stream().mapToDouble(Double::doubleValue).sum();
		if (Math.abs(suma - 100.0) > 0.01)
			throw new IllegalArgumentException(
				String.format("Los porcentajes deben sumar 100%%: %.2f%%", suma));
	}

	public double getPorcentaje(Persona _person) {
		validarPersonaEnCuenta(_person);
		return porcentajes.getOrDefault(_person, 0.0);
	}

	public Map<Persona, Double> getPorcentajes() { return new HashMap<>(porcentajes); }

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

	/** Devuelve la descripción del tipo para mostrar en la vista. */
	@Override
	public String obtenerTipoDescripcion() { return "Por porcentaje"; }

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(String.format("CuentaPorcentaje[id=%d, nombre=%s, gastoTotal=%.2f€, porcentajes={",
				getID(), getNombre(), calcularGastoTotal()));
		boolean first = true;
		for (Persona p : getPersonas()) {
			if (!first) sb.append(", ");
			sb.append(String.format("%s=%.1f%%", p.getNombre(), getPorcentaje(p)));
			first = false;
		}
		sb.append("}]");
		return sb.toString();
	}
}