package es.um.tds.gestiongastos.vista;

import es.um.tds.gestiongastos.modelo.*;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Sección de cuentas compartidas.
 *
 * Layout: SplitPane 35/65
 *   ├── Panel izquierdo — ListView de cuentas + botón Nueva
 *   └── Panel derecho   — Nombre, tipo, saldos (FlowPane chips), tabla gastos,
 *                         botones Añadir gasto / Eliminar cuenta
 */
public class CuentasView extends SeccionView {

    private ObservableList<CuentaCompartida> cuentaItems;
    private ListView<CuentaCompartida>       listaCuentas;

    private Label     lblNombre, lblMeta;
    private FlowPane  filaSaldos;
    private ObservableList<Gasto> gastoItems;
    private TableView<Gasto>      tablaGastos;
    private Button    btnAnadirGasto, btnEliminarCuenta;

    @Override public String getNombre() { return "Cuentas"; }

    @Override public String getIconPath() { return "/images/nav/cuentas.png";}
    
    @Override
    public Region getView() {
        return buildView();
    }

    private SplitPane buildView() {
        SplitPane split = new SplitPane();
        split.setDividerPositions(0.35);
        VBox panelIzq = buildPanelIzquierdo();
        VBox panelDer = buildPanelDerecho();
        split.getItems().addAll(panelIzq, panelDer);
        SplitPane.setResizableWithParent(split.getItems().get(0), false);

        if (!cuentaItems.isEmpty()) listaCuentas.getSelectionModel().selectFirst();

        return split;
    }

