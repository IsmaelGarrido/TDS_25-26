package es.um.tds.gestiongastos.vista;

import es.um.tds.gestiongastos.modelo.CuentaCompartida;
import es.um.tds.gestiongastos.modelo.Persona;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.List;
import java.util.Map;

/**
 * Sección de inicio.
 *
 * Muestra:
 *   ├── Dos KPIs: gasto personal del mes actual + total gestionado en cuentas
 *   └── Tarjetas de cuentas compartidas con saldos por persona
 *
 * Nota: cuando se implemente la clase Usuario, el chip del usuario se
 * resaltará y aparecerá primero en cada tarjeta. Por ahora se muestran
 * todos los saldos sin distinción.
 */
public class InicioView extends SeccionView {

    @Override
    public String getNombre() {
        return "Inicio";
    }
    
    @Override
    public String getIconPath() {return "/images/nav/inicio.png";}

    /**
     * Construye la vista de inicio con datos frescos del controlador.
     * Se llama cada vez que el usuario navega a esta sección.
     */
    @Override
    public Region getView() {
        VBox root = new VBox(16);
        root.setPadding(new Insets(20, 24, 20, 24));

        root.getChildren().addAll(
            buildFilaKPIs(),
            buildListaCuentas()
        );

        ScrollPane scroll = new ScrollPane(root);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.getStyleClass().add("scroll-transparente");

        return scroll;
    }

    private HBox buildFilaKPIs() {
        HBox fila = new HBox(12);

        double gastoMes       = controlador.getGastoPersonalMesActual();
        double totalEnCuentas = controlador.getTotalGastosEnCuentas();

        fila.getChildren().addAll(
            buildKPI("Gasto personal este mes",
                     String.format("%.2f €", gastoMes),
                     gastoMes > 0 ? "kpi-value-normal" : "kpi-value-vacio"),
            buildKPI("Total gestionado en cuentas",
                     String.format("%.2f €", totalEnCuentas),
                     "kpi-value-normal")
        );

        for (javafx.scene.Node n : fila.getChildren()) {
            HBox.setHgrow(n, Priority.ALWAYS);
        }

        return fila;
    }

    private VBox buildKPI(String etiqueta, String valor, String valorStyleClass) {
        VBox card = new VBox(4);
        card.getStyleClass().add("kpi-card");
        card.setPadding(new Insets(14, 18, 14, 18));

        Label lblEtiqueta = new Label(etiqueta);
        lblEtiqueta.getStyleClass().add("kpi-label");
        lblEtiqueta.setFont(Font.font("System", 11));

        Label lblValor = new Label(valor);
        lblValor.setFont(Font.font("System", FontWeight.BOLD, 22));
        lblValor.getStyleClass().add(valorStyleClass);

        card.getChildren().addAll(lblEtiqueta, lblValor);
        return card;
    }

    private VBox buildListaCuentas() {
        VBox contenedor = new VBox(10);

        List<CuentaCompartida> cuentas = controlador.getCuentas();

        if (cuentas.isEmpty()) {
            Label vacio = new Label("No hay cuentas compartidas. Créalas desde la sección Cuentas.");
            vacio.setStyle("-fx-text-fill: -fx-text-secondary; -fx-font-size: 12;");
            contenedor.getChildren().add(vacio);
            return contenedor;
        }

        Label titulo = new Label("Cuentas compartidas");
        titulo.setFont(Font.font("System", FontWeight.BOLD, 14));
        titulo.getStyleClass().add("panel-titulo");
        contenedor.getChildren().add(titulo);

        for (CuentaCompartida cuenta : cuentas) {
            contenedor.getChildren().add(buildTarjetaCuenta(cuenta));
        }

        return contenedor;
    }

    private VBox buildTarjetaCuenta(CuentaCompartida cuenta) {
        VBox card = new VBox(8);
        card.getStyleClass().add("cuenta-card");
        card.setPadding(new Insets(14, 16, 14, 16));

        HBox cabecera = new HBox(8);
        cabecera.setAlignment(Pos.CENTER_LEFT);

        Label lblNombre = new Label(cuenta.getNombre());
        lblNombre.setFont(Font.font("System", FontWeight.BOLD, 14));
        lblNombre.getStyleClass().add("cuenta-nombre");

        Label lblTipo = new Label(cuenta.obtenerTipoDescripcion());
        lblTipo.getStyleClass().add("tipo-badge");

        cabecera.getChildren().addAll(lblNombre, lblTipo);

        Label lblMeta = new Label(
            cuenta.calcularNumPersonas() + " miembros · "
            + String.format("Total: %.2f €", cuenta.calcularGastoTotal())
        );
        lblMeta.getStyleClass().add("cuenta-meta");
        lblMeta.setStyle("-fx-text-fill: -fx-text-secondary; -fx-font-size: 11;");

        FlowPane chipsSaldo = buildChipsSaldo(cuenta);

        card.getChildren().addAll(cabecera, lblMeta, new javafx.scene.control.Separator(), chipsSaldo);
        return card;
    }

    private FlowPane buildChipsSaldo(CuentaCompartida cuenta) {
        FlowPane fila = new FlowPane(8, 6);
        fila.setAlignment(Pos.CENTER_LEFT);

        Map<Persona, Double> saldos = cuenta.getSaldos();

        cuenta.getPersonas().stream()
            .sorted((a, b) -> Double.compare(
                saldos.getOrDefault(b, 0.0),
                saldos.getOrDefault(a, 0.0)))
            .forEach(persona -> {
                double saldo = saldos.getOrDefault(persona, 0.0);
                fila.getChildren().add(buildChip(persona.getNombre(), saldo));
            });

        return fila;
    }

    private VBox buildChip(String nombre, double saldo) {
        VBox chip = new VBox(2);
        chip.getStyleClass().add("saldo-chip");
        chip.setPadding(new Insets(5, 10, 5, 10));
        chip.setAlignment(Pos.CENTER);

        Label lblNombre = new Label(nombre);
        lblNombre.setStyle("-fx-font-size: 10; -fx-text-fill: -fx-text-secondary;");

        Label lblSaldo = new Label(String.format("%+.2f €", saldo));
        lblSaldo.setFont(Font.font("System", FontWeight.BOLD, 13));

        if (saldo > 0.005) {
            lblSaldo.setStyle("-fx-text-fill: #4ecca3;");   // positivo: verde
        } else if (saldo < -0.005) {
            lblSaldo.setStyle("-fx-text-fill: #e06060;");   // negativo: rojo
        } else {
            lblSaldo.setStyle("-fx-text-fill: -fx-text-secondary;"); // cero: gris
        }

        chip.getChildren().addAll(lblNombre, lblSaldo);
        return chip;
    }
}