package es.um.tds.gestiongastos.importador;

import es.um.tds.gestiongastos.modelo.Categoria;
import es.um.tds.gestiongastos.modelo.Gasto;
import es.um.tds.gestiongastos.repositorio.RepositorioCategorias;

import org.junit.jupiter.api.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para los importadores de gastos.
 */
@DisplayName("Tests de Importadores")
class TestImportador {
    
    private static final String ARCHIVO_CSV_TEST = "test_gastos.csv";
    private static final String ARCHIVO_JSON_TEST = "test_gastos.json";
    
    @BeforeEach
    void setUp() {
        RepositorioCategorias.resetInstance();
        Categoria.resetContador();
        Gasto.resetContador();
        FactoriaImportadores.resetInstance();
    }
    
    @AfterEach
    void tearDown() {
        // Eliminar archivos de test
        new File(ARCHIVO_CSV_TEST).delete();
        new File(ARCHIVO_JSON_TEST).delete();
    }
    
    // ==================== Tests ImportadorCSV ====================
    
    @Nested
    @DisplayName("Tests de ImportadorCSV")
    class TestImportadorCSV {
        
        @Test
        @DisplayName("Importar CSV válido")
        void importarCSVValido() throws IOException, ImportacionException {
            // Crear archivo CSV de prueba
            crearArchivoCSV("""
                Date,Account,Category,Subcategory,Note,Payer,Amount,Currency
                3/2/2022 10:11,Personal,con tarjeta,Comida,Desayuno,Me,4.50,EUR
                3/1/2022 19:50,Compartida 1,efectivo,Transporte,Metro,Juan,6.00,EUR
                """);
            
            ImportadorCSV importador = new ImportadorCSV();
            List<Gasto> gastos = importador.importar(ARCHIVO_CSV_TEST);
            
            assertEquals(2, gastos.size());
        }
        
        @Test
        @DisplayName("Primer gasto personal tiene pagador null")
        void gastoPersonalSinPagador() throws IOException, ImportacionException {
            crearArchivoCSV("""
                Date,Account,Category,Subcategory,Note,Payer,Amount,Currency
                3/2/2022 10:11,Personal,con tarjeta,Comida,Desayuno,Me,4.50,EUR
                """);
            
            ImportadorCSV importador = new ImportadorCSV();
            List<Gasto> gastos = importador.importar(ARCHIVO_CSV_TEST);
            
            assertTrue(gastos.get(0).isPersonal());
        }
        
        @Test
        @DisplayName("Gasto compartido tiene pagador")
        void gastoCompartidoConPagador() throws IOException, ImportacionException {
            crearArchivoCSV("""
                Date,Account,Category,Subcategory,Note,Payer,Amount,Currency
                3/1/2022 19:50,Compartida 1,efectivo,Transporte,Metro,Juan,6.00,EUR
                """);
            
            ImportadorCSV importador = new ImportadorCSV();
            List<Gasto> gastos = importador.importar(ARCHIVO_CSV_TEST);
            
            assertFalse(gastos.get(0).isPersonal());
            assertEquals("Juan", gastos.get(0).getPagador().get().getNombre());
        }
        
        @Test
        @DisplayName("Se crean categorías nuevas automáticamente")
        void crearCategoriasNuevas() throws IOException, ImportacionException {
            crearArchivoCSV("""
                Date,Account,Category,Subcategory,Note,Payer,Amount,Currency
                3/2/2022 10:11,Personal,tarjeta,CategoríaNueva,Test,Me,10.00,EUR
                """);
            
            ImportadorCSV importador = new ImportadorCSV();
            importador.importar(ARCHIVO_CSV_TEST);
            
            assertTrue(RepositorioCategorias.getInstance().existsByNombre("CategoríaNueva"));
        }
        
        @Test
        @DisplayName("Error con archivo inexistente")
        void errorArchivoInexistente() {
            ImportadorCSV importador = new ImportadorCSV();
            assertThrows(IOException.class, 
                () -> importador.importar("archivo_que_no_existe.csv"));
        }
        
        @Test
        @DisplayName("Error con ruta vacía")
        void errorRutaVacia() {
            ImportadorCSV importador = new ImportadorCSV();
            assertThrows(ImportacionException.class, 
                () -> importador.importar(""));
        }
        
        @Test
        @DisplayName("Soporta extensión CSV")
        void soportaExtensionCSV() {
            ImportadorCSV importador = new ImportadorCSV();
            assertTrue(importador.soportaArchivo("archivo.csv"));
            assertTrue(importador.soportaArchivo("archivo.CSV"));
            assertFalse(importador.soportaArchivo("archivo.json"));
        }
        
        private void crearArchivoCSV(String contenido) throws IOException {
            try (FileWriter writer = new FileWriter(ARCHIVO_CSV_TEST)) {
                writer.write(contenido);
            }
        }
    }
    
