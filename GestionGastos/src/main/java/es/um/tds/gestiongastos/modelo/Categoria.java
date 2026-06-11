package es.um.tds.gestiongastos.modelo;

import java.util.Objects;

/**
 * Representa una categoría de gasto.
 * Las categorías pueden ser predefinidas o personalizadas.
 */

public class Categoria {
	private static int counterID = 0;
	
	private int id;
	private String name;
	private boolean base;
	
	/**
	 * Constructor de Categoria
	 * @param name Nombre de la categoría (no null ni vacío)
	 * @param base true si es una categoría base del sistema
	 * @throws IllegalArgumentException si el nombre es null o vacio
	 */
	public Categoria(String _name, boolean _base) {
		if (_name == null || _name.trim().isEmpty()) {
			throw new IllegalArgumentException("El nombre no puede ser null ni vacío");
		}
		this.id = ++counterID;
		this.name = _name.trim();
		this.base = _base;
	}
	
	/**
	 * Constructor de Categoria no predefinida
	 * @param _name nombre de la categoría
	 */
	public Categoria(String _name) {
		this(_name, false);
	}
	
	protected Categoria() {
	}
	
	public int getID() {
		return id;
	}
	
	public String getNombre() {
		return name;
	}
	
	public boolean isBase() {
		return base;
	}
	
	/**
	 * Permite modificar el nombre de categorias no base
	 * @param _name Nuevo nombre
	 * @throws IllegalStateException si la categoria no es base
	 * @throws IllegalArgumentException si el nombre es null o vacío
	 */
	public void setNombre(String _name) {
		if (base) {
			throw new IllegalStateException("No se puede modificar categorías predefinidas");
		}
		if (_name == null || _name.trim().isEmpty()) {
			throw new IllegalArgumentException("El nombre no puede ser null ni vacío");
		}
		this.name = _name.trim();
	}
	
	protected void setID(int _id) {
		this.id = _id;
		if (_id >= counterID) {
			counterID = _id;
		}
	}
	
	protected void setBase(boolean _base) {
		this.base = _base;
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
		Categoria categoria = (Categoria) _obj;
		return id == categoria.id;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(id);
	}
	
	@Override 
	public String toString() {
		return name + (base ? " (predefinida)" : "");
	}
}
