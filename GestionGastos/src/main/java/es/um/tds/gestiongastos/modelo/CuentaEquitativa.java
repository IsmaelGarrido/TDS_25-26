package es.um.tds.gestiongastos.modelo;

import java.util.List;

public class CuentaEquitativa extends CuentaCompartida {

	public CuentaEquitativa(String _name, List<Persona> _people) { super(_name, _people); }
	public CuentaEquitativa(String _name, Persona... _people)    { super(_name, _people); }
	protected CuentaEquitativa() { super(); }

	@Override
	public double calcularProporcion(Persona _person) {
		validarPersonaEnCuenta(_person);
		return 1.0 / calcularNumPersonas();
	}

	/** Devuelve la descripción del tipo para mostrar en la vista. */
	@Override
	public String obtenerTipoDescripcion() { return "Equitativa"; }

	@Override
	public String toString() {
		return String.format("CuentaEquitativa[id=%d, nombre=%s, personas=%d, gastoTotal=%.2f€]",
				getID(), getNombre(), calcularNumPersonas(), calcularGastoTotal());
	}
}