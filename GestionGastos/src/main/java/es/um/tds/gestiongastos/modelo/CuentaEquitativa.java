package es.um.tds.gestiongastos.modelo;

import java.util.List;

/**
 * Cuenta compartida con reparto equitativo.
 * Todos los participantes asumen la misma proporcion del gasto.
 */
public class CuentaEquitativa extends CuentaCompartida{
	/*
	 * Constructor con lista de personas.
	 * @param _name Nombre de la cuenta
	 * @param _people Lista de personas (mínimo 2)
	 */
	public CuentaEquitativa(String _name, List<Persona> _people) {
		super(_name, _people);
	}
	
	/**
	 * Contructor con varargs.
	 * @param _name Nombre de la cuenta
	 * @param _people Personas de la cuenta (mínimo 2)
	 */
	public CuentaEquitativa(String _name, Persona... _people) {
		super(_name, _people);
	}
	
	protected CuentaEquitativa() {
		super();
	}
	
	@Override
	public double calcularProporcion(Persona _person) {
		validarPersonaEnCuenta(_person);
		return 1.0/ calcularNumPersonas();
	}
	
	@Override
	public String toString() {
		return String.format("CuentaEquitativa[id=%d, nombre=%s, personas=%d, gastoTotal=%.2f€", 
				getID(), getNombre(), calcularNumPersonas(), calcularGastoTotal());
	}
}
