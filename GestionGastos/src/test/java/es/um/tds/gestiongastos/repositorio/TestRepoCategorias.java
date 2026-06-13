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
    @DisplayName("No se puede eliminar categoría predefinida")
    void noEliminarPredefinida() {
        Categoria predefinida = repo.getCategoriasPredefinidas().get(0);
        assertThrows(IllegalStateException.class, () -> repo.deleteCategoria(predefinida));
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
    @DisplayName("editNameCategoria con éxito devuelve 0")
    void editarNombreExito() {
        Categoria cat = new Categoria("Mi categoría");
        repo.addCategoria(cat);
        assertEquals(0, repo.editNameCategoria(cat, "Nuevo nombre"));
        assertEquals("Nuevo nombre", cat.getNombre());
    }
    
    @Test
    @DisplayName("editNameCategoria con nombre null devuelve -1")
    void editarNombreNull() {
        Categoria cat = new Categoria("Mi categoría");
        repo.addCategoria(cat);
        assertEquals(-1, repo.editNameCategoria(cat, null));
        assertEquals("Mi categoría", cat.getNombre());
    }
    
    @Test
    @DisplayName("editNameCategoria con nombre vacío devuelve -1")
    void editarNombreVacio() {
        Categoria cat = new Categoria("Mi categoría");
        repo.addCategoria(cat);
        assertEquals(-1, repo.editNameCategoria(cat, "   "));
        assertEquals("Mi categoría", cat.getNombre());
    }
    
    @Test
    @DisplayName("editNameCategoria en predefinida devuelve -2")
    void editarNombrePredefinida() {
        Categoria predefinida = repo.getCategoriasPredefinidas().get(0);
        String nombreOriginal = predefinida.getNombre();
        assertEquals(-2, repo.editNameCategoria(predefinida, "Otro"));
        assertEquals(nombreOriginal, predefinida.getNombre());
    }
    
    @Test
    @DisplayName("editNameCategoria con categoría null devuelve -3")
    void editarNombreCategoriaNull() {
        assertEquals(-3, repo.editNameCategoria(null, "Nuevo"));
    }
    
    @Test
    @DisplayName("editNameCategoria con categoría no registrada devuelve -3")
    void editarNombreCategoriaNoRegistrada() {
        Categoria noRegistrada = new Categoria("No registrada");
        assertEquals(-3, repo.editNameCategoria(noRegistrada, "Nuevo"));
    }
    
    @Test
    @DisplayName("editNameCategoria con nombre duplicado devuelve -4")
    void editarNombreDuplicado() {
        Categoria cat1 = new Categoria("Categoria1");
        Categoria cat2 = new Categoria("Categoria2");
        repo.addCategoria(cat1);
        repo.addCategoria(cat2);
        assertEquals(-4, repo.editNameCategoria(cat2, "Categoria1"));
        assertEquals("Categoria2", cat2.getNombre());
    }
    
    @Test
    @DisplayName("nombre no cambia si editNameCategoria falla")
    void nombreNoCambiaSiEditFalla() {
        Categoria cat = new Categoria("Mi categoría");
        repo.addCategoria(cat);
        repo.editNameCategoria(cat, null);
        assertEquals("Mi categoría", cat.getNombre());
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
