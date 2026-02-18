package es.um.tds.gestiongastos.repositorio;

import es.um.tds.gestiongastos.modelo.CuentaCompartida;
import es.um.tds.gestiongastos.modelo.CuentaEquitativa;
import es.um.tds.gestiongastos.modelo.Persona;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para RepositorioCuentas.
 */
@DisplayName("Tests de RepositorioCuentas")
class TestRepositorioCuentas {
    
    private RepositorioCuentas repo;
    private Persona person1, person2, person3;
    
    @BeforeEach
    void setUp() {
        RepositorioCuentas.resetInstance();
        CuentaCompartida.resetContador();
        
        repo = RepositorioCuentas.getInstance();
        person1 = new Persona("Juan");
        person2 = new Persona("María");
        person3 = new Persona("Pedro");
    }
    
    @Test
    @DisplayName("Singleton devuelve misma instancia")
    void singletonMismaInstancia() {
        RepositorioCuentas repo2 = RepositorioCuentas.getInstance();
        assertSame(repo, repo2);
    }
    
    @Test
    @DisplayName("Repositorio inicia vacío")
    void repositorioIniciaVacio() {
        assertEquals(0, repo.count());
        assertTrue(repo.getTotalCuentas().isEmpty());
    }
    
    @Test
    @DisplayName("Guardar y recuperar cuenta")
    void guardarYRecuperar() {
        CuentaEquitativa account = new CuentaEquitativa("Piso", person1, person2);
        repo.addCuenta(account);
        
        assertEquals(1, repo.count());
        assertTrue(repo.getCuentaByID(account.getID()).isPresent());
    }
    
    @Test
    @DisplayName("No se puede guardar cuenta null")
    void noGuardarNull() {
        assertThrows(IllegalArgumentException.class, () -> repo.addCuenta(null));
    }
    
    @Test
    @DisplayName("No se guarda cuenta duplicada")
    void noGuardarDuplicada() {
        CuentaEquitativa account = new CuentaEquitativa("Piso", person1, person2);
        repo.addCuenta(account);
        repo.addCuenta(account);
        
        assertEquals(1, repo.count());
    }
    
    @Test
    @DisplayName("Buscar por nombre")
    void buscarPorNombre() {
        CuentaEquitativa account = new CuentaEquitativa("Vacaciones", person1, person2);
        repo.addCuenta(account);
        
        assertTrue(repo.getCuentaByNombre("Vacaciones").isPresent());
        assertEquals(account, repo.getCuentaByNombre("Vacaciones").get());
    }
    
    @Test
    @DisplayName("Buscar por nombre ignora mayúsculas")
    void buscarPorNombreIgnoraMayusculas() {
        CuentaEquitativa account = new CuentaEquitativa("Vacaciones", person1, person2);
        repo.addCuenta(account);
        
        assertTrue(repo.getCuentaByNombre("VACACIONES").isPresent());
        assertTrue(repo.getCuentaByNombre("vacaciones").isPresent());
    }
    
    @Test
    @DisplayName("Buscar por nombre null devuelve vacío")
    void buscarNombreNull() {
        assertTrue(repo.getCuentaByNombre(null).isEmpty());
    }
    
    @Test
    @DisplayName("Buscar cuentas por persona")
    void buscarPorPersona() {
        repo.addCuenta(new CuentaEquitativa("Cuenta1", person1, person2));
        repo.addCuenta(new CuentaEquitativa("Cuenta2", person1, person3));
        repo.addCuenta(new CuentaEquitativa("Cuenta3", person2, person3));
        
        List<CuentaCompartida> personAccounts1 = repo.getCuentasByPersona(person1);
        assertEquals(2, personAccounts1.size());
        
        List<CuentaCompartida> personAccounts3 = repo.getCuentasByPersona(person3);
        assertEquals(2, personAccounts3.size());
    }
    
    @Test
    @DisplayName("Buscar por persona null devuelve vacío")
    void buscarPersonaNull() {
        repo.addCuenta(new CuentaEquitativa("Cuenta1", person1, person2));
        assertTrue(repo.getCuentasByPersona(null).isEmpty());
    }
    
    @Test
    @DisplayName("Buscar por persona sin cuentas devuelve vacío")
    void buscarPersonaSinCuentas() {
        repo.addCuenta(new CuentaEquitativa("Cuenta1", person1, person2));
        Persona noAccounts = new Persona("Sin Cuentas");
        assertTrue(repo.getCuentasByPersona(noAccounts).isEmpty());
    }
    
    @Test
    @DisplayName("Eliminar cuenta")
    void eliminarCuenta() {
        CuentaEquitativa account = new CuentaEquitativa("Piso", person1, person2);
        repo.addCuenta(account);
        
        assertTrue(repo.removeCuenta(account));
        assertEquals(0, repo.count());
    }
    
    @Test
    @DisplayName("Eliminar cuenta por ID")
    void eliminarPorId() {
        CuentaEquitativa account = new CuentaEquitativa("Piso", person1, person2);
        repo.addCuenta(account);
        
        assertTrue(repo.deleteByID(account.getID()));
        assertTrue(repo.getCuentaByID(account.getID()).isEmpty());
    }
    
    @Test
    @DisplayName("Eliminar cuenta null devuelve false")
    void eliminarNullDevuelveFalse() {
        assertFalse(repo.removeCuenta(null));
    }
    
    @Test
    @DisplayName("Eliminar ID inexistente devuelve false")
    void eliminarIdInexistente() {
        assertFalse(repo.deleteByID(9999));
    }
    
    @Test
    @DisplayName("Verificar si existe por nombre")
    void existsByNombre() {
        repo.addCuenta(new CuentaEquitativa("Piso", person1, person2));
        
        assertTrue(repo.existsCuentaByNombre("Piso"));
        assertFalse(repo.existsCuentaByNombre("Inexistente"));
    }
    
    @Test
    @DisplayName("Reset limpia el repositorio")
    void resetRepositorio() {
        repo.addCuenta(new CuentaEquitativa("Cuenta1", person1, person2));
        repo.addCuenta(new CuentaEquitativa("Cuenta2", person1, person3));
        
        repo.reset();
        
        assertEquals(0, repo.count());
    }
}