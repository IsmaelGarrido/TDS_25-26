package es.um.tds.gestiongastos.repositorio;

import es.um.tds.gestiongastos.modelo.Categoria;
import es.um.tds.gestiongastos.modelo.Filtro;
import es.um.tds.gestiongastos.modelo.Gasto;
import es.um.tds.gestiongastos.modelo.Persona;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para RepositorioGastos.
 */
@DisplayName("Tests de RepositorioGastos")
class TestRepositorioGastos {
    
    private RepositorioGastos repo;
    private Categoria category;
    
    @BeforeEach
    void setUp() {
        RepositorioGastos.resetInstance();
        Categoria.resetContador();
        Gasto.resetContador();
        
        repo = RepositorioGastos.getInstance();
        category = new Categoria("Test", false);
    }
    
    @Test
    @DisplayName("Singleton devuelve misma instancia")
    void singletonMismaInstancia() {
        RepositorioGastos repo2 = RepositorioGastos.getInstance();
        assertSame(repo, repo2);
    }
    
    @Test
    @DisplayName("Repositorio inicia vacío")
    void repositorioIniciaVacio() {
        assertEquals(0, repo.count());
        assertTrue(repo.getTodosGastos().isEmpty());
    }
    
    @Test
    @DisplayName("Guardar y recuperar gasto")
    void guardarYRecuperar() {
        Gasto expense = new Gasto(50.0, category);
        repo.addGasto(expense);
        
        assertEquals(1, repo.count());
        assertTrue(repo.getGastoByID(expense.getID()).isPresent());
        assertEquals(expense, repo.getGastoByID(expense.getID()).get());
    }
    
    @Test
    @DisplayName("No se puede guardar gasto null")
    void noGuardarNull() {
        assertThrows(IllegalArgumentException.class, () -> repo.addGasto(null));
    }
    
    @Test
    @DisplayName("No se guarda gasto duplicado")
    void noGuardarDuplicado() {
        Gasto expense = new Gasto(50.0, category);
        repo.addGasto(expense);
        repo.addGasto(expense);  // Intentar guardar de nuevo
        
        assertEquals(1, repo.count());
    }
    
    @Test
    @DisplayName("Eliminar gasto")
    void eliminarGasto() {
        Gasto expense = new Gasto(50.0, category);
        repo.addGasto(expense);
        
        assertTrue(repo.deleteGasto(expense));
        assertEquals(0, repo.count());
        assertTrue(repo.getGastoByID(expense.getID()).isEmpty());
    }
    
    @Test
    @DisplayName("Eliminar gasto por ID")
    void eliminarGastoPorId() {
        Gasto expense = new Gasto(50.0, category);
        repo.addGasto(expense);
        
        assertTrue(repo.deleteGastoByID(expense.getID()));
        assertEquals(0, repo.count());
    }
    
    @Test
    @DisplayName("Eliminar gasto null devuelve false")
    void eliminarNullDevuelveFalse() {
        assertFalse(repo.deleteGasto(null));
    }
    
    @Test
    @DisplayName("Eliminar ID inexistente devuelve false")
    void eliminarIdInexistente() {
        assertFalse(repo.deleteGastoByID(9999));
    }
    
