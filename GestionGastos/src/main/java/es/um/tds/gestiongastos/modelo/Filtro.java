package es.um.tds.gestiongastos.modelo;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Encapsula los criterios de filtrado para consultar gastos.
 * Permite filtrar por meses, rango de fechas y categorías.
 * Los filtros son combinables (se aplican todos los que estén definidos).
 */

public class Filtro {

	private List<Month> months;
	private LocalDate startDate;
	private LocalDate endDate;
	private List<Categoria> categories;
	
	/**
	 * Constructor sin filtros.
	 */
	public Filtro() {
		this.months = null;
		this.startDate = null;
		this.endDate = null;
		this.categories = null;
	}
	
	/**
	 * Contructor con todos los filtros.
	 * @param _months Lista de meses a filtrar (null para no filtrar por mes)
	 * @param _startDate Fecha de inicio del rango (null para no filtrar)
	 * @param _endDate Fecha de fin del rango (null
	 */
	public Filtro(List<Month> _months, LocalDate _startDate, LocalDate _endDate, List<Categoria> _categories) {
		this.months = (_months != null && !_months.isEmpty()) ? new ArrayList<>(_months) : null;
		this.startDate = _startDate;
		this.endDate = _endDate;
		this.categories = (_categories != null && !_categories.isEmpty()) ? new ArrayList<>(_categories) : null;
	}
	
	public Optional<List<Month>> getMeses(){
		return Optional.ofNullable(months).map(ArrayList::new);
	}
	
	public Optional<LocalDate> getFechaInicio() {
		return Optional.ofNullable(startDate);
	}
	
	public Optional<LocalDate> getFechaFin(){
		return Optional.ofNullable(endDate);
	}
	
	public Optional<List<Categoria>> getCategorias(){
		return Optional.ofNullable(categories);
	}
	
	public void setMeses(List<Month> _months) {
		this.months = (_months !=null && !_months.isEmpty()) ? new ArrayList<>(_months) : null;
	}
	
	public void setRangoFechas(LocalDate _startDate, LocalDate _endDate) {
		this.startDate = _startDate;
		this.endDate = _endDate;
	}
	
	public void setFechaInicio(LocalDate _startDate) {
		this.startDate = _startDate;
	}
	
	public void setFechaFin(LocalDate _endDate) {
		this.endDate = _endDate;
	}
	
	public void setCategorias(List<Categoria> _categories) {
		this.categories = (_categories != null && !_categories.isEmpty()) ? new ArrayList<>(_categories) : null;
	}
	
	public void addMes(Month _month) {
		if (_month != null) {
			if(months != null) {
				months = new ArrayList<>();
			}
			if(!months.contains(_month)) {
				months.add(_month);
			}
		}
	}
	
	public void addCategoria(Categoria _category) {
		if (_category != null) {
			if (categories == null) {
				categories = new ArrayList<>();
			}
			if(!categories.contains(_category)) {
				categories.add(_category);
			}
		}
	}
	
	public void limpiarFiltros() {
		months = null;
		startDate = null;
		endDate = null;
		categories = null;
	}
	
	public boolean tieneFiltros() {
		return months != null || startDate != null || endDate != null || categories != null;
	}
	
	public List<Gasto> aplicarFiltros(List<Gasto> _expenses){
		if(_expenses == null || _expenses.isEmpty()) {
			return new ArrayList<>();
		}
		return _expenses.stream()
				.filter(this::cumpleFiltroMeses)
				.filter(this::cumpleFiltroFechas)
				.filter(this::cumpleFiltroCategorias)
				.collect(Collectors.toList());
	}
	
	private boolean cumpleFiltroMeses(Gasto _expense) {
		if (months == null || months.isEmpty()) {
			return true;
		}
		Month monthExpense = _expense.getFecha().getMonth();
		return months.contains(monthExpense);
	}
	
	private boolean cumpleFiltroFechas(Gasto _expense) {
		LocalDate dateExpense = _expense.getFecha().toLocalDate();
		
		if (startDate != null && dateExpense.isBefore(startDate)) {
			return false;
		}
		
		if (endDate != null && dateExpense.isAfter(endDate)) {
			return false;
		}
		
		return true;
	}
	
	public boolean cumpleFiltroCategorias(Gasto _expense) {
		if (categories == null || categories.isEmpty()) {
			return true;
		}
		
		return categories.contains(_expense.getCategoria());
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("Filtro[");
		
		getMeses().ifPresent(m -> sb.append("meses=").append(m).append(", "));
		getFechaInicio().ifPresent(f -> sb.append("desde=").append(f).append(", "));
		getCategorias().ifPresent(c -> sb.append("categorias=").append(c.size()));
		
		if(!tieneFiltros()) {
			sb.append(", sin filtros");
		}
		
		sb.append("]");
		return sb.toString();
		
	}
	
}
