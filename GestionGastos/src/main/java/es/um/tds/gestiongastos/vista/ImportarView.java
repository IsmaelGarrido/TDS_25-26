package es.um.tds.gestiongastos.vista;

import es.um.tds.gestiongastos.importador.ImportacionException;
import es.um.tds.gestiongastos.modelo.Gasto;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Sección de importación de gastos desde CSV o JSON.
 *
 * Layout (VBox centrada, max 560px):
 *   ├── Zona de selección  — botón + nombre del fichero seleccionado
 *   ├── TableView preview  — primeras filas del fichero
 *   ├── Label de estado    — nº filas / mensaje de error
 *   └── Barra de acciones  — Cancelar | Importar N filas
 */
public class ImportarView extends SeccionView {

    private static final int MAX_PREVIEW = 10;

    private File ficheroSeleccionado;

    private Label                   lblFichero;
    private ObservableList<Gasto>   previewItems;
    private Label                   lblEstado;
    private Button                  btnImportar;
    private Button                  btnCancelar;

    @Override 
    public String getNombre() { return "Importar"; }
    
    @Override
    public String getIconPath() {return "/images/nav/importar.png";}

    @Override
    public Region getView() {
        ficheroSeleccionado = null;
        return buildView();
    }

    private Region buildView() {
        VBox inner = new VBox(16);
        inner.setMaxWidth(560);
        inner.setPadding(new Insets(24, 0, 24, 0));

        inner.getChildren().addAll(
            buildZonaSeleccion(),
            buildTablaPreview(),
            buildEstado(),
            buildBarraAcciones()
        );

        StackPane wrapper = new StackPane(inner);
        wrapper.setPadding(new Insets(0, 24, 0, 24));
        StackPane.setAlignment(inner, Pos.TOP_CENTER);

        return wrapper;
    }

    private VBox buildZonaSeleccion() {
        VBox zona = new VBox(10);
        zona.setAlignment(Pos.CENTER);
        zona.setPadding(new Insets(20));
        zona.getStyleClass().add("drop-zone");

        Label lblIcono = new Label("↑");
        lblIcono.setFont(Font.font("System", FontWeight.BOLD, 32));
        lblIcono.setStyle("-fx-text-fill:-fx-text-secondary;");

        Label lblInstruccion = new Label("Selecciona un fichero CSV o JSON");
        lblInstruccion.setFont(Font.font("System", 14));
        lblInstruccion.setStyle("-fx-text-fill:-fx-text-secondary;");

        Button btnSeleccionar = new Button("Seleccionar fichero...");
        btnSeleccionar.getStyleClass().add("btn-accion");
        btnSeleccionar.setOnAction(e -> abrirSelector());

        lblFichero = new Label("Ningún fichero seleccionado");
        lblFichero.setStyle("-fx-text-fill:-fx-text-secondary;-fx-font-size:12;");

        zona.getChildren().addAll(lblIcono, lblInstruccion, btnSeleccionar, lblFichero);
        return zona;
    }

    private VBox buildTablaPreview() {
        VBox contenedor = new VBox(6);

        Label titulo = new Label("Vista previa");
        titulo.setFont(Font.font("System", FontWeight.BOLD, 13));
        titulo.getStyleClass().add("panel-titulo");

        previewItems = FXCollections.observableArrayList();
        TableView<Gasto> tabla = buildTablaPreviewTable();

        contenedor.getChildren().addAll(titulo, tabla);
        return contenedor;
    }

