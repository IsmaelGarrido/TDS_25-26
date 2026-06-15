package es.um.tds.gestiongastos.vista;

import es.um.tds.gestiongastos.controlador.GestionGastos;
import es.um.tds.gestiongastos.modelo.Categoria;
import es.um.tds.gestiongastos.modelo.Filtro;
import es.um.tds.gestiongastos.modelo.Gasto;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Vista del panel principal.
 *
 * Layout (VBox raíz):
 *   ├── Fila de KPIs  (gasto mes actual | mayor categoría | alertas activas)
 *   └── HBox inferior
 *         ├── PieChart gasto por categoría   [60%]
 *         └── VBox últimos 5 gastos          [40%]
 */
public class PanelView {

    private static final DateTimeFormatter FECHA_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final int MAX_RECIENTES = 5;

    private final GestionGastos controlador = GestionGastos.getInstance();

    private VBox root;

    public PanelView() {
        buildView();
    }

    private void buildView() {
        root = new VBox(14);
        root.setPadding(new Insets(4, 0, 0, 0));

        root.getChildren().addAll(
            buildFilaKPIs(),
            buildFilaGraficos()
        );

        VBox.setVgrow(buildFilaGraficos(), Priority.ALWAYS);
    }

    private HBox buildFilaKPIs() {
        HBox fila = new HBox(12);
        fila.setAlignment(Pos.CENTER_LEFT);

        double gastoMes      = calcularGastoMesActual();
        String mayorCategoria = calcularMayorCategoria();
        int alertasActivas   = controlador.getAlertasActivas().size();

        fila.getChildren().addAll(
            buildKPI("Gasto este mes",   String.format("%.2f €", gastoMes),    "#0F6E56"),
            buildKPI("Mayor categoría",  mayorCategoria,                         "#BA7517"),
            buildKPI("Alertas activas",  String.valueOf(alertasActivas),         "#A32D2D")
        );

        // Cada KPI ocupa el mismo espacio
        for (javafx.scene.Node n : fila.getChildren()) {
            HBox.setHgrow(n, Priority.ALWAYS);
        }

        return fila;
    }

    private VBox buildKPI(String etiqueta, String valor, String colorValor) {
        VBox kpi = new VBox(4);
        kpi.getStyleClass().add("kpi-card");
        kpi.setPadding(new Insets(12, 16, 12, 16));

        Label lblEtiqueta = new Label(etiqueta);
        lblEtiqueta.getStyleClass().add("kpi-label");
        lblEtiqueta.setFont(Font.font("System", 11));

        Label lblValor = new Label(valor);
        lblValor.setFont(Font.font("System", FontWeight.BOLD, 22));
        lblValor.setStyle("-fx-text-fill: " + colorValor + ";");

        kpi.getChildren().addAll(lblEtiqueta, lblValor);
        return kpi;
    }

    private HBox buildFilaGraficos() {
        HBox fila = new HBox(12);
        fila.setAlignment(Pos.TOP_LEFT);
        VBox.setVgrow(fila, Priority.ALWAYS);

        VBox panelGrafico  = buildPanelPieChart();
        VBox panelRecientes = buildPanelRecientes();

        HBox.setHgrow(panelGrafico,   Priority.ALWAYS);
        HBox.setHgrow(panelRecientes, Priority.ALWAYS);

        panelGrafico.setMaxWidth(Double.MAX_VALUE);
        panelRecientes.setMaxWidth(Double.MAX_VALUE);

        fila.getChildren().addAll(panelGrafico, panelRecientes);
        return fila;
    }

    /** Panel izquierdo: PieChart con gasto por categoría del mes actual */
    private VBox buildPanelPieChart() {
        VBox panel = new VBox(8);
        panel.getStyleClass().add("panel-grafico");
        panel.setPadding(new Insets(14));
        VBox.setVgrow(panel, Priority.ALWAYS);

        Label titulo = new Label("Gasto por categoría — mes actual");
        titulo.setFont(Font.font("System", FontWeight.BOLD, 13));
        titulo.getStyleClass().add("panel-titulo");

        PieChart chart = buildPieChart();
        VBox.setVgrow(chart, Priority.ALWAYS);

        panel.getChildren().addAll(titulo, chart);
        return panel;
    }

