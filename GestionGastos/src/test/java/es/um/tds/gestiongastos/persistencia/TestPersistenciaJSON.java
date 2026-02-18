package es.um.tds.gestiongastos.persistencia;

import es.um.tds.gestiongastos.modelo.*;
import es.um.tds.gestiongastos.repositorio.*;

import org.junit.jupiter.api.*;

import java.io.IOException;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para PersistenciaJSON.
 */
@DisplayName("Tests de PersistenciaJSON")
class TestPersistenciaJSON {
    
    private static final String ARCHIVO_TEST = "test_datos.json";
    private PersistenciaJSON persistencia;
    
    @BeforeEach
    void setUp() {
        // Usar archivo de test
        persistencia = new PersistenciaJSON(ARCHIVO_TEST);
        
        // Limpiar repositorios
        resetearRepositorios();
    }
    
    @AfterEach
    void tearDown() {
        // Eliminar archivo de test
        persistencia.eliminarArchivo();
        
        // Limpiar repositorios
        resetearRepositorios();
    }
    
    private void resetearRepositorios() {
        RepositorioCategorias.resetInstance();
        RepositorioGastos.resetInstance();
        RepositorioCuentas.resetInstance();
        RepositorioAlertas.resetInstance();
        HistorialNotificaciones.resetInstance();
        
        Categoria.resetContador();
        Gasto.resetContador();
        CuentaCompartida.resetContador();
        Alerta.resetContador();
        Notificacion.resetContador();
    }
    
    @Test
    @DisplayName("Guardar y cargar datos vacíos")
    void guardarCargarVacio() throws IOException {
        persistencia.guardar();
        assertTrue(persistencia.existeArchivo());
        
        resetearRepositorios();
        
        boolean loaded = persistencia.cargar();
        assertTrue(loaded);
    }
    
    @Test
    @DisplayName("Cargar archivo inexistente devuelve false")
    void cargarArchivoInexistente() throws IOException {
        assertFalse(persistencia.existeArchivo());
        assertFalse(persistencia.cargar());
    }
    
    @Test
    @DisplayName("Guardar y cargar categorías personalizadas")
    void guardarCargarCategorias() throws IOException {
        // Crear categoría personalizada
        Categoria cat = new Categoria("Mi Categoría Personalizada");
        RepositorioCategorias.getInstance().addCategoria(cat);
        
        // Guardar
        persistencia.guardar();
        
        // Resetear y cargar
        resetearRepositorios();
        persistencia.cargar();
        
        // Verificar
        RepositorioCategorias repo = RepositorioCategorias.getInstance();
        assertTrue(repo.existsByNombre("Mi Categoría Personalizada"));
    }
    
    @Test
    @DisplayName("Guardar y cargar gastos")
    void guardarCargarGastos() throws IOException {
        // Crear datos
        Categoria cat = new Categoria("Test");
        RepositorioCategorias.getInstance().addCategoria(cat);
        
        RepositorioGastos repoGastos = RepositorioGastos.getInstance();
        repoGastos.addGasto(new Gasto(50.0, LocalDateTime.now(), cat, "Gasto 1"));
        repoGastos.addGasto(new Gasto(75.0, LocalDateTime.now(), cat, "Gasto 2"));
        
        // Guardar
        persistencia.guardar();
        
        // Resetear y cargar
        resetearRepositorios();
        persistencia.cargar();
        
        // Verificar
        assertEquals(2, RepositorioGastos.getInstance().count());
    }
    
