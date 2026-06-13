package es.um.tds.gestiongastos.modelo;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Representa un gasto registrado en el sistema.
 * Un gasto puede ser personal (sin cuenta compartida) o estar asociado a una cuenta compartida.
 */
public class Gasto {
	private static int contadorID = 0;
	
	private int id;
	private double cantidad;
	private LocalDateTime fecha;
	private String nota;
	private String metodoPago;
	private String moneda;
	private Categoria categoria;
	private Persona pagador;
	
	/**
	 * Constructor completo de Gasto
	 * @param _amount Cantidad de gasto (debe ser positiva)
	 * @param _date Fecha y hora del gasto
	 * @param _note Descripcion del gasto
	 * @param _paymentMethod Metodo de pago
	 * @param _coin Moneda del gasto
	 * @param _category Categoria del gasto (no puede ser null)
	 * @param _payer Persona que pagó (null si es gasto personal)
	 * @throws IllegalArgumentException si _amount <= 0 o si _category es null
	 */
	public Gasto(double _amount, LocalDateTime _date, String _note, String _paymentMethod, 
					String _coin, Categoria _category, Persona _payer) {
		validarCantidad(_amount);
		validarCategoria(_category);
		
		this.id = ++contadorID;
		this.cantidad = _amount;
		this.fecha = (_date != null) ? _date : LocalDateTime.now();
		this.nota = (_note != null) ? _note.trim() : "";
		this.metodoPago = (_paymentMethod != null) ? _paymentMethod.trim() : "";
		this.moneda = (_coin != null && !_coin.trim().isEmpty()) ? _coin.trim() : "EUR";
		this.categoria = _category;
		this.pagador = _payer;
	}
	
	/**
	 * Constructor simplificado para gasto personal
	 * @param _amount Cantidad del gasto
	 * @param _date Fecha del Gasto
	 * @param _category Categoría del gasto
	 * @param _date Descripción del gasto
	 */
	public Gasto(double _amount, LocalDateTime _date, Categoria _category, String _note){
		this(_amount, _date, _note, null, "EUR", _category, null);
	}
	
	/**
	 * Constructor mínimo para gasto personal
	 * @param _amount Cantidad del gasto
	 * @param _category Categoria del gasto
	 */
	public Gasto(double _amount, Categoria _category) {
		this(_amount, LocalDateTime.now(), null, null, "EUR", _category, null);
	}
	
	protected Gasto() {
	}
	
	private void validarCantidad(double _amount) {
		if (_amount <= 0) {
			throw new IllegalArgumentException("La cantidad debe ser mayor que 0.");
		}
	}
	
	private void validarCategoria(Categoria _category) {
		if (_category == null) {
			throw new IllegalArgumentException("La categoria no puede ser null");
		}
	}
	
	public int getID() {
		return id;
	}
	
	public double getCantidad() {
		return cantidad;
	}
	
	public LocalDateTime getFecha() {
		return fecha;
	}
	
	public Optional<String> getNota(){
		return Optional.ofNullable(nota).filter(n -> !n.trim().isEmpty());
	}
	
	public Optional<String> getMetodoPago(){
		return Optional.ofNullable(metodoPago).filter(m -> !m.trim().isEmpty());
	}
	
	public String getMoneda() {
		return moneda;
	}
	
	public Categoria getCategoria() {
		return categoria;
	}
	
	public Optional<Persona> getPagador(){
		return Optional.ofNullable(pagador);
	}
	
	@JsonIgnore
	public boolean isPersonal() {
		return pagador == null;
	}
	
	public void setCantidad(double _amount) {
		validarCantidad(_amount);
		this.cantidad = _amount;
	}
	
	public void setFecha(LocalDateTime _date) {
		if (_date != null) {
			this.fecha = _date;
		}
	}
	
	public void setNota(String _note) {
		this.nota = _note;
	}
	
	public void setMetodoPago (String _paymentMethod) {
		this.metodoPago = _paymentMethod;
	}
	
	public void setMoneda(String _coin) {
		this.moneda = (_coin != null && !_coin.trim().isEmpty()) ? _coin.trim() : "EUR";
	}
	
	public void setCategoria(Categoria _category) {
		validarCategoria(_category);
		this.categoria = _category;
	}
	
	public void setPagador(Persona _payer) {
		this.pagador = _payer;
	}
	
	protected void setID(int _ID) {
		this.id = _ID;
		if (_ID >= contadorID) {
			contadorID = _ID;
		}
	}
	
	public boolean actualizarGasto(Double _cantidad, LocalDateTime _fecha,
            String _nota, String _metodoPago, Categoria _categoria, String _moneda) {
		if (_cantidad != null && _cantidad <= 0) return false;
		if (_cantidad != null) setCantidad(_cantidad);
		if (_fecha != null) setFecha(_fecha);
		if (_nota != null) setNota(_nota);
		if (_metodoPago != null) setMetodoPago(_metodoPago);
		if (_categoria != null) setCategoria(_categoria);
		if (_moneda != null) setMoneda(_moneda);
		return true;
	}
	
	public static void resetContador() {
		contadorID = 0;
	}
	
	public static void setContador(int _counter) {
		contadorID = _counter;
	}
	
	@Override
	public boolean equals(Object _obj) {
		if (this == _obj) return true;
		if (_obj == null || getClass() != _obj.getClass()) return false;
		Gasto gasto = (Gasto) _obj;
		return id == gasto.id;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(id);
	}
	
	@Override
	public String toString() {
		String noteStr = getNota().orElse("sin nota");
		return String.format("Gasto[id=%d, cantidad=%.2f %s, fecha=%s, categoria=%s, nota=%s]",
								id, cantidad, moneda, fecha.toLocalDate(), categoria.getNombre(), noteStr);
	}
}
