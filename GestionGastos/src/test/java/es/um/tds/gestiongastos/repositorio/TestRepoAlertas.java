package es.um.tds.gestiongastos.repositorio;

import es.um.tds.gestiongastos.modelo.Alerta;
import es.um.tds.gestiongastos.modelo.Categoria;
import es.um.tds.gestiongastos.modelo.TipoAlerta;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para RepositorioAlertas.
 */
@DisplayName("Tests de RepositorioAlertas")
class TestRepositorioAlertas {
    
    private RepositorioAlertas repo;
    private Categoria category;
    
    @BeforeEach
    void setUp() {
        RepositorioAlertas.resetInstance();
        Categoria.resetContador();
        Alerta.resetContador();
        
        repo = RepositorioAlertas.getInstance();
        category = new Categoria("Ocio", false);
    }
    
    @Test
    @DisplayName("Singleton devuelve misma instancia")
    void singletonMismaInstancia() {
        RepositorioAlertas repo2 = RepositorioAlertas.getInstance();
        assertSame(repo, repo2);
    }
    
    @Test
    @DisplayName("Repositorio inicia vacío")
    void repositorioIniciaVacio() {
        assertEquals(0, repo.count());
        assertTrue(repo.getTotalAlertas().isEmpty());
    }
    
    @Test
    @DisplayName("Guardar y recuperar alerta")
    void guardarYRecuperar() {
        Alerta alert = new Alerta(100.0, TipoAlerta.SEMANAL);
        repo.addAlerta(alert);
        
        assertEquals(1, repo.count());
        assertTrue(repo.getAlertaByID(alert.getID()).isPresent());
    }
    
    @Test
    @DisplayName("No se puede guardar alerta null")
    void noGuardarNull() {
        assertThrows(IllegalArgumentException.class, () -> repo.addAlerta(null));
    }
    
    @Test
    @DisplayName("No se guarda alerta duplicada")
    void noGuardarDuplicada() {
        Alerta alert = new Alerta(100.0, TipoAlerta.SEMANAL);
        repo.addAlerta(alert);
        repo.addAlerta(alert);
        
        assertEquals(1, repo.count());
    }
    
    @Test
    @DisplayName("Buscar alertas activas")
    void buscarActivas() {
        Alerta active = new Alerta(100.0, TipoAlerta.SEMANAL);
        Alerta inactive = new Alerta(200.0, TipoAlerta.MENSUAL);
        inactive.desactivar();
        
        repo.addAlerta(active);
        repo.addAlerta(inactive);
        
        List<Alerta> actives = repo.getAlertasActivas();
        assertEquals(1, actives.size());
        assertTrue(actives.get(0).isActiva());
    }
    
    @Test
    @DisplayName("Contar alertas activas")
    void contarActivas() {
        Alerta active1 = new Alerta(100.0, TipoAlerta.SEMANAL);
        Alerta active2 = new Alerta(150.0, TipoAlerta.MENSUAL);
        Alerta inactive = new Alerta(200.0, TipoAlerta.MENSUAL);
        inactive.desactivar();
        
        repo.addAlerta(active1);
        repo.addAlerta(active2);
        repo.addAlerta(inactive);
        
        assertEquals(2, repo.countActivas());
    }
    
    @Test
    @DisplayName("Buscar por tipo SEMANAL")
    void buscarPorTipoSemanal() {
        repo.addAlerta(new Alerta(100.0, TipoAlerta.SEMANAL));
        repo.addAlerta(new Alerta(200.0, TipoAlerta.SEMANAL));
        repo.addAlerta(new Alerta(300.0, TipoAlerta.MENSUAL));
        
        List<Alerta> weekly = repo.getAlertasByTipo(TipoAlerta.SEMANAL);
        assertEquals(2, weekly.size());
        assertTrue(weekly.stream().allMatch(a -> a.getTipoAlerta() == TipoAlerta.SEMANAL));
    }
    
    @Test
    @DisplayName("Buscar por tipo MENSUAL")
    void buscarPorTipoMensual() {
        repo.addAlerta(new Alerta(100.0, TipoAlerta.SEMANAL));
        repo.addAlerta(new Alerta(200.0, TipoAlerta.MENSUAL));
        repo.addAlerta(new Alerta(300.0, TipoAlerta.MENSUAL));
        
        List<Alerta> monthly = repo.getAlertasByTipo(TipoAlerta.MENSUAL);
        assertEquals(2, monthly.size());
    }
    
    @Test
    @DisplayName("Buscar por tipo null devuelve vacío")
    void buscarTipoNull() {
        repo.addAlerta(new Alerta(100.0, TipoAlerta.SEMANAL));
        assertTrue(repo.getAlertasByTipo(null).isEmpty());
    }
    
    @Test
    @DisplayName("Buscar alertas generales")
    void buscarGenerales() {
        repo.addAlerta(new Alerta(100.0, TipoAlerta.SEMANAL)); // General
        repo.addAlerta(new Alerta(200.0, TipoAlerta.MENSUAL, category)); // Con categoría
        repo.addAlerta(new Alerta(300.0, TipoAlerta.SEMANAL)); // General
        
        List<Alerta> generals = repo.getAlertasGenerales();
        assertEquals(2, generals.size());
        assertTrue(generals.stream().allMatch(Alerta::esGeneral));
    }
    
    @Test
    @DisplayName("Buscar por categoría específica")
    void buscarPorCategoria() {
        Categoria otherCat = new Categoria("Comida", false);
        
        repo.addAlerta(new Alerta(100.0, TipoAlerta.SEMANAL)); // General
        repo.addAlerta(new Alerta(200.0, TipoAlerta.MENSUAL, category)); // Ocio
        repo.addAlerta(new Alerta(300.0, TipoAlerta.SEMANAL, otherCat)); // Comida
        
        List<Alerta> ocio = repo.getAlertasByCategoria(category);
        assertEquals(1, ocio.size());
    }
    
    @Test
    @DisplayName("Buscar por categoría null devuelve generales")
    void buscarCategoriaNull() {
        repo.addAlerta(new Alerta(100.0, TipoAlerta.SEMANAL)); // General
        repo.addAlerta(new Alerta(200.0, TipoAlerta.MENSUAL, category)); // Con categoría
        
        List<Alerta> generals = repo.getAlertasByCategoria(null);
        assertEquals(1, generals.size());
        assertTrue(generals.get(0).esGeneral());
    }
    
    @Test
    @DisplayName("Eliminar alerta")
    void eliminarAlerta() {
        Alerta alert = new Alerta(100.0, TipoAlerta.SEMANAL);
        repo.addAlerta(alert);
        
        assertTrue(repo.removeAlerta(alert));
        assertEquals(0, repo.count());
    }
    
    @Test
    @DisplayName("Eliminar alerta por ID")
    void eliminarPorId() {
        Alerta alert = new Alerta(100.0, TipoAlerta.SEMANAL);
        repo.addAlerta(alert);
        
        assertTrue(repo.removeAlertaByID(alert.getID()));
        assertTrue(repo.getAlertaByID(alert.getID()).isEmpty());
    }
    
    @Test
    @DisplayName("Eliminar alerta null devuelve false")
    void eliminarNullDevuelveFalse() {
        assertFalse(repo.removeAlerta(null));
    }
    
    @Test
    @DisplayName("Reset limpia el repositorio")
    void resetRepositorio() {
        repo.addAlerta(new Alerta(100.0, TipoAlerta.SEMANAL));
        repo.addAlerta(new Alerta(200.0, TipoAlerta.MENSUAL));
        
        repo.reset();
        
        assertEquals(0, repo.count());
    }
}