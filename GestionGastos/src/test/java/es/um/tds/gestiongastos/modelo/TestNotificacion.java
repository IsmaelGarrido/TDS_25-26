package es.um.tds.gestiongastos.modelo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para la clase Notificacion.
 */
@DisplayName("Tests de Notificacion")
class TestNotificacion {
    
    @BeforeEach
    void setUp() {
        Notificacion.resetContador();
    }
    
    @Test
    @DisplayName("Crear notificación con mensaje")
    void crearNotificacionConMensaje() {
        Notificacion notif = new Notificacion("Mensaje de prueba");
        assertEquals("Mensaje de prueba", notif.getMensaje());
        assertFalse(notif.isRead());
        assertNotNull(notif.getFecha());
    }
    
    @Test
    @DisplayName("Crear notificación con fecha personalizada")
    void crearNotificacionConFecha() {
        LocalDateTime date = LocalDateTime.of(2024, 6, 15, 10, 30);
        Notificacion notif = new Notificacion("Mensaje", date);
        assertEquals(date, notif.getFecha());
    }
    
    @Test
    @DisplayName("Error al crear notificación con mensaje null")
    void crearNotificacionMensajeNull() {
        assertThrows(IllegalArgumentException.class, () -> new Notificacion(null));
    }
    
    @Test
    @DisplayName("Error al crear notificación con mensaje vacío")
    void crearNotificacionMensajeVacio() {
        assertThrows(IllegalArgumentException.class, () -> new Notificacion(""));
        assertThrows(IllegalArgumentException.class, () -> new Notificacion("   "));
    }
    
    @Test
    @DisplayName("Marcar notificación como leída")
    void marcarComoLeida() {
        Notificacion notif = new Notificacion("Mensaje");
        assertFalse(notif.isRead());
        
        notif.marcarLeida();
        assertTrue(notif.isRead());
    }
    
    @Test
    @DisplayName("Marcar notificación como no leída")
    void marcarComoNoLeida() {
        Notificacion notif = new Notificacion("Mensaje");
        notif.marcarLeida();
        assertTrue(notif.isRead());
        
        notif.marcarNoLeida();
        assertFalse(notif.isRead());
    }
    
    @Test
    @DisplayName("IDs son únicos y secuenciales")
    void idsUnicos() {
        Notificacion n1 = new Notificacion("Notif 1");
        Notificacion n2 = new Notificacion("Notif 2");
        Notificacion n3 = new Notificacion("Notif 3");
        
        assertEquals(1, n1.getID());
        assertEquals(2, n2.getID());
        assertEquals(3, n3.getID());
    }
    
    @Test
    @DisplayName("Dos notificaciones con mismo ID son iguales")
    void notificacionesIgualesPorId() {
        Notificacion n1 = new Notificacion("Notif 1");
        Notificacion n2 = new Notificacion("Notif 2");
        
        assertNotEquals(n1, n2);
        assertEquals(n1, n1);
    }
}
