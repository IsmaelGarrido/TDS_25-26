package es.um.tds.gestiongastos.modelo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para la clase CuentaEquitativa.
 */
@DisplayName("Tests de CuentaEquitativa")
class TestCuentaEquitativa {
    
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
    @DisplayName("Crear cuenta equitativa con varargs")
    void crearCuentaConVarargs() {
        CuentaEquitativa account = new CuentaEquitativa("Piso", raquel, luis, maria);
        
        assertEquals("Piso", account.getNombre());
        assertEquals(3, account.calcularNumPersonas());
        assertTrue(account.contienePersona(raquel));
        assertTrue(account.contienePersona(luis));
        assertTrue(account.contienePersona(maria));
    }
    
    @Test
    @DisplayName("Crear cuenta equitativa con lista")
    void crearCuentaConLista() {
        List<Persona> people = Arrays.asList(raquel, luis);
        CuentaEquitativa account = new CuentaEquitativa("Vacaciones", people);
        
        assertEquals("Vacaciones", account.getNombre());
        assertEquals(2, account.calcularNumPersonas());
    }
    
    @Test
    @DisplayName("Error al crear cuenta con menos de 2 personas")
    void errorMenosDeDosPersonas() {
        assertThrows(IllegalArgumentException.class, 
            () -> new CuentaEquitativa("Test", raquel));
    }
    
    @Test
    @DisplayName("Error al crear cuenta con lista null")
    void errorListaNull() {
        assertThrows(IllegalArgumentException.class, 
            () -> new CuentaEquitativa("Test", (List<Persona>) null));
    }
    
    @Test
    @DisplayName("Error al crear cuenta con nombre vacío")
    void errorNombreVacio() {
        assertThrows(IllegalArgumentException.class, 
            () -> new CuentaEquitativa("", raquel, luis));
    }
    
    @Test
    @DisplayName("Proporción equitativa con 2 personas")
    void proporcionEquitativaDosPersonas() {
        CuentaEquitativa account = new CuentaEquitativa("Test", raquel, luis);
        
        assertEquals(0.5, account.calcularProporcion(raquel), 0.001);
        assertEquals(0.5, account.calcularProporcion(luis), 0.001);
    }
    
    @Test
    @DisplayName("Proporción equitativa con 3 personas")
    void proporcionEquitativaTresPersonas() {
        CuentaEquitativa account = new CuentaEquitativa("Piso", raquel, luis, maria);
        
        assertEquals(1.0/3.0, account.calcularProporcion(raquel), 0.001);
        assertEquals(1.0/3.0, account.calcularProporcion(luis), 0.001);
        assertEquals(1.0/3.0, account.calcularProporcion(maria), 0.001);
    }
    
    @Test
    @DisplayName("Error al calcular proporción de persona externa")
    void errorProporcionPersonaExterna() {
        CuentaEquitativa account = new CuentaEquitativa("Test", raquel, luis);
        Persona otherPerson = new Persona("Externa");
        
        assertThrows(IllegalArgumentException.class, 
            () -> account.calcularProporcion(otherPerson));
    }
    
    @Test
    @DisplayName("Cálculo de saldos tras un gasto")
    void calculoSaldosTrasGasto() {
        CuentaEquitativa account = new CuentaEquitativa("Piso", raquel, luis, maria);
        Gasto expense = new Gasto(30.0, category);
        
        account.addGasto(expense, raquel);
        
        // Raquel pagó 30€, su parte es 10€, le deben 20€
        assertEquals(20.0, account.calcularSaldo(raquel), 0.01);
        // Luis y María deben 10€ cada uno
        assertEquals(-10.0, account.calcularSaldo(luis), 0.01);
        assertEquals(-10.0, account.calcularSaldo(maria), 0.01);
    }
    
    @Test
    @DisplayName("Cálculo de saldos tras múltiples gastos")
    void calculoSaldosMultiplesGastos() {
        CuentaEquitativa account = new CuentaEquitativa("Piso", raquel, luis);
        
        // Raquel paga 100€
        account.addGasto(new Gasto(100.0, category), raquel);
        // Luis paga 50€
        account.addGasto(new Gasto(50.0, category), luis);
        
        // Total: 150€, cada uno debe 75€
        // Raquel pagó 100€, le deben 25€
        assertEquals(25.0, account.calcularSaldo(raquel), 0.01);
        // Luis pagó 50€, debe 25€
        assertEquals(-25.0, account.calcularSaldo(luis), 0.01);
    }
    
    @Test
    @DisplayName("Gasto total de la cuenta")
    void gastoTotal() {
        CuentaEquitativa account = new CuentaEquitativa("Piso", raquel, luis);
        
        account.addGasto(new Gasto(100.0, category), raquel);
        account.addGasto(new Gasto(50.0, category), luis);
        
        assertEquals(150.0, account.calcularGastoTotal(), 0.01);
    }
    
    @Test
    @DisplayName("Eliminar gasto recalcula saldos")
    void eliminarGastoRecalculaSaldos() {
        CuentaEquitativa account = new CuentaEquitativa("Piso", raquel, luis);
        Gasto expense = new Gasto(100.0, category);
        
        account.addGasto(expense, raquel);
        assertEquals(50.0, account.calcularSaldo(raquel), 0.01);
        
        account.removeGasto(expense);
        assertEquals(0.0, account.calcularSaldo(raquel), 0.01);
        assertEquals(0.0, account.calcularSaldo(luis), 0.01);
    }
    
    @Test
    @DisplayName("Error al añadir gasto con pagador externo")
    void errorAddGastoPagadorExterno() {
        CuentaEquitativa account = new CuentaEquitativa("Piso", raquel, luis);
        Gasto expense = new Gasto(100.0, category);
        Persona otherPerson = new Persona("Externa");
        
        assertThrows(IllegalArgumentException.class, 
            () -> account.addGasto(expense, otherPerson));
    }
    
    @Test
    @DisplayName("Lista de personas es inmutable")
    void listaPersonasInmutable() {
        CuentaEquitativa account = new CuentaEquitativa("Piso", raquel, luis);
        List<Persona> people = account.getPersonas();
        
        assertThrows(UnsupportedOperationException.class, 
            () -> people.add(maria));
    }
    
    @Test
    @DisplayName("Lista de gastos es inmutable")
    void listaGastosInmutable() {
        CuentaEquitativa account = new CuentaEquitativa("Piso", raquel, luis);
        List<Gasto> expenses = account.getGastos();
        
        assertThrows(UnsupportedOperationException.class, 
            () -> expenses.add(new Gasto(10.0, category)));
    }
}