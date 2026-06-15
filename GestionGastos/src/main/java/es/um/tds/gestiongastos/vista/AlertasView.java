package es.um.tds.gestiongastos.vista;

import es.um.tds.gestiongastos.modelo.Alerta;
import es.um.tds.gestiongastos.modelo.Categoria;
import es.um.tds.gestiongastos.modelo.Notificacion;
import es.um.tds.gestiongastos.modelo.TipoAlerta;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.ArrayList;
import java.util.List;

public class AlertasView extends SeccionView {

    private ObservableList<Alerta>       alertaItems;
    private ObservableList<Notificacion> notifItems;

    private ListView<Alerta>       listaAlertas;
    private ListView<Notificacion> listaNotifs;

    @Override 
    public String getNombre() { return "Alertas"; }

    @Override 
    public String getIconPath() {return "/images/nav/alertas.png";}
    
    /** Reconstruye la vista cada vez para mostrar datos nuevos. */
    @Override
    public Region getView() {
        return buildView();
    }

    private VBox buildView() {
        VBox root = new VBox(14);
        root.setPadding(new Insets(16, 20, 16, 20));

        root.getChildren().addAll(
            buildBarraAcciones(),
            buildSeccionAlertas(),
            buildHistorial()
        );

        VBox.setVgrow(buildSeccionAlertas(), Priority.ALWAYS);
        return root;
    }

    private HBox buildBarraAcciones() {
        HBox barra = new HBox();
        Button btnNueva = new Button("+ Nueva alerta");
        btnNueva.getStyleClass().add("btn-accion");
        btnNueva.setOnAction(e -> mostrarDialogoNuevaAlerta());
        barra.getChildren().add(btnNueva);
        return barra;
    }

    private VBox buildSeccionAlertas() {
        VBox contenedor = new VBox(8);

        Label titulo = new Label("Alertas configuradas");
        titulo.setFont(Font.font("System", FontWeight.BOLD, 14));
        titulo.getStyleClass().add("panel-titulo");

        alertaItems = FXCollections.observableArrayList(controlador.getAlertas());
        listaAlertas = new ListView<>(alertaItems);
        listaAlertas.getStyleClass().add("lista-alertas");
        listaAlertas.setPrefHeight(200);
        listaAlertas.setFixedCellSize(56);
        listaAlertas.setPlaceholder(new Label("No hay alertas configuradas"));
        listaAlertas.setCellFactory(lv -> new AlertaCell());

        contenedor.getChildren().addAll(titulo, listaAlertas);
        VBox.setVgrow(listaAlertas, Priority.ALWAYS);
        return contenedor;
    }

    private TitledPane buildHistorial() {
        Label tituloGrafico = new Label("Historial de notificaciones");
        tituloGrafico.setFont(Font.font("System", FontWeight.BOLD, 13));

        Button btnLimpiar = new Button("Marcar todas como leídas");
        btnLimpiar.getStyleClass().add("btn-accion");
        btnLimpiar.setOnAction(e -> {
            controlador.marcarNotificacionesComoLeidas();
            recargarNotificaciones();
        });

        HBox barraNotif = new HBox(btnLimpiar);
        barraNotif.setAlignment(Pos.CENTER_RIGHT);

        notifItems = FXCollections.observableArrayList(controlador.getNotificaciones());
        listaNotifs = new ListView<>(notifItems);
        listaNotifs.getStyleClass().add("lista-notificaciones");
        listaNotifs.setPrefHeight(150);
        listaNotifs.setFixedCellSize(46);
        listaNotifs.setPlaceholder(new Label("Sin notificaciones"));
        listaNotifs.setCellFactory(lv -> new NotifCell());

        VBox contenido = new VBox(8, barraNotif, listaNotifs);
        contenido.setPadding(new Insets(8, 0, 4, 0));

        TitledPane pane = new TitledPane();
        pane.setGraphic(tituloGrafico);
        pane.setText(null);
        pane.setContent(contenido);
        pane.setExpanded(true);
        pane.getStyleClass().add("historial-pane");
        return pane;
    }

