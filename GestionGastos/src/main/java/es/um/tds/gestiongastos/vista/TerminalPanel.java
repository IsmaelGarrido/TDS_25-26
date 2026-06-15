package es.um.tds.gestiongastos.vista;

import es.um.tds.gestiongastos.controlador.GestionGastos;
import es.um.tds.gestiongastos.modelo.Categoria;
import es.um.tds.gestiongastos.modelo.Gasto;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.text.Font;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Panel de línea de comandos básica.
 *
 * Comandos disponibles:
 *   nuevo <cantidad> <categoria> [nota] [--metodo <metodo>]
 *   editar <id> cantidad|categoria|nota|metodo <valor>
 *   borrar <id>
 *   listar [--categoria <nombre>]
 *   ayuda
 *
 * Se coloca en la parte inferior de MainView como TitledPane colapsable.
 */
public class TerminalPanel {

    private static final String PROMPT = "$ ";

    private final GestionGastos controlador = GestionGastos.getInstance();

    private TextArea areaOutput;
    private TextField campoEntrada;

    /**
     * Devuelve el panel CLI listo para añadir al BorderPane de MainView.
     */
    public TitledPane buildPanel() {
        areaOutput = new TextArea();
        areaOutput.setEditable(false);
        areaOutput.setPrefHeight(140);
        areaOutput.setWrapText(true);
        areaOutput.setFont(Font.font("Monospaced", 12));
        areaOutput.getStyleClass().add("cli-output");
        VBox.setVgrow(areaOutput, Priority.ALWAYS);

        campoEntrada = new TextField();
        campoEntrada.setPromptText("Escribe un comando (ayuda para ver opciones)...");
        campoEntrada.setFont(Font.font("Monospaced", 12));
        campoEntrada.getStyleClass().add("cli-input");
        HBox.setHgrow(campoEntrada, Priority.ALWAYS);

        Button btnEjecutar = new Button("Ejecutar");
        btnEjecutar.getStyleClass().add("btn-accion");
        btnEjecutar.setOnAction(e -> procesarEntrada());

        campoEntrada.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) procesarEntrada();
        });

        campoEntrada.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER)  procesarEntrada();
            if (e.getCode() == KeyCode.UP)     recuperarUltimoComando();
        });

        HBox barraEntrada = new HBox(8, new Label(PROMPT), campoEntrada, btnEjecutar);
        barraEntrada.setAlignment(Pos.CENTER_LEFT);
        barraEntrada.setPadding(new Insets(6, 8, 6, 8));
        barraEntrada.getStyleClass().add("cli-barra");

        VBox contenido = new VBox(0, areaOutput, barraEntrada);
        contenido.getStyleClass().add("cli-contenido");

        TitledPane panel = new TitledPane();
        panel.setText("Línea de comandos");
        panel.setContent(contenido);
        panel.setExpanded(false);       
        panel.setAnimated(true);
        panel.getStyleClass().add("cli-panel");

        imprimir("GestionGastos CLI — escribe 'ayuda' para ver los comandos disponibles.");
        return panel;
    }

    private String ultimoComando = "";

    private void procesarEntrada() {
        String linea = campoEntrada.getText().trim();
        if (linea.isEmpty()) return;

        ultimoComando = linea;
        imprimir(PROMPT + linea);
        campoEntrada.clear();

        String[] partes = linea.split("\\s+", 2);
        String comando = partes[0].toLowerCase();
        String args    = partes.length > 1 ? partes[1] : "";

        switch (comando) {
            case "nuevo"  -> cmdNuevo(args);
            case "editar" -> cmdEditar(args);
            case "borrar" -> cmdBorrar(args);
            case "listar" -> cmdListar(args);
            case "ayuda"  -> cmdAyuda();
            default       -> imprimir("Comando desconocido: '" + comando
                                + "'. Escribe 'ayuda' para ver los comandos disponibles.");
        }
        imprimir("");
    }

    private void recuperarUltimoComando() {
        campoEntrada.setText(ultimoComando);
        campoEntrada.positionCaret(ultimoComando.length());
    }

    /**
     * nuevo <cantidad> <categoria> [nota] [--metodo <metodo>]
     */
    private void cmdNuevo(String args) {
        if (args.isBlank()) {
            imprimir("Uso: nuevo <cantidad> <categoria> [nota] [--metodo <metodo>]");
            imprimir("Ej:  nuevo 25.50 Ocio \"Cine\"");
            return;
        }

        String metodo = null;
        String argsLimpios = args;
        int idxMetodo = args.indexOf("--metodo");
        if (idxMetodo >= 0) {
            metodo = args.substring(idxMetodo + 8).trim();
            argsLimpios = args.substring(0, idxMetodo).trim();
        }

        String[] tokens = tokenizar(argsLimpios);
        if (tokens.length < 2) {
            imprimir("Error: faltan argumentos. Uso: nuevo <cantidad> <categoria> [nota]");
            return;
        }

        double cantidad;
        try {
            cantidad = Double.parseDouble(tokens[0].replace(',', '.'));
            if (cantidad <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            imprimir("Error: cantidad inválida '" + tokens[0] + "'. Debe ser un número positivo.");
            return;
        }

        String nombreCat = tokens[1];
        Optional<Categoria> cat = controlador.buscarCategoria(nombreCat);
        if (cat.isEmpty()) {
            imprimir("Error: categoría '" + nombreCat + "' no encontrada.");
            imprimir("Categorías disponibles: " + listarNombresCategorias());
            return;
        }

        String nota = tokens.length > 2 ? tokens[2] : null;
        Gasto gasto = controlador.registrarGasto(cantidad, nota, metodo, cat.get());
        imprimir(String.format("✓ Gasto registrado [id=%d] — %.2f € en %s%s",
            gasto.getID(), cantidad, nombreCat,
            nota != null ? " · " + nota : ""));
    }

    /**
     * editar <id> <campo> <valor>
     * Campos: cantidad | categoria | nota | metodo
     */
    private void cmdEditar(String args) {
        if (args.isBlank()) {
            imprimir("Uso: editar <id> <campo> <valor>");
            imprimir("Campos: cantidad | categoria | nota | metodo");
            return;
        }

        String[] tokens = tokenizar(args);
        if (tokens.length < 3) {
            imprimir("Error: faltan argumentos. Uso: editar <id> <campo> <valor>");
            return;
        }

        int id;
        try { id = Integer.parseInt(tokens[0]); }
        catch (NumberFormatException e) {
            imprimir("Error: id inválido '" + tokens[0] + "'. Debe ser un número entero.");
            return;
        }

        Gasto gasto = buscarGastoPorId(id);
        if (gasto == null) {
            imprimir("Error: no existe ningún gasto con id=" + id + ".");
            imprimir("Usa 'listar' para ver los IDs disponibles.");
            return;
        }

        String campo = tokens[1].toLowerCase();
        String valor = tokens[2];

        Double nuevaCantidad   = null;
        LocalDateTime nuevaFecha = null;
        String nuevaNota       = gasto.getNota().orElse(null);
        String nuevoMetodo     = gasto.getMetodoPago().orElse(null);
        Categoria nuevaCat     = gasto.getCategoria();

        switch (campo) {
            case "cantidad" -> {
                try {
                    nuevaCantidad = Double.parseDouble(valor.replace(',', '.'));
                    if (nuevaCantidad <= 0) throw new NumberFormatException();
                } catch (NumberFormatException e) {
                    imprimir("Error: cantidad inválida '" + valor + "'.");
                    return;
                }
            }
            case "categoria" -> {
                Optional<Categoria> cat = controlador.buscarCategoria(valor);
                if (cat.isEmpty()) {
                    imprimir("Error: categoría '" + valor + "' no encontrada.");
                    imprimir("Categorías disponibles: " + listarNombresCategorias());
                    return;
                }
                nuevaCat = cat.get();
            }
            case "nota"   -> nuevaNota   = valor.isEmpty() ? null : valor;
            case "metodo" -> nuevoMetodo = valor.isEmpty() ? null : valor;
            default -> {
                imprimir("Error: campo '" + campo + "' desconocido.");
                imprimir("Campos válidos: cantidad | categoria | nota | metodo");
                return;
            }
        }

        boolean ok = controlador.editarGasto(gasto, nuevaCantidad, nuevaFecha,
                nuevaNota, nuevoMetodo, nuevaCat,
                gasto.getMoneda(), gasto.getPagador().orElse(null));

        if (ok) imprimir("✓ Gasto id=" + id + " actualizado correctamente.");
        else    imprimir("Error: no se pudo actualizar el gasto id=" + id + ".");
    }

    /**
     * borrar <id>
     */
    private void cmdBorrar(String args) {
        if (args.isBlank()) {
            imprimir("Uso: borrar <id>");
            return;
        }

        int id;
        try { id = Integer.parseInt(args.trim()); }
        catch (NumberFormatException e) {
            imprimir("Error: id inválido '" + args + "'. Debe ser un número entero.");
            return;
        }

        Gasto gasto = buscarGastoPorId(id);
        if (gasto == null) {
            imprimir("Error: no existe ningún gasto con id=" + id + ".");
            return;
        }

        String resumen = String.format("%.2f € en %s", gasto.getCantidad(),
                gasto.getCategoria().getNombre());
        boolean ok = controlador.eliminarGasto(gasto);
        if (ok) imprimir("✓ Gasto id=" + id + " eliminado (" + resumen + ").");
        else    imprimir("Error: no se pudo eliminar el gasto id=" + id + ".");
    }

    /**
     * listar [--categoria <nombre>]
     */
    private void cmdListar(String args) {
        List<Gasto> gastos;

        if (args.contains("--categoria")) {
            String nombreCat = args.substring(args.indexOf("--categoria") + 11).trim();
            Optional<Categoria> cat = controlador.buscarCategoria(nombreCat);
            if (cat.isEmpty()) {
                imprimir("Error: categoría '" + nombreCat + "' no encontrada.");
                return;
            }
            gastos = controlador.getGastosPorCategoria(cat.get());
            imprimir(String.format("%-6s %-12s %-18s %s", "ID", "Cantidad", "Categoría", "Nota"));
            imprimir("─".repeat(54));
        } else {
            gastos = controlador.getGastos();
            imprimir(String.format("%-6s %-12s %-18s %s", "ID", "Cantidad", "Categoría", "Nota"));
            imprimir("─".repeat(54));
        }

        if (gastos.isEmpty()) {
            imprimir("(sin gastos)");
            return;
        }

        gastos.stream()
            .sorted((a, b) -> b.getFecha().compareTo(a.getFecha()))
            .limit(10)
            .forEach(g -> imprimir(String.format("%-6d %-12s %-18s %s",
                g.getID(),
                String.format("%.2f €", g.getCantidad()),
                g.getCategoria().getNombre(),
                g.getNota().orElse("—"))));

        if (gastos.size() > 10)
            imprimir("  ... (" + (gastos.size() - 10) + " gastos más, usa filtros en la sección Gastos)");
    }

    /** Muestra la ayuda de todos los comandos. */
    private void cmdAyuda() {
        imprimir("Comandos disponibles:");
        imprimir("  nuevo <cantidad> <categoria> [nota] [--metodo <m>]");
        imprimir("      Registra un nuevo gasto personal.");
        imprimir("      Ej: nuevo 25.50 Ocio \"Cine\"");
        imprimir("");
        imprimir("  editar <id> <campo> <valor>");
        imprimir("      Modifica un campo de un gasto existente.");
        imprimir("      Campos: cantidad | categoria | nota | metodo");
        imprimir("      Ej: editar 3 cantidad 30");
        imprimir("");
        imprimir("  borrar <id>");
        imprimir("      Elimina el gasto con el id indicado.");
        imprimir("      Ej: borrar 5");
        imprimir("");
        imprimir("  listar [--categoria <nombre>]");
        imprimir("      Muestra los últimos 10 gastos (o por categoría).");
        imprimir("");
        imprimir("  ayuda");
        imprimir("      Muestra esta ayuda.");
        imprimir("");
        imprimir("Categorías: " + listarNombresCategorias());
    }

    private void imprimir(String texto) {
        areaOutput.appendText(texto + "\n");
    }

    private Gasto buscarGastoPorId(int id) {
        return controlador.getGastos().stream()
            .filter(g -> g.getID() == id)
            .findFirst().orElse(null);
    }

    private String listarNombresCategorias() {
        return String.join(", ", controlador.getCategorias().stream()
            .map(c -> c.getNombre()).toList());
    }

    /**
     * Tokeniza una cadena respetando textos entre comillas dobles.
     */
    private String[] tokenizar(String texto) {
        java.util.List<String> tokens = new java.util.ArrayList<>();
        java.util.regex.Matcher m = java.util.regex.Pattern
            .compile("\"([^\"]*)\"|([^\\s]+)")
            .matcher(texto);
        while (m.find())
            tokens.add(m.group(1) != null ? m.group(1) : m.group(2));
        return tokens.toArray(new String[0]);
    }
}