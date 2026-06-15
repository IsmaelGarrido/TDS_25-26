package es.um.tds.gestiongastos.vista;

import es.um.tds.gestiongastos.modelo.Categoria;
import es.um.tds.gestiongastos.modelo.Filtro;
import es.um.tds.gestiongastos.modelo.Gasto;

import com.calendarfx.model.Calendar;
import com.calendarfx.model.CalendarSource;
import com.calendarfx.model.Entry;
import com.calendarfx.view.CalendarView;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.LocalDate;
import java.time.Month;
import java.util.*;

/**
 * Sección de gestión de gastos.
 *
 * Layout:
 *   VBox
 *   ├── FiltrosBar  — chips de meses + rango fechas + categoría
 *   └── TabPane
 *       ├── Tabla      — TableView con Nuevo / Editar / Eliminar
 *       ├── Gráficos   — BarChart + PieChart por categoría
 *       └── Calendario — CalendarFX (vista mensual)
 *
 * El filtro es compartido: al pulsar "Filtrar" se actualiza
 * gastosFiltrados y los tres tabs se refrescan automáticamente.
 */
public class GastosView extends SeccionView {

    private VBox cachedView;

    private final Set<Month> mesesActivos = new HashSet<>();
    private DatePicker dpDesde, dpHasta;
    private ComboBox<Categoria> cmbCategoria;

    private List<Gasto> gastosFiltrados = new ArrayList<>();

    private ObservableList<Gasto> tablaItems;
    private TableView<Gasto>      tabla;
    private Button btnEditar, btnEliminar;

    private BarChart<String, Number> barChart;
    private PieChart                 pieChart;

    private Calendar<Gasto> gastosCalendar;
    private CalendarView calendarView;

    @Override 
    public String getNombre() { return "Gastos"; }

    @Override
    public String getIconPath() {return "/images/nav/gastos.png";}

    @Override
    public Region getView() {
        if (cachedView == null) {
            gastosFiltrados = new ArrayList<>(controlador.getGastos());
            cachedView = buildView();
        } else {
            aplicarFiltros();
        }
        return cachedView;
    }

    private VBox buildView() {
        VBox root = new VBox(0);
        TabPane tabs = buildTabPane();
        root.getChildren().addAll(buildFiltrosBar(), tabs);
        VBox.setVgrow(tabs, Priority.ALWAYS);
        return root;
    }

    private HBox buildFiltrosBar() {
        HBox barra = new HBox(8);
        barra.setPadding(new Insets(8, 14, 8, 14));
        barra.setAlignment(Pos.CENTER_LEFT);
        barra.getStyleClass().add("filtros-bar");

        barra.getChildren().add(new Label("Meses:"));
        barra.getChildren().add(buildChipsMeses());
        barra.getChildren().add(new Separator(javafx.geometry.Orientation.VERTICAL));

        dpDesde = new DatePicker();
        dpDesde.setPromptText("Desde");
        dpDesde.setPrefWidth(125);

        dpHasta = new DatePicker();
        dpHasta.setPromptText("Hasta");
        dpHasta.setPrefWidth(125);

        barra.getChildren().addAll(dpDesde, dpHasta);
        barra.getChildren().add(new Separator(javafx.geometry.Orientation.VERTICAL));

        List<Categoria> cats = new ArrayList<>();
        cats.add(null);
        cats.addAll(controlador.getCategorias());
        cmbCategoria = new ComboBox<>(FXCollections.observableArrayList(cats));
        cmbCategoria.setValue(null);
        cmbCategoria.setPrefWidth(145);
        cmbCategoria.setButtonCell(new CategoriaCell("Todas las categ."));
        cmbCategoria.setCellFactory(lv -> new CategoriaCell("Todas las categ."));
        barra.getChildren().add(cmbCategoria);

        Button btnFiltrar = new Button("Filtrar");
        btnFiltrar.getStyleClass().add("btn-accion");
        btnFiltrar.setOnAction(e -> aplicarFiltros());

        Button btnLimpiar = new Button("Limpiar");
        btnLimpiar.getStyleClass().add("btn-accion");
        btnLimpiar.setOnAction(e -> limpiarFiltros());

        barra.getChildren().addAll(btnFiltrar, btnLimpiar);
        return barra;
    }

