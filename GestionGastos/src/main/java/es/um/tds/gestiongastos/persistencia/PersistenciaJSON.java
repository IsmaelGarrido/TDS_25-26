package es.um.tds.gestiongastos.persistencia;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import es.um.tds.gestiongastos.modelo.*;
import es.um.tds.gestiongastos.repositorio.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Gestiona la persistencia de datos de la aplicación en formato JSON.
 * Utiliza Jackson para serialización/deserialización.
 */
public class PersistenciaJSON  {
    
    private static final String NOMBRE_ARCHIVO_DEFAULT = "datos_gastos.json";
    private static final String DIRECTORIO_DATOS = System.getProperty("user.home") + 
                                                    File.separator + ".gestiongastos";
    
    private final ObjectMapper mapper;
    private final String rutaArchivo;
    
    /**
     * Constructor con ruta por defecto.
     */
    public PersistenciaJSON() {
        this(DIRECTORIO_DATOS + File.separator + NOMBRE_ARCHIVO_DEFAULT);
    }
    
    /**
     * Constructor con ruta personalizada.
     * @param rutaArchivo Ruta completa del archivo JSON
     */
    public PersistenciaJSON(String rutaArchivo) {
        this.rutaArchivo = rutaArchivo;
        this.mapper = configurarMapper();
    }
    
    /**
     * Configura el ObjectMapper de Jackson.
     */
    private ObjectMapper configurarMapper() {
        ObjectMapper om = new ObjectMapper();
        
        // Registrar módulo para fechas Java 8+
        om.registerModule(new JavaTimeModule());
        
        // Formato legible (indentado)
        om.enable(SerializationFeature.INDENT_OUTPUT);
        
        // No fallar con fechas como timestamps
        om.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        // Acceder a campos privados
        om.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        
        // Habilitar polimorfismo para CuentaCompartida
        om.activateDefaultTyping(
            om.getPolymorphicTypeValidator(),
            ObjectMapper.DefaultTyping.NON_FINAL
        );
        
        return om;
    }
    
    // ==================== GUARDAR ====================
    
    /**
     * Guarda todos los datos de la aplicación en JSON.
     * @throws IOException si hay error de escritura
     */
    public void guardar() throws IOException {
        DatosAplicacion datos = recopilarDatos();
        guardar(datos);
    }
    
    /**
     * Guarda los datos proporcionados en JSON.
     * @param datos Datos a guardar
     * @throws IOException si hay error de escritura
     */
    public void guardar(DatosAplicacion datos) throws IOException {
        // Crear directorio si no existe
        crearDirectorioSiNoExiste();
        
        // Escribir archivo
        mapper.writeValue(new File(rutaArchivo), datos);
    }
    
    /**
     * Recopila los datos de todos los repositorios.
     */
    private DatosAplicacion recopilarDatos() {
        return new DatosAplicacion(
            RepositorioCategorias.getInstance().getTodasCategorias(),
            RepositorioGastos.getInstance().getTodosGastos(),
            RepositorioCuentas.getInstance().getTotalCuentas(),
            RepositorioAlertas.getInstance().getTotalAlertas(),
            HistorialNotificaciones.getInstance().getNotificaciones()
        );
    }
    
    // ==================== CARGAR ====================
    
    /**
     * Carga los datos desde el archivo JSON y los restaura en los repositorios.
     * @return true si se cargaron datos, false si el archivo no existe
     * @throws IOException si hay error de lectura
     */
    public boolean cargar() throws IOException {
        if (!existeArchivo()) {
            return false;
        }
        
        DatosAplicacion datos = leerArchivo();
        restaurarDatos(datos);
        return true;
    }
    
    /**
     * Lee el archivo JSON y devuelve los datos.
     * @return Datos leídos del archivo
     * @throws IOException si hay error de lectura
     */
    public DatosAplicacion leerArchivo() throws IOException {
        return mapper.readValue(new File(rutaArchivo), DatosAplicacion.class);
    }
    
    /**
     * Restaura los datos en los repositorios.
     * @param datos Datos a restaurar
     */
    public void restaurarDatos(DatosAplicacion datos) {
        // Limpiar repositorios
        limpiarRepositorios();
        
        // Restaurar contadores primero (para evitar colisiones de ID)
        Categoria.setContador(datos.getContadorCategoria());
        Gasto.setContador(datos.getContadorGasto());
        CuentaCompartida.setContador(datos.getContadorCuenta());
        Alerta.setContador(datos.getContadorAlerta());
        Notificacion.setContador(datos.getContadorNotificacion());
        
        // Restaurar categorías (las predefinidas ya están, solo añadir personalizadas)
        RepositorioCategorias repoCategorias = RepositorioCategorias.getInstance();
        for (Categoria cat : datos.getCategorias()) {
            if (!cat.isBase() && !repoCategorias.existsByNombre(cat.getNombre())) {
                repoCategorias.addCategoria(cat);
            }
        }
        
        // Restaurar gastos
        RepositorioGastos repoGastos = RepositorioGastos.getInstance();
        for (Gasto gasto : datos.getGastos()) {
            repoGastos.addGasto(gasto);
        }
        
        // Restaurar cuentas
        RepositorioCuentas repoCuentas = RepositorioCuentas.getInstance();
        for (CuentaCompartida cuenta : datos.getCuentas()) {
            repoCuentas.addCuenta(cuenta);
        }
        
        // Restaurar alertas
        RepositorioAlertas repoAlertas = RepositorioAlertas.getInstance();
        for (Alerta alerta : datos.getAlertas()) {
            repoAlertas.addAlerta(alerta);
        }
        
        // Restaurar notificaciones
        HistorialNotificaciones historial = HistorialNotificaciones.getInstance();
        for (Notificacion notif : datos.getNotificaciones()) {
            historial.addNotificacion(notif);
        }
    }
    
    /**
     * Limpia todos los repositorios.
     */
    private void limpiarRepositorios() {
        RepositorioCategorias.getInstance().reset();
        RepositorioGastos.getInstance().reset();
        RepositorioCuentas.getInstance().reset();
        RepositorioAlertas.getInstance().reset();
        HistorialNotificaciones.getInstance().reset();
    }
    
    // ==================== UTILIDADES ====================
    
    /**
     * Crea el directorio de datos si no existe.
     */
    private void crearDirectorioSiNoExiste() throws IOException {
        Path directorio = Paths.get(rutaArchivo).getParent();
        if (directorio != null && !Files.exists(directorio)) {
            Files.createDirectories(directorio);
        }
    }
    
    /**
     * Verifica si existe el archivo de datos.
     * @return true si el archivo existe
     */
    public boolean existeArchivo() {
        return Files.exists(Paths.get(rutaArchivo));
    }
    
    /**
     * Elimina el archivo de datos.
     * @return true si se eliminó correctamente
     */
    public boolean eliminarArchivo() {
        try {
            return Files.deleteIfExists(Paths.get(rutaArchivo));
        } catch (IOException e) {
            return false;
        }
    }
    
    /**
     * Obtiene la ruta del archivo de datos.
     * @return Ruta del archivo
     */
    public String getRutaArchivo() {
        return rutaArchivo;
    }
    
    /**
     * Obtiene el directorio de datos por defecto.
     * @return Directorio de datos
     */
    public static String getDirectorioDatos() {
        return DIRECTORIO_DATOS;
    }
}