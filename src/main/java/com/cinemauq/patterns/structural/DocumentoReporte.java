package com.cinemauq.patterns.structural;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

final class DocumentoReporte {

    private static final double ANCHO = 612;
    private static final double ALTO = 792;
    private static final double MARGEN = 40;
    private static final double MARGEN_SUPERIOR = 56;
    private static final double MARGEN_INFERIOR = 56;
    private static final double ANCHO_UTIL = ANCHO - 2 * MARGEN;

    private static final double[] ROJO = {0.624, 0.129, 0.133};
    private static final double[] NEGRO = {0.11, 0.11, 0.12};
    private static final double[] GRIS = {0.42, 0.42, 0.45};
    private static final double[] GRIS_CLARO = {0.90, 0.90, 0.92};
    private static final double[] GRIS_FONDO = {0.96, 0.96, 0.97};
    private static final double[] BLANCO = {1, 1, 1};

    private static final String F_NORMAL = "F1";
    private static final String F_NEGRITA = "F2";
    private static final String F_MONO = "F3";

    private static final Charset WIN_1252 = Charset.forName("windows-1252");

    private static final int[] ANCHOS_HELVETICA = {
            278, 278, 355, 556, 556, 889, 667, 191, 333, 333, 389, 584, 278, 333, 278, 278,
            556, 556, 556, 556, 556, 556, 556, 556, 556, 556, 278, 278, 584, 584, 584, 556,
            1015, 667, 667, 722, 722, 667, 611, 778, 722, 278, 500, 667, 556, 833, 722, 778,
            667, 778, 722, 667, 611, 722, 667, 944, 667, 667, 611, 278, 278, 278, 469, 556,
            333, 556, 556, 500, 556, 556, 278, 556, 556, 222, 222, 500, 222, 833, 556, 556,
            556, 556, 333, 500, 278, 556, 500, 722, 500, 500, 500, 334, 260, 334, 584
    };

    private final ReporteGrafico reporte;
    private final List<StringBuilder> paginas = new ArrayList<>();
    private StringBuilder pagina;
    private double y;

    DocumentoReporte(ReporteGrafico reporte) {
        this.reporte = reporte;
    }

    byte[] generar() throws IOException {
        nuevaPagina(true);
        dibujarIndicadores();
        if (reporte.puntos().isEmpty()) {
            dibujarSinDatos();
        } else {
            dibujarGrafico();
            dibujarTabla();
        }
        dibujarDetalle();
        agregarPies();
        return ensamblar();
    }

    private void nuevaPagina(boolean primera) {
        pagina = new StringBuilder();
        paginas.add(pagina);
        if (primera) {
            rect(0, 0, ANCHO, 74, ROJO);
            texto(MARGEN, 32, F_NEGRITA, 20, "CINEMAUQ", BLANCO);
            texto(MARGEN, 54, F_NORMAL, 12, "Reporte: " + reporte.titulo(), BLANCO);
            y = 96;
            texto(MARGEN, y, F_NORMAL, 9, "Periodo consultado: " + reporte.periodo(), GRIS);
            texto(ANCHO - MARGEN - ancho("Generado: " + reporte.generado(), F_NORMAL, 9), y, F_NORMAL, 9,
                    "Generado: " + reporte.generado(), GRIS);
            y += 22;
        } else {
            texto(MARGEN, 34, F_NEGRITA, 9, "CinemaUQ  ·  " + reporte.titulo() + "  (continuación)", GRIS);
            linea(MARGEN, 42, ANCHO - MARGEN, 42, 0.5, GRIS_CLARO);
            y = MARGEN_SUPERIOR;
        }
    }

    private void asegurar(double alto) {
        if (y + alto > ALTO - MARGEN_INFERIOR) nuevaPagina(false);
    }

    private void titulo(String texto) {
        asegurar(40);
        texto(MARGEN, y + 11, F_NEGRITA, 12, texto, NEGRO);
        rect(MARGEN, y + 16, 36, 2, ROJO);
        y += 28;
    }

