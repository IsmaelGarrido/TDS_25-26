package es.um.tds.gestiongastos.modelo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para la clase Categoria.
 */
@DisplayName("Tests de Categoria")
class TestCategoria {
    
    @BeforeEach
    void setUp() {
        Categoria.resetContador();
    }
    
    @Test
    @DisplayName("Crear categoría predefinida")
    void crearCategoriaPredefinida() {
        Categoria cat = new Categoria("Alimentación", true);
        assertEquals("Alimentación", cat.getNombre());
        assertTrue(cat.isBase());
    }
    
    @Test
    @DisplayName("Crear categoría personalizada")
    void crearCategoriaPersonalizada() {
        Categoria cat = new Categoria("Mi categoría");
        assertEquals("Mi categoría", cat.getNombre());
        assertFalse(cat.isBase());
    }
    
    @Test
    @DisplayName("Crear categoría con espacios en nombre")
    void crearCategoriaConEspacios() {
        Categoria cat = new Categoria("  Mi categoría  ");
        assertEquals("Mi categoría", cat.getNombre());
    }
    
    @Test
    @DisplayName("Error al crear categoría con nombre null")
    void crearCategoriaNombreNull() {
        assertThrows(IllegalArgumentException.class, () -> new Categoria(null));
    }
    
    @Test
    @DisplayName("Error al crear categoría con nombre vacío")
    void crearCategoriaNombreVacio() {
        assertThrows(IllegalArgumentException.class, () -> new Categoria(""));
        assertThrows(IllegalArgumentException.class, () -> new Categoria("   "));
    }
    
    @Test
    @DisplayName("setNombre cambia el nombre directamente sin validar base")
    void setNombreCambiaDirectamente() {
        Categoria cat = new Categoria("Mi categoría");
        cat.setNombre("Nuevo nombre");
        assertEquals("Nuevo nombre", cat.getNombre());
    }
    
    @Test
    @DisplayName("cambiarNombre con éxito devuelve 0")
    void cambiarNombreExito() {
        Categoria cat = new Categoria("Mi categoría");
        assertEquals(0, cat.cambiarNombre("Nuevo nombre"));
        assertEquals("Nuevo nombre", cat.getNombre());
    }
    
    @Test
    @DisplayName("cambiarNombre elimina espacios")
    void cambiarNombreEliminaEspacios() {
        Categoria cat = new Categoria("Mi categoría");
        assertEquals(0, cat.cambiarNombre("  Nuevo nombre  "));
        assertEquals("Nuevo nombre", cat.getNombre());
    }
    
    @Test
    @DisplayName("cambiarNombre con nombre null devuelve -1")
    void cambiarNombreNull() {
        Categoria cat = new Categoria("Mi categoría");
        assertEquals(-1, cat.cambiarNombre(null));
        assertEquals("Mi categoría", cat.getNombre());
    }
    
    @Test
    @DisplayName("cambiarNombre con nombre vacío devuelve -1")
    void cambiarNombreVacio() {
        Categoria cat = new Categoria("Mi categoría");
        assertEquals(-1, cat.cambiarNombre("   "));
        assertEquals("Mi categoría", cat.getNombre());
    }
    
    @Test
    @DisplayName("cambiarNombre en predefinida devuelve -2")
    void cambiarNombrePredefinida() {
        Categoria cat = new Categoria("Alimentación", true);
        assertEquals(-2, cat.cambiarNombre("Otro"));
        assertEquals("Alimentación", cat.getNombre());
    }
    
    @Test
    @DisplayName("nombre no cambia si cambiarNombre falla")
    void nombreNoCambiasiCambiarNombreFalla() {
        Categoria cat = new Categoria("Mi categoría");
        cat.cambiarNombre(null);
        assertEquals("Mi categoría", cat.getNombre());
    }
    
    @Test
    @DisplayName("IDs son únicos y secuenciales")
    void idsUnicos() {
        Categoria cat1 = new Categoria("Cat1");
        Categoria cat2 = new Categoria("Cat2");
        Categoria cat3 = new Categoria("Cat3");
        
        assertEquals(1, cat1.getID());
        assertEquals(2, cat2.getID());
        assertEquals(3, cat3.getID());
    }
    
    @Test
    @DisplayName("Dos categorías con mismo ID son iguales")
    void categoriasIgualesPorId() {
        Categoria cat1 = new Categoria("Cat1");
        Categoria cat2 = new Categoria("Cat2");
        
        // Solo son iguales si tienen el mismo ID
        assertNotEquals(cat1.getID(), cat2.getID());
        assertNotEquals(cat1, cat2);
        
        assertEquals(cat1, cat1);
        assertEquals(cat1.hashCode(), cat1.hashCode());
    }
}