    private HBox buildChipsMeses() {
        HBox fila = new HBox(4);
        String[] abrev = {"En","Fe","Ma","Ab","My","Jn","Jl","Ag","Se","Oc","No","Di"};
        Month[]  meses = Month.values();
        for (int i = 0; i < 12; i++) {
            final Month mes = meses[i];
            ToggleButton chip = new ToggleButton(abrev[i]);
            chip.getStyleClass().add("mes-chip");
            chip.setOnAction(e -> {
                if (chip.isSelected()) mesesActivos.add(mes);
                else                   mesesActivos.remove(mes);
            });
            fila.getChildren().add(chip);
        }
        return fila;
    }

    private TabPane buildTabPane() {
        TabPane tp = new TabPane();
        tp.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tp.getStyleClass().add("gastos-tabs");

        Tab tabTabla      = new Tab("Tabla",      buildTablaContent());
        Tab tabGraficos   = new Tab("Gráficos",   buildGraficosContent());
        Tab tabCalendario = new Tab("Calendario", buildCalendarioContent());

        tp.getTabs().addAll(tabTabla, tabGraficos, tabCalendario);

        tp.getSelectionModel().selectedItemProperty().addListener((obs, ant, sel) -> {
            if (sel == tabGraficos)   actualizarGraficos();
            if (sel == tabCalendario) actualizarCalendario();
        });
        return tp;
    }

    private VBox buildTablaContent() {
        VBox c = new VBox(8);
        c.setPadding(new Insets(10, 14, 10, 14));
        tabla = buildTabla();
        c.getChildren().addAll(buildAccionesTabla(), tabla);
        VBox.setVgrow(tabla, Priority.ALWAYS);
        return c;
    }

    private HBox buildAccionesTabla() {
        HBox barra = new HBox(8);

        Button btnNuevo = new Button("+ Nuevo gasto");
        btnNuevo.getStyleClass().add("btn-accion");
        btnNuevo.setOnAction(e -> mostrarDialogoGasto(null));

        btnEditar = new Button("Editar");
        btnEditar.getStyleClass().add("btn-accion");
        btnEditar.setDisable(true);
        btnEditar.setOnAction(e -> {
            Gasto sel = tabla.getSelectionModel().getSelectedItem();
            if (sel != null) mostrarDialogoGasto(sel);
        });

        btnEliminar = new Button("Eliminar");
        btnEliminar.getStyleClass().addAll("btn-accion", "btn-peligro");
        btnEliminar.setDisable(true);
        btnEliminar.setOnAction(e -> confirmarEliminar());

        barra.getChildren().addAll(btnNuevo, btnEditar, btnEliminar);
        return barra;
    }

