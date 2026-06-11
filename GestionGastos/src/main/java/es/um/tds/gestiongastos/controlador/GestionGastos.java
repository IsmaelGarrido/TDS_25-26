package es.um.tds.gestiongastos.controlador;

import es.um.tds.gestiongastos.importador.FactoriaImportadores;
import es.um.tds.gestiongastos.importador.ImportacionException;
import es.um.tds.gestiongastos.importador.ImportadorGastos;
import es.um.tds.gestiongastos.modelo.*;
import es.um.tds.gestiongastos.persistencia.PersistenciaJSON;
import es.um.tds.gestiongastos.repositorio.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Controlador principal de la aplicación.
 * Actúa como fachada que coordina todas las operaciones del sistema.
 * Implementa el patrón Singleton.
 */
public class GestionGastos {
    
    private static GestionGastos instance;
    
    private final RepositorioCategorias repoCategorias;
    private final RepositorioGastos repoGastos;
    private final RepositorioCuentas repoCuentas;
    private final RepositorioAlertas repoAlertas;
    private final HistorialNotificaciones historialNotificaciones;
    private final PersistenciaJSON persistencia;
    
    /**
     * Constructor privado (Singleton).
     */
    private GestionGastos() {
        this.repoCategorias = RepositorioCategorias.getInstance();
        this.repoGastos = RepositorioGastos.getInstance();
        this.repoCuentas = RepositorioCuentas.getInstance();
        this.repoAlertas = RepositorioAlertas.getInstance();
        this.historialNotificaciones = HistorialNotificaciones.getInstance();
        this.persistencia = new PersistenciaJSON();
    }
    
    /**
     * Obtiene la instancia única del controlador.
     * @return Instancia del controlador
     */
    public static synchronized GestionGastos getInstance() {
        if (instance == null) {
            instance = new GestionGastos();
        }
        return instance;
    }
    
    // ==================== INICIALIZACIÓN ====================
    
