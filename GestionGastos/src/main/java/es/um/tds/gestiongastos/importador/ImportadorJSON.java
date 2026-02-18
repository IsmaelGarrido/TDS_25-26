package es.um.tds.gestiongastos.importador;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import es.um.tds.gestiongastos.modelo.Categoria;
import es.um.tds.gestiongastos.modelo.Gasto;
import es.um.tds.gestiongastos.modelo.Persona;
import es.um.tds.gestiongastos.repositorio.RepositorioCategorias;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Importador de gastos desde archivos JSON.
 * Adapta el formato JSON externo al modelo interno de la aplicación.
 * 
 * Soporta dos formatos:
 * 1. Array de gastos con estructura similar al CSV
 * 2. Formato interno de la aplicación (exportado por PersistenciaJSON)
 */
public class ImportadorJSON implements ImportadorGastos {
    
    private static final String[] EXTENSIONES = {"json"};
    
    private final ObjectMapper mapper;
    
    public ImportadorJSON() {
        this.mapper = configurarMapper();
    }
    
    private ObjectMapper configurarMapper() {
        ObjectMapper om = new ObjectMapper();
        om.registerModule(new JavaTimeModule());
        om.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        om.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        return om;
    }
    
    @Override
    public List<Gasto> importar(String rutaArchivo) throws IOException, ImportacionException {
        if (rutaArchivo == null || rutaArchivo.isEmpty()) {
            throw new ImportacionException("La ruta del archivo no puede ser vacía");
        }
        
        File file = new File(rutaArchivo);
        if (!file.exists()) {
            throw new ImportacionException("El archivo no existe: " + rutaArchivo);
        }
        
        try {
            // Intentar leer como array de objetos genéricos
            List<Map<String, Object>> data = mapper.readValue(
                file, 
                new TypeReference<List<Map<String, Object>>>() {}
            );
            
            return convertirAGastos(data);
            
        } catch (IOException e) {
            throw new ImportacionException("Error al leer el archivo JSON: " + e.getMessage(), e);
        }
    }
    
    /**
     * Convierte la lista de mapas a objetos Gasto.
     */
    private List<Gasto> convertirAGastos(List<Map<String, Object>> data) throws ImportacionException {
        List<Gasto> expenses = new ArrayList<>();
        int index = 0;
        
        for (Map<String, Object> record : data) {
            index++;
            try {
                Gasto expense = convertirRegistro(record);
                expenses.add(expense);
            } catch (Exception e) {
                throw new ImportacionException(
                    String.format("Error en registro %d: %s", index, e.getMessage()), e);
            }
        }
        
        return expenses;
    }
    
    /**
     * Convierte un registro (mapa) a un objeto Gasto.
     */
    private Gasto convertirRegistro(Map<String, Object> registro) throws ImportacionException {
        // Obtener campos (soporta múltiples nombres)
        double amount = obtenerCantidad(registro);
        LocalDateTime date = obtenerFecha(registro);
        String categoryName = obtenerString(registro, "category", "subcategory", "categoria");
        String note = obtenerString(registro, "note", "nota", "description");
        String paymentMethod = obtenerString(registro, "paymentMethod", "metodoPago", "method");
        String currency = obtenerStringODefault(registro, "EUR", "currency", "moneda");
        String payerName = obtenerString(registro, "payer", "pagador");
        String accountType = obtenerString(registro, "account", "cuenta");
        
        // Validar campos obligatorios
        if (categoryName == null || categoryName.isEmpty()) {
            throw new ImportacionException("Categoría es obligatoria");
        }
        
        // Obtener o crear categoría
        Categoria category = obtenerOCrearCategoria(categoryName);
        
        // Determinar pagador
        Persona payer = null;
        if (accountType != null && !accountType.equalsIgnoreCase("Personal")) {
            if (payerName != null && !payerName.isEmpty() && 
                !payerName.equalsIgnoreCase("Me")) {
                payer = new Persona(payerName);
            }
        }
        
        return new Gasto(amount, date, note, paymentMethod, currency, category, payer);
    }
    
    /**
     * Obtiene la cantidad del registro.
     */
    private double obtenerCantidad(Map<String, Object> registro) throws ImportacionException {
        Object value = obtenerValor(registro, "amount", "cantidad", "importe");
        
        if (value == null) {
            throw new ImportacionException("Cantidad es obligatoria");
        }
        
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        
        try {
            return Double.parseDouble(value.toString().replace(",", "."));
        } catch (NumberFormatException e) {
            throw new ImportacionException("Cantidad inválida: " + value);
        }
    }
    
    /**
     * Obtiene la fecha del registro.
     */
    private LocalDateTime obtenerFecha(Map<String, Object> registro) {
        Object value = obtenerValor(registro, "date", "fecha", "dateTime");
        
        if (value == null) {
            return LocalDateTime.now();
        }
        
        if (value instanceof String) {
            try {
                return LocalDateTime.parse((String) value);
            } catch (Exception e) {
                // Si no puede parsear, usar fecha actual
                return LocalDateTime.now();
            }
        }
        
        return LocalDateTime.now();
    }
    
    /**
     * Obtiene un valor String del registro buscando múltiples claves.
     */
    private String obtenerString(Map<String, Object> registro, String... claves) {
        Object value = obtenerValor(registro, claves);
        return (value != null) ? value.toString().trim() : null;
    }
    
    /**
     * Obtiene un valor String o un valor por defecto.
     */
    private String obtenerStringODefault(Map<String, Object> registro, String valorDefault, String... claves) {
        String value = obtenerString(registro, claves);
        return (value != null && !value.isEmpty()) ? value : valorDefault;
    }
    
    /**
     * Obtiene un valor del registro buscando múltiples claves posibles.
     */
    private Object obtenerValor(Map<String, Object> registro, String... keys) {
        for (String key : keys) {
            // Buscar clave exacta
            if (registro.containsKey(key)) {
                return registro.get(key);
            }
            // Buscar ignorando mayúsculas
            for (String k : registro.keySet()) {
                if (k.equalsIgnoreCase(key)) {
                    return registro.get(k);
                }
            }
        }
        return null;
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