    private void dibujarIndicadores() {
        List<ReporteGrafico.Indicador> lista = reporte.indicadores();
        if (lista.isEmpty()) return;
        titulo("Indicadores");
        int porFila = Math.min(4, lista.size());
        double gap = 10;
        double anchoCaja = (ANCHO_UTIL - gap * (porFila - 1)) / porFila;
        for (int i = 0; i < lista.size(); i += porFila) {
            asegurar(52);
            for (int k = 0; k < porFila && i + k < lista.size(); k++) {
                ReporteGrafico.Indicador ind = lista.get(i + k);
                double x = MARGEN + k * (anchoCaja + gap);
                rect(x, y, anchoCaja, 44, GRIS_FONDO);
                rect(x, y, 3, 44, ROJO);
                List<String> nombre = envolver(ind.nombre(), F_NORMAL, 8, anchoCaja - 14);
                texto(x + 10, y + 13, F_NORMAL, 8, nombre.get(0), GRIS);
                double tam = 14;
                while (tam > 8 && ancho(ind.valor(), F_NEGRITA, tam) > anchoCaja - 14) tam -= 1;
                texto(x + 10, y + 34, F_NEGRITA, tam, ind.valor(), NEGRO);
            }
            y += 54;
        }
        y += 6;
    }

    private void dibujarSinDatos() {
        titulo(reporte.tituloGrafico());
        asegurar(40);
        rect(MARGEN, y, ANCHO_UTIL, 34, GRIS_FONDO);
        texto(MARGEN + 12, y + 21, F_NORMAL, 10,
                "No hay datos para graficar en el periodo seleccionado.", GRIS);
        y += 50;
    }

    private static double pasoAgradable(double maximo) {
        if (maximo <= 0) return 1;
        double bruto = maximo * 1.15 / 5;
        double potencia = Math.pow(10, Math.floor(Math.log10(bruto)));
        double fraccion = bruto / potencia;
        double base = fraccion <= 1 ? 1 : fraccion <= 2 ? 2 : fraccion <= 5 ? 5 : 10;
        return Math.max(1, base * potencia);
    }

    private String formatoValor(double v) {
        return String.format(reporte.monetario() ? "$%,.0f" : "%,.0f", v);
    }

    private void dibujarGrafico() {
        titulo(reporte.tituloGrafico());

        List<ReporteGrafico.Punto> puntos = reporte.puntos();
        double maximo = 0;
        for (ReporteGrafico.Punto p : puntos) maximo = Math.max(maximo, p.valor());
        double paso = pasoAgradable(maximo);
        double limite = Math.max(paso, Math.ceil(maximo * 1.15 / paso) * paso);

        double xEtiquetas = MARGEN;
        double anchoEtiquetas = 190;
        double plotIzq = MARGEN + 200;
        double plotDer = ANCHO - MARGEN - 102;
        double plotAncho = plotDer - plotIzq;

        double[] altos = new double[puntos.size()];
        List<List<String>> lineasEtiqueta = new ArrayList<>();
        for (int i = 0; i < puntos.size(); i++) {
            List<String> lineas = envolver(puntos.get(i).etiqueta(), F_NORMAL, 8, anchoEtiquetas);
            lineasEtiqueta.add(lineas);
            altos[i] = Math.max(22, lineas.size() * 10 + 10);
        }

        int i = 0;
        while (i < puntos.size()) {
            asegurar(18 + altos[i] + 22);
            double plotArriba = y + 18;
            double limiteY = ALTO - MARGEN_INFERIOR - 22;
            double yy = plotArriba;
            int j = i;
            while (j < puntos.size() && yy + altos[j] <= limiteY) {
                yy += altos[j];
                j++;
            }
            if (j == i) {
                yy += altos[i];
                j = i + 1;
            }
            double plotAbajo = yy;

            rect(plotIzq, plotArriba, plotAncho, plotAbajo - plotArriba, GRIS_FONDO);
            int ticks = (int) Math.round(limite / paso);
            for (int t = 0; t <= ticks; t++) {
                double valor = t * paso;
                double x = plotIzq + plotAncho * (valor / limite);
                linea(x, plotArriba, x, plotAbajo, t == 0 ? 0.9 : 0.4, t == 0 ? GRIS : GRIS_CLARO);
                String et = formatoValor(valor);
                texto(x - ancho(et, F_NORMAL, 7) / 2, plotArriba - 5, F_NORMAL, 7, et, GRIS);
            }

            double cursor = plotArriba;
            for (int k = i; k < j; k++) {
                ReporteGrafico.Punto p = puntos.get(k);
                double centro = cursor + altos[k] / 2;
                List<String> lineas = lineasEtiqueta.get(k);
                double yTexto = centro - (lineas.size() * 10) / 2.0 + 8;
                for (String l : lineas) {
                    texto(xEtiquetas, yTexto, F_NORMAL, 8, l, NEGRO);
                    yTexto += 10;
                }
                double largo = limite > 0 ? plotAncho * (p.valor() / limite) : 0;
                if (p.valor() > 0) largo = Math.max(largo, 1.5);
                if (largo > 0) rect(plotIzq, centro - 7, largo, 14, ROJO);
                texto(plotIzq + largo + 4, centro + 3, F_NEGRITA, 8, p.textoValor(), NEGRO);
                cursor += altos[k];
            }

            String eje = reporte.ejeValores();
            texto(plotIzq + plotAncho / 2 - ancho(eje, F_NEGRITA, 8) / 2, plotAbajo + 14, F_NEGRITA, 8, eje, GRIS);
            y = plotAbajo + 26;
            i = j;
            if (i < puntos.size()) nuevaPagina(false);
        }
    }