    private TableView<Gasto> buildTabla() {
        tablaItems = FXCollections.observableArrayList(gastosFiltrados);
        TableView<Gasto> tv = new TableView<>(tablaItems);
        tv.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tv.setPlaceholder(new Label("No hay gastos con los filtros seleccionados"));
        tv.getStyleClass().add("tabla-gastos");

        TableColumn<Gasto, String> colFecha = new TableColumn<>("Fecha");
        colFecha.setPrefWidth(90);
        colFecha.setCellValueFactory(d ->
            new SimpleStringProperty(d.getValue().getFecha().format(FECHA_FMT)));

        TableColumn<Gasto, String> colCat = new TableColumn<>("Categoría");
        colCat.setPrefWidth(125);
        colCat.setCellValueFactory(d ->
            new SimpleStringProperty(d.getValue().getCategoria().getNombre()));

        TableColumn<Gasto, String> colNota = new TableColumn<>("Nota");
        colNota.setCellValueFactory(d ->
            new SimpleStringProperty(d.getValue().getNota().orElse("—")));

        TableColumn<Gasto, String> colMetodo = new TableColumn<>("Método pago");
        colMetodo.setPrefWidth(105);
        colMetodo.setCellValueFactory(d ->
            new SimpleStringProperty(d.getValue().getMetodoPago().orElse("—")));

        TableColumn<Gasto, Gasto> colCantidad = new TableColumn<>("Cantidad");
        colCantidad.setPrefWidth(90);
        colCantidad.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue()));
        colCantidad.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Gasto g, boolean empty) {
                super.updateItem(g, empty);
                if (empty || g == null) { setText(null); setStyle(""); }
                else {
                    setText(String.format("−%.2f %s", g.getCantidad(), g.getMoneda()));
                    setStyle("-fx-text-fill:#e06060;-fx-alignment:CENTER-RIGHT;-fx-font-weight:bold;");
                }
            }
        });

        tv.getColumns().add(colFecha);
        tv.getColumns().add(colCat);
        tv.getColumns().add(colNota);
        tv.getColumns().add(colMetodo);
        tv.getColumns().add(colCantidad);

        tv.getSelectionModel().selectedItemProperty().addListener((obs, ant, sel) -> {
            boolean hay = sel != null;
            btnEditar.setDisable(!hay);
            btnEliminar.setDisable(!hay);
        });

        return tv;
    }

    private HBox buildGraficosContent() {
        HBox c = new HBox(12);
        c.setPadding(new Insets(12, 14, 12, 14));

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis   yAxis = new NumberAxis();
        yAxis.setLabel("€");
        barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Gasto por categoría");
        barChart.setLegendVisible(false);
        barChart.setAnimated(false);
        HBox.setHgrow(barChart, Priority.ALWAYS);

        pieChart = new PieChart();
        pieChart.setTitle("Distribución");
        pieChart.setLabelsVisible(false);
        pieChart.setAnimated(false);
        pieChart.setPrefWidth(240);

        actualizarGraficos();
        c.getChildren().addAll(barChart, pieChart);
        return c;
    }

    private void actualizarGraficos() {
        if (barChart == null || pieChart == null) return;

        Map<String, Double> porCat = controlador.getGastosPorCategoriaAgrupados(gastosFiltrados);

        XYChart.Series<String, Number> serie = new XYChart.Series<>();
        porCat.entrySet().stream()
            .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
            .forEach(e -> serie.getData().add(new XYChart.Data<>(e.getKey(), e.getValue())));
        barChart.getData().clear();
        if (!serie.getData().isEmpty()) barChart.getData().add(serie);

        double total = porCat.values().stream().mapToDouble(Double::doubleValue).sum();
        ObservableList<PieChart.Data> pieDatos = FXCollections.observableArrayList();
        porCat.entrySet().stream()
            .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
            .forEach(e -> {
                double pct = total > 0 ? e.getValue() / total * 100 : 0;
                pieDatos.add(new PieChart.Data(
                    String.format("%s %.1f%%", e.getKey(), pct), e.getValue()));
            });
        pieChart.setData(pieDatos.isEmpty()
            ? FXCollections.observableArrayList(new PieChart.Data("Sin datos", 1))
            : pieDatos);
    }

    private Node buildCalendarioContent() {
        gastosCalendar = new Calendar<>("Gastos");
        gastosCalendar.setStyle(Calendar.Style.STYLE1);

        CalendarSource source = new CalendarSource("GestionGastos");
        source.getCalendars().add(gastosCalendar);

        calendarView = new CalendarView();
        calendarView.getCalendarSources().setAll(source);
        calendarView.showMonthPage();
        calendarView.setShowAddCalendarButton(false);
        calendarView.setShowPrintButton(false);
        calendarView.setShowSearchField(false);
        calendarView.setShowSourceTrayButton(false);

        actualizarCalendario();
        return calendarView;
    }

    private void actualizarCalendario() {
        if (gastosCalendar == null) return;
        gastosCalendar.clear();
        for (Gasto g : gastosFiltrados) {
            String titulo = g.getCategoria().getNombre()
                + "  −" + String.format("%.2f €", g.getCantidad());
            Entry<Gasto> entry = new Entry<>(titulo);
            entry.setInterval(g.getFecha(), g.getFecha().plusMinutes(30));
            entry.setUserObject(g);
            gastosCalendar.addEntry(entry);
        }
    }

    private void mostrarDialogoGasto(Gasto existente) {
        boolean esNuevo = existente == null;
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle(esNuevo ? "Nuevo gasto" : "Editar gasto");
        dialog.setHeaderText(esNuevo ? "Registrar gasto personal" : "Modificar gasto");

        ButtonType btnGuardar = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnGuardar, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(12); grid.setVgap(10);
        grid.setPadding(new Insets(16));

        TextField txtCantidad = new TextField(esNuevo ? "" : String.valueOf(existente.getCantidad()));
        txtCantidad.setPromptText("Ej: 45.50");

        ComboBox<Categoria> cmbCat = new ComboBox<>(
            FXCollections.observableArrayList(controlador.getCategorias()));
        cmbCat.setMaxWidth(Double.MAX_VALUE);
        cmbCat.setButtonCell(new CategoriaCell());
        cmbCat.setCellFactory(lv -> new CategoriaCell());
        if (!esNuevo) cmbCat.setValue(existente.getCategoria());
        else if (!controlador.getCategorias().isEmpty())
            cmbCat.setValue(controlador.getCategorias().get(0));

        TextField txtNota   = new TextField(esNuevo ? "" : existente.getNota().orElse(""));
        txtNota.setPromptText("Descripción (opcional)");
        TextField txtMetodo = new TextField(esNuevo ? "" : existente.getMetodoPago().orElse(""));
        txtMetodo.setPromptText("Ej: Tarjeta, Efectivo (opcional)");

        DatePicker dpFecha = new DatePicker(
            esNuevo ? LocalDate.now() : existente.getFecha().toLocalDate());

        int fila = 0;
        grid.add(new Label("Cantidad (€):"), 0, fila); grid.add(txtCantidad, 1, fila++);
        grid.add(new Label("Categoría:"),    0, fila); grid.add(cmbCat,      1, fila++);
        grid.add(new Label("Nota:"),         0, fila); grid.add(txtNota,     1, fila++);
        grid.add(new Label("Método pago:"),  0, fila); grid.add(txtMetodo,   1, fila++);
        if (!esNuevo) {
            grid.add(new Label("Fecha:"), 0, fila); grid.add(dpFecha, 1, fila);
        }

        ColumnConstraints c0 = new ColumnConstraints(); c0.setMinWidth(100);
        ColumnConstraints c1 = new ColumnConstraints(); c1.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(c0, c1);
        dialog.getDialogPane().setContent(grid);

        Node nodoGuardar = dialog.getDialogPane().lookupButton(btnGuardar);
        nodoGuardar.setDisable(!esNumeroPositivo(txtCantidad.getText()) || cmbCat.getValue() == null);
        txtCantidad.textProperty().addListener((obs, o, v) ->
            nodoGuardar.setDisable(!esNumeroPositivo(v) || cmbCat.getValue() == null));
        cmbCat.valueProperty().addListener((obs, o, v) ->
            nodoGuardar.setDisable(!esNumeroPositivo(txtCantidad.getText()) || v == null));

        dialog.setResultConverter(tipo -> {
            if (tipo != btnGuardar) return null;
            try {
                double    cantidad = parsearDouble(txtCantidad.getText());
                String    nota     = txtNota.getText().trim();
                String    metodo   = txtMetodo.getText().trim();
                Categoria cat      = cmbCat.getValue();
                if (esNuevo) {
                    controlador.registrarGasto(
                        cantidad, nota.isEmpty() ? null : nota,
                        metodo.isEmpty() ? null : metodo, cat);
                } else {
                    java.time.LocalDateTime fechaHora =
                        dpFecha.getValue().atTime(existente.getFecha().toLocalTime());
                    controlador.editarGasto(existente, cantidad, fechaHora,
                        nota.isEmpty() ? null : nota,
                        metodo.isEmpty() ? null : metodo,
                        cat, existente.getMoneda(),
                        existente.getPagador().orElse(null));
                }
                return true;
            } catch (IllegalArgumentException ex) {
                mostrarError(ex.getMessage());
                return null;
            }
        });

        dialog.showAndWait().ifPresent(ok -> { if (ok) aplicarFiltros(); });
    }

    private void confirmarEliminar() {
        Gasto sel = tabla.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        boolean ok = mostrarConfirmacion(
            "Eliminar gasto",
            String.format("¿Eliminar gasto de %.2f €?", sel.getCantidad()),
            sel.getCategoria().getNombre() + " · " + sel.getFecha().format(FECHA_FMT));
        if (ok) { controlador.eliminarGasto(sel); aplicarFiltros(); }
    }

    private void aplicarFiltros() {
        Filtro filtro = new Filtro();
        mesesActivos.forEach(filtro::addMes);

        LocalDate desde = dpDesde  != null ? dpDesde.getValue()  : null;
        LocalDate hasta = dpHasta  != null ? dpHasta.getValue()   : null;
        if (desde != null || hasta != null) filtro.setRangoFechas(desde, hasta);

        Categoria cat = cmbCategoria != null ? cmbCategoria.getValue() : null;
        if (cat != null) filtro.addCategoria(cat);

        gastosFiltrados = filtro.tieneFiltros()
            ? controlador.getGastosPorFiltro(filtro)
            : new ArrayList<>(controlador.getGastos());

        if (tablaItems != null) tablaItems.setAll(gastosFiltrados);
        actualizarGraficos();
        actualizarCalendario();
    }

    private void limpiarFiltros() {
        mesesActivos.clear();
        if (cachedView != null)
            cachedView.lookupAll(".mes-chip").forEach(n -> {
                if (n instanceof ToggleButton tb) tb.setSelected(false);
            });
        if (dpDesde      != null) dpDesde.setValue(null);
        if (dpHasta      != null) dpHasta.setValue(null);
        if (cmbCategoria != null) cmbCategoria.setValue(null);
        aplicarFiltros();
    }
}