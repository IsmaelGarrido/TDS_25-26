package es.um.tds.gestiongastos.modelo;

import java.util.Objects;

/**
 * Representa una categoría de gasto.
 * Las categorías pueden ser predefinidas o personalizadas.
 */

public class Categoria {
	private static int contadorID = 0;
	
	private int id;
	private String nombre;
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
		this.id = ++contadorID;
		this.nombre = _name.trim();
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
		return nombre;
	}
	
	public boolean isBase() {
		return base;
	}
	
	/**
	 * Permite modificar el nombre de categorias no base
	 * @param _name Nuevo nombre
	 */
	public void setNombre(String _name) {
		this.nombre = _name.trim();
	}
	
	/**
	 * Intenta cambiar nombre de categoría.
	 * @param _name
	 * @return 	0 en cambio exitoso
	 * 		   -1 si nombre inválido (null o vacío)
	 *         -2 si la categoría no se puede modificar 
	 */
	public int cambiarNombre(String _name) {
		if (base) {
			return -2;
		}
		if (_name == null || _name.trim().isEmpty()) {
			return -1;
		}
		setNombre(_name);
		return 0;
	}
	
	protected void setID(int _id) {
		this.id = _id;
		if (_id >= contadorID) {
			contadorID = _id;
		}
	}
	
	protected void setBase(boolean _base) {
		this.base = _base;
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
		Categoria categoria = (Categoria) _obj;
		return id == categoria.id;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(id);
	}
	
	@Override 
	public String toString() {
		return nombre + (base ? " (predefinida)" : "");
	}
}
