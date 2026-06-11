package es.um.tds.gestiongastos.modelo;

import java.util.Objects;

public class Persona {

	private String nombre;
	
	/**
	 * Constructor de Persona.
	 * @param name Nombre de la persona
	 * @throws IllegalArgumentException
	 */
	public Persona(String _name) {
		if(_name == null || _name.trim().isEmpty()) {
			throw new IllegalArgumentException("El nombre no puede ser null ni vacio");
		}
		this.nombre = _name.trim();
	}
	
	protected Persona() {
	}
	
	public String getNombre() {
		return this.nombre;
	}
	
	protected void setNombre(String _name) {
		this.nombre = _name;
	}
	
	@Override
	public boolean equals(Object _obj) {
		if (this == _obj) return true;
		if (_obj == null || getClass() != _obj.getClass()) return false;
		Persona persona = (Persona) _obj;
		return Objects.equals(nombre,  persona.nombre);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(nombre);
	}
	
	@Override
	public String toString() {
		return nombre;
	}
}