    private PieChart buildPieChart() {
        Map<Categoria, Double> porCategoria = calcularGastoPorCategoriaMesActual();

        ObservableList<PieChart.Data> datos = FXCollections.observableArrayList();

        if (porCategoria.isEmpty()) {
            datos.add(new PieChart.Data("Sin datos", 1));
        } else {
            porCategoria.entrySet().stream()
                    .sorted(Map.Entry.<Categoria, Double>comparingByValue().reversed())
                    .forEach(e -> datos.add(
                        new PieChart.Data(
                            e.getKey().getNombre() + String.format(" (%.2f €)", e.getValue()),
                            e.getValue()
                        )
                    ));
        }

        PieChart chart = new PieChart(datos);
        chart.setLegendVisible(true);
        chart.setLabelsVisible(false);
        chart.setAnimated(false);
        chart.setMinHeight(220);
        return chart;
    }

    /** Panel derecho: últimos MAX_RECIENTES gastos */
    private VBox buildPanelRecientes() {
        VBox panel = new VBox(8);
        panel.getStyleClass().add("panel-grafico");
        panel.setPadding(new Insets(14));

        Label titulo = new Label("Últimos gastos");
        titulo.setFont(Font.font("System", FontWeight.BOLD, 13));
        titulo.getStyleClass().add("panel-titulo");

        VBox lista = new VBox(0);
        lista.getStyleClass().add("lista-recientes");

        List<Gasto> recientes = controlador.getGastos().stream()
                .sorted(Comparator.comparing(Gasto::getFecha).reversed())
                .limit(MAX_RECIENTES)
                .collect(Collectors.toList());

        if (recientes.isEmpty()) {
            Label vacio = new Label("No hay gastos registrados");
            vacio.getStyleClass().add("placeholder-desc");
            lista.getChildren().add(vacio);
        } else {
            for (Gasto g : recientes) {
                lista.getChildren().add(buildFilaGasto(g));
            }
        }

        panel.getChildren().addAll(titulo, lista);
        return panel;
    }

    /** Fila individual de un gasto reciente */
    private HBox buildFilaGasto(Gasto gasto) {
        HBox fila = new HBox(8);
        fila.setAlignment(Pos.CENTER_LEFT);
        fila.setPadding(new Insets(8, 4, 8, 4));
        fila.getStyleClass().add("fila-reciente");

        VBox info = new VBox(2);
        Label lblNota = new Label(gasto.getNota().orElse("Sin nota"));
        lblNota.getStyleClass().add("reciente-nota");
        lblNota.setFont(Font.font("System", FontWeight.NORMAL, 13));

        Label lblMeta = new Label(
            gasto.getCategoria().getNombre() + " · " + gasto.getFecha().format(FECHA_FMT));
        lblMeta.getStyleClass().add("reciente-meta");
        lblMeta.setFont(Font.font("System", 11));
        lblMeta.setStyle("-fx-text-fill: #888780;");

        info.getChildren().addAll(lblNota, lblMeta);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label lblCantidad = new Label(
            String.format("−%.2f €", gasto.getCantidad()));
        lblCantidad.setFont(Font.font("System", FontWeight.BOLD, 13));
        lblCantidad.setStyle("-fx-text-fill: #A32D2D;");

        fila.getChildren().addAll(info, spacer, lblCantidad);
        return fila;
    }
    
    /** Total gastado desde el primer día del mes actual hasta hoy */
    private double calcularGastoMesActual() {
        LocalDate hoy     = LocalDate.now();
        LocalDate primero = hoy.withDayOfMonth(1);

        Filtro filtro = new Filtro();
        filtro.setRangoFechas(primero, hoy);
        return controlador.getTotalGastos(filtro);
    }

    /** Nombre de la categoría con mayor gasto en el mes actual, o "—" si no hay datos */
    private String calcularMayorCategoria() {
        return calcularGastoPorCategoriaMesActual().entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(e -> e.getKey().getNombre())
                .orElse("—");
    }

    /** Gasto agrupado por categoría para el mes actual */
    private Map<Categoria, Double> calcularGastoPorCategoriaMesActual() {
        LocalDate hoy     = LocalDate.now();
        LocalDate primero = hoy.withDayOfMonth(1);

        Filtro filtro = new Filtro();
        filtro.setRangoFechas(primero, hoy);

        return controlador.getGastosPorFiltro(filtro).stream()
                .collect(Collectors.groupingBy(
                    Gasto::getCategoria,
                    Collectors.summingDouble(Gasto::getCantidad)
                ));
    }

    public VBox getView() {
        return root;
    }
}