    private void dibujarTabla() {
        titulo("Datos del gráfico");
        double xNum = MARGEN;
        double xCat = MARGEN + 30;
        double anchoCat = 268;
        double xValDer = MARGEN + 30 + anchoCat + 80;
        double xDet = xValDer + 12;
        double anchoDet = ANCHO - MARGEN - xDet;

        encabezadoTabla(xNum, xCat, xValDer, xDet);
        List<ReporteGrafico.Punto> puntos = reporte.puntos();
        double total = 0;
        for (int i = 0; i < puntos.size(); i++) {
            ReporteGrafico.Punto p = puntos.get(i);
            List<String> cat = envolver(p.etiqueta(), F_NORMAL, 8, anchoCat);
            List<String> det = envolver(p.textoValor(), F_NORMAL, 8, anchoDet);
            double alto = Math.max(cat.size(), det.size()) * 10 + 8;
            if (y + alto > ALTO - MARGEN_INFERIOR) {
                nuevaPagina(false);
                encabezadoTabla(xNum, xCat, xValDer, xDet);
            }
            if (i % 2 == 1) rect(MARGEN, y, ANCHO_UTIL, alto, GRIS_FONDO);
            texto(xNum + 4, y + 12, F_NORMAL, 8, String.valueOf(i + 1), GRIS);
            double yc = y + 12;
            for (String l : cat) { texto(xCat, yc, F_NORMAL, 8, l, NEGRO); yc += 10; }
            String val = formatoValor(p.valor());
            texto(xValDer - ancho(val, F_NORMAL, 8), y + 12, F_NORMAL, 8, val, NEGRO);
            double yd = y + 12;
            for (String l : det) { texto(xDet, yd, F_NORMAL, 8, l, NEGRO); yd += 10; }
            y += alto;
            total += p.valor();
        }
        if (y + 22 > ALTO - MARGEN_INFERIOR) {
            nuevaPagina(false);
            encabezadoTabla(xNum, xCat, xValDer, xDet);
        }
        linea(MARGEN, y, ANCHO - MARGEN, y, 0.8, GRIS);
        texto(xCat, y + 14, F_NEGRITA, 8.5, "TOTAL", NEGRO);
        String tot = formatoValor(total);
        texto(xValDer - ancho(tot, F_NEGRITA, 8.5), y + 14, F_NEGRITA, 8.5, tot, NEGRO);
        y += 30;
    }

    private void encabezadoTabla(double xNum, double xCat, double xValDer, double xDet) {
        rect(MARGEN, y, ANCHO_UTIL, 18, ROJO);
        texto(xNum + 4, y + 12, F_NEGRITA, 8, "#", BLANCO);
        texto(xCat, y + 12, F_NEGRITA, 8, "Categoría", BLANCO);
        texto(xValDer - ancho("Valor", F_NEGRITA, 8), y + 12, F_NEGRITA, 8, "Valor", BLANCO);
        texto(xDet, y + 12, F_NEGRITA, 8, "Detalle", BLANCO);
        y += 18;
    }

