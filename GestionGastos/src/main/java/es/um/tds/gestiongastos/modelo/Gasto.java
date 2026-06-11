package es.um.tds.gestiongastos.modelo;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

/**
 * Representa un gasto registrado en el sistema.
 * Un gasto puede ser personal (sin cuenta compartida) o estar asociado a una cuenta compartida.
 */
public class Gasto {
	private static int counterID = 0;
	
	private int id;
	private double amount;
	private LocalDateTime date;
	private String note;
	private String paymentMethod;
	private String coin;
	private Categoria category;
	private Persona payer;
	
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
		
		this.id = ++counterID;
		this.amount = _amount;
		this.date = (_date != null) ? _date : LocalDateTime.now();
		this.note = (_note != null) ? _note.trim() : "";
		this.paymentMethod = (_paymentMethod != null) ? _paymentMethod.trim() : "";
		this.coin = (_coin != null && !_coin.trim().isEmpty()) ? _coin.trim() : "EUR";
		this.category = _category;
		this.payer = _payer;
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
		return amount;
	}
	
	public LocalDateTime getFecha() {
		return date;
	}
	
	public Optional<String> getNota(){
		return Optional.ofNullable(note).filter(n -> !n.trim().isEmpty());
	}
	
	public Optional<String> getMetodoPago(){
		return Optional.ofNullable(paymentMethod).filter(m -> !m.trim().isEmpty());
	}
	
	public String getMoneda() {
		return coin;
	}
	
	public Categoria getCategoria() {
		return category;
	}
	
	public Optional<Persona> getPagador(){
		return Optional.ofNullable(payer);
	}
	
	public boolean isPersonal() {
		return payer == null;
	}
	
	public void setCantidad(double _amount) {
		validarCantidad(_amount);
		this.amount = _amount;
	}
	
	public void setFecha(LocalDateTime _date) {
		if (_date != null) {
			this.date = _date;
		}
	}
	
	public void setNota(String _note) {
		this.note = _note;
	}
	
	public void setMetodoPago (String _paymentMethod) {
		this.paymentMethod = _paymentMethod;
	}
	
	public void setMoneda(String _coin) {
		this.coin = (_coin != null && !_coin.trim().isEmpty()) ? _coin.trim() : "EUR";
	}
	
	public void setCategoria(Categoria _category) {
		validarCategoria(_category);
		this.category = _category;
	}
	
	public void setPagador(Persona _payer) {
		this.payer = _payer;
	}
	
	protected void setID(int _ID) {
		this.id = _ID;
		if (_ID >= counterID) {
			counterID = _ID;
		}
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
								id, amount, coin, date.toLocalDate(), category.getNombre(), noteStr);
	}
}
