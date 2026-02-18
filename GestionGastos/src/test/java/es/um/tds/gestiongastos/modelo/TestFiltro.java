package es.um.tds.gestiongastos.modelo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para la clase Filtro.
 */
@DisplayName("Tests de Filtro")
class TestFiltro {
    
    private Categoria catOcio, catComida, catTransporte;
    private List<Gasto> expenses;
    
    @BeforeEach
    void setUp() {
        Categoria.resetContador();
        Gasto.resetContador();
        
        catOcio = new Categoria("Ocio", false);
        catComida = new Categoria("Comida", false);
        catTransporte = new Categoria("Transporte", false);
        
        expenses = Arrays.asList(
            new Gasto(50.0, LocalDateTime.of(2024, 1, 15, 10, 0), catOcio, "Enero Ocio"),
            new Gasto(30.0, LocalDateTime.of(2024, 1, 20, 10, 0), catComida, "Enero Comida"),
            new Gasto(40.0, LocalDateTime.of(2024, 6, 10, 10, 0), catOcio, "Junio Ocio"),
            new Gasto(60.0, LocalDateTime.of(2024, 6, 25, 10, 0), catComida, "Junio Comida"),
            new Gasto(20.0, LocalDateTime.of(2024, 12, 5, 10, 0), catTransporte, "Diciembre Transporte")
        );
    }
    
    @Test
    @DisplayName("Filtro vacío devuelve todos los gastos")
    void filtroVacioDevuelveTodos() {
        Filtro filter = new Filtro();
        
        assertFalse(filter.tieneFiltros());
        List<Gasto> result = filter.aplicarFiltros(expenses);
        assertEquals(5, result.size());
    }
    