    private void dibujarDetalle() {
        String detalle = reporte.detalle() == null ? "" : reporte.detalle().replace("\r\n", "\n");
        if (detalle.isBlank()) return;
        titulo("Detalle del reporte");
        int maxCaracteres = (int) (ANCHO_UTIL / (0.6 * 8)) - 1;
        for (String linea : detalle.split("\n", -1)) {
            List<String> partes = partir(linea, maxCaracteres);
            for (String parte : partes) {
                asegurar(11);
                texto(MARGEN, y + 8, F_MONO, 8, parte, NEGRO);
                y += 10.5;
            }
        }
    }

    private List<String> partir(String linea, int max) {
        List<String> res = new ArrayList<>();
        if (linea.length() <= max) {
            res.add(linea);
            return res;
        }
        String resto = linea;
        String sangria = "";
        while (resto.length() + sangria.length() > max) {
            int corte = max - sangria.length();
            int espacio = resto.lastIndexOf(' ', corte);
            if (espacio < corte / 2) espacio = corte;
            res.add(sangria + resto.substring(0, espacio).stripTrailing());
            resto = resto.substring(espacio).stripLeading();
            sangria = "    ";
            if (resto.isEmpty()) return res;
        }
        res.add(sangria + resto);
        return res;
    }

    private void agregarPies() {
        int total = paginas.size();
        for (int i = 0; i < total; i++) {
            StringBuilder previo = pagina;
            pagina = paginas.get(i);
            linea(MARGEN, ALTO - 40, ANCHO - MARGEN, ALTO - 40, 0.5, GRIS_CLARO);
            texto(MARGEN, ALTO - 28, F_NORMAL, 8, "CinemaUQ — Sistema Integral de Gestión de Cine", GRIS);
            String num = "Página " + (i + 1) + " de " + total;
            texto(ANCHO - MARGEN - ancho(num, F_NORMAL, 8), ALTO - 28, F_NORMAL, 8, num, GRIS);
            pagina = previo;
        }
    }

    private byte[] ensamblar() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        List<Integer> offsets = new ArrayList<>();
        out.write("%PDF-1.4\n".getBytes(StandardCharsets.ISO_8859_1));

        int numPaginas = paginas.size();
        int totalObjetos = 5 + numPaginas * 2;
        List<String> kids = new ArrayList<>();
        for (int i = 0; i < numPaginas; i++) kids.add((6 + i * 2) + " 0 R");

        escribir(out, offsets, "1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n");
        escribir(out, offsets, "2 0 obj\n<< /Type /Pages /Kids [" + String.join(" ", kids)
                + "] /Count " + numPaginas + " >>\nendobj\n");
        escribir(out, offsets, "3 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica "
                + "/Encoding /WinAnsiEncoding >>\nendobj\n");
        escribir(out, offsets, "4 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold "
                + "/Encoding /WinAnsiEncoding >>\nendobj\n");
        escribir(out, offsets, "5 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Courier "
                + "/Encoding /WinAnsiEncoding >>\nendobj\n");

        for (int i = 0; i < numPaginas; i++) {
            int objPagina = 6 + i * 2;
            int objContenido = objPagina + 1;
            byte[] contenido = paginas.get(i).toString().getBytes(WIN_1252);

            escribir(out, offsets, objPagina + " 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] "
                    + "/Resources << /Font << /F1 3 0 R /F2 4 0 R /F3 5 0 R >> >> /Contents "
                    + objContenido + " 0 R >>\nendobj\n");

            offsets.add(out.size());
            out.write((objContenido + " 0 obj\n<< /Length " + contenido.length + " >>\nstream\n")
                    .getBytes(StandardCharsets.ISO_8859_1));
            out.write(contenido);
            out.write("\nendstream\nendobj\n".getBytes(StandardCharsets.ISO_8859_1));
        }

