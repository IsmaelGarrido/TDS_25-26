package es.um.tds.gestiongastos.modelo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para el enum TipoAlerta.
 */
@DisplayName("Tests de TipoAlerta")
class TestTipoAlerta {
    
    @Test
    @DisplayName("TipoAlerta SEMANAL tiene descripción correcta")
    void semanalDescripcion() {
        assertEquals("Semanal", TipoAlerta.SEMANAL.getDescripcion());
    }
    
    @Test
    @DisplayName("TipoAlerta MENSUAL tiene descripción correcta")
    void mensualDescripcion() {
        assertEquals("Mensual", TipoAlerta.MENSUAL.getDescripcion());
    }
    
    @Test
    @DisplayName("TipoAlerta SEMANAL tiene 7 días")
    void semanalDias() {
        assertEquals(7, TipoAlerta.SEMANAL.getDias());
    }
    
    @Test
    @DisplayName("TipoAlerta MENSUAL tiene 30 días")
    void mensualDias() {
        assertEquals(30, TipoAlerta.MENSUAL.getDias());
    }
    
    @Test
    @DisplayName("toString devuelve la descripción")
    void toStringDevuelveDescripcion() {
        assertEquals("Semanal", TipoAlerta.SEMANAL.toString());
        assertEquals("Mensual", TipoAlerta.MENSUAL.toString());
    }
    
    @Test
    @DisplayName("Existen exactamente 2 tipos de alerta")
    void existenDosTipos() {
        assertEquals(2, TipoAlerta.values().length);
    }
    
    @Test
    @DisplayName("valueOf funciona correctamente")
    void valueOfFunciona() {
        assertEquals(TipoAlerta.SEMANAL, TipoAlerta.valueOf("SEMANAL"));
        assertEquals(TipoAlerta.MENSUAL, TipoAlerta.valueOf("MENSUAL"));
    }
}