    private TableView<Gasto> buildTablaPreviewTable() {
        TableView<Gasto> tabla = new TableView<>(previewItems);
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tabla.setPrefHeight(200);
        tabla.setPlaceholder(new Label("Selecciona un fichero para ver la vista previa"));
        tabla.getStyleClass().add("tabla-gastos");

        TableColumn<Gasto, String> colFecha = new TableColumn<>("Fecha");
        colFecha.setPrefWidth(120);
        colFecha.setCellValueFactory(d ->
            new SimpleStringProperty(d.getValue().getFecha().format(FECHA_HORA_FMT)));

        TableColumn<Gasto, String> colCat = new TableColumn<>("Categoría");
        colCat.setPrefWidth(120);
        colCat.setCellValueFactory(d ->
            new SimpleStringProperty(d.getValue().getCategoria().getNombre()));

        TableColumn<Gasto, String> colNota = new TableColumn<>("Nota");
        colNota.setCellValueFactory(d ->
            new SimpleStringProperty(d.getValue().getNota().orElse("—")));

        TableColumn<Gasto, String> colCantidad = new TableColumn<>("Cantidad");
        colCantidad.setPrefWidth(90);
        colCantidad.setCellValueFactory(d ->
            new SimpleStringProperty(
                String.format("%.2f %s",
                    d.getValue().getCantidad(),
                    d.getValue().getMoneda())));
        colCantidad.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); }
                else { setText(item); setStyle("-fx-alignment:CENTER-RIGHT;"); }
            }
        });

        tabla.getColumns().add(colFecha);
        tabla.getColumns().add(colCat);
        tabla.getColumns().add(colNota);
        tabla.getColumns().add(colCantidad);

        return tabla;
    }

    private Label buildEstado() {
        lblEstado = new Label("");
        lblEstado.setStyle("-fx-font-size:12;");
        return lblEstado;
    }

    private HBox buildBarraAcciones() {
        HBox barra = new HBox(8);
        barra.setAlignment(Pos.CENTER_RIGHT);

        btnCancelar = new Button("Cancelar");
        btnCancelar.getStyleClass().add("btn-accion");
        btnCancelar.setDisable(true);
        btnCancelar.setOnAction(e -> limpiar());

        btnImportar = new Button("Importar");
        btnImportar.getStyleClass().add("btn-accion");
        btnImportar.setDisable(true);
        btnImportar.setOnAction(e -> ejecutarImportacion());

        barra.getChildren().addAll(btnCancelar, btnImportar);
        return barra;
    }

    private void abrirSelector() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Seleccionar fichero de gastos");
        chooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Ficheros soportados", "*.csv", "*.json"),
            new FileChooser.ExtensionFilter("CSV",  "*.csv"),
            new FileChooser.ExtensionFilter("JSON", "*.json")
        );

        javafx.stage.Window ventana = lblFichero.getScene() != null
            ? lblFichero.getScene().getWindow() : null;

        File fichero = chooser.showOpenDialog(ventana);
        if (fichero == null) return;

        if (!controlador.puedeImportar(fichero.getAbsolutePath())) {
            setEstado("Formato no soportado. Usa ficheros .csv o .json", false);
            return;
        }

        cargarPreview(fichero);
    }

    private void cargarPreview(File fichero) {
        try {
            List<Gasto> todos = controlador.previsualizarGastos(fichero.getAbsolutePath());
            ficheroSeleccionado = fichero;

            List<Gasto> muestra = todos.stream().limit(MAX_PREVIEW).collect(Collectors.toList());
            previewItems.setAll(muestra);

            lblFichero.setText(fichero.getName());
            lblFichero.setStyle("-fx-text-fill:#4ecca3;-fx-font-size:12;");

            String msg = todos.size() + " filas encontradas";
            if (todos.size() > MAX_PREVIEW)
                msg += " (mostrando las primeras " + MAX_PREVIEW + ")";
            setEstado(msg, true);

            btnImportar.setText("Importar " + todos.size() + " filas");
            btnImportar.setDisable(false);
            btnCancelar.setDisable(false);

        } catch (ImportacionException | IOException ex) {
            previewItems.clear();
            ficheroSeleccionado = null;
            setEstado("Error al leer el fichero: " + ex.getMessage(), false);
            btnImportar.setDisable(true);
            btnCancelar.setDisable(false);
        }
    }

    private void ejecutarImportacion() {
        if (ficheroSeleccionado == null) return;
        try {
            int importados = controlador.importarGastos(ficheroSeleccionado.getAbsolutePath());
            setEstado("✓ " + importados + " gastos importados correctamente.", true);
            btnImportar.setDisable(true);
            ficheroSeleccionado = null;
        } catch (ImportacionException ex) {
            setEstado("Error de formato: " + ex.getMessage(), false);
        } catch (IOException ex) {
            setEstado("Error de lectura: " + ex.getMessage(), false);
        }
    }

    private void limpiar() {
        ficheroSeleccionado = null;
        previewItems.clear();
        lblFichero.setText("Ningún fichero seleccionado");
        lblFichero.setStyle("-fx-text-fill:-fx-text-secondary;-fx-font-size:12;");
        lblEstado.setText("");
        btnImportar.setText("Importar");
        btnImportar.setDisable(true);
        btnCancelar.setDisable(true);
    }

    private void setEstado(String mensaje, boolean ok) {
        lblEstado.setText(mensaje);
        lblEstado.setStyle(ok
            ? "-fx-text-fill:#4ecca3;-fx-font-size:12;"
            : "-fx-text-fill:#e06060;-fx-font-size:12;");
    }
}