package es.um.tds.gestiongastos.importador;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Factoría que crea el importador adecuado según el tipo de archivo.
 * Implementa el patrón Factoría para desacoplar la creación de importadores.
 */
public class FactoriaImportadores {
    
    private static FactoriaImportadores instance;
    
    private final Map<String, ImportadorGastos> importadores;
    
    /**
     * Constructor privado (Singleton).
     */
    private FactoriaImportadores() {
        this.importadores = new HashMap<>();
        registrarImportadoresPorDefecto();
    }
    
    /**
     * Obtiene la instancia única de la factoría.
     * @return Instancia de la factoría
     */
    public static synchronized FactoriaImportadores getInstance() {
        if (instance == null) {
            instance = new FactoriaImportadores();
        }
        return instance;
    }
    
    /**
     * Registra los importadores por defecto.
     */
    private void registrarImportadoresPorDefecto() {
        registrarImportador(new ImportadorCSV());
        registrarImportador(new ImportadorJSON());
    }
    
    /**
     * Registra un importador para sus extensiones soportadas.
     * @param importador Importador a registrar
     */
    public void registrarImportador(ImportadorGastos importador) {
        if (importador == null) {
            return;
        }
        
        for (String extension : importador.getExtensionesPermitidas()) {
            importadores.put(extension.toLowerCase(), importador);
        }
    }
    
    /**
     * Obtiene el importador adecuado para un archivo.
     * @param rutaArchivo Ruta del archivo a importar
     * @return Importador adecuado
     * @throws ImportacionException si no hay importador para el tipo de archivo
     */
    public ImportadorGastos getImportador(String rutaArchivo) throws ImportacionException {
        if (rutaArchivo == null || rutaArchivo.isEmpty()) {
            throw new ImportacionException("La ruta del archivo no puede ser vacía");
        }
        
        String extension = obtenerExtension(rutaArchivo);
        
        if (extension == null || extension.isEmpty()) {
            throw new ImportacionException("El archivo no tiene extensión: " + rutaArchivo);
        }
        
        ImportadorGastos importador = importadores.get(extension.toLowerCase());
        
        if (importador == null) {
            throw new ImportacionException(
                String.format("No hay importador registrado para archivos .%s. Extensiones soportadas: %s",
                    extension, getExtensionesRegistradas()));
        }
        
        return importador;
    }
    
    /**
     * Obtiene la extensión de un archivo.
     */
    private String obtenerExtension(String rutaArchivo) {
        int ultimoPunto = rutaArchivo.lastIndexOf('.');
        if (ultimoPunto == -1 || ultimoPunto == rutaArchivo.length() - 1) {
            return null;
        }
        return rutaArchivo.substring(ultimoPunto + 1);
    }
    
    /**
     * Verifica si hay un importador registrado para un archivo.
     * @param rutaArchivo Ruta del archivo
     * @return true si hay importador disponible
     */
    public boolean existeImportador(String rutaArchivo) {
        if (rutaArchivo == null || rutaArchivo.isEmpty()) {
            return false;
        }
        
        String extension = obtenerExtension(rutaArchivo);
        return extension != null && importadores.containsKey(extension.toLowerCase());
    }
    
    /**
     * Obtiene las extensiones registradas.
     * @return Set de extensiones soportadas
     */
    public Set<String> getExtensionesRegistradas() {
        return Set.copyOf(importadores.keySet());
    }
    
    /**
     * Reinicia la factoría (para testing).
     */
    public static synchronized void resetInstance() {
        instance = null;
    }
}