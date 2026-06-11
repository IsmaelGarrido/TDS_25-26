package es.um.tds.gestiongastos.modelo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public abstract class CuentaCompartida {

	private static int counterID = 0;
	
	protected int id;
	protected String name;
	protected List<Persona> people;
	protected List<Gasto> expenses;
	protected Map<Persona, Double> balances;
	
	/**
	 * Constructor de CuentaCompartida
	 * @param _name Nombre de la cuenta (no null ni vacío)
	 * @param _people Lista de persona (min 2)
	 * @throws IllegalArgumentException si name es null/vacío o hay menos de 2 personas
	 */
	public CuentaCompartida(String _name, List<Persona> _people) {
		validarNombre(_name);
		validarPersonas(_people);
		
		this.id = ++counterID;
		this.name = _name.trim();
		this.people = new ArrayList<>(_people);
		this.expenses = new ArrayList<>();
		this.balances = new HashMap<>();
		
		for (Persona p : this.people) {
			balances.put(p,  0.0);
		}
	}
	
	/**
	 * Constructor alternativo
	 * @param _name Nombre de la cuenta (no null ni vacío)
	 * @param _people Personas de la cuenta (min 2)
	 * @throws IllegalArgumentException si _name es null/vacío o hay menos de 2 personas.
	 */
	public CuentaCompartida(String _name, Persona... _people) {
		this(_name, (_people != null) ? List.of(_people) : null);
	}
	
	protected CuentaCompartida() {
		this.people = new ArrayList<>();
		this.expenses = new ArrayList<>();
		this.balances = new HashMap<>();
	}
	
	private void validarNombre(String _name) {
		if (_name == null || _name.trim().isEmpty()) {
			throw new IllegalArgumentException("El nombre no puede ser null ni vacío");
		}
	}
	
	private void validarPersonas(List<Persona> _people) {
		if (_people == null || _people.size() < 2) {
			throw new IllegalArgumentException("La cuenta debe tener al menos 2 personas");
		}
	}
	
	protected void validarPersonaEnCuenta(Persona _person) {
		if (_person == null || !people.contains(_person)) {
			throw new IllegalArgumentException("La persona no pertenece a esta cuenta");
		}
	}
	
	public int getID() {
		return id;
	}
	
	public String getNombre() {
		return name;
	}
	
	public List<Persona> getPersonas(){
		return Collections.unmodifiableList(people);
	}
	
	public List<Gasto> getGastos(){
		return Collections.unmodifiableList(expenses);
	}
	
	public int getNumPersonas() {
		return people.size();
	}
	
	/**
	 * Obtiene el saldo de una persona.
	 * Positivo = le deben dinero, Negativo = debe dinero.
	 * @param _person Persona de la cuenta.
	 * @return Saldo de la persona.
	 * @throws IllegalArgumentException si la persona no está en la cuenta.
	 */
	public double getSaldo(Persona _person) {
		validarPersonaEnCuenta(_person);
		return balances.getOrDefault(_person, 0.0);
	}
	
	public Map<Persona, Double> getSaldos(){
		return new HashMap<>(balances);
	}
	
	public double getGastoTotal() {
		return expenses.stream().mapToDouble(Gasto::getCantidad).sum();
	}
	
	/**
	 * Modifica el nombre de la cuenta.
	 * @param _name Nuevo nombre.
	 * @throws IllegalArgumentException si el nombre es null o vacío.
	 */
	public void setNombre(String _name) {
		validarNombre(_name);
		this.name = _name.trim();
	}
	
	protected void setID(int _ID) {
		this.id = _ID;
		if (_ID >= counterID) {
			counterID = _ID;
		}
	}
	
	protected void setPersonas(List<Persona> _people) {
		this.people = new ArrayList<>(_people);
	}
	
	protected void setGastos(List<Gasto> _expenses) {
		this.expenses = new ArrayList<>(_expenses);
	}
	
	protected void setSaldos(Map<Persona, Double> _balances) {
		this.balances = new HashMap<>(_balances);
	}
	
	/**
	 * Añade un nuevo gasto a la cuenta y actualiza saldos.
	 * @param _expense Gasto a añadir.
	 * @param _payer Persona que pagó el gasto.
	 * @throws IllegalArgumentException si el gasto es null o el pagador no está en la cuenta.
	 */
	public void addGasto(Gasto _expense, Persona _payer) {
		if (_expense == null) {
			throw new IllegalArgumentException("El gasto no puede ser null");
		}
		validarPersonaEnCuenta(_payer);
		
		_expense.setPagador(_payer);
		expenses.add(_expense);
		
		actualizarSaldos(_expense, _payer);
	}

	private void actualizarSaldos(Gasto _expense, Persona _payer) {
		double amount = _expense.getCantidad();
		
		for (Persona person: people) {
			double proportion = calcularProporcion(person);
			double debt = amount * proportion;
			
			if (person.equals(_payer)) {
				double received = amount - debt;
				balances.merge(person, received, Double::sum);
			} else {
				balances.merge(person,  -debt, Double::sum);
			}
		}
	}
	
	public boolean removeGasto(Gasto _expense) {
		if (_expense == null || !expenses.contains(_expense)) {
			return false;
		}
		
		expenses.remove(_expense);
		recalcularSaldos();
		return true;
	}
	
	protected void recalcularSaldos() {
		for (Persona p : people) {
			balances.put(p, 0.0);
		}
		
		for (Gasto expense: expenses) {
			expense.getPagador().ifPresent(pagador -> actualizarSaldos(expense, pagador));
		}
	}
	
	public abstract double calcularProporcion(Persona _person);
	
	public boolean contienePersona(Persona _person) {
		return people.contains(_person);
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
		CuentaCompartida cuenta = (CuentaCompartida) _obj;
		return id == cuenta.id;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(id);
	}
	
	@Override
	public String toString() {
		return String.format("CuentaCompartida [id=%d, nombre=%s, personas=%d, gastoTotal=%.2f€]",
					id, name, people.size(), getGastoTotal());
	}
}