        int xrefOffset = out.size();
        StringBuilder xref = new StringBuilder();
        xref.append("xref\n0 ").append(totalObjetos + 1).append("\n");
        xref.append("0000000000 65535 f \n");
        for (int off : offsets) xref.append(String.format(Locale.ROOT, "%010d 00000 n \n", off));
        xref.append("trailer\n<< /Size ").append(totalObjetos + 1).append(" /Root 1 0 R >>\nstartxref\n")
                .append(xrefOffset).append("\n%%EOF");
        out.write(xref.toString().getBytes(StandardCharsets.ISO_8859_1));
        return out.toByteArray();
    }

    private void escribir(ByteArrayOutputStream out, List<Integer> offsets, String objeto) throws IOException {
        offsets.add(out.size());
        out.write(objeto.getBytes(StandardCharsets.ISO_8859_1));
    }

    private static String num(double v) {
        return String.format(Locale.ROOT, "%.2f", v);
    }

    private static String color(double[] c) {
        return num(c[0]) + " " + num(c[1]) + " " + num(c[2]);
    }

    private void rect(double x, double yTop, double w, double h, double[] relleno) {
        pagina.append(color(relleno)).append(" rg ")
                .append(num(x)).append(' ').append(num(ALTO - yTop - h)).append(' ')
                .append(num(w)).append(' ').append(num(h)).append(" re f\n");
    }

    private void linea(double x1, double y1Top, double x2, double y2Top, double grosor, double[] c) {
        pagina.append(color(c)).append(" RG ").append(num(grosor)).append(" w ")
                .append(num(x1)).append(' ').append(num(ALTO - y1Top)).append(" m ")
                .append(num(x2)).append(' ').append(num(ALTO - y2Top)).append(" l S\n");
    }

    private void texto(double x, double yBase, String fuente, double tam, String texto, double[] c) {
        pagina.append("BT /").append(fuente).append(' ').append(num(tam)).append(" Tf ")
                .append(color(c)).append(" rg ")
                .append(num(x)).append(' ').append(num(ALTO - yBase)).append(" Td (")
                .append(escapar(texto)).append(") Tj ET\n");
    }

    private static String escapar(String t) {
        StringBuilder sb = new StringBuilder();
        for (char ch : t.toCharArray()) {
            if (ch == '\\' || ch == '(' || ch == ')') sb.append('\\');
            if (ch == '\n' || ch == '\r' || ch == '\t') { sb.append(' '); continue; }
            sb.append(ch);
        }
        return sb.toString();
    }

    private static double ancho(String t, String fuente, double tam) {
        if (F_MONO.equals(fuente)) return t.length() * 0.6 * tam;
        double total = 0;
        for (char ch : t.toCharArray()) total += anchoCaracter(ch);
        if (F_NEGRITA.equals(fuente)) total *= 1.07;
        return total * tam / 1000.0;
    }

    private static int anchoCaracter(char ch) {
        if (ch >= 32 && ch <= 126) return ANCHOS_HELVETICA[ch - 32];
        String base = Normalizer.normalize(String.valueOf(ch), Normalizer.Form.NFD);
        char b = base.charAt(0);
        if (b != ch && b >= 32 && b <= 126) return ANCHOS_HELVETICA[b - 32];
        return switch (ch) {
            case '·' -> 278;
            case '…', '—' -> 1000;
            case '–' -> 556;
            default -> 556;
        };
    }

    private static List<String> envolver(String t, String fuente, double tam, double maxAncho) {
        List<String> lineas = new ArrayList<>();
        String actual = "";
        for (String palabra : t.split(" ")) {
            String intento = actual.isEmpty() ? palabra : actual + " " + palabra;
            if (ancho(intento, fuente, tam) <= maxAncho) {
                actual = intento;
                continue;
            }
            if (!actual.isEmpty()) lineas.add(actual);
            actual = palabra;
            while (ancho(actual, fuente, tam) > maxAncho && actual.length() > 1) {
                int corte = actual.length() - 1;
                while (corte > 1 && ancho(actual.substring(0, corte), fuente, tam) > maxAncho) corte--;
                lineas.add(actual.substring(0, corte));
                actual = actual.substring(corte);
            }
        }
        lineas.add(actual);
        return lineas;
    }
}
