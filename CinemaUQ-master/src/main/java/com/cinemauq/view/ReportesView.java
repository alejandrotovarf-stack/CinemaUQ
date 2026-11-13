package com.cinemauq.view;

import com.cinemauq.app.AppContext;
import com.cinemauq.service.ServicioReporte;
import com.cinemauq.view.util.UiKit;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReportesView extends VBox {

    private final AppContext ctx;
    private final BarChart<Number, String> chart;
    private final NumberAxis valorAxis = new NumberAxis();
    private final CategoryAxis categoriaAxis = new CategoryAxis();
    private final List<Text> etiquetasValor = new ArrayList<>();
    private final TextArea preview = new TextArea();
    private final Label chartCaption = new Label();

    private final ComboBox<ServicioReporte.TipoReporte> tipoCombo =
            new ComboBox<>(FXCollections.observableArrayList(ServicioReporte.TipoReporte.values()));
    private final DatePicker desdePicker = new DatePicker(LocalDate.now().minusDays(7));
    private final DatePicker hastaPicker = new DatePicker(LocalDate.now().plusDays(7));

    public ReportesView(AppContext ctx) {
        this(ctx, false);
    }

    public ReportesView(AppContext ctx, boolean embebido) {
        this.ctx = ctx;
        setSpacing(16);
        setPadding(embebido ? new Insets(4, 0, 16, 0) : new Insets(28, 32, 28, 32));
        getStyleClass().add("view-root");

        tipoCombo.setValue(ServicioReporte.TipoReporte.VENTAS);
        tipoCombo.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(ServicioReporte.TipoReporte tipo) {
                if (tipo == null) return "";
                return switch (tipo) {
                    case VENTAS -> "Ventas generales";
                    case OCUPACION -> "Ocupación de salas";
                    case CONFITERIA -> "Ventas de confitería";
                    case CANCELACIONES -> "Cancelaciones y reembolsos";
                    case PUNTOS -> "Puntos CinemaUQ";
                };
            }
            @Override
            public ServicioReporte.TipoReporte fromString(String s) { return null; }
        });
        tipoCombo.setPrefWidth(230);

        ToggleGroup formatoGroup = new ToggleGroup();
        RadioButton txtRb = new RadioButton("TXT");
        txtRb.setToggleGroup(formatoGroup);
        txtRb.setSelected(true);
        RadioButton pdfRb = new RadioButton("PDF");
        pdfRb.setToggleGroup(formatoGroup);

        Button generarBtn = UiKit.primario("Exportar archivo");

        FlowPane filtros = new FlowPane(16, 12);
        filtros.setAlignment(Pos.CENTER_LEFT);
        filtros.getChildren().addAll(
                campo("Tipo", tipoCombo),
                campo("Desde", desdePicker),
                campo("Hasta", hastaPicker),
                campo("Formato", new HBox(12, txtRb, pdfRb)),
                generarBtn);
        filtros.setPadding(new Insets(16, 20, 16, 20));
        filtros.getStyleClass().add("form-card");

        valorAxis.setLabel("");
        categoriaAxis.setLabel("");
        chart = new BarChart<>(valorAxis, categoriaAxis);
        chart.getStyleClass().add("grafico-horizontal");
        chart.setLegendVisible(false);
        chart.setPrefHeight(300);
        chart.setMinHeight(300);
        chart.setAnimated(false);
        chart.setCategoryGap(8);
        chart.setHorizontalGridLinesVisible(false);

        chartCaption.getStyleClass().add("card-meta");
        chartCaption.setWrapText(true);

        VBox chartCard = new VBox(8, chart, chartCaption);
        chartCard.setPadding(new Insets(16, 20, 16, 20));
        chartCard.getStyleClass().add("chart-card");

        preview.setEditable(false);
        preview.setFocusTraversable(false);
        preview.setWrapText(true);
        preview.setPrefRowCount(10);
        preview.getStyleClass().add("preview-area");
        VBox.setVgrow(preview, Priority.ALWAYS);

        tipoCombo.valueProperty().addListener((obs, old, nuevo) -> refrescarVistaPrevia());
        desdePicker.valueProperty().addListener((obs, old, nuevo) -> refrescarVistaPrevia());
        hastaPicker.valueProperty().addListener((obs, old, nuevo) -> refrescarVistaPrevia());

        generarBtn.setOnAction(e -> {
            LocalDate d = desdePicker.getValue();
            LocalDate h = hastaPicker.getValue();
            if (d == null || h == null || d.isAfter(h)) {
                ctx.mostrarError("Rango inválido", "Verifica el rango de fechas seleccionado.");
                return;
            }
            String ruta = ctx.getServicioReporte().generarYExportar(tipoCombo.getValue(), d, h, pdfRb.isSelected());
            ctx.mostrarInfo("Reporte generado", "Archivo exportado en:\n" + System.getProperty("user.dir") + "/" + ruta);
        });

        refrescarVistaPrevia();

        if (!embebido) {
            getChildren().add(UiKit.tituloBrutalista("Reportes y", "estadísticas"));
        }
        getChildren().addAll(filtros, chartCard, UiKit.seccion("Vista previa del contenido exportado"), preview);
    }

    private VBox campo(String etiqueta, javafx.scene.Node control) {
        Label l = new Label(etiqueta.toUpperCase());
        l.getStyleClass().add("field-label");
        VBox box = new VBox(4, l, control);
        box.setAlignment(Pos.BOTTOM_LEFT);
        return box;
    }

    public void refrescar() {
        refrescarVistaPrevia();
    }

    private void refrescarVistaPrevia() {
        LocalDate d = desdePicker.getValue();
        LocalDate h = hastaPicker.getValue();
        ServicioReporte.TipoReporte tipo = tipoCombo.getValue();
        if (d == null || h == null || tipo == null || d.isAfter(h)) return;

        actualizarGrafico(tipo, d, h);
        preview.setText(ctx.getServicioReporte().construirDatos(tipo, d, h));
    }

    private void actualizarGrafico(ServicioReporte.TipoReporte tipo, LocalDate desde, LocalDate hasta) {
        String tituloChart;
        String caption;
        String ejeValores;
        boolean monetario = false;

        switch (tipo) {
            case VENTAS -> {
                tituloChart = "Ventas confirmadas por día";
                caption = "Suma del total pagado en compras CONFIRMADAS, agrupado por día de compra dentro del rango.";
                ejeValores = "Ventas ($)";
                monetario = true;
            }
            case OCUPACION -> {
                tituloChart = "Ocupación por función (asientos vendidos)";
                caption = "Asientos vendidos en compras CONFIRMADAS por cada función con fecha dentro del rango. "
                        + "Junto a cada barra: vendidos de la capacidad de la sala y porcentaje de ocupación.";
                ejeValores = "Asientos vendidos";
            }
            case CONFITERIA -> {
                tituloChart = "Unidades vendidas por producto/combo";
                caption = "Cuenta de ítems de confitería incluidos en compras CONFIRMADAS dentro del rango de fechas.";
                ejeValores = "Unidades vendidas";
            }
            case CANCELACIONES -> {
                tituloChart = "Valor reembolsado por día";
                caption = "Suma de los reembolsos ejecutados (Strategy de cancelación) agrupados por día.";
                ejeValores = "Valor reembolsado ($)";
                monetario = true;
            }
            case PUNTOS -> {
                tituloChart = "Saldo de puntos vigente por cliente";
                caption = "Saldo actual (no vencido) de puntos CinemaUQ por cada cliente registrado.";
                ejeValores = "Puntos";
            }
            default -> {
                tituloChart = "";
                caption = "";
                ejeValores = "";
            }
        }

        List<ServicioReporte.PuntoGrafico> puntos = ctx.getServicioReporte().construirSerie(tipo, desde, hasta);

        limpiarEtiquetasValor();
        chart.getData().clear();

        List<String> categorias = new ArrayList<>();
        double maximo = 0;
        for (ServicioReporte.PuntoGrafico p : puntos) {
            categorias.add(p.etiqueta());
            maximo = Math.max(maximo, p.valor());
        }
        categoriaAxis.setAutoRanging(false);
        categoriaAxis.setCategories(FXCollections.observableArrayList(categorias));
        configurarEjeValores(maximo, monetario, ejeValores);

        double alto = Math.max(300, 100 + puntos.size() * 32);
        chart.setMinHeight(alto);
        chart.setPrefHeight(alto);

        XYChart.Series<Number, String> serie = new XYChart.Series<>();
        for (ServicioReporte.PuntoGrafico p : puntos) {
            XYChart.Data<Number, String> dato = new XYChart.Data<>(p.valor(), p.etiqueta());
            instalarEtiquetaValor(dato, p.textoValor());
            serie.getData().add(dato);
        }
        chart.setTitle(tituloChart);
        chart.getData().add(serie);
        chartCaption.setText(puntos.isEmpty() ? caption + " No hay datos para el periodo seleccionado." : caption);
    }

    private void configurarEjeValores(double maximo, boolean monetario, String titulo) {
        double paso = pasoAgradable(maximo);
        double limite = Math.max(paso, Math.ceil(maximo * 1.15 / paso) * paso);
        valorAxis.setAutoRanging(false);
        valorAxis.setLowerBound(0);
        valorAxis.setUpperBound(limite);
        valorAxis.setTickUnit(paso);
        valorAxis.setMinorTickVisible(false);
        valorAxis.setLabel(titulo);
        valorAxis.setTickLabelFormatter(new javafx.util.StringConverter<Number>() {
            @Override
            public String toString(Number valor) {
                return String.format(monetario ? "$%,.0f" : "%,.0f", valor.doubleValue());
            }
            @Override
            public Number fromString(String texto) { return null; }
        });
    }

    private static double pasoAgradable(double maximo) {
        if (maximo <= 0) return 1;
        double bruto = maximo * 1.15 / 5;
        double potencia = Math.pow(10, Math.floor(Math.log10(bruto)));
        double fraccion = bruto / potencia;
        double base = fraccion <= 1 ? 1 : fraccion <= 2 ? 2 : fraccion <= 5 ? 5 : 10;
        return Math.max(1, base * potencia);
    }

    private void instalarEtiquetaValor(XYChart.Data<Number, String> dato, String texto) {
        dato.nodeProperty().addListener((obs, anterior, barra) -> {
            if (barra == null) return;
            Text etiqueta = new Text(texto);
            etiqueta.setStyle("-fx-fill: white; -fx-font-size: 11px; -fx-font-weight: bold;");
            etiqueta.setTextOrigin(VPos.CENTER);
            etiquetasValor.add(etiqueta);
            Tooltip.install(barra, new Tooltip(dato.getYValue() + ": " + texto));
            barra.parentProperty().addListener((o, viejoPadre, padre) -> {
                if (padre instanceof Group grupo && !grupo.getChildren().contains(etiqueta)) {
                    grupo.getChildren().add(etiqueta);
                }
            });
            barra.boundsInParentProperty().addListener((o, viejos, limites) -> {
                etiqueta.setLayoutX(limites.getMaxX() + 6);
                etiqueta.setLayoutY(limites.getMinY() + limites.getHeight() / 2);
            });
        });
    }

    private void limpiarEtiquetasValor() {
        for (Text etiqueta : etiquetasValor) {
            if (etiqueta.getParent() instanceof Group grupo) {
                grupo.getChildren().remove(etiqueta);
            }
        }
        etiquetasValor.clear();
    }
}
