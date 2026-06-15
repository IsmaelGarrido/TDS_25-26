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
	protected String nombre;
	protected List<Persona> personas;
	protected List<Gasto> gastos;
	protected Map<Persona, Double> saldos;
	
	public CuentaCompartida(String _name, List<Persona> _people) {
		validarNombre(_name);
		validarPersonas(_people);
		this.id = ++counterID;
		this.nombre = _name.trim();
		this.personas = new ArrayList<>(_people);
		this.gastos = new ArrayList<>();
		this.saldos = new HashMap<>();
		for (Persona p : this.personas) saldos.put(p, 0.0);
	}
	
	public CuentaCompartida(String _name, Persona... _people) {
		this(_name, (_people != null) ? List.of(_people) : null);
	}
	
	protected CuentaCompartida() {
		this.personas = new ArrayList<>();
		this.gastos   = new ArrayList<>();
		this.saldos   = new HashMap<>();
	}
	
	private void validarNombre(String _name) {
		if (_name == null || _name.trim().isEmpty())
			throw new IllegalArgumentException("El nombre no puede ser null ni vacío");
	}
	
	private void validarPersonas(List<Persona> _people) {
		if (_people == null || _people.size() < 2)
			throw new IllegalArgumentException("La cuenta debe tener al menos 2 personas");
	}
	
	protected void validarPersonaEnCuenta(Persona _person) {
		if (_person == null || !personas.contains(_person))
			throw new IllegalArgumentException("La persona no pertenece a esta cuenta");
	}
	
	public int getID()                       { return id; }
	public String getNombre()                { return nombre; }
	public List<Persona> getPersonas()       { return Collections.unmodifiableList(personas); }
	public List<Gasto> getGastos()           { return Collections.unmodifiableList(gastos); }
	public int calcularNumPersonas()         { return personas.size(); }
	public Map<Persona, Double> getSaldos()  { return new HashMap<>(saldos); }

	public double calcularSaldo(Persona _person) {
		validarPersonaEnCuenta(_person);
		return saldos.getOrDefault(_person, 0.0);
	}

	public double calcularGastoTotal() {
		return gastos.stream().mapToDouble(Gasto::getCantidad).sum();
	}

	public void setNombre(String _name) {
		validarNombre(_name);
		this.nombre = _name.trim();
	}

	protected void setID(int _ID) {
		this.id = _ID;
		if (_ID >= counterID) counterID = _ID;
	}

	protected void setPersonas(List<Persona> _people) { this.personas = new ArrayList<>(_people); }
	protected void setGastos(List<Gasto> _expenses)   { this.gastos = new ArrayList<>(_expenses); }
	protected void setSaldos(Map<Persona, Double> _b)  { this.saldos = new HashMap<>(_b); }

	public void addGasto(Gasto _expense, Persona _payer) {
		if (_expense == null) throw new IllegalArgumentException("El gasto no puede ser null");
		validarPersonaEnCuenta(_payer);
		_expense.setPagador(_payer);
		gastos.add(_expense);
		actualizarSaldos(_expense, _payer);
	}

	private void actualizarSaldos(Gasto _expense, Persona _payer) {
		double amount = _expense.getCantidad();
		for (Persona person : personas) {
			double proportion = calcularProporcion(person);
			double debt = amount * proportion;
			if (person.equals(_payer)) saldos.merge(person,  amount - debt, Double::sum);
			else                       saldos.merge(person, -debt,           Double::sum);
		}
	}

	public boolean removeGasto(Gasto _expense) {
		if (_expense == null || !gastos.contains(_expense)) return false;
		gastos.remove(_expense);
		recalcularSaldos();
		return true;
	}

	protected void recalcularSaldos() {
		for (Persona p : personas) saldos.put(p, 0.0);
		for (Gasto expense : gastos)
			expense.getPagador().ifPresent(pagador -> actualizarSaldos(expense, pagador));
	}

	public abstract double calcularProporcion(Persona _person);

	public abstract String obtenerTipoDescripcion();

	public boolean contienePersona(Persona _person) { return personas.contains(_person); }

	public static void resetContador()         { counterID = 0; }
	public static void setContador(int _c)     { counterID = _c; }

	@Override
	public boolean equals(Object _obj) {
		if (this == _obj) return true;
		if (_obj == null || getClass() != _obj.getClass()) return false;
		return id == ((CuentaCompartida) _obj).id;
	}

	@Override public int hashCode() { return Objects.hash(id); }

	@Override
	public String toString() {
		return String.format("CuentaCompartida[id=%d, nombre=%s, personas=%d, gastoTotal=%.2f€]",
				id, nombre, personas.size(), calcularGastoTotal());
	}
}