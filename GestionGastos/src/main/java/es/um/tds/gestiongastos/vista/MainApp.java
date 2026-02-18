package es.um.tds.gestiongastos.vista;

import es.um.tds.gestiongastos.controlador.GestionGastos;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Clase principal de la aplicación JavaFX.
 */
public class MainApp extends Application {
    
    private static final String TITULO = "Gestión de Gastos";
    private static final double MIN_WIDTH = 900;
    private static final double MIN_HEIGHT = 600;
    private static final double PREF_WIDTH = 1200;
    private static final double PREF_HEIGHT = 700;
    
    @Override
    public void start(Stage primaryStage) {
        // Inicializar controlador y cargar datos
        GestionGastos.getInstance().inicializar();
        
        // Crear la vista principal
        MainView mainView = new MainView();
        
        // Crear escena
        Scene scene = new Scene(mainView.getRoot(), PREF_WIDTH, PREF_HEIGHT);
        
        // Cargar estilos CSS
        scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
        
        // Configurar ventana
        primaryStage.setTitle(TITULO);
        primaryStage.setMinWidth(MIN_WIDTH);
        primaryStage.setMinHeight(MIN_HEIGHT);
        primaryStage.setScene(scene);
        
        // Guardar datos al cerrar
        primaryStage.setOnCloseRequest(e -> {
            GestionGastos.getInstance().guardar();
        });
        
        primaryStage.show();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}