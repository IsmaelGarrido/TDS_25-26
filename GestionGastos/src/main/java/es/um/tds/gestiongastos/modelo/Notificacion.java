package es.um.tds.gestiongastos.modelo;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Representa una notificación generada por el sistema.
 * Las notificaciones se crean cuando se supera el tope de gasto configurado.
 */

public class Notificacion {
	private static int counterID = 0;
	
	private int id;
	private String message;
	private LocalDateTime date;
	private boolean read;
	
	/**
	 * Constructor de Notificacion.
	 * @param _message Mensaje de la notificación (ni null ni vacío)
	 * @throws IllegalArgumentException si el mensaje es null o vacío.
	 */
	public Notificacion(String _message) {
		if (_message == null || _message.trim().isEmpty()) {
			throw new IllegalArgumentException("El mensaje no puede ser null ni vacío");
		}
		this.id = ++counterID;
		this.message = _message.trim();
		this.date = LocalDateTime.now();
		this.read = false;
	}
	
	/**
	 * Constructor con fecha personalizada.
	 * @param _message Mensaje de la notificación.
	 * @param _date Fecha de la notificación.
	 */
	public Notificacion(String _message, LocalDateTime _date) {
		this(_message);
		if (_date != null) {
			this.date = _date;
		}
	}
	
	protected Notificacion() {
	}
	
	public int getID() {
		return id;
	}
	
	public String getMensaje() {
		return message;
	}
	
	public LocalDateTime getFecha() {
		return date;
	}
	
	public boolean isRead() {
		return read;
	}
	
	public void marcarLeida() {
		this.read = true;
	}

	public void marcarNoLeida() {
		this.read = false;
	}
	
	protected void setID(int _id) {
		this.id = _id;
		if (_id >= counterID) {
			counterID = _id;
		}
	}
	
	protected void setMensaje(String _message) {
		this.message = _message;
	}
	
	protected void setFecha(LocalDateTime _date) {
		this.date = _date;
	}
	
	protected void setLeida(boolean _read) {
		this.read = _read;
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
		Notificacion notificacion = (Notificacion) _obj;
		return id == notificacion.id; 
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(id);
	}
	
	@Override
	public String toString() {
		return String.format("[%s] %s %s", date.toLocalDate(), message, read ? "(leída)" : "(no leída)");
	}
}