    /**
     * Inicializa la aplicación cargando datos guardados.
     * @return true si se cargaron datos previos
     */
    public boolean inicializar() {
        try {
            return persistencia.cargar();
        } catch (IOException e) {
            System.err.println("Error al cargar datos: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Guarda todos los datos de la aplicación.
     * @return true si se guardaron correctamente
     */
    public boolean guardar() {
        try {
            persistencia.guardar();
            return true;
        } catch (IOException e) {
            System.err.println("Error al guardar datos: " + e.getMessage());
            return false;
        }
    }
    
    // ==================== GESTIÓN DE GASTOS ====================
    
    /**
     * Registra un nuevo gasto personal.
     * @param cantidad Cantidad del gasto
     * @param categoria Categoría del gasto
     * @param nota Descripción (opcional)
     * @return Gasto creado
     */
    public Gasto registrarGasto(double cantidad, String nota, Categoria categoria) {
        return registrarGasto(cantidad, nota, null, categoria);
    }
    
    /**
     * Registra un nuevo gasto personal completo.
     * @param cantidad Cantidad del gasto
     * @param categoria Categoría del gasto
     * @param nota Descripción
     * @param metodoPago Método de pago
     * @return Gasto creado
     */
    public Gasto registrarGasto(double cantidad, String nota, String metodoPago, Categoria categoria) {
        return registrarGastoCompartido(cantidad, nota, metodoPago, categoria, null, null);
    }
    
    /**
     * Registra un gasto en una cuenta compartida.
     * @param cantidad Cantidad del gasto
     * @param categoria Categoría del gasto
     * @param nota Descripción
     * @param cuenta Cuenta compartida
     * @param pagador Persona que pagó
     * @return Gasto creado
     */
    public Gasto registrarGastoCompartido(double cantidad, String nota, String metodoPago,
    										Categoria categoria, Persona pagador) {
    	return registrarGastoCompartido(cantidad, nota, metodoPago, categoria, null, pagador);
    }
    
    /**
     * Registra un gasto completo (método principal).
     * @param cantidad Cantidad del gasto
     * @param categoria Categoría del gasto
     * @param nota Descripción
     * @param metodoPago Método de pago (puede ser null)
     * @param cuenta Cuenta compartida (null para gasto personal)
     * @param pagador Persona que pagó (null para gasto personal)
     * @return Gasto creado
     */
    private Gasto registrarGastoCompartido(double cantidad, String nota, String metodoPago, Categoria categoria,
    										CuentaCompartida cuenta, Persona pagador) {        
        Gasto gasto = new Gasto(cantidad, java.time.LocalDateTime.now(), nota, metodoPago,
				"EUR",  categoria, pagador);
        if (cuenta != null && pagador != null) {
        	cuenta.addGasto(gasto, pagador);
        }
		repoGastos.addGasto(gasto);
		verificarAlertas();
		return gasto;
    }
    
    /**
     * Elimina un gasto.
     * @param gasto Gasto a eliminar
     * @return true si se eliminó correctamente
     */
    public boolean eliminarGasto(Gasto gasto) {
        return repoGastos.deleteGasto(gasto);
    }
    
    /**
     * Obtiene todos los gastos.
     * @return Lista de todos los gastos
     */
    public List<Gasto> getGastos() {
        return repoGastos.getTodosGastos();
    }
    
    /**
     * Obtiene los gastos personales.
     * @return Lista de gastos personales
     */
    public List<Gasto> getGastosPersonales() {
        return repoGastos.getGastosPersonales();
    }
    
    /**
     * Obtiene los gastos filtrados.
     * @param filtro Filtro a aplicar
     * @return Lista de gastos filtrados
     */
    public List<Gasto> getGastosPorFiltro(Filtro filtro) {
        return repoGastos.getGastosByFiltro(filtro);
    }
    
    /**
     * Obtiene los gastos de una categoría.
     * @param categoria Categoría a buscar
     * @return Lista de gastos de esa categoría
     */
    public List<Gasto> getGastosPorCategoria(Categoria categoria) {
        return repoGastos.getGastosByCategoria(categoria);
    }
    
    /**
     * Calcula el total de gastos.
     * @return Total de todos los gastos
     */
    public double getTotalGastos() {
        return repoGastos.calcularTotal();
    }
    
    /**
     * Calcula el total de gastos filtrados.
     * @param filtro Filtro a aplicar
     * @return Total de gastos filtrados
     */
    public double getTotalGastos(Filtro filtro) {
        return repoGastos.calcularTotal(filtro);
    }
    
    // ==================== GESTIÓN DE CATEGORÍAS ====================
    
    /**
     * Crea una nueva categoría personalizada.
     * @param nombre Nombre de la categoría
     * @return Categoría creada
     * @throws IllegalArgumentException si ya existe
     */
    public Categoria crearCategoria(String nombre) {
        if (repoCategorias.existsByNombre(nombre)) {
            throw new IllegalArgumentException("Ya existe una categoría con ese nombre");
        }
        Categoria categoria = new Categoria(nombre);
        repoCategorias.addCategoria(categoria);
        return categoria;
    }
    
    /**
     * Obtiene todas las categorías.
     * @return Lista de categorías
     */
    public List<Categoria> getCategorias() {
        return repoCategorias.getTodasCategorias();
    }
    
    /**
     * Obtiene las categorías predefinidas.
     * @return Lista de categorías predefinidas
     */
    public List<Categoria> getCategoriasPredefinidas() {
        return repoCategorias.getCategoriasPredefinidas();
    }
    
    /**
     * Obtiene las categorías personalizadas.
     * @return Lista de categorías personalizadas
     */
    public List<Categoria> getCategoriasPersonalizadas() {
        return repoCategorias.getCategoriasPersonalizadas();
    }
    
    /**
     * Busca una categoría por nombre.
     * @param nombre Nombre a buscar
     * @return Optional con la categoría
     */
    public Optional<Categoria> buscarCategoria(String nombre) {
        return repoCategorias.getCategoriaByNombre(nombre);
    }
    
    /**
     * Elimina una categoría personalizada.
     * @param categoria Categoría a eliminar
     * @return true si se eliminó
     */
    public boolean eliminarCategoria(Categoria categoria) {
        return repoCategorias.deleteCategoria(categoria);
    }
    
    /**
     * Intenta cambiar una categoría
     * @param categoria Categoría a modificar
     * @param nombre Nuevo Nombre
     * @return 0 en cambio exitoso
     *        -1 si nombre inválido
     *        -2 si categoría predefinida
     *        -3 si categoría no existe
     *        -4 si el nombre ya existe
     */
    public int editarNombreCategoría(Categoria categoria, String nombre) {
    	return repoCategorias.editNameCategoria(categoria, nombre);
    }
    
    // ==================== GESTIÓN DE CUENTAS COMPARTIDAS ====================
    
    /**
     * Crea una cuenta compartida equitativa.
     * @param nombre Nombre de la cuenta
     * @param personas Personas que participan
     * @return Cuenta creada
     */
    public CuentaEquitativa crearCuentaEquitativa(String nombre, Persona... personas) {
        CuentaEquitativa cuenta = new CuentaEquitativa(nombre, personas);
        repoCuentas.addCuenta(cuenta);
        return cuenta;
    }
    
    /**
     * Crea una cuenta compartida equitativa.
     * @param nombre Nombre de la cuenta
     * @param personas Lista de personas
     * @return Cuenta creada
     */
    public CuentaEquitativa crearCuentaEquitativa(String nombre, List<Persona> personas) {
        CuentaEquitativa cuenta = new CuentaEquitativa(nombre, personas);
        repoCuentas.addCuenta(cuenta);
        return cuenta;
    }
    
    /**
     * Crea una cuenta compartida por porcentajes.
     * @param nombre Nombre de la cuenta
     * @param personas Lista de personas
     * @param porcentajes Mapa de porcentajes
     * @return Cuenta creada
     */
    public CuentaPorcentaje crearCuentaPorcentaje(String nombre, List<Persona> personas,
                                                   Map<Persona, Double> porcentajes) {
        CuentaPorcentaje cuenta = new CuentaPorcentaje(nombre, personas, porcentajes);
        repoCuentas.addCuenta(cuenta);
        return cuenta;
    }
    
    /**
     * Obtiene todas las cuentas compartidas.
     * @return Lista de cuentas
     */
    public List<CuentaCompartida> getCuentas() {
        return repoCuentas.getTotalCuentas();
    }
    
    /**
     * Busca una cuenta por nombre.
     * @param nombre Nombre a buscar
     * @return Optional con la cuenta
     */
    public Optional<CuentaCompartida> buscarCuenta(String nombre) {
        return repoCuentas.getCuentaByNombre(nombre);
    }
    
    /**
     * Obtiene las cuentas de una persona.
     * @param persona Persona a buscar
     * @return Lista de cuentas donde participa
     */
    public List<CuentaCompartida> getCuentas(Persona persona) {
        return repoCuentas.getCuentasByPersona(persona);
    }
    
    /**
     * Elimina una cuenta compartida.
     * @param cuenta Cuenta a eliminar
     * @return true si se eliminó
     */
    public boolean eliminarCuenta(CuentaCompartida cuenta) {
        return repoCuentas.removeCuenta(cuenta);
    }
    
    // ==================== GESTIÓN DE ALERTAS ====================
    
    /**
     * Crea una alerta general.
     * @param topeGasto Tope de gasto
     * @param tipo Tipo de alerta (semanal/mensual)
     * @return Alerta creada
     */
    public Alerta crearAlerta(double topeGasto, TipoAlerta tipo) {
        Alerta alerta = new Alerta(topeGasto, tipo);
        repoAlertas.addAlerta(alerta);
        return alerta;
    }
    
    /**
     * Crea una alerta para una categoría específica.
     * @param topeGasto Tope de gasto
     * @param tipo Tipo de alerta
     * @param categoria Categoría a monitorear
     * @return Alerta creada
     */
    public Alerta crearAlerta(double topeGasto, TipoAlerta tipo, Categoria categoria) {
        Alerta alerta = new Alerta(topeGasto, tipo, categoria);
        repoAlertas.addAlerta(alerta);
        return alerta;
    }
    
    /**
     * Obtiene todas las alertas.
     * @return Lista de alertas
     */
    public List<Alerta> getAlertas() {
        return repoAlertas.getTotalAlertas();
    }
    
    /**
     * Obtiene las alertas activas.
     * @return Lista de alertas activas
     */
    public List<Alerta> getAlertasActivas() {
        return repoAlertas.getAlertasActivas();
    }
    
    /**
     * Elimina una alerta.
     * @param alerta Alerta a eliminar
     * @return true si se eliminó
     */
    public boolean eliminarAlerta(Alerta alerta) {
        return repoAlertas.removeAlerta(alerta);
    }
    
    /**
     * Verifica todas las alertas activas y genera notificaciones si es necesario.
     */
    public void verificarAlertas() {
        List<Gasto> gastos = repoGastos.getTodosGastos();
        
        for (Alerta alerta : repoAlertas.getAlertasActivas()) {
            alerta.generarNotificacion(gastos)
                  .ifPresent(historialNotificaciones::addNotificacion);
        }
    }
    
    // ==================== GESTIÓN DE NOTIFICACIONES ====================
    
    /**
     * Obtiene todas las notificaciones.
     * @return Lista de notificaciones
     */
    public List<Notificacion> getNotificaciones() {
        return historialNotificaciones.getNotificacionesOrdenadas();
    }
    
    /**
     * Obtiene las notificaciones no leídas.
     * @return Lista de notificaciones no leídas
     */
    public List<Notificacion> getNotificacionesNoLeidas() {
        return historialNotificaciones.getNoLeidas();
    }
    
    /**
     * Obtiene el número de notificaciones no leídas.
     * @return Número de no leídas
     */
    public int getNumeroNotificacionesNoLeidas() {
        return historialNotificaciones.countNoLeidas();
    }
    
    /**
     * Marca todas las notificaciones como leídas.
     */
    public void marcarNotificacionesComoLeidas() {
        historialNotificaciones.marcarTodasNotificacionesComoLeidas();
    }
    
    /**
     * Elimina las notificaciones leídas.
     * @return Número de notificaciones eliminadas
     */
    public int limpiarNotificacionesLeidas() {
        return historialNotificaciones.removeNotificacionesLeidas();
    }
    
    // ==================== IMPORTACIÓN ====================
    
    /**
     * Importa gastos desde un archivo.
     * @param rutaArchivo Ruta del archivo
     * @return Número de gastos importados
     * @throws IOException si hay error de lectura
     * @throws ImportacionException si hay error de formato
     */
    public int importarGastos(String rutaArchivo) throws IOException, ImportacionException {
        FactoriaImportadores factoria = FactoriaImportadores.getInstance();
        ImportadorGastos importador = factoria.getImportador(rutaArchivo);
        
        List<Gasto> gastosImportados = importador.importar(rutaArchivo);
        
        for (Gasto gasto : gastosImportados) {
            repoGastos.addGasto(gasto);
        }
        
        verificarAlertas();
        return gastosImportados.size();
    }
    
    /**
     * Verifica si se puede importar un archivo.
     * @param rutaArchivo Ruta del archivo
     * @return true si hay importador disponible
     */
    public boolean puedeImportar(String rutaArchivo) {
        return FactoriaImportadores.getInstance().existeImportador(rutaArchivo);
    }
    
    // ==================== ESTADÍSTICAS ====================
    
    /**
     * Obtiene el gasto total por categoría.
     * @return Mapa de categoría a total
     */
    public Map<Categoria, Double> getGastosPorCategoriaAgrupados() {
        return repoGastos.getTodosGastos().stream()
                .collect(Collectors.groupingBy(
                    Gasto::getCategoria,
                    Collectors.summingDouble(Gasto::getCantidad)
                ));
    }
    
    /**
     * Obtiene el gasto total por mes.
     * @return Mapa de mes a total
     */
    public Map<Month, Double> getGastosPorMes() {
        return repoGastos.getTodosGastos().stream()
                .collect(Collectors.groupingBy(
                    g -> g.getFecha().getMonth(),
                    Collectors.summingDouble(Gasto::getCantidad)
                ));
    }
    
    /**
     * Obtiene el gasto total en un rango de fechas.
     * @param inicio Fecha inicio
     * @param fin Fecha fin
     * @return Total de gastos en el rango
     */
    public double getGastosEnRango(LocalDate inicio, LocalDate fin) {
        Filtro filtro = new Filtro();
        filtro.setRangoFechas(inicio, fin);
        return repoGastos.calcularTotal(filtro);
    }
    
    // ==================== UTILIDADES ====================
    
    /**
     * Reinicia la instancia del controlador (para testing).
     */
    public static synchronized void resetInstance() {
        instance = null;
    }
}