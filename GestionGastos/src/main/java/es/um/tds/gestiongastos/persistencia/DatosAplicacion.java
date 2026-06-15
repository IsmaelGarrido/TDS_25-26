package es.um.tds.gestiongastos.persistencia;

import es.um.tds.gestiongastos.modelo.*;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import java.util.ArrayList;
import java.util.List;

@JsonAutoDetect(
		fieldVisibility = JsonAutoDetect.Visibility.ANY,
		getterVisibility = JsonAutoDetect.Visibility.NONE,
		setterVisibility = JsonAutoDetect.Visibility.NONE,
		isGetterVisibility = JsonAutoDetect.Visibility.NONE
)

/**
 * DTO que agrupa todos los datos de la aplicación para su persistencia.
 * Contiene las listas de todas las entidades que deben guardarse/cargarse.
 */
public class DatosAplicacion {
	
	private List<Categoria> categories;
	private List<Gasto> expenses;
	private List<CuentaCompartida> accounts;
	private List<Alerta> alerts;
	private List<Notificacion> notifications;
	
	private int counterCategory;
	private int counterExpense;
	private int counterAccount;
	private int counterAlert;
	private int counterNotification;
	
	/**
	 * Constructor vacío para Jackson.
	 */
	public DatosAplicacion() {
		this.categories = new ArrayList<>();
		this.expenses = new ArrayList<>();
		this.accounts = new ArrayList<>();
		this.alerts = new ArrayList<>();
		this.notifications = new ArrayList<>();
	}
	
	/**
	 * Constructor con todos los datos.
	 */
	public DatosAplicacion(List<Categoria> _categories, List<Gasto> _expenses,
							List<CuentaCompartida> _accounts, List<Alerta> _alerts,
							List<Notificacion> _notifications) {
		this.categories = (_categories != null) ? new ArrayList<>(_categories) : new ArrayList<>();
		this.expenses = (_expenses != null) ? new ArrayList<>(_expenses) : new ArrayList<>();
		this.accounts = (_accounts != null) ? new ArrayList<>(_accounts) : new ArrayList<>();
		this.alerts = (_alerts != null) ? new ArrayList<>(_alerts) : new ArrayList<>();
		this.notifications = (_notifications != null) ? new ArrayList<>(_notifications) : new ArrayList<>();
		
		calcularContadores();
	}
	
	/**
	 * Calcula los contadores máximos de ID para cada entidad.
	 */
	private void calcularContadores() {
		counterCategory = categories.stream()
				.mapToInt(Categoria::getID)
				.max()
				.orElse(0);
		
		counterExpense = expenses.stream()
				.mapToInt(Gasto::getID)
				.max()
				.orElse(0);
		
		counterAccount = accounts.stream()
				.mapToInt(CuentaCompartida::getID)
				.max()
				.orElse(0);
		
		counterAlert = alerts.stream()
				.mapToInt(Alerta::getID)
				.max()
				.orElse(0);
		
		counterNotification = notifications.stream()
				.mapToInt(Notificacion::getID)
				.max()
				.orElse(0);
	}
	
	public List<Categoria> getCategorias(){
		return categories;
	}
	
	public List<Gasto> getGastos(){
		return expenses;
	}
	
	public List<CuentaCompartida> getCuentas(){
		return accounts;
	}
	
	public List<Alerta> getAlertas(){
		return alerts;
	}
	
	public List<Notificacion> getNotificaciones(){
		return notifications;
	}
	
	public int getContadorCategoria() {
		return counterCategory;
	}

	public int getContadorGasto() {
		return counterExpense;
	}
	
	public int getContadorCuenta() {
		return counterAccount;
	}
	
	public int getContadorAlerta() {
		return counterAlert;
	}
	
	public int getContadorNotificacion() {
		return counterNotification;
	}
	
	public void setCategorias(List<Categoria> _categories) {
		this.categories = _categories;
	}
	
	public void setGastos(List<Gasto> _expenses) {
		this.expenses = _expenses;
	}
	
	public void setCuentas(List<CuentaCompartida> _accounts) {
		this.accounts = _accounts;
	}
	
	public void setAlertas(List<Alerta> _alerts) {
		this.alerts = _alerts;
	}
	
	public void setNotificaciones(List<Notificacion> _notifications) {
		this.notifications = _notifications;
	}
	
	public void setContadorCategoria(int _counterCategory) {
		this.counterCategory = _counterCategory;
	}
	
	public void setContadorGasto(int _counterExpense) {
		this.counterExpense = _counterExpense;
	}
	
	public void setContadorCuenta(int _counterAccount) {
		this.counterAccount = _counterAccount;
	}
	
	public void setContadorAlerta(int _counterAlert) {
		this.counterAlert = _counterAlert;
	}
	
	public void setContadorNotificacion(int _counterNotification) {
		this.counterNotification = _counterNotification;
	}
	
	/**
	 * Indica si hay datos cargados.
	 * @return true si hay al menos un elemento en alguna lista
	 */
	public boolean tieneDatos() {
		return !categories.isEmpty() || !expenses.isEmpty() ||
			   !accounts.isEmpty() || !alerts.isEmpty() ||
			   !notifications.isEmpty();
	}
	
	@Override
	public String toString() {
		return String.format("DatosAplicacion[categorias=%d, gastos=%d, cuentas=%d, notificaciones=%d]",
				categories.size(), expenses.size(), accounts.size(), 
				alerts.size(), notifications.size());
	}	
}