package es.um.tds.gestiongastos.modelo;

import java.time.LocalDate;

public interface EstrategiaAlerta {
	LocalDate calcularInicioPeriodo(LocalDate fechaActual);
}
