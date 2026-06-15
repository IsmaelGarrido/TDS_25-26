package es.um.tds.gestiongastos.vista;

import es.um.tds.gestiongastos.controlador.GestionGastos;
import es.um.tds.gestiongastos.modelo.Categoria;
import es.um.tds.gestiongastos.modelo.Persona;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListCell;
import javafx.scene.layout.Region;

import java.time.format.DateTimeFormatter;

/**
 * Clase base abstracta para todas las secciones de la aplicación.
 *
 * Cada sección concreta extiende esta clase e implementa:
 *   - getNombre()  → etiqueta que aparece en el botón de navegación
 *   - getView()    → Region raíz que se coloca en el ContentArea
 *
 * La clase base provee:
 *   - Referencia al controlador (singleton)
 *   - Formato de fecha estándar compartido
 *   - Diálogos de error y confirmación reutilizables
 *   - Validación numérica compartida
 *   - Celdas de ListCell para Categoria y Persona (inner classes estáticas)
 */
public abstract class SeccionView {

    /** Formato de fecha estándar para toda la aplicación. */
    protected static final DateTimeFormatter FECHA_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /** Formato de fecha y hora para notificaciones e importación. */
    protected static final DateTimeFormatter FECHA_HORA_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /** Controlador Singleton accesible desde todas las secciones. */
    protected final GestionGastos controlador = GestionGastos.getInstance();

    /**
     * Nombre de la sección. Se usa como etiqueta del botón de navegación.
     * Ejemplo: "Gastos", "Alertas", "Categorías"…
     */
    public abstract String getNombre();

    /**
     * Ruta del recurso de imagen para el icono de navegación.
     * Devuelve null si la sección no tiene icono asignado aún.
     * Ejemplo: "/images/nav/gastos.png"
     */
    public String getIconPath() {
        return null;
    }

    /**
     * Devuelve el nodo raíz de la sección.
     * MainView llama a este método cada vez que el usuario navega a esta sección.
     */
    public abstract Region getView();

    /**
     * Muestra un diálogo de error modal.
     *
     * @param mensaje Texto del error
     */
    protected void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    /**
     * Muestra un diálogo de confirmación modal.
     *
     * @param titulo    Título de la ventana
     * @param cabecera  Texto en negrita
     * @param contenido Detalle de la operación
     * @return true si el usuario pulsó OK
     */
    protected boolean mostrarConfirmacion(String titulo, String cabecera, String contenido) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(cabecera);
        alert.setContentText(contenido);
        return alert.showAndWait()
                    .filter(r -> r == ButtonType.OK)
                    .isPresent();
    }

    /**
     * Muestra un diálogo de información modal.
     *
     * @param titulo   Título de la ventana
     * @param mensaje  Texto informativo
     */
    protected void mostrarInfo(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    /**
     * Comprueba si un texto representa un número positivo.
     * Acepta tanto punto como coma como separador decimal.
     *
     * @param texto Texto a validar
     * @return true si es un número positivo
     */
    protected boolean esNumeroPositivo(String texto) {
        if (texto == null || texto.isBlank()) return false;
        try {
            return Double.parseDouble(texto.trim().replace(',', '.')) > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Parsea un texto como double.
     * Acepta tanto punto como coma como separador decimal.
     *
     * @param texto Texto a parsear
     * @return Valor double
     * @throws NumberFormatException si el texto no es un número válido
     */
    protected double parsearDouble(String texto) {
        return Double.parseDouble(texto.trim().replace(',', '.'));
    }
    /**
     * ListCell para ComboBox/ListView de Categoria.
     * Muestra el nombre de la categoría, o un texto alternativo si es null.
     */
    public static class CategoriaCell extends ListCell<Categoria> {

        private final String textoNull;

        /** Constructor con texto alternativo para el elemento null. */
        public CategoriaCell(String textoNull) {
            this.textoNull = textoNull;
        }

        /** Constructor por defecto: muestra "— Todas las categorías" para null. */
        public CategoriaCell() {
            this("— Todas las categorías");
        }

        @Override
        protected void updateItem(Categoria cat, boolean empty) {
            super.updateItem(cat, empty);
            if (empty) {
                setText(null);
            } else if (cat == null) {
                setText(textoNull);
            } else {
                setText(cat.getNombre() + (cat.isBase() ? "" : ""));
            }
        }
    }

    /**
     * ListCell para ComboBox/ListView de Persona.
     * Muestra el nombre de la persona.
     */
    public static class PersonaCell extends ListCell<Persona> {
        @Override
        protected void updateItem(Persona persona, boolean empty) {
            super.updateItem(persona, empty);
            setText(empty || persona == null ? null : persona.getNombre());
        }
    }
}