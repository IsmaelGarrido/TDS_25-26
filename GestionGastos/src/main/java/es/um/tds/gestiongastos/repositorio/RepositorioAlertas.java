package es.um.tds.gestiongastos.repositorio;

import es.um.tds.gestiongastos.modelo.Alerta;
import es.um.tds.gestiongastos.modelo.Categoria;
import es.um.tds.gestiongastos.modelo.TipoAlerta;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Repositorio de alertas.
 * Implementa patrón Singleton para garantizar una única instancia.
 */
public class RepositorioAlertas {
	
	private static RepositorioAlertas instancia;
	
	private List<Alerta> alertas;
	
	/**
	 * Constructor privado (Singleton)
	 */
	private RepositorioAlertas() {
		this.alertas = new ArrayList<>();
	}
	
	/**
     * Obtiene la instancia única del repositorio.
     * @return Instancia del repositorio
     */
	public static synchronized RepositorioAlertas getInstance() {
		if (instancia == null) {
			instancia = new RepositorioAlertas();
		}
		return instancia;
	}
	
	/**
     * Obtiene todas las alertas.
     * @return Lista de todas las alertas
     */
	public List<Alerta> getTotalAlertas(){
		return new ArrayList<>(alertas);
	}
	
	/**
     * Busca una alerta por ID.
     * @param _ID ID de la alerta
     * @return Optional con la alerta si existe
     */
    public Optional<Alerta> getAlertaByID(int _ID) {
        return alertas.stream()
                .filter(a -> a.getID() == _ID)
                .findFirst();
    }
    
    /**
     * Guarda una alerta (añade si es nueva).
     * @param _alert Alerta a guardar
     * @throws IllegalArgumentException si la alerta es null
     */
    public void addAlerta(Alerta _alert) {
        if (_alert == null) {
            throw new IllegalArgumentException("La alerta no puede ser null");
        }
        
        // Verificar si ya existe
        if (getAlertaByID(_alert.getID()).isEmpty()) {
            alertas.add(_alert);
        }
    }
    
    /**
     * Elimina una alerta.
     * @param _alert Alerta a eliminar
     * @return true si se eliminó correctamente
     */
    public boolean removeAlerta(Alerta _alert) {
        if (_alert == null) {
            return false;
        }
        return alertas.remove(_alert);
    }
    
    /**
     * Elimina una alerta por ID.
     * @param _ID ID de la alerta
     * @return true si se eliminó correctamente
     */
    public boolean removeAlertaByID(int _ID) {
        Optional<Alerta> alerta = getAlertaByID(_ID);
        return alerta.map(this::removeAlerta).orElse(false);
    }
    
    /**
     * Obtiene solo las alertas activas.
     * @return Lista de alertas activas
     */
    public List<Alerta> getAlertasActivas() {
        return alertas.stream()
                .filter(Alerta::getActiva)
                .collect(Collectors.toList());
    }
    
    /**
     * Busca alertas por tipo.
     * @param _type Tipo de alerta (SEMANAL o MENSUAL)
     * @return Lista de alertas del tipo especificado
     */
    public List<Alerta> getAlertasByTipo(TipoAlerta _type) {
        if (_type == null) {
            return new ArrayList<>();
        }
        return alertas.stream()
                .filter(a -> a.getTipoAlerta() == _type)
                .collect(Collectors.toList());
    }
	
    /**
     * Busca alertas por categoría.
     * @param _category Categoría a buscar (null para alertas generales)
     * @return Lista de alertas de esa categoría
     */
    public List<Alerta> getAlertasByCategoria(Categoria _category) {
        return alertas.stream()
                .filter(a -> {
                    if (_category == null) {
                        return a.esGeneral();
                    }
                    return a.getCategoria()
                            .map(c -> c.equals(_category))
                            .orElse(false);
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Busca alertas generales (sin categoría específica).
     * @return Lista de alertas generales
     */
    public List<Alerta> getAlertasGenerales() {
        return alertas.stream()
                .filter(Alerta::esGeneral)
                .collect(Collectors.toList());
    }
    
    /**
     * Obtiene el número total de alertas.
     * @return Número de alertas
     */
    public int count() {
        return alertas.size();
    }
    
    /**
     * Obtiene el número de alertas activas.
     * @return Número de alertas activas
     */
    public int countActivas() {
        return (int) alertas.stream()
                .filter(Alerta::getActiva)
                .count();
    }
    
   
    /**
     * Reinicia el repositorio.
     */
    public void reset() {
        alertas.clear();
        Alerta.resetContador();
    }
    
    /**
     * Reinicia la instancia del Singleton.
     */
    public static synchronized void resetInstance() {
        instancia = null;
    }
}
