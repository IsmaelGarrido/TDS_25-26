package es.um.tds.gestiongastos.modelo;

import java.util.Objects;

public class Persona {

	private String name;
	
	/**
	 * Constructor de Persona.
	 * @param name Nombre de la persona
	 * @throws IllegalArgumentException
	 */
	public Persona(String _name) {
		if(_name == null || name.trim().isEmpty()) {
			throw new IllegalArgumentException("El nombre no puede ser null ni vacio");
		}
		this.name = _name.trim();
	}
	
	protected Persona() {
	}
	
	public String getNombre() {
		return this.name;
	}
	
	protected void setNombre(String _name) {
		this.name = _name;
	}
	
	@Override
	public boolean equals(Object _obj) {
		if (this == _obj) return true;
		if (_obj == null || getClass() != _obj.getClass()) return false;
		Persona persona = (Persona) _obj;
		return Objects.equals(name,  persona.name);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(name);
	}
	
	@Override
	public String toString() {
		return name;
	}
}
