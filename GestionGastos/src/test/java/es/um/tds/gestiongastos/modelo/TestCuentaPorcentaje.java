package es.um.tds.gestiongastos.modelo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para la clase CuentaPorcentaje.
 */
@DisplayName("Tests de CuentaPorcentaje")
class TestCuentaPorcentaje {
    
    private Persona raquel, luis, maria;
    private Categoria category;
    
    @BeforeEach
    void setUp() {
        Categoria.resetContador();
        Gasto.resetContador();
        CuentaCompartida.resetContador();
        
        raquel = new Persona("Raquel");
        luis = new Persona("Luis");
        maria = new Persona("María");
        category = new Categoria("Comida", false);
    }
    
    @Test
    @DisplayName("Crear cuenta con porcentajes específicos")
    void crearCuentaConPorcentajes() {
        Map<Persona, Double> percentages = Map.of(
            raquel, 40.0,
            luis, 30.0,
            maria, 30.0
        );
        
        CuentaPorcentaje account = new CuentaPorcentaje("Piso", 
            Arrays.asList(raquel, luis, maria), percentages);
        
        assertEquals(40.0, account.getPorcentaje(raquel), 0.01);
        assertEquals(30.0, account.getPorcentaje(luis), 0.01);
        assertEquals(30.0, account.getPorcentaje(maria), 0.01);
    }
    
    @Test
    @DisplayName("Crear cuenta con reparto equitativo por defecto")
    void crearCuentaEquitativaPorDefecto() {
        CuentaPorcentaje account = new CuentaPorcentaje("Piso", 
            Arrays.asList(raquel, luis));
        
        assertEquals(50.0, account.getPorcentaje(raquel), 0.01);
        assertEquals(50.0, account.getPorcentaje(luis), 0.01);
    }
    
    @Test
    @DisplayName("Crear cuenta con varargs (equitativo)")
    void crearCuentaConVarargs() {
        CuentaPorcentaje account = new CuentaPorcentaje("Piso", raquel, luis, maria);
        
        double expected = 100.0 / 3.0;
        assertEquals(expected, account.getPorcentaje(raquel), 0.01);
        assertEquals(expected, account.getPorcentaje(luis), 0.01);
        assertEquals(expected, account.getPorcentaje(maria), 0.01);
    }
    
    @Test
    @DisplayName("Error si porcentajes no suman 100")
    void errorPorcentajesNoSuman100() {
        Map<Persona, Double> percentages = Map.of(
            raquel, 40.0,
            luis, 40.0  // Total: 80%
        );
        
        assertThrows(IllegalArgumentException.class, 
            () -> new CuentaPorcentaje("Test", Arrays.asList(raquel, luis), percentages));
    }
    
    @Test
    @DisplayName("Error si falta porcentaje de una persona")
    void errorFaltaPorcentaje() {
        Map<Persona, Double> percentages = Map.of(
            raquel, 100.0
            // Falta luis
        );
        
        assertThrows(IllegalArgumentException.class, 
            () -> new CuentaPorcentaje("Test", Arrays.asList(raquel, luis), percentages));
    }
    
    @Test
    @DisplayName("Error si porcentaje es negativo")
    void errorPorcentajeNegativo() {
        Map<Persona, Double> percentages = new HashMap<>();
        percentages.put(raquel, -20.0);
        percentages.put(luis, 120.0);
        
        assertThrows(IllegalArgumentException.class, 
            () -> new CuentaPorcentaje("Test", Arrays.asList(raquel, luis), percentages));
    }
    
    @Test
    @DisplayName("Proporción según porcentaje")
    void proporcionSegunPorcentaje() {
        Map<Persona, Double> percentages = Map.of(
            raquel, 60.0,
            luis, 40.0
        );
        
        CuentaPorcentaje account = new CuentaPorcentaje("Test", 
            Arrays.asList(raquel, luis), percentages);
        
        assertEquals(0.6, account.calcularProporcion(raquel), 0.001);
        assertEquals(0.4, account.calcularProporcion(luis), 0.001);
    }
    
    @Test
    @DisplayName("Cálculo de saldos con porcentajes personalizados")
    void calculoSaldosConPorcentajes() {
        Map<Persona, Double> percentages = Map.of(
            raquel, 40.0,
            luis, 30.0,
            maria, 30.0
        );
        
        CuentaPorcentaje account = new CuentaPorcentaje("Piso", 
            Arrays.asList(raquel, luis, maria), percentages);
        
        Gasto expense = new Gasto(100.0, category);
        account.addGasto(expense, raquel);
        
        // Raquel pagó 100€, su parte es 40€, le deben 60€
        assertEquals(60.0, account.getSaldo(raquel), 0.01);
        // Luis debe 30€
        assertEquals(-30.0, account.getSaldo(luis), 0.01);
        // María debe 30€
        assertEquals(-30.0, account.getSaldo(maria), 0.01);
    }
    
    @Test
    @DisplayName("Cálculo de saldos con porcentajes desiguales")
    void calculoSaldosPorcentajesDesiguales() {
        Map<Persona, Double> percentages = Map.of(
            raquel, 70.0,
            luis, 30.0
        );
        
        CuentaPorcentaje account = new CuentaPorcentaje("Test", 
            Arrays.asList(raquel, luis), percentages);
        
        // Luis paga 100€
        account.addGasto(new Gasto(100.0, category), luis);
        
        // Luis pagó 100€, su parte es 30€, le deben 70€
        assertEquals(70.0, account.getSaldo(luis), 0.01);
        // Raquel debe 70€
        assertEquals(-70.0, account.getSaldo(raquel), 0.01);
    }
    
    @Test
    @DisplayName("Modificar porcentajes recalcula saldos")
    void modificarPorcentajesRecalculaSaldos() {
        CuentaPorcentaje account = new CuentaPorcentaje("Test", raquel, luis);
        account.addGasto(new Gasto(100.0, category), raquel);
        
        // Con 50-50: Raquel le deben 50€
        assertEquals(50.0, account.getSaldo(raquel), 0.01);
        
        // Cambiar a 70-30
        Map<Persona, Double> newPercentages = Map.of(
            raquel, 70.0,
            luis, 30.0
        );
        account.setPorcentajes(newPercentages);
        
        // Con 70-30: Raquel le deben 30€
        assertEquals(30.0, account.getSaldo(raquel), 0.01);
        assertEquals(-30.0, account.getSaldo(luis), 0.01);
    }
    
    @Test
    @DisplayName("Obtener mapa de porcentajes es copia")
    void getMapaPorcentajesEsCopia() {
        CuentaPorcentaje account = new CuentaPorcentaje("Test", raquel, luis);
        Map<Persona, Double> percetages = account.getPorcentajes();
        
        // Modificar la copia no afecta al original
        percetages.put(raquel, 99.0);
        assertEquals(50.0, account.getPorcentaje(raquel), 0.01);
    }
    
    @Test
    @DisplayName("Tolerancia de redondeo en porcentajes")
    void toleranciaRedondeo() {
        // 33.33 + 33.33 + 33.34 = 100.00 (con tolerancia)
        Map<Persona, Double> percentages = new HashMap<>();
        percentages.put(raquel, 33.33);
        percentages.put(luis, 33.33);
        percentages.put(maria, 33.34);
        
        // No debería lanzar excepción
        assertDoesNotThrow(() -> 
            new CuentaPorcentaje("Test", Arrays.asList(raquel, luis, maria), percentages));
    }
}