package es.um.tds.gestiongastos.vista;

import es.um.tds.gestiongastos.controlador.GestionGastos;
import es.um.tds.gestiongastos.modelo.Notificacion;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Popup;

import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

/**
 * Vista principal de la aplicación.
 *
 * Layout:
 *   BorderPane
 *   ├── TOP    → Toolbar (HBox, ancho completo)
 *   └── CENTER → SplitPane 33/67
 *                ├── NavPanel (VBox con botones de navegación)
 *                └── ContentArea (StackPane — intercambia SeccionView)
 *
 * La lista de secciones se construye en crearSecciones().
 * Añadir una nueva sección solo requiere añadir una línea ahí.
 */
public class MainView {

    private final GestionGastos controlador = GestionGastos.getInstance();

    private static final String PREF_USUARIO = "nombre_usuario";
    private final Preferences prefs = Preferences.userNodeForPackage(MainView.class);

    private BorderPane root;
    private Button btnUsuario;
    private Button btnNotificaciones;
    private Popup popupNotif;

    private final List<SeccionView> secciones = new ArrayList<>();
    private VBox navPanel;
    private StackPane contentArea;

    public MainView() {
        crearSecciones();
        buildView();
        navegarA(0);
    }

    /**
     * Define el orden y las secciones disponibles.
     * Para añadir una nueva sección basta con añadir una línea aquí.
     */
    private void crearSecciones() {
        secciones.add(new InicioView());
        secciones.add(new GastosView());
        secciones.add(new CuentasView());
        secciones.add(new AlertasView());
        secciones.add(new ImportarView());
        secciones.add(new CategoriasView());
    }

    private void buildView() {
        root = new BorderPane();
        root.getStyleClass().add("main-container");
        root.setTop(buildToolbar());
        root.setCenter(buildSplitPane());
        root.setBottom(new TerminalPanel().buildPanel());
    }

