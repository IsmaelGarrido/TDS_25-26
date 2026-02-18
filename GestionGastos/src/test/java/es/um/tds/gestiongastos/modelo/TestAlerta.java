package es.um.tds.gestiongastos.modelo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para la clase Alerta.
 */
@DisplayName("Tests de Alerta")
class TestAlerta {
    
    private Categoria category;
    private List<Gasto> expenses;
    
    @BeforeEach
    void setUp() {
        Categoria.resetContador();
        Gasto.resetContador();
        Alerta.resetContador();
        Notificacion.resetContador();
        
        category = new Categoria("Ocio", false);
        
        // Crear gastos de prueba (total: 105€)
        expenses = Arrays.asList(
            new Gasto(50.0, LocalDateTime.now(), category, "Gasto 1"),
            new Gasto(30.0, LocalDateTime.now(), category, "Gasto 2"),
            new Gasto(25.0, LocalDateTime.now(), category, "Gasto 3")
        );
    }
    
    @Test
    @DisplayName("Crear alerta semanal general")
    void crearAlertaSemanalGeneral() {
        Alerta alert = new Alerta(100.0, TipoAlerta.SEMANAL);
        
        assertEquals(100.0, alert.getMaxGasto());
        assertEquals(TipoAlerta.SEMANAL, alert.getTipoAlerta());
        assertTrue(alert.isActiva());
        assertTrue(alert.esGeneral());
        assertTrue(alert.getCategoria().isEmpty());
    }
    
    @Test
    @DisplayName("Crear alerta mensual con categoría")
    void crearAlertaMensualConCategoria() {
        Alerta alert = new Alerta(200.0, TipoAlerta.MENSUAL, category);
        
        assertEquals(200.0, alert.getMaxGasto());
        assertEquals(TipoAlerta.MENSUAL, alert.getTipoAlerta());
        assertFalse(alert.esGeneral());
        assertEquals(category, alert.getCategoria().orElse(null));
    }
    
    @Test
    @DisplayName("Error al crear alerta con tope cero")
    void crearAlertaTopeCero() {
        assertThrows(IllegalArgumentException.class, 
            () -> new Alerta(0.0, TipoAlerta.SEMANAL));
    }
    
    @Test
    @DisplayName("Error al crear alerta con tope negativo")
    void crearAlertaTopeNegativo() {
        assertThrows(IllegalArgumentException.class, 
            () -> new Alerta(-100.0, TipoAlerta.SEMANAL));
    }
    
    @Test
    @DisplayName("Error al crear alerta sin tipo")
    void crearAlertaSinTipo() {
        assertThrows(IllegalArgumentException.class, 
            () -> new Alerta(100.0, null));
    }
    
    @Test
    @DisplayName("Calcular gasto actual")
    void calcularGastoActual() {
        Alerta alert = new Alerta(200.0, TipoAlerta.SEMANAL);
        double currentExpense = alert.calcularGastoActual(expenses);
        assertEquals(105.0, currentExpense, 0.01);
    }
    
    @Test
    @DisplayName("Calcular gasto actual con lista vacía")
    void calcularGastoActualListaVacia() {
        Alerta alert = new Alerta(100.0, TipoAlerta.SEMANAL);
        assertEquals(0.0, alert.calcularGastoActual(Collections.emptyList()));
    }
    
    @Test
    @DisplayName("Calcular gasto actual con lista null")
    void calcularGastoActualListaNull() {
        Alerta alert = new Alerta(100.0, TipoAlerta.SEMANAL);
        assertEquals(0.0, alert.calcularGastoActual(null));
    }
    
    @Test
    @DisplayName("Verificar alerta superada")
    void verificarAlertaSuperada() {
        Alerta alert = new Alerta(100.0, TipoAlerta.SEMANAL);
        // Gastos suman 105€, supera el tope de 100€
        assertTrue(alert.verificar(expenses));
    }
    
    @Test
    @DisplayName("Verificar alerta no superada")
    void verificarAlertaNoSuperada() {
        Alerta alert = new Alerta(200.0, TipoAlerta.SEMANAL);
        // Gastos suman 105€, no supera el tope de 200€
        assertFalse(alert.verificar(expenses));
    }
    
    @Test
    @DisplayName("Verificar alerta exactamente en el tope")
    void verificarAlertaEnElTope() {
        Alerta alert = new Alerta(105.0, TipoAlerta.SEMANAL);
        // Gastos suman 105€, igual al tope (no supera)
        assertFalse(alert.verificar(expenses));
    }
    
    @Test
    @DisplayName("Alerta desactivada no verifica")
    void alertaDesactivadaNoVerifica() {
        Alerta alert = new Alerta(50.0, TipoAlerta.SEMANAL);
        alert.desactivar();
        // Aunque gastos superan tope, alerta desactivada no verifica
        assertFalse(alert.verificar(expenses));
    }
    
    @Test
    @DisplayName("Generar notificación cuando se supera tope")
    void generarNotificacionCuandoSupera() {
        Alerta alert = new Alerta(100.0, TipoAlerta.SEMANAL);
        assertTrue(alert.generarNotificacion(expenses).isPresent());
    }
    
    @Test
    @DisplayName("No generar notificación si no se supera tope")
    void noGenerarNotificacionSiNoSupera() {
        Alerta alert = new Alerta(200.0, TipoAlerta.SEMANAL);
        assertTrue(alert.generarNotificacion(expenses).isEmpty());
    }
    
    @Test
    @DisplayName("Notificación contiene mensaje descriptivo")
    void notificacionConMensajeDescriptivo() {
        Alerta alert = new Alerta(100.0, TipoAlerta.SEMANAL);
        Notificacion notif = alert.generarNotificacion(expenses).orElseThrow();
        
        assertTrue(notif.getMensaje().contains("Alerta"));
        assertTrue(notif.getMensaje().contains("100"));
    }
    
    @Test
    @DisplayName("Activar y desactivar alerta")
    void activarDesactivarAlerta() {
        Alerta alert = new Alerta(100.0, TipoAlerta.SEMANAL);
        assertTrue(alert.isActiva());
        
        alert.desactivar();
        assertFalse(alert.isActiva());
        
        alert.activar();
        assertTrue(alert.isActiva());
    }
    
    @Test
    @DisplayName("Modificar tope de gasto")
    void modificarTopeGasto() {
        Alerta alert = new Alerta(100.0, TipoAlerta.SEMANAL);
        alert.setMaxGasto(150.0);
        assertEquals(150.0, alert.getMaxGasto());
    }
    
    @Test
    @DisplayName("Error al modificar tope a negativo")
    void modificarTopeNegativo() {
        Alerta alert = new Alerta(100.0, TipoAlerta.SEMANAL);
        assertThrows(IllegalArgumentException.class, () -> alert.setMaxGasto(-50.0));
    }
    
    @Test
    @DisplayName("Filtrar gastos por categoría de alerta")
    void filtrarPorCategoriaDeAlerta() {
        Categoria otherCategory = new Categoria("Comida", false);
        Gasto expenseOtherCat = new Gasto(100.0, LocalDateTime.now(), otherCategory, "Otro");
        
        List<Gasto> allExpenses = Arrays.asList(
            expenses.get(0),  // Ocio: 50€
            expenses.get(1),  // Ocio: 30€
            expenseOtherCat    // Comida: 100€
        );
        
        // Alerta solo para categoría "Ocio"
        Alerta alert = new Alerta(100.0, TipoAlerta.SEMANAL, category);
        
        // Solo cuenta gastos de Ocio: 50 + 30 = 80€
        assertEquals(80.0, alert.calcularGastoActual(allExpenses), 0.01);
    }
}
