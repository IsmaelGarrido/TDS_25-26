package es.um.tds.gestiongastos.importador;

/**
 * Excepción lanzada cuando hay un error durante la importación de gastos.
 */
public class ImportacionException extends Exception {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * Constructor con mensaje.
     * @param mensaje Mensaje de error
     */
    public ImportacionException(String mensaje) {
        super(mensaje);
    }
    
    /**
     * Constructor con mensaje y causa.
     * @param mensaje Mensaje de error
     * @param causa Excepción que causó el error
     */
    public ImportacionException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
    
    /**
     * Constructor con causa.
     * @param causa Excepción que causó el error
     */
    public ImportacionException(Throwable causa) {
        super(causa);
    }
}