    private HBox buildToolbar() {
        HBox toolbar = new HBox(8);
        toolbar.getStyleClass().add("toolbar");
        toolbar.setPadding(new Insets(0, 14, 0, 14));
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setPrefHeight(48);

        Label logo = new Label("GestionGastos");
        logo.getStyleClass().add("toolbar-logo");
        logo.setFont(Font.font("System", FontWeight.BOLD, 14));

        javafx.scene.image.ImageView logoIcon = cargarIcono("/images/toolbar/logo.png",20);
        if (logoIcon != null) {logo.setGraphic(logoIcon); logo.setGraphicTextGap(8);}
        
        Separator sep = new Separator(javafx.geometry.Orientation.VERTICAL);

        Button btnGuardar = crearToolbarBtn("Guardar",
            cargarIcono("/images/toolbar/guardar.png", 16), () -> {
            boolean ok = controlador.guardar();
            Alert a = new Alert(Alert.AlertType.INFORMATION);
            a.setHeaderText(null);
            a.setContentText(ok ? "Datos guardados." : "Error al guardar.");
            a.showAndWait();
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        btnUsuario = crearToolbarBtn(
            "👤 " + prefs.get(PREF_USUARIO, "Usuario"),
            cargarIcono("/images/toolbar/usuario.png", 16),
            this::cambiarNombreUsuario);

        btnNotificaciones = crearToolbarBtn("",
            cargarIcono("/images/toolbar/notificaciones.png", 16),
            this::togglePopupNotificaciones);
        actualizarBtnNotificaciones();

        toolbar.getChildren().addAll(logo, sep, btnGuardar, spacer, btnUsuario, btnNotificaciones);
        return toolbar;
    }

    private Button crearToolbarBtn(String texto,
            javafx.scene.image.ImageView icono, Runnable accion) {
        Button btn = new Button(texto);
        btn.getStyleClass().add("btn-accion");
        if (icono != null) {
            btn.setGraphic(icono);
            btn.setGraphicTextGap(6);
        }
        btn.setOnAction(e -> accion.run());
        return btn;
    }

    public void actualizarBtnNotificaciones() {
        int n = controlador.getNumeroNotificacionesNoLeidas();
        btnNotificaciones.setText(n > 0 ? "(" + n + ")" : "");
    }

    private void cambiarNombreUsuario() {
        String actual = prefs.get(PREF_USUARIO, "Usuario");
        TextInputDialog dialog = new TextInputDialog(actual);
        dialog.setTitle("Nombre de usuario");
        dialog.setHeaderText("Cambiar nombre de usuario");
        dialog.setContentText("Nombre:");
        dialog.showAndWait().ifPresent(nombre -> {
            if (!nombre.isBlank()) {
                String n = nombre.trim();
                prefs.put(PREF_USUARIO, n);          // persiste entre ejecuciones
                btnUsuario.setText("👤 " + n);
            }
        });
    }

    /** Muestra u oculta el popup de notificaciones no leídas. */
    private void togglePopupNotificaciones() {
        if (popupNotif != null && popupNotif.isShowing()) {
            popupNotif.hide();
            return;
        }
        popupNotif = buildPopupNotificaciones();
        if (popupNotif == null) return;
        javafx.geometry.Bounds b =
            btnNotificaciones.localToScreen(btnNotificaciones.getBoundsInLocal());
        popupNotif.show(btnNotificaciones,
            b.getMaxX() - 300,   // ancho del popup = 300px
            b.getMaxY() + 6);
    }

    /**
     * Construye el popup con las notificaciones no leídas.
     * Devuelve null si no hay ninguna.
     */
    private Popup buildPopupNotificaciones() {
        List<Notificacion> noLeidas = controlador.getNotificacionesNoLeidas();

        Popup popup = new Popup();
        popup.setAutoHide(true);

        VBox contenedor = new VBox(0);
        contenedor.setPrefWidth(300);
        contenedor.setMaxWidth(300);
        contenedor.setStyle(
            "-fx-background-color:#16213e;" +
            "-fx-border-color:#2a3f5f;" +
            "-fx-border-radius:8;" +
            "-fx-background-radius:8;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 12, 0, 0, 4);"
        );

        HBox cabecera = new HBox();
        cabecera.setAlignment(Pos.CENTER_LEFT);
        cabecera.setPadding(new Insets(10, 14, 10, 14));
        cabecera.setStyle("-fx-border-color:#2a3f5f; -fx-border-width:0 0 1 0;");

        Label lblTitulo = new Label("Notificaciones sin leer");
        lblTitulo.setStyle("-fx-text-fill:#eeeeee; -fx-font-size:13; -fx-font-weight:bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label lblContador = new Label(noLeidas.size() + "");
        lblContador.setStyle(
            "-fx-background-color:#4ecca3; -fx-text-fill:#1a1a2e;" +
            "-fx-background-radius:10; -fx-padding:1 7;" +
            "-fx-font-size:11; -fx-font-weight:bold;"
        );

        cabecera.getChildren().addAll(lblTitulo, spacer, lblContador);
        contenedor.getChildren().add(cabecera);

        if (noLeidas.isEmpty()) {
            Label vacio = new Label("Sin notificaciones pendientes");
            vacio.setStyle("-fx-text-fill:#888888; -fx-font-size:12; -fx-padding:16;");
            vacio.setMaxWidth(Double.MAX_VALUE);
            vacio.setAlignment(Pos.CENTER);
            contenedor.getChildren().add(vacio);
        } else {
            ScrollPane scroll = new ScrollPane();
            scroll.setFitToWidth(true);
            scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            scroll.setStyle("-fx-background:transparent; -fx-background-color:transparent;");
            scroll.setMaxHeight(260);

            VBox lista = new VBox(0);
            lista.setStyle("-fx-background-color:#16213e;");

            noLeidas.stream().limit(10).forEach(notif -> {
                HBox fila = buildFilaNotif(notif);
                lista.getChildren().add(fila);
            });

            scroll.setContent(lista);
            contenedor.getChildren().add(scroll);
        }

        HBox pie = new HBox(8);
        pie.setPadding(new Insets(8, 14, 8, 14));
        pie.setAlignment(Pos.CENTER_RIGHT);
        pie.setStyle("-fx-border-color:#2a3f5f; -fx-border-width:1 0 0 0;");

        Button btnMarcar = new Button("Marcar todas leídas");
        btnMarcar.setStyle(
            "-fx-background-color:transparent; -fx-text-fill:#888888;" +
            "-fx-font-size:11; -fx-cursor:hand; -fx-padding:0;"
        );
        btnMarcar.setOnAction(e -> {
            controlador.marcarNotificacionesComoLeidas();
            actualizarBtnNotificaciones();
            popup.hide();
        });

        Button btnVerTodas = new Button("Ver en Alertas →");
        btnVerTodas.setStyle(
            "-fx-background-color:transparent; -fx-text-fill:#4ecca3;" +
            "-fx-font-size:11; -fx-cursor:hand; -fx-padding:0;"
        );
        btnVerTodas.setOnAction(e -> {
            popup.hide();
            irAlertas();
        });

        pie.getChildren().addAll(btnMarcar, btnVerTodas);
        contenedor.getChildren().add(pie);

        popup.getContent().add(contenedor);
        return popup;
    }

    /** Construye una fila individual de notificación dentro del popup. */
    private HBox buildFilaNotif(Notificacion notif) {
        HBox fila = new HBox(10);
        fila.setPadding(new Insets(10, 14, 10, 14));
        fila.setAlignment(Pos.TOP_LEFT);
        fila.setStyle("-fx-border-color:#2a3f5f; -fx-border-width:0 0 1 0;");

        // Indicador de no leída
        Label punto = new Label("●");
        punto.setStyle("-fx-text-fill:#f39c12; -fx-font-size:10; -fx-padding:3 0 0 0;");
        punto.setMinWidth(12);

        // Contenido
        VBox info = new VBox(3);
        HBox.setHgrow(info, Priority.ALWAYS);

        Label lblMsg = new Label(notif.getMensaje());
        lblMsg.setStyle("-fx-text-fill:#eeeeee; -fx-font-size:11;");
        lblMsg.setWrapText(true);
        lblMsg.setMaxWidth(220);

        Label lblFecha = new Label(notif.getFecha().format(
            java.time.format.DateTimeFormatter.ofPattern("dd/MM HH:mm")));
        lblFecha.setStyle("-fx-text-fill:#888888; -fx-font-size:10;");

        info.getChildren().addAll(lblMsg, lblFecha);
        fila.getChildren().addAll(punto, info);

        fila.setOnMouseEntered(e ->
            fila.setStyle("-fx-background-color:#1a2744;" +
                          "-fx-border-color:#2a3f5f; -fx-border-width:0 0 1 0;"));
        fila.setOnMouseExited(e ->
            fila.setStyle("-fx-border-color:#2a3f5f; -fx-border-width:0 0 1 0;"));

        return fila;
    }

    private void irAlertas() {
        for (int i = 0; i < secciones.size(); i++) {
            if (secciones.get(i) instanceof AlertasView) { navegarA(i); return; }
        }
    }
    
    private SplitPane buildSplitPane() {
        SplitPane split = new SplitPane();
        split.getStyleClass().add("main-split");
        split.setDividerPositions(0.33);
        split.getItems().addAll(buildNavPanel(), buildContentArea());
        SplitPane.setResizableWithParent(split.getItems().get(0), false);
        return split;
    }

    /**
     * Construye el panel de navegación dinámicamente a partir de la lista
     * de secciones. Cada SeccionView aporta su propio nombre e icono.
     */
    private VBox buildNavPanel() {
        navPanel = new VBox(0);
        navPanel.getStyleClass().add("nav-panel");

        for (int i = 0; i < secciones.size(); i++) {
            final int idx = i;
            SeccionView seccion = secciones.get(i);

            Button btn = new Button(seccion.getNombre());
            btn.getStyleClass().add("nav-button");
            btn.setMaxWidth(Double.MAX_VALUE);
            btn.setAlignment(Pos.CENTER_LEFT);
            btn.setPadding(new Insets(12, 20, 12, 20));
            btn.setGraphicTextGap(10);

            // Cargar icono si la sección lo proporciona
            javafx.scene.image.ImageView icono = cargarIcono(seccion.getIconPath(), 18);
            if (icono != null) btn.setGraphic(icono);

            btn.setOnAction(e -> navegarA(idx));
            navPanel.getChildren().add(btn);
        }

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        navPanel.getChildren().add(spacer);

        return navPanel;
    }

    /**
     * Carga una imagen desde recursos y devuelve un ImageView del tamaño indicado.
     * Devuelve null si la ruta es null o el recurso no existe.
     *
     * @param ruta   Ruta del recurso, ej: "/images/nav/gastos.png"
     * @param tamanyo Tamaño en píxeles (ancho y alto)
     */
    private javafx.scene.image.ImageView cargarIcono(String ruta, int tamanyo) {
        if (ruta == null) return null;
        try {
            var stream = getClass().getResourceAsStream(ruta);
            if (stream == null) return null;
            javafx.scene.image.Image img = new javafx.scene.image.Image(stream);
            javafx.scene.image.ImageView iv = new javafx.scene.image.ImageView(img);
            iv.setFitWidth(tamanyo);
            iv.setFitHeight(tamanyo);
            iv.setPreserveRatio(true);
            iv.setSmooth(true);
            return iv;
        } catch (Exception e) {
            return null; // Si el icono no está disponible, el botón funciona sin él
        }
    }

    private StackPane buildContentArea() {
        contentArea = new StackPane();
        contentArea.getStyleClass().add("content-area");
        return contentArea;
    }
    
    /**
     * Navega a la sección en la posición indicada.
     * Actualiza el estilo del botón activo y carga la vista de la sección.
     */
    private void navegarA(int indice) {
        if (indice < 0 || indice >= secciones.size()) return;

        for (int i = 0; i < secciones.size(); i++) {
            Button btn = (Button) navPanel.getChildren().get(i);
            btn.getStyleClass().remove("nav-button-selected");
            if (i == indice) btn.getStyleClass().add("nav-button-selected");
        }

        javafx.scene.layout.Region vista = secciones.get(indice).getView();
        vista.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        contentArea.getChildren().clear();
        contentArea.getChildren().add(vista);
        StackPane.setAlignment(vista, Pos.TOP_LEFT);

        actualizarBtnNotificaciones();
    }

    public BorderPane getRoot() {
        return root;
    }
}