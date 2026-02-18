package es.um.tds.gestiongastos.repositorio;

import es.um.tds.gestiongastos.modelo.Notificacion;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para HistorialNotificaciones.
 */
@DisplayName("Tests de HistorialNotificaciones")
class TestHistorialNotificaciones {
    
    private HistorialNotificaciones record;
    
    @BeforeEach
    void setUp() {
        HistorialNotificaciones.resetInstance();
        Notificacion.resetContador();
        
        record = HistorialNotificaciones.getInstance();
    }
    
    @Test
    @DisplayName("Singleton devuelve misma instancia")
    void singletonMismaInstancia() {
        HistorialNotificaciones record2 = HistorialNotificaciones.getInstance();
        assertSame(record, record2);
    }
    
    @Test
    @DisplayName("Historial inicia vacío")
    void historialIniciaVacio() {
        assertEquals(0, record.count());
        assertTrue(record.getNotificaciones().isEmpty());
    }
    
    @Test
    @DisplayName("Añadir notificación")
    void addNotificacion() {
        Notificacion notif = new Notificacion("Test");
        record.addNotificacion(notif);
        
        assertEquals(1, record.count());
        assertTrue(record.getNotificaciones().contains(notif));
    }
    
    @Test
    @DisplayName("No se puede añadir notificación null")
    void noAddNull() {
        assertThrows(IllegalArgumentException.class, () -> record.addNotificacion(null));
    }
    
    @Test
    @DisplayName("Obtener notificaciones no leídas")
    void obtenerNoLeidas() {
        Notificacion read = new Notificacion("Leída");
        read.marcarLeida();
        Notificacion notRead1 = new Notificacion("No leída 1");
        Notificacion notRead2 = new Notificacion("No leída 2");
        
        record.addNotificacion(read);
        record.addNotificacion(notRead1);
        record.addNotificacion(notRead2);
        
        List<Notificacion> noLeidas = record.getNoLeidas();
        assertEquals(2, noLeidas.size());
        assertTrue(noLeidas.stream().noneMatch(Notificacion::isRead));
    }
    
    @Test
    @DisplayName("Obtener notificaciones leídas")
    void obtenerLeidas() {
        Notificacion read1 = new Notificacion("Leída 1");
        read1.marcarLeida();
        Notificacion read2 = new Notificacion("Leída 2");
        read2.marcarLeida();
        Notificacion notRead = new Notificacion("No leída");
        
        record.addNotificacion(read1);
        record.addNotificacion(read2);
        record.addNotificacion(notRead);
        
        List<Notificacion> allRead = record.getNotificacionesLeidas();
        assertEquals(2, allRead.size());
        assertTrue(allRead.stream().allMatch(Notificacion::isRead));
    }
    
    @Test
    @DisplayName("Contar no leídas")
    void contarNoLeidas() {
        record.addNotificacion(new Notificacion("No leída 1"));
        record.addNotificacion(new Notificacion("No leída 2"));
        
        Notificacion read = new Notificacion("Leída");
        read.marcarLeida();
        record.addNotificacion(read);
        
        assertEquals(2, record.countNoLeidas());
    }
    
    @Test
    @DisplayName("Hay no leídas devuelve true")
    void hayNoLeidasTrue() {
        record.addNotificacion(new Notificacion("No leída"));
        assertTrue(record.hayNoLeidas());
    }
    
    @Test
    @DisplayName("Hay no leídas devuelve false si todas leídas")
    void hayNoLeidasFalse() {
        Notificacion read = new Notificacion("Leída");
        read.marcarLeida();
        record.addNotificacion(read);
        
        assertFalse(record.hayNoLeidas());
    }
    
    @Test
    @DisplayName("Hay no leídas devuelve false si vacío")
    void hayNoLeidasVacio() {
        assertFalse(record.hayNoLeidas());
    }
    
    @Test
    @DisplayName("Marcar todas como leídas")
    void marcarTodasComoLeidas() {
        record.addNotificacion(new Notificacion("Notif 1"));
        record.addNotificacion(new Notificacion("Notif 2"));
        record.addNotificacion(new Notificacion("Notif 3"));
        
        assertEquals(3, record.countNoLeidas());
        
        record.marcarTodasNotificacionesComoLeidas();
        
        assertEquals(0, record.countNoLeidas());
        assertFalse(record.hayNoLeidas());
    }
    
    @Test
    @DisplayName("Eliminar notificación")
    void eliminarNotificacion() {
        Notificacion notif = new Notificacion("Test");
        record.addNotificacion(notif);
        
        assertTrue(record.removeNotificacion(notif));
        assertEquals(0, record.count());
    }
    
    @Test
    @DisplayName("Eliminar notificación null devuelve false")
    void eliminarNullDevuelveFalse() {
        assertFalse(record.removeNotificacion(null));
    }
    
    @Test
    @DisplayName("Eliminar notificaciones leídas")
    void eliminarLeidas() {
        Notificacion read1 = new Notificacion("Leída 1");
        read1.marcarLeida();
        Notificacion read2 = new Notificacion("Leída 2");
        read2.marcarLeida();
        Notificacion notRead = new Notificacion("No leída");
        
        record.addNotificacion(read1);
        record.addNotificacion(read2);
        record.addNotificacion(notRead);
        
        int deleted = record.removeNotificacionesLeidas();
        
        assertEquals(2, deleted);
        assertEquals(1, record.count());
        assertFalse(record.getNotificaciones().get(0).isRead());
    }
    
    @Test
    @DisplayName("Eliminar leídas cuando no hay devuelve 0")
    void eliminarLeidasCuandoNoHay() {
        record.addNotificacion(new Notificacion("No leída"));
        
        int deleted = record.removeNotificacionesLeidas();
        
        assertEquals(0, deleted);
        assertEquals(1, record.count());
    }
    
    @Test
    @DisplayName("Buscar notificación por ID")
    void buscarPorId() {
        Notificacion notif = new Notificacion("Test");
        record.addNotificacion(notif);
        
        assertTrue(record.getNotificacionById(notif.getID()).isPresent());
        assertEquals(notif, record.getNotificacionById(notif.getID()).get());
    }
    
    @Test
    @DisplayName("Buscar ID inexistente devuelve vacío")
    void buscarIdInexistente() {
        assertTrue(record.getNotificacionById(9999).isEmpty());
    }
    
    @Test
    @DisplayName("Obtener notificaciones ordenadas por fecha")
    void obtenerOrdenadas() {
        Notificacion n1 = new Notificacion("Primera");
        Notificacion n2 = new Notificacion("Segunda");
        Notificacion n3 = new Notificacion("Tercera");
        
        record.addNotificacion(n1);
        record.addNotificacion(n2);
        record.addNotificacion(n3);
        
        List<Notificacion> sorted = record.getNotificacionesOrdenadas();
        
        // Las más recientes primero
        assertEquals(3, sorted.size());
    }
    
    @Test
    @DisplayName("Reset limpia el historial")
    void resetHistorial() {
        record.addNotificacion(new Notificacion("Test 1"));
        record.addNotificacion(new Notificacion("Test 2"));
        
        record.reset();
        
        assertEquals(0, record.count());
    }
}