    /** Celda de alerta: icono | info | botón toggle | botón eliminar */
    private class AlertaCell extends ListCell<Alerta> {

        private final HBox   fila        = new HBox(10);
        private final Label  lblIcono    = new Label();
        private final Label  lblNombre   = new Label();
        private final Label  lblMeta     = new Label();
        private final VBox   info        = new VBox(3, lblNombre, lblMeta);
        private final Region spacer      = new Region();
        private final Button btnToggle   = new Button();
        private final Button btnEliminar = new Button("Eliminar");

        AlertaCell() {
            lblNombre.getStyleClass().add("alerta-nombre");
            lblNombre.setFont(Font.font("System", FontWeight.SEMI_BOLD, 13));
            lblMeta.setStyle("-fx-text-fill:-fx-text-secondary;-fx-font-size:11;");
            lblIcono.setMinWidth(28);
            lblIcono.setAlignment(Pos.CENTER);
            lblIcono.setFont(Font.font("System", 18));
            btnToggle.getStyleClass().add("btn-accion");
            btnEliminar.getStyleClass().addAll("btn-accion", "btn-peligro");
            HBox.setHgrow(spacer, Priority.ALWAYS);
            HBox.setHgrow(info,   Priority.ALWAYS);
            fila.setAlignment(Pos.CENTER_LEFT);
            fila.setPadding(new Insets(6, 8, 6, 8));
            fila.getChildren().addAll(lblIcono, info, spacer, btnToggle, btnEliminar);
        }

        @Override
        protected void updateItem(Alerta alerta, boolean empty) {
            super.updateItem(alerta, empty);
            if (empty || alerta == null) { setGraphic(null); return; }

            String nombreCat = alerta.getCategoria()
                .map(Categoria::getNombre).orElse("General");
            lblNombre.setText(nombreCat);
            lblMeta.setText(alerta.getTipoAlerta().getDescripcion()
                + "  ·  Tope: " + String.format("%.2f €", alerta.getMaxGasto()));

            if (alerta.getActiva()) {
                lblIcono.setText("●");
                lblIcono.setStyle("-fx-text-fill:#4ecca3;");
                btnToggle.setText("Desactivar");
                fila.setOpacity(1.0);
            } else {
                lblIcono.setText("○");
                lblIcono.setStyle("-fx-text-fill:#888780;");
                btnToggle.setText("Activar");
                fila.setOpacity(0.55);
            }

            btnToggle.setOnAction(e -> {
                if (alerta.getActiva()) controlador.desactivarAlerta(alerta);
                else                    controlador.activarAlerta(alerta);
                recargarAlertas();
            });

            btnEliminar.setOnAction(e -> confirmarEliminar(alerta));

            setGraphic(fila);
            setText(null);
        }
    }

    /** Celda de notificación: indicador leída | mensaje | fecha */
    private class NotifCell extends ListCell<Notificacion> {

        private final HBox   fila      = new HBox(10);
        private final Label  lblPunto  = new Label();
        private final Label  lblMsg    = new Label();
        private final Region spacer    = new Region();
        private final Label  lblFecha  = new Label();

        NotifCell() {
            lblPunto.setMinWidth(14);
            lblPunto.setFont(Font.font("System", 14));
            lblMsg.setWrapText(true);
            lblMsg.setStyle("-fx-font-size:12;");
            lblFecha.setStyle("-fx-text-fill:-fx-text-secondary;-fx-font-size:11;");
            HBox.setHgrow(spacer, Priority.ALWAYS);
            fila.setAlignment(Pos.CENTER_LEFT);
            fila.setPadding(new Insets(6, 8, 6, 8));
            fila.getChildren().addAll(lblPunto, lblMsg, spacer, lblFecha);
        }