    @Test
    @DisplayName("Guardar y cargar alertas")
    void guardarCargarAlertas() throws IOException {
        // Crear alertas
        RepositorioAlertas repoAlertas = RepositorioAlertas.getInstance();
        repoAlertas.addAlerta(new Alerta(100.0, TipoAlerta.SEMANAL));
        repoAlertas.addAlerta(new Alerta(500.0, TipoAlerta.MENSUAL));
        
        // Guardar
        persistencia.guardar();
        
        // Resetear y cargar
        resetearRepositorios();
        persistencia.cargar();
        
        // Verificar
        RepositorioAlertas repo = RepositorioAlertas.getInstance();
        assertEquals(2, repo.count());
        assertEquals(1, repo.getAlertasByTipo(TipoAlerta.SEMANAL).size());
        assertEquals(1, repo.getAlertasByTipo(TipoAlerta.MENSUAL).size());
    }
    
    @Test
    @DisplayName("Guardar y cargar notificaciones")
    void guardarCargarNotificaciones() throws IOException {
        // Crear notificaciones
        Notificacion n1 = new Notificacion("Notificación 1");
        Notificacion n2 = new Notificacion("Notificación 2");
        n2.marcarLeida();
        
        HistorialNotificaciones.getInstance().addNotificacion(n1);
        HistorialNotificaciones.getInstance().addNotificacion(n2);
        
        // Guardar
        persistencia.guardar();
        
        // Resetear y cargar
        resetearRepositorios();
        persistencia.cargar();
        
        // Verificar
        HistorialNotificaciones record = HistorialNotificaciones.getInstance();
        assertEquals(2, record.count());
        assertEquals(1, record.countNoLeidas());
    }
    
    @Test
    @DisplayName("Guardar y cargar cuentas compartidas")
    void guardarCargarCuentas() throws IOException {
        // Crear cuenta
        Persona p1 = new Persona("Juan");
        Persona p2 = new Persona("María");
        CuentaEquitativa account = new CuentaEquitativa("Piso compartido", p1, p2);
        
        RepositorioCuentas.getInstance().addCuenta(account);
        
        // Guardar
        persistencia.guardar();
        
        // Resetear y cargar
        resetearRepositorios();
        persistencia.cargar();
        
        // Verificar
        assertEquals(1, RepositorioCuentas.getInstance().count());
        assertTrue(RepositorioCuentas.getInstance().getCuentaByNombre("Piso compartido").isPresent());
    }
    
    @Test
    @DisplayName("Contadores se restauran correctamente")
    void contadoresRestaurados() throws IOException {
        // Crear varios elementos para aumentar contadores
        Categoria cat1 = new Categoria("Cat1");
        Categoria cat2 = new Categoria("Cat2");
        Categoria cat3 = new Categoria("Cat3");
        
        RepositorioCategorias repo = RepositorioCategorias.getInstance();
        repo.addCategoria(cat1);
        repo.addCategoria(cat2);
        repo.addCategoria(cat3);
        
        int idMaxAntes = cat3.getID();
        
        // Guardar
        persistencia.guardar();
        
        // Resetear y cargar
        resetearRepositorios();
        persistencia.cargar();
        
        // Crear nueva categoría y verificar que ID es mayor
        Categoria newcat = new Categoria("Nueva");
        assertTrue(newcat.getID() > idMaxAntes);
    }
    
    @Test
    @DisplayName("Eliminar archivo funciona")
    void eliminarArchivo() throws IOException {
        persistencia.guardar();
        assertTrue(persistencia.existeArchivo());
        
        assertTrue(persistencia.eliminarArchivo());
        assertFalse(persistencia.existeArchivo());
    }
    
    @Test
    @DisplayName("Obtener ruta de archivo")
    void obtenerRutaArchivo() {
        assertEquals(ARCHIVO_TEST, persistencia.getRutaArchivo());
    }
    
    @Test
    @DisplayName("DatosAplicacion tieneDatos funciona")
    void datosAplicacionTieneDatos() {
        DatosAplicacion empty = new DatosAplicacion();
        assertFalse(empty.tieneDatos());
        
        Categoria cat = new Categoria("Test");
        DatosAplicacion full = new DatosAplicacion(
            java.util.List.of(cat), null, null, null, null
        );
        assertTrue(full.tieneDatos());
    }
}