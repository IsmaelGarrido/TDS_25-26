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
	
	private static RepositorioCategorias instance;
	
	private List<Categoria> categories;
	
	/**
	 * Contructor privado (Singleton).
	 */
	private RepositorioCategorias() {
		this.categories = new ArrayList<>();
		inicializarCategoriasPredefinidas();
	}
	
	public static synchronized RepositorioCategorias getInstance() {
		if (instance == null) {
			instance = new RepositorioCategorias();
		}
		
		return instance;
	}
	
	private void inicializarCategoriasPredefinidas() {
		categories.add(new Categoria("Alimentación", true));
        categories.add(new Categoria("Transporte Público", true));
        categories.add(new Categoria("Ocio", true));
        categories.add(new Categoria("Gasolina", true));
        categories.add(new Categoria("Regalos", true));
        categories.add(new Categoria("Reparaciones", true));
        categories.add(new Categoria("Gastos Médicos", true));
        categories.add(new Categoria("Hogar", true));
        categories.add(new Categoria("Ropa", true));
        categories.add(new Categoria("Entretenimiento", true));
        categories.add(new Categoria("Educación", true));
        categories.add(new Categoria("Otros", true));
	}
	
	public List<Categoria> getTodasCategorias(){
		return new ArrayList<>(categories);
	}
	
	public Optional<Categoria> getCategoriaByID(int _ID){
		return categories.stream()
				.filter(c -> c.getID() == _ID)
				.findFirst();
	}
	
	public Optional<Categoria> getCategoriaByNombre(String _name){
		if (_name == null) return Optional.empty();
		
		return categories.stream()
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
		
		categories.add(_category);
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
		
		return categories.remove(_category);
	}
	
	public boolean deleteCategoriaByID(int _ID) {
		Optional<Categoria> category = getCategoriaByID(_ID);
		return category.map(this::deleteCategoria).orElse(false);
	}
	
	public List<Categoria> getCategoriasPredefinidas(){
		return categories.stream()
				.filter(Categoria::isBase)
				.collect(Collectors.toList());
	}
	
	public List<Categoria> getCategoriasPersonalizadas(){
		return categories.stream()
				.filter(c -> !c.isBase())
				.collect(Collectors.toList());
	}
	
	public boolean existsByNombre(String _name) {
		return getCategoriaByNombre(_name).isPresent();
	}
	
	public int countCategorias() {
		return categories.size();
	}
	
	public void reset() {
		categories.clear();
		Categoria.resetContador();
		inicializarCategoriasPredefinidas();
	}
	
	public static synchronized void resetInstance() {
		instance = null;
	}
}