    @Test
    @DisplayName("Buscar por categoría")
    void buscarPorCategoria() {
        Categoria otherCat = new Categoria("Otra", false);
        
        repo.addGasto(new Gasto(50.0, category));
        repo.addGasto(new Gasto(30.0, category));
        repo.addGasto(new Gasto(20.0, otherCat));
        
        List<Gasto> result = repo.getGastosByCategoria(category);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(g -> g.getCategoria().equals(category)));
    }
    
    @Test
    @DisplayName("Buscar por categoría null devuelve vacío")
    void buscarCategoriaNullDevuelveVacio() {
        repo.addGasto(new Gasto(50.0, category));
        assertTrue(repo.getGastosByCategoria(null).isEmpty());
    }
    
    @Test
    @DisplayName("Buscar gastos personales")
    void buscarPersonales() {
        Persona payer = new Persona("Juan");
        
        Gasto personal = new Gasto(50.0, category);
        Gasto shared = new Gasto(30.0, LocalDateTime.now(), "nota", "tarjeta",
        		"EUR", category, payer);
        
        repo.addGasto(personal);
        repo.addGasto(shared);
        
        List<Gasto> personales = repo.getGastosPersonales();
        assertEquals(1, personales.size());
        assertTrue(personales.get(0).isPersonal());
    }
    
    @Test
    @DisplayName("Buscar gastos compartidos")
    void buscarCompartidos() {
        Persona payer = new Persona("Juan");
        
        Gasto personal = new Gasto(50.0, category);
        Gasto shared = new Gasto(30.0, LocalDateTime.now(), "nota", "tarjeta",
        		"EUR", category,  payer);
        
        repo.addGasto(personal);
        repo.addGasto(shared);
        
        List<Gasto> shareds = repo.getGastosCompartidos();
        assertEquals(1, shareds.size());
        assertFalse(shareds.get(0).isPersonal());
    }
    
    @Test
    @DisplayName("Buscar por rango de fechas")
    void buscarPorRangoFechas() {
        repo.addGasto(new Gasto(50.0, LocalDateTime.of(2024, 1, 15, 10, 0), category, "Enero"));
        repo.addGasto(new Gasto(30.0, LocalDateTime.of(2024, 6, 15, 10, 0), category, "Junio"));
        repo.addGasto(new Gasto(20.0, LocalDateTime.of(2024, 12, 15, 10, 0), category, "Diciembre"));
        
        List<Gasto> result = repo.getGastosByRangoFechas(
                LocalDate.of(2024, 1, 1), 
                LocalDate.of(2024, 6, 30));
        
        assertEquals(2, result.size());
    }
    
    @Test
    @DisplayName("Calcular total")
    void calcularTotal() {
        repo.addGasto(new Gasto(50.0, category));
        repo.addGasto(new Gasto(30.0, category));
        repo.addGasto(new Gasto(20.0, category));
        
        assertEquals(100.0, repo.calcularTotal(), 0.01);
    }
    
    @Test
    @DisplayName("Calcular total con repositorio vacío")
    void calcularTotalVacio() {
        assertEquals(0.0, repo.calcularTotal(), 0.01);
    }
    
    @Test
    @DisplayName("Buscar con filtro")
    void buscarConFiltro() {
        Categoria otherCat = new Categoria("Otra", false);
        
        repo.addGasto(new Gasto(50.0, category));
        repo.addGasto(new Gasto(30.0, otherCat));
        
        Filtro filter = new Filtro();
        filter.addCategoria(category);
        
        List<Gasto> result = repo.getGastosByFiltro(filter);
        assertEquals(1, result.size());
    }
    
    @Test
    @DisplayName("Buscar con filtro null devuelve todos")
    void buscarConFiltroNull() {
        repo.addGasto(new Gasto(50.0, category));
        repo.addGasto(new Gasto(30.0, category));
        
        List<Gasto> result = repo.getGastosByFiltro(null);
        assertEquals(2, result.size());
    }
    
    @Test
    @DisplayName("Calcular total con filtro")
    void calcularTotalConFiltro() {
        Categoria otherCat = new Categoria("Otra", false);
        
        repo.addGasto(new Gasto(50.0, category));
        repo.addGasto(new Gasto(30.0, category));
        repo.addGasto(new Gasto(100.0, otherCat));
        
        Filtro filter = new Filtro();
        filter.addCategoria(category);
        
        assertEquals(80.0, repo.calcularTotal(filter), 0.01);
    }
    
    @Test
    @DisplayName("Reset limpia el repositorio")
    void resetRepositorio() {
        repo.addGasto(new Gasto(50.0, category));
        repo.addGasto(new Gasto(30.0, category));
        
        repo.reset();
        
        assertEquals(0, repo.count());
    }
}