    @Test
    @DisplayName("Filtrar por un mes")
    void filtrarPorUnMes() {
        Filtro filter = new Filtro();
        filter.addMes(Month.JANUARY);
        
        List<Gasto> result = filter.aplicarFiltros(expenses);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(g -> 
            g.getFecha().getMonth() == Month.JANUARY));
    }
    
    @Test
    @DisplayName("Filtrar por múltiples meses")
    void filtrarPorMultiplesMeses() {
        Filtro filter = new Filtro();
        filter.addMes(Month.JANUARY);
        filter.addMes(Month.JUNE);
        
        List<Gasto> result = filter.aplicarFiltros(expenses);
        assertEquals(4, result.size());
    }
    
    @Test
    @DisplayName("Filtrar por una categoría")
    void filtrarPorUnaCategoria() {
        Filtro filter = new Filtro();
        filter.addCategoria(catOcio);
        
        List<Gasto> result = filter.aplicarFiltros(expenses);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(g -> 
            g.getCategoria().equals(catOcio)));
    }
    
    @Test
    @DisplayName("Filtrar por múltiples categorías")
    void filtrarPorMultiplesCategorias() {
        Filtro filter = new Filtro();
        filter.addCategoria(catOcio);
        filter.addCategoria(catComida);
        
        List<Gasto> result = filter.aplicarFiltros(expenses);
        assertEquals(4, result.size());
    }
    
    @Test
    @DisplayName("Filtrar por fecha inicio")
    void filtrarPorFechaInicio() {
        Filtro filter = new Filtro();
        filter.setFechaInicio(LocalDate.of(2024, 6, 1));
        
        List<Gasto> result = filter.aplicarFiltros(expenses);
        assertEquals(3, result.size());
        assertTrue(result.stream().allMatch(g -> 
            !g.getFecha().toLocalDate().isBefore(LocalDate.of(2024, 6, 1))));
    }
    
    @Test
    @DisplayName("Filtrar por fecha fin")
    void filtrarPorFechaFin() {
        Filtro filter = new Filtro();
        filter.setFechaFin(LocalDate.of(2024, 6, 30));
        
        List<Gasto> result = filter.aplicarFiltros(expenses);
        assertEquals(4, result.size());
    }
    
    @Test
    @DisplayName("Filtrar por rango de fechas")
    void filtrarPorRangoFechas() {
        Filtro filter = new Filtro();
        filter.setRangoFechas(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 6, 30));
        
        List<Gasto> result = filter.aplicarFiltros(expenses);
        assertEquals(4, result.size());
    }
    
    @Test
    @DisplayName("Filtrar combinando mes y categoría")
    void filtrarCombinandoMesYCategoria() {
        Filtro filter = new Filtro();
        filter.addMes(Month.JUNE);
        filter.addCategoria(catComida);
        
        List<Gasto> result = filter.aplicarFiltros(expenses);
        assertEquals(1, result.size());
        assertEquals("Junio Comida", result.get(0).getNota().orElse(""));
    }
    
    @Test
    @DisplayName("Filtrar combinando todos los criterios")
    void filtrarCombinandoTodo() {
        Filtro filter = new Filtro();
        filter.addMes(Month.JANUARY);
        filter.addMes(Month.JUNE);
        filter.addCategoria(catOcio);
        filter.setFechaInicio(LocalDate.of(2024, 1, 1));
        filter.setFechaFin(LocalDate.of(2024, 6, 30));
        
        List<Gasto> result = filter.aplicarFiltros(expenses);
        assertEquals(2, result.size());  // Enero Ocio y Junio Ocio
    }
    
    @Test
    @DisplayName("Filtro sin resultados")
    void filtroSinResultados() {
        Filtro filter = new Filtro();
        filter.addMes(Month.MARCH);  // No hay gastos en marzo
        
        List<Gasto> result = filter.aplicarFiltros(expenses);
        assertTrue(result.isEmpty());
    }
    
    @Test
    @DisplayName("Limpiar filtro")
    void limpiarFiltro() {
        Filtro filter = new Filtro();
        filter.addMes(Month.JANUARY);
        filter.addCategoria(catOcio);
        filter.setFechaInicio(LocalDate.of(2024, 1, 1));
        
        assertTrue(filter.tieneFiltros());
        
        filter.limpiarFiltros();
        
        assertFalse(filter.tieneFiltros());
        assertEquals(5, filter.aplicarFiltros(expenses).size());
    }
    
    @Test
    @DisplayName("Aplicar filtro a lista vacía")
    void aplicarFiltroListaVacia() {
        Filtro filter = new Filtro();
        filter.addMes(Month.JANUARY);
        
        List<Gasto> result = filter.aplicarFiltros(Arrays.asList());
        assertTrue(result.isEmpty());
    }
    
    @Test
    @DisplayName("Aplicar filtro a lista null")
    void aplicarFiltroListaNull() {
        Filtro filter = new Filtro();
        filter.addMes(Month.JANUARY);
        
        List<Gasto> result = filter.aplicarFiltros(null);
        assertTrue(result.isEmpty());
    }
    
    @Test
    @DisplayName("No añadir mes duplicado")
    void noAddMesDuplicado() {
        Filtro filter = new Filtro();
        filter.addMes(Month.JANUARY);
        filter.addMes(Month.JANUARY);
        
        assertEquals(1, filter.getMeses().orElse(Arrays.asList()).size());
    }
    
    @Test
    @DisplayName("No añadir categoría duplicada")
    void noAddCategoriaDuplicada() {
        Filtro filter = new Filtro();
        filter.addCategoria(catOcio);
        filter.addCategoria(catOcio);
        
        assertEquals(1, filter.getCategorias().orElse(Arrays.asList()).size());
    }
    
    @Test
    @DisplayName("Getters devuelven Optional vacío si no hay filtros")
    void gettersOptionalVacio() {
        Filtro filter = new Filtro();
        
        assertTrue(filter.getMeses().isEmpty());
        assertTrue(filter.getFechaInicio().isEmpty());
        assertTrue(filter.getFechaFin().isEmpty());
        assertTrue(filter.getCategorias().isEmpty());
    }
    
    @Test
    @DisplayName("Fecha inicio incluida en el filtro")
    void fechaInicioIncluida() {
        Filtro filter = new Filtro();
        filter.setFechaInicio(LocalDate.of(2024, 1, 15));
        
        List<Gasto> result = filter.aplicarFiltros(expenses);
        // Debe incluir el gasto del 15 de enero
        assertTrue(result.stream().anyMatch(g -> 
            g.getNota().orElse("").equals("Enero Ocio")));
    }
    
    @Test
    @DisplayName("Fecha fin incluida en el filtro")
    void fechaFinIncluida() {
        Filtro filter = new Filtro();
        filter.setFechaFin(LocalDate.of(2024, 6, 25));
        
        List<Gasto> result = filter.aplicarFiltros(expenses);
        // Debe incluir el gasto del 25 de junio
        assertTrue(result.stream().anyMatch(g -> 
            g.getNota().orElse("").equals("Junio Comida")));
    }
    
    @Test
    @DisplayName("Constructor con todos los parámetros")
    void constructorCompleto() {
        List<Month> months = Arrays.asList(Month.JANUARY, Month.JUNE);
        List<Categoria> categories = Arrays.asList(catOcio);
        LocalDate start = LocalDate.of(2024, 1, 1);
        LocalDate end = LocalDate.of(2024, 12, 31);
        
        Filtro filter = new Filtro(months, start, end, categories);
        
        assertTrue(filter.tieneFiltros());
        assertEquals(2, filter.getMeses().orElse(Arrays.asList()).size());
        assertEquals(1, filter.getCategorias().orElse(Arrays.asList()).size());
    }
}