    // ==================== Tests ImportadorJSON ====================
    
    @Nested
    @DisplayName("Tests de ImportadorJSON")
    class TestImportadorJSON {
        
        @Test
        @DisplayName("Importar JSON válido")
        void importarJSONValido() throws IOException, ImportacionException {
            crearArchivoJSON("""
                [
                    {"amount": 50.0, "category": "Comida", "note": "Almuerzo"},
                    {"amount": 25.0, "category": "Transporte", "note": "Bus"}
                ]
                """);
            
            ImportadorJSON importador = new ImportadorJSON();
            List<Gasto> gastos = importador.importar(ARCHIVO_JSON_TEST);
            
            assertEquals(2, gastos.size());
        }
        
        @Test
        @DisplayName("Soporta diferentes nombres de campos")
        void soportaCamposAlternativos() throws IOException, ImportacionException {
            crearArchivoJSON("""
                [
                    {"cantidad": 30.0, "categoria": "Ocio", "nota": "Cine", "moneda": "EUR"}
                ]
                """);
            
            ImportadorJSON importador = new ImportadorJSON();
            List<Gasto> gastos = importador.importar(ARCHIVO_JSON_TEST);
            
            assertEquals(1, gastos.size());
            assertEquals(30.0, gastos.get(0).getCantidad(), 0.01);
        }
        
        @Test
        @DisplayName("Error con archivo inexistente")
        void errorArchivoInexistente() {
            ImportadorJSON importador = new ImportadorJSON();
            assertThrows(ImportacionException.class, 
                () -> importador.importar("archivo_que_no_existe.json"));
        }
        
        @Test
        @DisplayName("Soporta extensión JSON")
        void soportaExtensionJSON() {
            ImportadorJSON importador = new ImportadorJSON();
            assertTrue(importador.soportaArchivo("archivo.json"));
            assertTrue(importador.soportaArchivo("archivo.JSON"));
            assertFalse(importador.soportaArchivo("archivo.csv"));
        }
        
        private void crearArchivoJSON(String contenido) throws IOException {
            try (FileWriter writer = new FileWriter(ARCHIVO_JSON_TEST)) {
                writer.write(contenido);
            }
        }
    }
    
    // ==================== Tests FactoriaImportadores ====================
    
    @Nested
    @DisplayName("Tests de FactoriaImportadores")
    class TestFactoriaImportadores {
        
        @Test
        @DisplayName("Singleton devuelve misma instancia")
        void singletonMismaInstancia() {
            FactoriaImportadores f1 = FactoriaImportadores.getInstance();
            FactoriaImportadores f2 = FactoriaImportadores.getInstance();
            assertSame(f1, f2);
        }
        
        @Test
        @DisplayName("Obtener importador CSV")
        void obtenerImportadorCSV() throws ImportacionException {
            FactoriaImportadores factoria = FactoriaImportadores.getInstance();
            ImportadorGastos importador = factoria.getImportador("archivo.csv");
            
            assertNotNull(importador);
            assertTrue(importador instanceof ImportadorCSV);
        }
        
        @Test
        @DisplayName("Obtener importador JSON")
        void obtenerImportadorJSON() throws ImportacionException {
            FactoriaImportadores factoria = FactoriaImportadores.getInstance();
            ImportadorGastos importador = factoria.getImportador("archivo.json");
            
            assertNotNull(importador);
            assertTrue(importador instanceof ImportadorJSON);
        }
        
        @Test
        @DisplayName("Error con extensión no soportada")
        void errorExtensionNoSoportada() {
            FactoriaImportadores factoria = FactoriaImportadores.getInstance();
            assertThrows(ImportacionException.class, 
                () -> factoria.getImportador("archivo.xml"));
        }
        
        @Test
        @DisplayName("Error con archivo sin extensión")
        void errorSinExtension() {
            FactoriaImportadores factoria = FactoriaImportadores.getInstance();
            assertThrows(ImportacionException.class, 
                () -> factoria.getImportador("archivo"));
        }
        
        @Test
        @DisplayName("Verificar si existe importador")
        void existeImportador() {
            FactoriaImportadores factoria = FactoriaImportadores.getInstance();
            
            assertTrue(factoria.existeImportador("archivo.csv"));
            assertTrue(factoria.existeImportador("archivo.json"));
            assertFalse(factoria.existeImportador("archivo.xml"));
        }
        
        @Test
        @DisplayName("Obtener extensiones registradas")
        void extensionesRegistradas() {
            FactoriaImportadores factoria = FactoriaImportadores.getInstance();
            
            assertTrue(factoria.getExtensionesRegistradas().contains("csv"));
            assertTrue(factoria.getExtensionesRegistradas().contains("json"));
        }
    }
}