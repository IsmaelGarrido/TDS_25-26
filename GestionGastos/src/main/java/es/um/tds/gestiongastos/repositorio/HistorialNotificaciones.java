package es.um.tds.gestiongastos.repositorio;

import es.um.tds.gestiongastos.modelo.Notificacion;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Historial de notificaciones.
 * Implementa el patrón Singleton para garantizar una única instancia.
 * Gestiona las notificaciones generadas por las alertas.
 */
public class HistorialNotificaciones {
	private static HistorialNotificaciones instance;
	
	private List<Notificacion> notifications;
	
	/**
     * Constructor privado (Singleton).
     */
    private HistorialNotificaciones() {
        this.notifications = new ArrayList<>();
    }
    
    /**
     * Obtiene la instancia única del historial.
     * @return Instancia del historial
     */
    public static synchronized HistorialNotificaciones getInstance() {
        if (instance == null) {
            instance = new HistorialNotificaciones();
        }
        return instance;
    }
    
    /**
     * Añade una notificación al historial.
     * @param _notification Notificación a añadir
     * @throws IllegalArgumentException si la notificación es null
     */
    public void addNotificacion(Notificacion _notification) {
        if (_notification == null) {
            throw new IllegalArgumentException("La notificación no puede ser null");
        }
        notifications.add(_notification);
    }
    
    /**
     * Obtiene todas las notificaciones.
     * @return Lista de todas las notificaciones
     */
    public List<Notificacion> getNotificaciones() {
        return new ArrayList<>(notifications);
    }
    
    /**
     * Obtiene todas las notificaciones ordenadas por fecha (más recientes primero).
     * @return Lista ordenada de notificaciones
     */
    public List<Notificacion> getNotificacionesOrdenadas() {
        return notifications.stream()
                .sorted(Comparator.comparing(Notificacion::getFecha).reversed())
                .collect(Collectors.toList());
    }
    
    /**
     * Busca una notificación por ID.
     * @param _ID ID de la notificación
     * @return Optional con la notificación si existe
     */
    public Optional<Notificacion> getNotificacionById(int _ID) {
        return notifications.stream()
                .filter(n -> n.getID() == _ID)
                .findFirst();
    }
    
    /**
     * Obtiene las notificaciones no leídas.
     * @return Lista de notificaciones no leídas
     */
    public List<Notificacion> getNoLeidas() {
        return notifications.stream()
                .filter(n -> !n.isRead())
                .collect(Collectors.toList());
    }
    
    /**
     * Obtiene las notificaciones leídas.
     * @return Lista de notificaciones leídas
     */
    public List<Notificacion> getNotificacionesLeidas() {
        return notifications.stream()
                .filter(Notificacion::isRead)
                .collect(Collectors.toList());
    }
    
    /**
     * Marca todas las notificaciones como leídas.
     */
    public void marcarTodasNotificacionesComoLeidas() {
        notifications.forEach(Notificacion::marcarLeida);
    }
    
    /**
     * Elimina una notificación.
     * @param _notification Notificación a eliminar
     * @return true si se eliminó correctamente
     */
    public boolean removeNotificacion(Notificacion _notification) {
        if (_notification == null) {
            return false;
        }
        return notifications.remove(_notification);
    }
    
    /**
     * Elimina todas las notificaciones leídas.
     * @return Número de notificaciones eliminadas
     */
    public int removeNotificacionesLeidas() {
        List<Notificacion> leidas = getNotificacionesLeidas();
        notifications.removeAll(leidas);
        return leidas.size();
    }
    
    /**
     * Obtiene el número total de notificaciones.
     * @return Número de notificaciones
     */
    public int count() {
        return notifications.size();
    }
    
    /**
     * Obtiene el número de notificaciones no leídas.
     * @return Número de no leídas
     */
    public int countNoLeidas() {
        return (int) notifications.stream()
                .filter(n -> !n.isRead())
                .count();
    }
    
    /**
     * Indica si hay notificaciones no leídas.
     * @return true si hay no leídas
     */
    public boolean hayNoLeidas() {
        return notifications.stream()
                .anyMatch(n -> !n.isRead());
    }
    
    
    /**
     * Reinicia el historial (para testing).
     */
    public void reset() {
        notifications.clear();
        Notificacion.resetContador();
    }
    
    /**
     * Reinicia la instancia del Singleton.
     */
    public static synchronized void resetInstance() {
        instance = null;
    }
}