        @Override
        protected void updateItem(Notificacion notif, boolean empty) {
            super.updateItem(notif, empty);
            if (empty || notif == null) { setGraphic(null); return; }

            lblMsg.setText(notif.getMensaje());
            lblFecha.setText(notif.getFecha().format(FECHA_HORA_FMT));

            if (notif.esLeida()) {
                lblPunto.setText("·");
                lblPunto.setStyle("-fx-text-fill:#888780;");
                fila.setOpacity(0.6);
            } else {
                lblPunto.setText("●");
                lblPunto.setStyle("-fx-text-fill:#f39c12;");
                fila.setOpacity(1.0);
            }

            setGraphic(fila);
            setText(null);
        }
    }

    private void mostrarDialogoNuevaAlerta() {
        Dialog<Alerta> dialog = new Dialog<>();
        dialog.setTitle("Nueva alerta");
        dialog.setHeaderText("Configurar alerta de gasto");

        ButtonType btnCrear = new ButtonType("Crear", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnCrear, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(12); grid.setVgap(10);
        grid.setPadding(new Insets(16));

        ComboBox<TipoAlerta> cmbTipo = new ComboBox<>(
            FXCollections.observableArrayList(TipoAlerta.values()));
        cmbTipo.setValue(TipoAlerta.MENSUAL);
        cmbTipo.setMaxWidth(Double.MAX_VALUE);

        TextField txtTope = new TextField();
        txtTope.setPromptText("Ej: 200.00");

        List<Categoria> cats = new ArrayList<>();
        cats.add(null);
        cats.addAll(controlador.getCategorias());
        ComboBox<Categoria> cmbCat = new ComboBox<>(
            FXCollections.observableArrayList(cats));
        cmbCat.setValue(null);
        cmbCat.setMaxWidth(Double.MAX_VALUE);
        cmbCat.setButtonCell(new CategoriaCell("— General (sin categoría)"));
        cmbCat.setCellFactory(lv -> new CategoriaCell("— General (sin categoría)"));

        int f = 0;
        grid.add(new Label("Tipo:"),           0, f); grid.add(cmbTipo, 1, f++);
        grid.add(new Label("Tope (€):"),       0, f); grid.add(txtTope, 1, f++);
        grid.add(new Label("Categoría:"),      0, f); grid.add(cmbCat,  1, f++);
        grid.add(new Label("(vacío = alerta general)"), 1, f);

        ColumnConstraints c0 = new ColumnConstraints(); c0.setMinWidth(100);
        ColumnConstraints c1 = new ColumnConstraints(); c1.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(c0, c1);
        dialog.getDialogPane().setContent(grid);

        Node nodoCrear = dialog.getDialogPane().lookupButton(btnCrear);
        nodoCrear.setDisable(true);
        txtTope.textProperty().addListener((obs, o, v) ->
            nodoCrear.setDisable(!esNumeroPositivo(v)));

        dialog.setResultConverter(tipo -> {
            if (tipo != btnCrear) return null;
            try {
                double    tope = parsearDouble(txtTope.getText());
                Categoria cat  = cmbCat.getValue();
                return cat == null
                    ? controlador.crearAlerta(tope, cmbTipo.getValue())
                    : controlador.crearAlerta(tope, cmbTipo.getValue(), cat);
            } catch (IllegalArgumentException ex) {
                mostrarError(ex.getMessage());
                return null;
            }
        });

        dialog.showAndWait().ifPresent(a -> { if (a != null) recargarAlertas(); });
    }

    private void confirmarEliminar(Alerta alerta) {
        String cat = alerta.getCategoria()
            .map(Categoria::getNombre).orElse("General");
        boolean ok = mostrarConfirmacion(
            "Eliminar alerta",
            "¿Eliminar alerta de " + cat + "?",
            "Tope: " + String.format("%.2f €", alerta.getMaxGasto())
                + " · " + alerta.getTipoAlerta().getDescripcion());
        if (ok) {
            controlador.eliminarAlerta(alerta);
            recargarAlertas();
        }
    }

    private void recargarAlertas() {
        alertaItems.setAll(controlador.getAlertas());
        recargarNotificaciones();
    }

    private void recargarNotificaciones() {
        notifItems.setAll(controlador.getNotificaciones());
    }
}