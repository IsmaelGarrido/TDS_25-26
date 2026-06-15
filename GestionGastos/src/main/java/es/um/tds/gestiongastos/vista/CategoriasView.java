package es.um.tds.gestiongastos.vista;

import es.um.tds.gestiongastos.modelo.Categoria;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Sección de gestión de categorías.
 *
 * Layout: SplitPane 40/60
 *   ├── Panel izquierdo — ListView de categorías + botón Nueva
 *   └── Panel derecho   — Nombre, tipo, botones Renombrar / Eliminar
 */
public class CategoriasView extends SeccionView {

    private ObservableList<Categoria> catItems;
    private ListView<Categoria>       listaCats;
    private Label   lblNombre, lblTipo;
    private Button  btnRenombrar, btnEliminar;

    @Override 
    public String getNombre() { return "Categorías"; }

    @Override
    public String getIconPath() {return "/images/nav/categorias.png";}    
    @Override
    public Region getView() {
        return buildView();
    }

    private SplitPane buildView() {
        SplitPane split = new SplitPane();
        split.setDividerPositions(0.40);
        VBox panelIzq = buildPanelIzquierdo();
        VBox panelDer = buildPanelDerecho();
        split.getItems().addAll(panelIzq, panelDer);
        SplitPane.setResizableWithParent(split.getItems().get(0), false);

        if (!catItems.isEmpty()) listaCats.getSelectionModel().selectFirst();

        return split;
    }

    private VBox buildPanelIzquierdo() {
        VBox panel = new VBox(8);
        panel.setPadding(new Insets(14));
        panel.getStyleClass().add("panel-lista");

        HBox cab = new HBox(8);
        cab.setAlignment(Pos.CENTER_LEFT);
        Label titulo = new Label("Categorías");
        titulo.setFont(Font.font("System", FontWeight.BOLD, 14));
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button btnNueva = new Button("+ Nueva");
        btnNueva.getStyleClass().add("btn-accion");
        btnNueva.setOnAction(e -> mostrarDialogoNueva());
        cab.getChildren().addAll(titulo, spacer, btnNueva);

        catItems = FXCollections.observableArrayList(controlador.getCategorias());
        listaCats = new ListView<>(catItems);
        listaCats.getStyleClass().add("lista-categorias");
        VBox.setVgrow(listaCats, Priority.ALWAYS);

        listaCats.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Categoria cat, boolean empty) {
                super.updateItem(cat, empty);
                if (empty || cat == null) { setText(null); setGraphic(null); return; }
                HBox fila = new HBox(6);
                fila.setAlignment(Pos.CENTER_LEFT);
                Label nombre = new Label(cat.getNombre());
                fila.getChildren().add(nombre);
                if (cat.isBase()) {
                    Label badge = new Label("base");
                    badge.getStyleClass().add("badge-base");
                    fila.getChildren().add(badge);
                }
                setGraphic(fila);
                setText(null);
            }
        });

        listaCats.getSelectionModel().selectedItemProperty()
            .addListener((obs, ant, sel) -> actualizarDetalle(sel));

        panel.getChildren().addAll(cab, listaCats);
        return panel;
    }

    private VBox buildPanelDerecho() {
        VBox panel = new VBox(12);
        panel.setPadding(new Insets(16));
        panel.getStyleClass().add("panel-detalle");

        lblNombre = new Label("—");
        lblNombre.setFont(Font.font("System", FontWeight.BOLD, 18));

        lblTipo = new Label("");
        lblTipo.setStyle("-fx-text-fill:-fx-text-secondary;-fx-font-size:12;");

        VBox info = new VBox(4, lblNombre, lblTipo);

        btnRenombrar = new Button("Renombrar");
        btnRenombrar.getStyleClass().add("btn-accion");
        btnRenombrar.setDisable(true);
        btnRenombrar.setOnAction(e -> mostrarDialogoRenombrar());

        btnEliminar = new Button("Eliminar");
        btnEliminar.getStyleClass().addAll("btn-accion", "btn-peligro");
        btnEliminar.setDisable(true);
        btnEliminar.setOnAction(e -> confirmarEliminar());

        HBox acciones = new HBox(8, btnRenombrar, btnEliminar);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        panel.getChildren().addAll(info, new Separator(), acciones, spacer);
        return panel;
    }

    private void actualizarDetalle(Categoria cat) {
        if (cat == null) {
            lblNombre.setText("—");
            lblTipo.setText("");
            btnRenombrar.setDisable(true);
            btnEliminar.setDisable(true);
            return;
        }
        lblNombre.setText(cat.getNombre());
        if (cat.isBase()) {
            lblTipo.setText("Categoría predefinida · no modificable");
            btnRenombrar.setDisable(true);
            btnEliminar.setDisable(true);
        } else {
            lblTipo.setText("Categoría personalizada");
            btnRenombrar.setDisable(false);
            btnEliminar.setDisable(false);
        }
    }

    private void mostrarDialogoNueva() {
        TextInputDialog d = new TextInputDialog();
        d.setTitle("Nueva categoría");
        d.setHeaderText("Crear categoría personalizada");
        d.setContentText("Nombre:");
        d.getEditor().setPromptText("Ej: Suscripciones");
        d.showAndWait().ifPresent(nombre -> {
            if (nombre.isBlank()) { mostrarError("El nombre no puede estar vacío."); return; }
            try {
                Categoria nueva = controlador.crearCategoria(nombre.trim());
                recargar();
                listaCats.getSelectionModel().select(nueva);
            } catch (IllegalArgumentException ex) {
                mostrarError(ex.getMessage());
            }
        });
    }

    private void mostrarDialogoRenombrar() {
        Categoria cat = listaCats.getSelectionModel().getSelectedItem();
        if (cat == null) return;
        TextInputDialog d = new TextInputDialog(cat.getNombre());
        d.setTitle("Renombrar categoría");
        d.setHeaderText("Nuevo nombre para \"" + cat.getNombre() + "\"");
        d.setContentText("Nombre:");
        d.showAndWait().ifPresent(nuevo -> {
            if (nuevo.isBlank()) { mostrarError("El nombre no puede estar vacío."); return; }
            int resultado = controlador.editarNombreCategoría(cat, nuevo.trim());
            switch (resultado) {
                case  0 -> recargar();
                case -1 -> mostrarError("El nombre no puede estar vacío.");
                case -2 -> mostrarError("No se puede modificar una categoría predefinida.");
                case -4 -> mostrarError("Ya existe una categoría con ese nombre.");
                default -> mostrarError("No se pudo cambiar el nombre.");
            }
        });
    }

    private void confirmarEliminar() {
        Categoria cat = listaCats.getSelectionModel().getSelectedItem();
        if (cat == null) return;
        boolean ok = mostrarConfirmacion(
            "Eliminar categoría",
            "¿Eliminar \"" + cat.getNombre() + "\"?",
            "Esta acción no se puede deshacer.");
        if (ok) {
            try {
                controlador.eliminarCategoria(cat);
                recargar();
                if (listaCats.getSelectionModel().isEmpty()) actualizarDetalle(null);
            } catch (IllegalStateException ex) {
                mostrarError(ex.getMessage());
            }
        }
    }

    private void recargar() {
        Categoria sel = listaCats.getSelectionModel().getSelectedItem();
        catItems.setAll(controlador.getCategorias());
        catItems.stream()
            .filter(c -> sel != null && c.getID() == sel.getID())
            .findFirst()
            .ifPresent(c -> listaCats.getSelectionModel().select(c));
    }
}