    private VBox buildPanelIzquierdo() {
        VBox panel = new VBox(8);
        panel.setPadding(new Insets(14));
        panel.getStyleClass().add("panel-lista");

        HBox cab = new HBox(8);
        cab.setAlignment(Pos.CENTER_LEFT);
        Label titulo = new Label("Cuentas");
        titulo.setFont(Font.font("System", FontWeight.BOLD, 14));
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button btnNueva = new Button("+ Nueva");
        btnNueva.getStyleClass().add("btn-accion");
        btnNueva.setOnAction(e -> mostrarDialogoNuevaCuenta());
        cab.getChildren().addAll(titulo, spacer, btnNueva);

        cuentaItems = FXCollections.observableArrayList(controlador.getCuentas());
        listaCuentas = new ListView<>(cuentaItems);
        listaCuentas.getStyleClass().add("lista-cuentas");
        VBox.setVgrow(listaCuentas, Priority.ALWAYS);

        listaCuentas.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(CuentaCompartida c, boolean empty) {
                super.updateItem(c, empty);
                if (empty || c == null) { setText(null); setGraphic(null); return; }
                VBox box = new VBox(2);
                Label nombre = new Label(c.getNombre());
                nombre.setFont(Font.font("System", FontWeight.SEMI_BOLD, 13));
                Label meta = new Label(c.calcularNumPersonas() + " personas · " + c.obtenerTipoDescripcion());
                meta.setStyle("-fx-text-fill:-fx-text-secondary;-fx-font-size:11;");
                box.getChildren().addAll(nombre, meta);
                setGraphic(box); setText(null);
            }
        });

        listaCuentas.getSelectionModel().selectedItemProperty()
            .addListener((obs, ant, sel) -> actualizarDetalle(sel));

        panel.getChildren().addAll(cab, listaCuentas);
        return panel;
    }

    private VBox buildPanelDerecho() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(14));
        panel.getStyleClass().add("panel-detalle");

        lblNombre = new Label("—");
        lblNombre.setFont(Font.font("System", FontWeight.BOLD, 18));
        lblMeta = new Label("");
        lblMeta.setStyle("-fx-text-fill:-fx-text-secondary;-fx-font-size:12;");

        Label lblSaldosTit = new Label("Saldos");
        lblSaldosTit.setFont(Font.font("System", FontWeight.BOLD, 13));
        filaSaldos = new FlowPane(8, 6);

        Label lblGastosTit = new Label("Gastos de la cuenta");
        lblGastosTit.setFont(Font.font("System", FontWeight.BOLD, 13));
        gastoItems  = FXCollections.observableArrayList();
        tablaGastos = buildTablaGastos();
        VBox.setVgrow(tablaGastos, Priority.ALWAYS);

        btnAnadirGasto = new Button("+ Añadir gasto");
        btnAnadirGasto.getStyleClass().add("btn-accion");
        btnAnadirGasto.setDisable(true);
        btnAnadirGasto.setOnAction(e -> mostrarDialogoAnadirGasto());

        btnEliminarCuenta = new Button("Eliminar cuenta");
        btnEliminarCuenta.getStyleClass().addAll("btn-accion", "btn-peligro");
        btnEliminarCuenta.setDisable(true);
        btnEliminarCuenta.setOnAction(e -> confirmarEliminarCuenta());

        HBox acciones = new HBox(8, btnAnadirGasto, btnEliminarCuenta);

        panel.getChildren().addAll(
            new VBox(3, lblNombre, lblMeta),
            new Separator(),
            lblSaldosTit, filaSaldos,
            lblGastosTit, tablaGastos,
            acciones
        );
        return panel;
    }

    private TableView<Gasto> buildTablaGastos() {
        TableView<Gasto> tabla = new TableView<>(gastoItems);
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tabla.setPlaceholder(new Label("Sin gastos en esta cuenta"));
        tabla.getStyleClass().add("tabla-gastos");

        TableColumn<Gasto, String> colFecha = new TableColumn<>("Fecha");
        colFecha.setPrefWidth(80);
        colFecha.setCellValueFactory(d ->
            new SimpleStringProperty(d.getValue().getFecha().format(FECHA_FMT)));

        TableColumn<Gasto, String> colNota = new TableColumn<>("Nota");
        colNota.setCellValueFactory(d ->
            new SimpleStringProperty(d.getValue().getNota().orElse("—")));

        TableColumn<Gasto, String> colPagador = new TableColumn<>("Pagador");
        colPagador.setPrefWidth(90);
        colPagador.setCellValueFactory(d ->
            new SimpleStringProperty(
                d.getValue().getPagador().map(Persona::getNombre).orElse("—")));

        TableColumn<Gasto, String> colImporte = new TableColumn<>("Importe");
        colImporte.setPrefWidth(80);
        colImporte.setCellValueFactory(d ->
            new SimpleStringProperty(
                String.format("%.2f €", d.getValue().getCantidad())));
        colImporte.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); }
                else { setText(item); setStyle("-fx-alignment:CENTER-RIGHT;"); }
            }
        });

        tabla.getColumns().add(colFecha);
        tabla.getColumns().add(colNota);
        tabla.getColumns().add(colPagador);
        tabla.getColumns().add(colImporte);

        return tabla;
    }

    private void actualizarDetalle(CuentaCompartida cuenta) {
        if (cuenta == null) {
            lblNombre.setText("—");
            lblMeta.setText("");
            filaSaldos.getChildren().clear();
            gastoItems.clear();
            btnAnadirGasto.setDisable(true);
            btnEliminarCuenta.setDisable(true);
            return;
        }

        lblNombre.setText(cuenta.getNombre());
        lblMeta.setText(cuenta.obtenerTipoDescripcion() + " · "
            + cuenta.calcularNumPersonas() + " personas · "
            + String.format("Total: %.2f €", cuenta.calcularGastoTotal()));

        filaSaldos.getChildren().clear();
        Map<Persona, Double> saldos = cuenta.getSaldos();
        cuenta.getPersonas().stream()
            .sorted(Comparator.comparingDouble(
                p -> -saldos.getOrDefault(p, 0.0)))
            .forEach(p -> filaSaldos.getChildren().add(
                buildChipSaldo(p.getNombre(), saldos.getOrDefault(p, 0.0))));

        gastoItems.setAll(cuenta.getGastos());
        btnAnadirGasto.setDisable(false);
        btnEliminarCuenta.setDisable(false);
    }

    private VBox buildChipSaldo(String nombre, double saldo) {
        VBox chip = new VBox(2);
        chip.getStyleClass().add("kpi-card");
        chip.setPadding(new Insets(5, 10, 5, 10));
        chip.setAlignment(Pos.CENTER);

        Label lblN = new Label(nombre);
        lblN.setStyle("-fx-font-size:10;-fx-text-fill:-fx-text-secondary;");

        Label lblS = new Label(String.format("%+.2f €", saldo));
        lblS.setFont(Font.font("System", FontWeight.BOLD, 12));
        if      (saldo >  0.005) lblS.setStyle("-fx-text-fill:#4ecca3;");
        else if (saldo < -0.005) lblS.setStyle("-fx-text-fill:#e06060;");
        else                     lblS.setStyle("-fx-text-fill:-fx-text-secondary;");

        chip.getChildren().addAll(lblN, lblS);
        return chip;
    }

    private void mostrarDialogoNuevaCuenta() {
        Dialog<CuentaCompartida> dialog = new Dialog<>();
        dialog.setTitle("Nueva cuenta compartida");
        dialog.setHeaderText("Configurar cuenta");

        ButtonType btnCrear = new ButtonType("Crear", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnCrear, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(12); grid.setVgap(10);
        grid.setPadding(new Insets(16));

        TextField txtNombre = new TextField();
        txtNombre.setPromptText("Ej: Piso compartido");

        ComboBox<String> cmbTipo = new ComboBox<>(
            FXCollections.observableArrayList("Equitativa", "Por porcentaje"));
        cmbTipo.setValue("Equitativa");
        cmbTipo.setMaxWidth(Double.MAX_VALUE);

        TextArea txtPersonas = new TextArea();
        txtPersonas.setPromptText("Una persona por línea\nEj:\nJuan\nMaría\nPedro");
        txtPersonas.setPrefRowCount(4);

        Label lblPct = new Label("Porcentajes:");
        TextArea txtPct = new TextArea();
        txtPct.setPromptText("Nombre=porcentaje por línea\nEj:\nJuan=50\nMaría=50");
        txtPct.setPrefRowCount(4);
        lblPct.setVisible(false); lblPct.setManaged(false);
        txtPct.setVisible(false); txtPct.setManaged(false);

        cmbTipo.valueProperty().addListener((obs, o, v) -> {
            boolean esPct = "Por porcentaje".equals(v);
            lblPct.setVisible(esPct); lblPct.setManaged(esPct);
            txtPct.setVisible(esPct); txtPct.setManaged(esPct);
            dialog.getDialogPane().getScene().getWindow().sizeToScene();
        });

        int f = 0;
        grid.add(new Label("Nombre:"),   0, f); grid.add(txtNombre,  1, f++);
        grid.add(new Label("Tipo:"),     0, f); grid.add(cmbTipo,    1, f++);
        grid.add(new Label("Personas:"), 0, f); grid.add(txtPersonas,1, f++);
        grid.add(lblPct, 0, f); grid.add(txtPct, 1, f);

        ColumnConstraints c0 = new ColumnConstraints(); c0.setMinWidth(90);
        ColumnConstraints c1 = new ColumnConstraints(); c1.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(c0, c1);
        dialog.getDialogPane().setContent(grid);

        javafx.scene.Node nodoCrear = dialog.getDialogPane().lookupButton(btnCrear);
        nodoCrear.setDisable(true);
        txtNombre.textProperty().addListener((obs, o, v) ->
            nodoCrear.setDisable(v.isBlank() || txtPersonas.getText().isBlank()));
        txtPersonas.textProperty().addListener((obs, o, v) ->
            nodoCrear.setDisable(txtNombre.getText().isBlank() || v.isBlank()));

        dialog.setResultConverter(tipo -> {
            if (tipo != btnCrear) return null;
            try {
                String nombre = txtNombre.getText().trim();
                List<String> nombres = parsearPersonas(txtPersonas.getText());
                if (nombres.size() < 2) {
                    mostrarError("Se necesitan al menos 2 personas."); return null;
                }
                if ("Por porcentaje".equals(cmbTipo.getValue())) {
                    Map<String, Double> pcts = parsearPorcentajes(txtPct.getText(), nombres);
                    return controlador.crearCuentaPorcentajeDesdeNombres(nombre, nombres, pcts);
                } else {
                    return controlador.crearCuentaEquitativaDesdeNombres(nombre, nombres);
                }
            } catch (IllegalArgumentException ex) {
                mostrarError(ex.getMessage()); return null;
            }
        });

        dialog.showAndWait().ifPresent(cuenta -> {
            if (cuenta != null) {
                recargarLista();
                listaCuentas.getSelectionModel().select(cuenta);
            }
        });
    }

    private void mostrarDialogoAnadirGasto() {
        CuentaCompartida cuenta = listaCuentas.getSelectionModel().getSelectedItem();
        if (cuenta == null) return;

        Dialog<Gasto> dialog = new Dialog<>();
        dialog.setTitle("Añadir gasto");
        dialog.setHeaderText("Nuevo gasto en \"" + cuenta.getNombre() + "\"");

        ButtonType btnGuardar = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnGuardar, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(12); grid.setVgap(10);
        grid.setPadding(new Insets(16));

        TextField txtCantidad = new TextField();
        txtCantidad.setPromptText("Ej: 45.50");

        ComboBox<Categoria> cmbCat = new ComboBox<>(
            FXCollections.observableArrayList(controlador.getCategorias()));
        cmbCat.setMaxWidth(Double.MAX_VALUE);
        cmbCat.setButtonCell(new CategoriaCell());
        cmbCat.setCellFactory(lv -> new CategoriaCell());
        if (!controlador.getCategorias().isEmpty())
            cmbCat.setValue(controlador.getCategorias().get(0));

        TextField txtNota = new TextField();
        txtNota.setPromptText("Descripción (opcional)");

        ComboBox<Persona> cmbPagador = new ComboBox<>(
            FXCollections.observableArrayList(cuenta.getPersonas()));
        cmbPagador.setMaxWidth(Double.MAX_VALUE);
        cmbPagador.setButtonCell(new PersonaCell());
        cmbPagador.setCellFactory(lv -> new PersonaCell());
        if (!cuenta.getPersonas().isEmpty())
            cmbPagador.setValue(cuenta.getPersonas().get(0));

        int f = 0;
        grid.add(new Label("Cantidad (€):"), 0, f); grid.add(txtCantidad, 1, f++);
        grid.add(new Label("Categoría:"),    0, f); grid.add(cmbCat,      1, f++);
        grid.add(new Label("Nota:"),         0, f); grid.add(txtNota,     1, f++);
        grid.add(new Label("Pagador:"),      0, f); grid.add(cmbPagador,  1, f);

        ColumnConstraints c0 = new ColumnConstraints(); c0.setMinWidth(90);
        ColumnConstraints c1 = new ColumnConstraints(); c1.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(c0, c1);
        dialog.getDialogPane().setContent(grid);

        javafx.scene.Node nodoGuardar = dialog.getDialogPane().lookupButton(btnGuardar);
        nodoGuardar.setDisable(true);
        txtCantidad.textProperty().addListener((obs, o, v) ->
            nodoGuardar.setDisable(!esNumeroPositivo(v)
                || cmbCat.getValue() == null || cmbPagador.getValue() == null));

        dialog.setResultConverter(tipo -> {
            if (tipo != btnGuardar) return null;
            try {
                double    cantidad = parsearDouble(txtCantidad.getText());
                String    nota     = txtNota.getText().trim();
                Categoria cat      = cmbCat.getValue();
                Persona   pagador  = cmbPagador.getValue();
                Gasto gasto = controlador.registrarGastoCompartido(
                    cantidad, nota.isEmpty() ? null : nota, null, cat, cuenta, pagador);
                return gasto;
            } catch (IllegalArgumentException ex) {
                mostrarError(ex.getMessage()); return null;
            }
        });

        dialog.showAndWait().ifPresent(g -> {
            if (g != null) actualizarDetalle(cuenta);
        });
    }

    private void confirmarEliminarCuenta() {
        CuentaCompartida cuenta = listaCuentas.getSelectionModel().getSelectedItem();
        if (cuenta == null) return;
        boolean ok = mostrarConfirmacion(
            "Eliminar cuenta",
            "¿Eliminar \"" + cuenta.getNombre() + "\"?",
            "Se eliminará la cuenta y sus datos de saldo.");
        if (ok) {
            controlador.eliminarCuenta(cuenta);
            recargarLista();
            actualizarDetalle(null);
        }
    }

    private List<String> parsearPersonas(String texto) {
        return Arrays.stream(texto.split("\\n"))
            .map(String::trim).filter(s -> !s.isEmpty())
            .collect(Collectors.toList());
    }

    private Map<String, Double> parsearPorcentajes(String texto, List<String> nombres) {
        Map<String, Double> resultado = new LinkedHashMap<>();
        for (String linea : texto.split("\\n")) {
            String t = linea.trim();
            if (t.isEmpty()) continue;
            String[] p = t.split("=");
            if (p.length != 2) throw new IllegalArgumentException(
                "Formato incorrecto: \"" + t + "\". Usa Nombre=porcentaje");
            try {
                resultado.put(p[0].trim(),
                    Double.parseDouble(p[1].trim().replace(',', '.')));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Porcentaje no válido en: \"" + t + "\"");
            }
        }
        for (String nombre : nombres)
            if (!resultado.containsKey(nombre))
                throw new IllegalArgumentException("Falta el porcentaje para: " + nombre);
        return resultado;
    }

    private void recargarLista() {
        CuentaCompartida sel = listaCuentas.getSelectionModel().getSelectedItem();
        cuentaItems.setAll(controlador.getCuentas());
        if (sel != null) cuentaItems.stream()
            .filter(c -> c.getID() == sel.getID())
            .findFirst()
            .ifPresent(c -> listaCuentas.getSelectionModel().select(c));
    }

}