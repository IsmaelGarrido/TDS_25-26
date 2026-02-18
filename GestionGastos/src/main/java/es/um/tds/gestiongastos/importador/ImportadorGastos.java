package es.um.tds.gestiongastos.importador;

import es.um.tds.gestiongastos.modelo.Gasto;

import java.io.IOException;
import java.util.List;

/**
 * Interfaz que define el contrato para importar gastos desde diferentes formatos.
 * Implementa el patrón Adaptador para adaptar diferentes fuentes de datos
 * al formato interno de la aplicación.
 */
public interface ImportadorGastos {
    
    /**
     * Importa gastos desde un archivo.
     * @param rutaArchivo Ruta del archivo a importar
     * @return Lista de gastos importados
     * @throws IOException si hay error de lectura
     * @throws ImportacionException si hay error en el formato de datos
     */
    List<Gasto> importar(String rutaArchivo) throws IOException, ImportacionException;
    
    /**
     * Obtiene las extensiones de archivo soportadas.
     * @return Array de extensiones (ej: ["csv"], ["json"])
     */
    String[] getExtensionesPermitidas();
    
    /**
     * Verifica si el importador soporta un archivo.
     * @param rutaArchivo Ruta del archivo
     * @return true si el archivo es soportado
     */
    default boolean soportaArchivo(String rutaArchivo) {
        if (rutaArchivo == null || rutaArchivo.isEmpty()) {
            return false;
        }
        
        String rutaLower = rutaArchivo.toLowerCase();
        for (String ext : getExtensionesPermitidas()) {
            if (rutaLower.endsWith("." + ext.toLowerCase())) {
                return true;
            }
        }
        return false;
    }
}