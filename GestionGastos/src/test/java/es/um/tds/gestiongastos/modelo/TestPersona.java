package es.um.tds.gestiongastos.modelo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test unitarios para la clase Persona.
 */
@DisplayName("Tests de Persona")
class TestPersona {
	
	@Test
	@DisplayName("Crear persona con nombre válido")
	void crearPersonaValida() {
		Persona person = new Persona("Juan");
		assertEquals("Juan", person.getNombre());
	}
	
	@Test
	@DisplayName("Crear persona con nombre con espacios")
	void crearPersonaConEspacios() {
		Persona person = new Persona("   María   ");
		assertEquals("María", person.getNombre());
	}
	
	@Test
    @DisplayName("Error al crear persona con nombre null")
    void crearPersonaNombreNull() {
        assertThrows(IllegalArgumentException.class, () -> new Persona(null));
    }
	
	@Test
    @DisplayName("Error al crear persona con nombre vacío")
    void crearPersonaNombreVacio() {
        assertThrows(IllegalArgumentException.class, () -> new Persona(""));
        assertThrows(IllegalArgumentException.class, () -> new Persona("   "));
    }
    
    @Test
    @DisplayName("Dos personas con mismo nombre son iguales")
    void personasIguales() {
        Persona p1 = new Persona("Juan");
        Persona p2 = new Persona("Juan");
        assertEquals(p1, p2);
        assertEquals(p1.hashCode(), p2.hashCode());
    }
    
    @Test
    @DisplayName("Dos personas con distinto nombre son diferentes")
    void personasDiferentes() {
        Persona p1 = new Persona("Juan");
        Persona p2 = new Persona("María");
        assertNotEquals(p1, p2);
    }
    
    @Test
    @DisplayName("toString devuelve el nombre")
    void toStringDevuelveNombre() {
        Persona person = new Persona("Juan");
        assertEquals("Juan", person.toString());
    }
}
