package es.um.tds.gestiongastos.repositorio;

import es.um.tds.gestiongastos.modelo.Categoria;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Repositorio de categorías.
 * Implementa el patrón Singleton para garantizar una única instancia.
 * Gestiona las categorías predefinidas y personalizadas.
 */

public class RepositorioCategorias {
	
	private static RepositorioCategorias instancia;
	
	private List<Categoria> categorias;
	
	/**
	 * Contructor privado (Singleton).
	 */
	private RepositorioCategorias() {
		this.categorias = new ArrayList<>();
		inicializarCategoriasPredefinidas();
	}
	
	public static synchronized RepositorioCategorias getInstance() {
		if (instancia == null) {
			instancia = new RepositorioCategorias();
		}
		
		return instancia;
	}
	
	private void inicializarCategoriasPredefinidas() {
		categorias.add(new Categoria("Alimentación", true));
        categorias.add(new Categoria("Transporte Público", true));
        categorias.add(new Categoria("Ocio", true));
        categorias.add(new Categoria("Gasolina", true));
        categorias.add(new Categoria("Regalos", true));
        categorias.add(new Categoria("Reparaciones", true));
        categorias.add(new Categoria("Gastos Médicos", true));
        categorias.add(new Categoria("Hogar", true));
        categorias.add(new Categoria("Ropa", true));
        categorias.add(new Categoria("Entretenimiento", true));
        categorias.add(new Categoria("Educación", true));
        categorias.add(new Categoria("Otros", true));
	}
	
	public List<Categoria> getTodasCategorias(){
		return new ArrayList<>(categorias);
	}
	
	public Optional<Categoria> getCategoriaByID(int _ID){
		return categorias.stream()
				.filter(c -> c.getID() == _ID)
				.findFirst();
	}
	
	public Optional<Categoria> getCategoriaByNombre(String _name){
		if (_name == null) return Optional.empty();
		
		return categorias.stream()
				.filter(c -> c.getNombre().equalsIgnoreCase(_name.trim()))
				.findFirst();
	}
	
	/**
	 * Guarda una categoria (añade si es nueva).
	 * @param _category Categoria a guardar
	 * @throws IllegalArgumentException si la categoría es null o ya existe una.
	 */
	public void addCategoria(Categoria _category) {
		if (_category == null) {
            throw new IllegalArgumentException("La categoría no puede ser null");
		}
		
		if (getCategoriaByID(_category.getID()).isPresent()) {
			return;
		}
		
		if (getCategoriaByNombre(_category.getNombre()).isPresent()) {
			throw new IllegalArgumentException("Ya existe una categoría con el nombre: " + _category.getNombre());
		}
		
		categorias.add(_category);
	}
	
	/**
	 * Intenta cambiar el nombre de una categoría
	 * @param _category Categoría a modificar
	 * @param _name Nuevo nombre
	 * @return 0 en cambio exitoso
	 *        -1 si es nombre null o vacío
	 *        -2 si es categoría predefinida
	 *        -3 si no existe la categoría
	 *        -4 si el nombre ya existe
	 */
	public int editNameCategoria(Categoria _category, String _name) {
		if (_category == null || getCategoriaByID(_category.getID()).isEmpty()) {
			return -3;
		}
		if (existsByNombre(_name)) {
			return -4;
		}
		
		return _category.cambiarNombre(_name);
	}
	
	/**
	 * Elimina una categoría.
	 * Solo se pueden eliminar categorías no predefinidas.
	 * @param _category Categoria a eliminar.
	 * @return true si se eliminó correctamente.
	 * @throws IllegalStateException si se intenta eliminar una categoria predefinida.
	 */
	public boolean deleteCategoria(Categoria _category) {
		if(_category == null) {
			return false;
		}
		
		if(_category.isBase()) {
			throw new IllegalStateException("No se puede eliminar una categoría predefinida.");
		}
		
		return categorias.remove(_category);
	}
	
	public boolean deleteCategoriaByID(int _ID) {
		Optional<Categoria> category = getCategoriaByID(_ID);
		return category.map(this::deleteCategoria).orElse(false);
	}
	
	public List<Categoria> getCategoriasPredefinidas(){
		return categorias.stream()
				.filter(Categoria::isBase)
				.collect(Collectors.toList());
	}
	
	public List<Categoria> getCategoriasPersonalizadas(){
		return categorias.stream()
				.filter(c -> !c.isBase())
				.collect(Collectors.toList());
	}
	
	public boolean existsByNombre(String _name) {
		return getCategoriaByNombre(_name).isPresent();
	}
	
	public int countCategorias() {
		return categorias.size();
	}
	
	public void reset() {
		categorias.clear();
		Categoria.resetContador();
		inicializarCategoriasPredefinidas();
	}
	
	public static synchronized void resetInstance() {
		instancia = null;
	}
}
