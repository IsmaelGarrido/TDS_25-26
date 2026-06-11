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

	private List<Month> meses;
	private LocalDate fechaInicio;
	private LocalDate fechaFin;
	private List<Categoria> categorias;
	
	/**
	 * Constructor sin filtros.
	 */
	public Filtro() {
		this.meses = null;
		this.fechaInicio = null;
		this.fechaFin = null;
		this.categorias = null;
	}
	
	/**
	 * Contructor con todos los filtros.
	 * @param _months Lista de meses a filtrar (null para no filtrar por mes)
	 * @param _startDate Fecha de inicio del rango (null para no filtrar)
	 * @param _endDate Fecha de fin del rango (null
	 */
	public Filtro(List<Month> _months, LocalDate _startDate, LocalDate _endDate, List<Categoria> _categories) {
		this.meses = (_months != null && !_months.isEmpty()) ? new ArrayList<>(_months) : null;
		this.fechaInicio = _startDate;
		this.fechaFin = _endDate;
		this.categorias = (_categories != null && !_categories.isEmpty()) ? new ArrayList<>(_categories) : null;
	}
	
	public Optional<List<Month>> getMeses(){
		return Optional.ofNullable(meses).map(ArrayList::new);
	}
	
	public Optional<LocalDate> getFechaInicio() {
		return Optional.ofNullable(fechaInicio);
	}
	
	public Optional<LocalDate> getFechaFin(){
		return Optional.ofNullable(fechaFin);
	}
	
	public Optional<List<Categoria>> getCategorias(){
		return Optional.ofNullable(categorias);
	}
	
	public void setMeses(List<Month> _months) {
		this.meses = (_months !=null && !_months.isEmpty()) ? new ArrayList<>(_months) : null;
	}
	
	public void setRangoFechas(LocalDate _startDate, LocalDate _endDate) {
		this.fechaInicio = _startDate;
		this.fechaFin = _endDate;
	}
	
	public void setFechaInicio(LocalDate _startDate) {
		this.fechaInicio = _startDate;
	}
	
	public void setFechaFin(LocalDate _endDate) {
		this.fechaFin = _endDate;
	}
	
	public void setCategorias(List<Categoria> _categories) {
		this.categorias = (_categories != null && !_categories.isEmpty()) ? new ArrayList<>(_categories) : null;
	}
	
	public void addMes(Month _month) {
		if (_month != null) {
			if(meses == null) {
				meses = new ArrayList<>();
			}
			if(!meses.contains(_month)) {
				meses.add(_month);
			}
		}
	}
	
	public void addCategoria(Categoria _category) {
		if (_category != null) {
			if (categorias == null) {
				categorias = new ArrayList<>();
			}
			if(!categorias.contains(_category)) {
				categorias.add(_category);
			}
		}
	}
	
	public void limpiarFiltros() {
		meses = null;
		fechaInicio = null;
		fechaFin = null;
		categorias = null;
	}
	
	public boolean tieneFiltros() {
		return meses != null || fechaInicio != null || fechaFin != null || categorias != null;
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
		if (meses == null || meses.isEmpty()) {
			return true;
		}
		Month monthExpense = _expense.getFecha().getMonth();
		return meses.contains(monthExpense);
	}
	
	private boolean cumpleFiltroFechas(Gasto _expense) {
		LocalDate dateExpense = _expense.getFecha().toLocalDate();
		
		if (fechaInicio != null && dateExpense.isBefore(fechaInicio)) {
			return false;
		}
		
		if (fechaFin != null && dateExpense.isAfter(fechaFin)) {
			return false;
		}
		
		return true;
	}
	
	public boolean cumpleFiltroCategorias(Gasto _expense) {
		if (categorias == null || categorias.isEmpty()) {
			return true;
		}
		
		return categorias.contains(_expense.getCategoria());
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
