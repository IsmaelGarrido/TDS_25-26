package es.um.tds.gestiongastos.repositorio;


import es.um.tds.gestiongastos.modelo.Categoria;
import es.um.tds.gestiongastos.modelo.Filtro;
import es.um.tds.gestiongastos.modelo.Gasto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Repositorio de gastos.
 * Implementa el patrón Singleton para garantizar una única instancia.
 */
public class RepositorioGastos {
	
	private static RepositorioGastos instance;
	
	private List<Gasto> expenses;
	
	/**
	 * Constructor privado (Singleton).
	 */
	private RepositorioGastos() {
		this.expenses = new ArrayList<>();
	}
	
	public static synchronized RepositorioGastos getInstance() {
		if (instance == null) {
			instance = new RepositorioGastos();
		}
		
		return instance;
	}
	
	public List<Gasto> getTodosGastos(){
		return new ArrayList<>(expenses);
	}
	
	public Optional<Gasto> getGastoByID(int _ID){
		return expenses.stream()
				.filter(g -> g.getID() == _ID)
				.findFirst();
	}
	
	/**
	 * Guarda un gasto (añade si es nuevo).
	 * @param _expense Gasto a guardar
	 * @throws IllegalArgumentException si el gasto es null
	 */
	public void addGasto(Gasto _expense) {
		if (_expense == null) {
			throw new IllegalArgumentException("El gasto no puede ser null");
		}
		
		if (getGastoByID(_expense.getID()).isEmpty()) {
			expenses.add(_expense);
		}
	}
	
	public boolean deleteGasto(Gasto _expense) {
		if (_expense == null) {
			return false;
		}
		
		return expenses.remove(_expense);
	}
	
	public boolean deleteGastoByID(int _ID) {
		Optional<Gasto> expense = getGastoByID(_ID);
		return expense.map(this::deleteGasto).orElse(false);
	}
	
	public List<Gasto> getGastosByFiltro(Filtro _filter){
		if (_filter == null || !_filter.tieneFiltros()) {
			return getTodosGastos();
		}
		return _filter.aplicarFiltros(expenses);
	}
	
	public List<Gasto> getGastosByCategoria(Categoria _category){
		if (_category == null) {
			return new ArrayList<>();
		}
		
		return expenses.stream()
				.filter(g -> g.getCategoria().equals(_category))
				.collect(Collectors.toList());
	}
	
	public List<Gasto> getGastosByRangoFechas(LocalDate _startDate, LocalDate _endDate){
		return expenses.stream()
				.filter(g -> {
					LocalDate date = g.getFecha().toLocalDate();
					boolean afterStart = _startDate == null || !date.isBefore(_startDate);
					boolean beforeEnd = _endDate == null || !date.isAfter(_endDate);
					return afterStart && beforeEnd;
				})
				.collect(Collectors.toList());
	}
	
	public List<Gasto> getGastosPersonales(){
		return expenses.stream()
				.filter(Gasto::isPersonal)
				.collect(Collectors.toList());
	}
	
	public List<Gasto> getGastosCompartidos(){
		return expenses.stream()
				.filter(g -> !g.isPersonal())
				.collect(Collectors.toList());
	}
	
	public double calcularTotal() {
		return expenses.stream()
				.mapToDouble(Gasto::getCantidad)
				.sum();
	}
	
	public double calcularTotal(Filtro _filter) {
		return getGastosByFiltro(_filter).stream()
				.mapToDouble(Gasto::getCantidad)
				.sum();
	}
	
	public int count() {
		return expenses.size();
	}
	
	public void reset() {
		expenses.clear();
		Gasto.resetContador();
	}
	
	public static synchronized void resetInstance() {
		instance = null;
	}
}
