package es.um.tds.gestiongastos.importador;

import es.um.tds.gestiongastos.modelo.Categoria;
import es.um.tds.gestiongastos.modelo.Gasto;
import es.um.tds.gestiongastos.modelo.Persona;
import es.um.tds.gestiongastos.repositorio.RepositorioCategorias;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Importador de gastos desde archivos CSV.
 * Adapta el formato CSV externo al modelo interno de la aplicación.
 * 
 * Formato esperado del CSV:
 * Date,Account,Category,Subcategory,Note,Payer,Amount,Currency
 */
public class ImportadorCSV implements ImportadorGastos {
    
    private static final String SEPARADOR = ",";
    private static final String[] EXTENSIONES = {"csv"};
    
    // Índices de columnas según el formato del CSV de ejemplo
    private static final int COL_DATE = 0;
    private static final int COL_ACCOUNT = 1;
    private static final int COL_CATEGORY = 2;      // Método de pago en el CSV de ejemplo
    private static final int COL_SUBCATEGORY = 3;   // Categoría real del gasto
    private static final int COL_NOTE = 4;
    private static final int COL_PAYER = 5;
    private static final int COL_AMOUNT = 6;
    private static final int COL_CURRENCY = 7;
    
    private static final int NUM_COLUMNAS = 8;
    
    // Formatos de fecha posibles
    private static final DateTimeFormatter[] FORMATOS_FECHA = {
        DateTimeFormatter.ofPattern("M/d/yyyy H:mm"),
        DateTimeFormatter.ofPattern("M/d/yyyy HH:mm"),
        DateTimeFormatter.ofPattern("d/M/yyyy H:mm"),
        DateTimeFormatter.ofPattern("d/M/yyyy HH:mm"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
    };
    
    @Override
    public List<Gasto> importar(String rutaArchivo) throws IOException, ImportacionException {
        if (rutaArchivo == null || rutaArchivo.isEmpty()) {
            throw new ImportacionException("La ruta del archivo no puede ser vacía");
        }
        
        List<Gasto> expenses = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(rutaArchivo))) {
            String line;
            int lineNumber = 0;
            
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                
                // Saltar cabecera
                if (lineNumber == 1) {
                    validarCabecera(line);
                    continue;
                }
                
                // Saltar líneas vacías
                if (line.trim().isEmpty()) {
                    continue;
                }
                
                try {
                    Gasto expense = parsearLinea(line, lineNumber);
                    expenses.add(expense);
                } catch (Exception e) {
                    throw new ImportacionException(
                        String.format("Error en línea %d: %s", lineNumber, e.getMessage()), e);
                }
            }
        }
        
        return expenses;
    }
    
    /**
     * Valida que la cabecera del CSV sea correcta.
     */
    private void validarCabecera(String cabecera) throws ImportacionException {
        String[] columns = cabecera.split(SEPARADOR);
        if (columns.length < NUM_COLUMNAS) {
            throw new ImportacionException(
                String.format("Cabecera inválida: se esperan %d columnas, encontradas %d", 
                    NUM_COLUMNAS, columns.length));
        }
    }
    
    /**
     * Parsea una línea del CSV y crea un Gasto.
     */
    private Gasto parsearLinea(String linea, int numeroLinea) throws ImportacionException {
        String[] fields = linea.split(SEPARADOR);
        
        if (fields.length < NUM_COLUMNAS) {
            throw new ImportacionException(
                String.format("Número de columnas incorrecto: se esperan %d, encontradas %d",
                    NUM_COLUMNAS, fields.length));
        }
        
        // Parsear campos
        LocalDateTime date = parsearFecha(fields[COL_DATE].trim());
        String account = fields[COL_ACCOUNT].trim();
        String paymentMethod = fields[COL_CATEGORY].trim();
        String categoryName = fields[COL_SUBCATEGORY].trim();
        String note = fields[COL_NOTE].trim();
        String payerName = fields[COL_PAYER].trim();
        double amount = parsearCantidad(fields[COL_AMOUNT].trim());
        String currency = fields[COL_CURRENCY].trim();
        
        // Obtener o crear categoría
        Categoria category = obtenerOCrearCategoria(categoryName);
        
        // Determinar si es gasto personal o compartido
        Persona payer = null;
        if (!account.equalsIgnoreCase("Personal") && !payerName.equalsIgnoreCase("Me")) {
            payer = new Persona(payerName);
        }
        
        return new Gasto(amount, date, note, paymentMethod, currency, category, payer);
    }
    
    /**
     * Parsea la fecha intentando varios formatos.
     */
    private LocalDateTime parsearFecha(String fechaStr) throws ImportacionException {
        for (DateTimeFormatter format : FORMATOS_FECHA) {
            try {
                return LocalDateTime.parse(fechaStr, format);
            } catch (DateTimeParseException e) {
                // Intentar siguiente formato
            }
        }
        throw new ImportacionException("Formato de fecha no reconocido: " + fechaStr);
    }
    
    /**
     * Parsea la cantidad del gasto.
     */
    private double parsearCantidad(String cantidadStr) throws ImportacionException {
        try {
            // Manejar tanto punto como coma decimal
            String normalized = cantidadStr.replace(",", ".");
            return Double.parseDouble(normalized);
        } catch (NumberFormatException e) {
            throw new ImportacionException("Cantidad inválida: " + cantidadStr);
        }
    }
    
    /**
     * Obtiene una categoría existente o crea una nueva.
     */
    private Categoria obtenerOCrearCategoria(String nombreCategoria) {
        RepositorioCategorias repo = RepositorioCategorias.getInstance();
        
        return repo.getCategoriaByNombre(nombreCategoria)
                .orElseGet(() -> {
                    Categoria newCat = new Categoria(nombreCategoria);
                    repo.addCategoria(newCat);
                    return newCat;
                });
    }
    
    @Override
    public String[] getExtensionesPermitidas() {
        return EXTENSIONES.clone();
    }
}