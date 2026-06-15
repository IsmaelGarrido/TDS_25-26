package es.um.tds.gestiongastos.main;

import es.um.tds.gestiongastos.controlador.GestionGastos;
import es.um.tds.gestiongastos.vista.MainView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Punto de entrada de la aplicación JavaFX.
 * Inicializa el controlador, construye la vista principal
 * y gestiona el ciclo de vida de la ventana.
 */
public class MainApp extends Application {

    private static final String TITULO     = "Gestión de Gastos";
    private static final double MIN_ANCHO  = 900;
    private static final double MIN_ALTO   = 600;
    private static final double PREF_ANCHO = 1200;
    private static final double PREF_ALTO  = 700;

    @Override
    public void start(Stage primaryStage) {

        // 1. Inicializar controlador y cargar datos persistidos
        GestionGastos.getInstance().inicializar();

        // 2. Construir la vista principal
        MainView mainView = new MainView();

        // 3. Crear la escena y cargar el CSS
        Scene scene = new Scene(mainView.getRoot(), PREF_ANCHO, PREF_ALTO);
        scene.getStylesheets().add(
            getClass().getResource("/css/styles.css").toExternalForm()
        );

        // 4. Configurar la ventana
        primaryStage.setTitle(TITULO);
        primaryStage.setMinWidth(MIN_ANCHO);
        primaryStage.setMinHeight(MIN_ALTO);
        primaryStage.setScene(scene);

        // 5. Guardar datos al cerrar
        primaryStage.setOnCloseRequest(e -> GestionGastos.getInstance().guardar());

        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}