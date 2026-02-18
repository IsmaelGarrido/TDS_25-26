package es.um.tds.gestiongastos.repositorio;

import es.um.tds.gestiongastos.modelo.Categoria;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para RepositorioCategorias.
 */
@DisplayName("Tests de RepositorioCategorias")
class TestRepositorioCategorias {
    
    private RepositorioCategorias repo;
    
    @BeforeEach
    void setUp() {
        RepositorioCategorias.resetInstance();
        Categoria.resetContador();
        repo = RepositorioCategorias.getInstance();
    }
    
    @Test
    @DisplayName("Singleton devuelve misma instancia")
    void singletonMismaInstancia() {
        RepositorioCategorias repo2 = RepositorioCategorias.getInstance();
        assertSame(repo, repo2);
    }
    
    @Test
    @DisplayName("Categorías predefinidas inicializadas")
    void categoriasPredefinidas() {
        List<Categoria> predefinidas = repo.getCategoriasPredefinidas();
        assertFalse(predefinidas.isEmpty());
        assertTrue(predefinidas.stream().allMatch(Categoria::isBase));
    }
    
    @Test
    @DisplayName("Existen categorías predefinidas esperadas")
    void existenCategoriasPredefinidas() {
        assertTrue(repo.existsByNombre("Alimentación"));
        assertTrue(repo.existsByNombre("Transporte Público"));
        assertTrue(repo.existsByNombre("Ocio"));
        assertTrue(repo.existsByNombre("Gasolina"));
    }
    
    @Test
    @DisplayName("Guardar categoría personalizada")
    void guardarCategoriaPersonalizada() {
        int countInicial = repo.countCategorias();
        Categoria cat = new Categoria("Mi categoría");
        repo.addCategoria(cat);
        
        assertEquals(countInicial + 1, repo.countCategorias());
        assertTrue(repo.getCategoriaByNombre("Mi categoría").isPresent());
    }
    
    @Test
    @DisplayName("No se puede guardar categoría null")
    void noGuardarNull() {
        assertThrows(IllegalArgumentException.class, () -> repo.addCategoria(null));
    }
    
    @Test
    @DisplayName("No se puede guardar categoría duplicada")
    void noDuplicados() {
        Categoria cat = new Categoria("Nueva");
        repo.addCategoria(cat);
        
        Categoria duplicada = new Categoria("Nueva");
        assertThrows(IllegalArgumentException.class, () -> repo.addCategoria(duplicada));
    }
    
    @Test
    @DisplayName("No se puede eliminar categoría predefinida")
    void noEliminarPredefinida() {
        Categoria predefinida = repo.getCategoriasPredefinidas().get(0);
        assertThrows(IllegalStateException.class, () -> repo.deleteCategoria(predefinida));
    }
    
    @Test
    @DisplayName("Eliminar categoría personalizada")
    void eliminarPersonalizada() {
        Categoria cat = new Categoria("Temporal");
        repo.addCategoria(cat);
        
        assertTrue(repo.deleteCategoria(cat));
        assertFalse(repo.existsByNombre("Temporal"));
    }
    
    @Test
    @DisplayName("Eliminar categoría por ID")
    void eliminarPorId() {
        Categoria cat = new Categoria("Temporal");
        repo.addCategoria(cat);
        int id = cat.getID();
        
        assertTrue(repo.deleteCategoriaByID(id));
        assertTrue(repo.getCategoriaByID(id).isEmpty());
    }
    
    @Test
    @DisplayName("Buscar por nombre ignora mayúsculas")
    void buscarIgnoraMayusculas() {
        assertTrue(repo.getCategoriaByNombre("ALIMENTACIÓN").isPresent());
        assertTrue(repo.getCategoriaByNombre("alimentación").isPresent());
        assertTrue(repo.getCategoriaByNombre("Alimentación").isPresent());
    }
    
    @Test
    @DisplayName("Buscar por nombre null devuelve vacío")
    void buscarNombreNull() {
        assertTrue(repo.getCategoriaByNombre(null).isEmpty());
    }
    
    @Test
    @DisplayName("Buscar por ID inexistente devuelve vacío")
    void buscarIdInexistente() {
        assertTrue(repo.getCategoriaByID(9999).isEmpty());
    }
    
    @Test
    @DisplayName("Obtener categorías personalizadas")
    void obtenerPersonalizadas() {
        repo.addCategoria(new Categoria("Custom1"));
        repo.addCategoria(new Categoria("Custom2"));
        
        List<Categoria> personalizadas = repo.getCategoriasPersonalizadas();
        assertEquals(2, personalizadas.size());
        assertTrue(personalizadas.stream().noneMatch(Categoria::isBase));
    }
    
    @Test
    @DisplayName("Reset limpia y reinicializa predefinidas")
    void resetRepositorio() {
        repo.addCategoria(new Categoria("Temporal"));
        int countConTemporal = repo.countCategorias();
        
        repo.reset();
        
        assertTrue(repo.countCategorias() < countConTemporal);
        assertFalse(repo.existsByNombre("Temporal"));
        assertTrue(repo.existsByNombre("Alimentación"));
    }
}
