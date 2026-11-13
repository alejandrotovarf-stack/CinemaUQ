package com.cinemauq.patterns.structural;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

public class LibreriaPDFExterna {

    private static final int LINEAS_POR_PAGINA = 45;

    public void renderPDFStream(String path, String data) {
        try {
            byte[] pdfBytes = construirPDF(data);
            Files.createDirectories(Path.of(path).getParent());
            Files.write(Path.of(path), pdfBytes);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo generar el archivo PDF: " + e.getMessage(), e);
        }
    }

    public void renderPDFStream(String path, ReporteGrafico reporte) {
        try {
            byte[] pdfBytes = new DocumentoReporte(reporte).generar();
            Path destino = Path.of(path);
            if (destino.getParent() != null) Files.createDirectories(destino.getParent());
            Files.write(destino, pdfBytes);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo generar el archivo PDF: " + e.getMessage(), e);
        }
    }

    private byte[] construirPDF(String data) throws IOException {
        List<String> lineas = new ArrayList<>();
        for (String linea : data.split("\n", -1)) {
            lineas.add(escapar(linea));
        }
        List<List<String>> paginas = new ArrayList<>();
        for (int i = 0; i < lineas.size(); i += LINEAS_POR_PAGINA) {
            paginas.add(lineas.subList(i, Math.min(i + LINEAS_POR_PAGINA, lineas.size())));
        }
        if (paginas.isEmpty()) paginas.add(List.of(""));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        List<Integer> offsets = new ArrayList<>();
        StringBuilder header = new StringBuilder();
        header.append("%PDF-1.4\n");
        out.write(header.toString().getBytes(StandardCharsets.ISO_8859_1));

        int numPaginas = paginas.size();
        int objCount = 3 + numPaginas * 2;
        List<String> kids = new ArrayList<>();
        for (int i = 0; i < numPaginas; i++) kids.add((4 + i * 2) + " 0 R");

        offsets.add(out.size());
        out.write(("1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n").getBytes(StandardCharsets.ISO_8859_1));

        offsets.add(out.size());
        out.write(("2 0 obj\n<< /Type /Pages /Kids [" + String.join(" ", kids) + "] /Count " + numPaginas + " >>\nendobj\n")
                .getBytes(StandardCharsets.ISO_8859_1));

        offsets.add(out.size());
        out.write(("3 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Courier >>\nendobj\n")
                .getBytes(StandardCharsets.ISO_8859_1));

        int objNum = 4;
        for (List<String> pagina : paginas) {
            int pageObjNum = objNum;
            int contentObjNum = objNum + 1;

            StringBuilder content = new StringBuilder();
            content.append("BT /F1 10 Tf 40 780 Td 14 TL\n");
            for (String linea : pagina) {
                content.append("(").append(linea).append(") Tj T*\n");
            }
            content.append("ET");
            byte[] contentBytes = content.toString().getBytes(StandardCharsets.ISO_8859_1);

            offsets.add(out.size());
            out.write((pageObjNum + " 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] "
                    + "/Resources << /Font << /F1 3 0 R >> >> /Contents " + contentObjNum + " 0 R >>\nendobj\n")
                    .getBytes(StandardCharsets.ISO_8859_1));

            offsets.add(out.size());
            out.write((contentObjNum + " 0 obj\n<< /Length " + contentBytes.length + " >>\nstream\n")
                    .getBytes(StandardCharsets.ISO_8859_1));
            out.write(contentBytes);
            out.write("\nendstream\nendobj\n".getBytes(StandardCharsets.ISO_8859_1));

            objNum += 2;
        }

        int xrefOffset = out.size();
        StringBuilder xref = new StringBuilder();
        xref.append("xref\n0 ").append(objCount + 1).append("\n");
        xref.append("0000000000 65535 f \n");
        for (int off : offsets) {
            xref.append(String.format("%010d 00000 n \n", off));
        }
        xref.append("trailer\n<< /Size ").append(objCount + 1).append(" /Root 1 0 R >>\nstartxref\n")
                .append(xrefOffset).append("\n%%EOF");
        out.write(xref.toString().getBytes(StandardCharsets.ISO_8859_1));

        return out.toByteArray();
    }

    private String escapar(String texto) {
        return texto.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)");
    }
}
