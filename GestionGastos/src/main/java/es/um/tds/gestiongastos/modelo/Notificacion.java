package es.um.tds.gestiongastos.modelo;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Representa una notificación generada por el sistema.
 * Las notificaciones se crean cuando se supera el tope de gasto configurado.
 */

public class Notificacion {
	private static int contadorID = 0;
	
	private int id;
	private String mensaje;
	private LocalDateTime fecha;
	private boolean leida;
	
	/**
	 * Constructor de Notificacion.
	 * @param _message Mensaje de la notificación (ni null ni vacío)
	 * @throws IllegalArgumentException si el mensaje es null o vacío.
	 */
	public Notificacion(String _message) {
		if (_message == null || _message.trim().isEmpty()) {
			throw new IllegalArgumentException("El mensaje no puede ser null ni vacío");
		}
		this.id = ++contadorID;
		this.mensaje = _message.trim();
		this.fecha = LocalDateTime.now();
		this.leida = false;
	}
	
	/**
	 * Constructor con fecha personalizada.
	 * @param _message Mensaje de la notificación.
	 * @param _date Fecha de la notificación.
	 */
	public Notificacion(String _message, LocalDateTime _date) {
		this(_message);
		if (_date != null) {
			this.fecha = _date;
		}
	}
	
	protected Notificacion() {
	}
	
	public int getID() {
		return id;
	}
	
	public String getMensaje() {
		return mensaje;
	}
	
	public LocalDateTime getFecha() {
		return fecha;
	}
	
	public boolean esLeida() {
		return leida;
	}
	
	public void marcarLeida() {
		this.leida = true;
	}

	public void marcarNoLeida() {
		this.leida = false;
	}
	
	protected void setID(int _id) {
		this.id = _id;
		if (_id >= contadorID) {
			contadorID = _id;
		}
	}
	
	protected void setMensaje(String _message) {
		this.mensaje = _message;
	}
	
	protected void setFecha(LocalDateTime _date) {
		this.fecha = _date;
	}
	
	protected void setLeida(boolean _read) {
		this.leida = _read;
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
		Notificacion notificacion = (Notificacion) _obj;
		return id == notificacion.id; 
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(id);
	}
	
	@Override
	public String toString() {
		return String.format("[%s] %s %s", fecha.toLocalDate(), mensaje, leida ? "(leída)" : "(no leída)");
	}
}

