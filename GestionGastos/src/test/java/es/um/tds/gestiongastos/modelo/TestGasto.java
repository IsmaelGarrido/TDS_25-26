package es.um.tds.gestiongastos.modelo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para la clase Gasto.
 */
@DisplayName("Tests de Gasto")
class TestGasto {
    
    private Categoria category;
    
    @BeforeEach
    void setUp() {
        Categoria.resetContador();
        Gasto.resetContador();
        category = new Categoria("Test", false);
    }
    
    @Test
    @DisplayName("Crear gasto mínimo válido")
    void crearGastoMinimo() {
        Gasto expense = new Gasto(50.0, category);
        assertEquals(50.0, expense.getCantidad());
        assertEquals(category, expense.getCategoria());
        assertEquals("EUR", expense.getMoneda());
        assertTrue(expense.isPersonal());
        assertNotNull(expense.getFecha());
    }
    
    @Test
    @DisplayName("Crear gasto con todos los campos")
    void crearGastoCompleto() {
        Persona payer = new Persona("Juan");
        LocalDateTime date = LocalDateTime.of(2024, 6, 15, 10, 30);
        
        Gasto expense = new Gasto(100.0, date, "Cena", "Tarjeta", "EUR", category, payer);
        
        assertEquals(100.0, expense.getCantidad());
        assertEquals(date, expense.getFecha());
        assertEquals("Cena", expense.getNota().orElse(""));
        assertEquals("Tarjeta", expense.getMetodoPago().orElse(""));
        assertEquals(payer, expense.getPagador().orElse(null));
        assertFalse(expense.isPersonal());
    }
    
    @Test
    @DisplayName("Crear gasto simplificado")
    void crearGastoSimplificado() {
        LocalDateTime date = LocalDateTime.of(2024, 6, 15, 10, 30);
        Gasto expense = new Gasto(75.0, date, category, "Almuerzo");
        
        assertEquals(75.0, expense.getCantidad());
        assertEquals(date, expense.getFecha());
        assertEquals("Almuerzo", expense.getNota().orElse(""));
        assertTrue(expense.isPersonal());
    }
    
    @Test
    @DisplayName("Error al crear gasto con cantidad cero")
    void crearGastoCantidadCero() {
        assertThrows(IllegalArgumentException.class, () -> new Gasto(0.0, category));
    }
    
    @Test
    @DisplayName("Error al crear gasto con cantidad negativa")
    void crearGastoCantidadNegativa() {
        assertThrows(IllegalArgumentException.class, () -> new Gasto(-10.0, category));
    }
    
    @Test
    @DisplayName("Error al crear gasto sin categoría")
    void crearGastoSinCategoria() {
        assertThrows(IllegalArgumentException.class, () -> new Gasto(50.0, null));
    }
    
    @Test
    @DisplayName("Optional vacío para campos opcionales null")
    void optionalVacioParaCamposNull() {
        Gasto expense = new Gasto(50.0, category);
        assertTrue(expense.getNota().isEmpty());
        assertTrue(expense.getMetodoPago().isEmpty());
        assertTrue(expense.getPagador().isEmpty());
    }
    
    @Test
    @DisplayName("Optional vacío para nota vacía")
    void optionalVacioParaNotaVacia() {
        Gasto expense = new Gasto(50.0, LocalDateTime.now(), category, "");
        assertTrue(expense.getNota().isEmpty());
    }
    
    @Test
    @DisplayName("Modificar cantidad válida")
    void modificarCantidadValida() {
        Gasto expense = new Gasto(50.0, category);
        expense.setCantidad(100.0);
        assertEquals(100.0, expense.getCantidad());
    }
    
    @Test
    @DisplayName("Error al modificar cantidad a negativa")
    void modificarCantidadNegativa() {
        Gasto expense = new Gasto(50.0, category);
        assertThrows(IllegalArgumentException.class, () -> expense.setCantidad(-10.0));
    }
    
    @Test
    @DisplayName("Modificar categoría")
    void modificarCategoria() {
        Gasto expense = new Gasto(50.0, category);
        Categoria newCat = new Categoria("Nueva");
        
        expense.setCategoria(newCat);
        assertEquals(newCat, expense.getCategoria());
    }
    
    @Test
    @DisplayName("Error al modificar categoría a null")
    void modificarCategoriaNull() {
        Gasto expense = new Gasto(50.0, category);
        assertThrows(IllegalArgumentException.class, () -> expense.setCategoria(null));
    }
    
    @Test
    @DisplayName("Modificar pagador convierte en gasto compartido")
    void modificarPagador() {
        Gasto expense = new Gasto(50.0, category);
        assertTrue(expense.isPersonal());
        
        Persona payer = new Persona("Juan");
        expense.setPagador(payer);
        
        assertFalse(expense.isPersonal());
        assertEquals(payer, expense.getPagador().orElse(null));
    }
    
    @Test
    @DisplayName("Moneda por defecto es EUR")
    void monedaPorDefecto() {
        Gasto gasto = new Gasto(50.0, category);
        assertEquals("EUR", gasto.getMoneda());
    }
    
    @Test
    @DisplayName("IDs son únicos y secuenciales")
    void idsUnicos() {
        Gasto g1 = new Gasto(10.0, category);
        Gasto g2 = new Gasto(20.0, category);
        Gasto g3 = new Gasto(30.0, category);
        
        assertEquals(1, g1.getID());
        assertEquals(2, g2.getID());
        assertEquals(3, g3.getID());
    }
}