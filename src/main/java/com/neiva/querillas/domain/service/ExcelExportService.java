package com.neiva.querillas.domain.service;

import com.neiva.querillas.domain.entity.Querella;
import com.neiva.querillas.domain.repo.QuerellaRepository;
import com.neiva.querillas.web.dto.QuerellaReporteExcelDTO;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExcelExportService {

    private final EntityManager entityManager;
    private final QuerellaRepository querellaRepo;

    /**
     * Generar reporte trimestral en formato Excel
     */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('DIRECTORA','AUXILIAR')")
    public byte[] generarReporteTrimestraExcel(
            LocalDate desde,
            LocalDate hasta,
            Long inspectorId
    ) throws IOException {
        log.info("Generando reporte trimestral Excel - desde: {}, hasta: {}, inspectorId: {}", desde, hasta, inspectorId);

        // Obtener datos
        List<QuerellaReporteExcelDTO> datos = obtenerDatosReporte(desde, hasta, inspectorId);

        // Crear workbook de Excel
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Reporte Trimestral");

            // Crear estilos
            CellStyle headerStyle = crearEstiloEncabezado(workbook);
            CellStyle dateStyle = crearEstiloFecha(workbook);
            CellStyle normalStyle = crearEstiloNormal(workbook);

            // Crear encabezados
            crearEncabezados(sheet, headerStyle);

            // Llenar datos
            llenarDatos(sheet, datos, dateStyle, normalStyle);

            // Ajustar anchos de columna
            ajustarAnchoColumnas(sheet);

            // Escribir a byte array
            workbook.write(out);
            byte[] bytes = out.toByteArray();

            log.info("Reporte Excel generado exitosamente - {} filas", datos.size());
            return bytes;
        }
    }

    /**
     * Obtener datos del reporte desde la base de datos
     */
    private List<QuerellaReporteExcelDTO> obtenerDatosReporte(
            LocalDate desde,
            LocalDate hasta,
            Long inspectorId
    ) {
        OffsetDateTime desdeDateTime = desde.atStartOfDay().atOffset(ZoneOffset.UTC);
        OffsetDateTime hastaDateTime = hasta.plusDays(1).atStartOfDay().atOffset(ZoneOffset.UTC);

        String jpql = """
            SELECT q
            FROM Querella q
            LEFT JOIN FETCH q.inspectorAsignado i
            LEFT JOIN FETCH q.tema t
            LEFT JOIN FETCH q.comuna c
            WHERE q.creadoEn >= :desde
              AND q.creadoEn < :hasta
              AND (:inspectorId IS NULL OR q.inspectorAsignado.id = :inspectorId)
            ORDER BY q.creadoEn ASC
            """;

        var query = entityManager.createQuery(jpql, Querella.class);
        query.setParameter("desde", desdeDateTime);
        query.setParameter("hasta", hastaDateTime);
        query.setParameter("inspectorId", inspectorId);

        List<Querella> querellas = query.getResultList();

        // Convertir a DTO
        return querellas.stream()
                .map(this::convertirAExcelDTO)
                .toList();
    }

    /**
     * Convertir Querella a DTO de Excel
     */
    private QuerellaReporteExcelDTO convertirAExcelDTO(Querella q) {
        // Obtener estado actual
        String estadoActual = obtenerEstadoActual(q.getId());

        return QuerellaReporteExcelDTO.builder()
                .fechaRadicado(q.getCreadoEn())
                .inspectorNombre(q.getInspectorAsignado() != null ? q.getInspectorAsignado().getNombre() : "")
                .inspectorZona(q.getInspectorAsignado() != null && q.getInspectorAsignado().getZona() != null ?
                        q.getInspectorAsignado().getZona().name() : "")
                .radicadoInterno(q.getRadicadoInterno())
                .idLocal(q.getIdLocal() != null ? q.getIdLocal() : "")
                .temaNombre(q.getTema() != null ? q.getTema().getNombre() : "")
                .descripcion(q.getDescripcion() != null ? q.getDescripcion() : "")
                .generoQuerellante(q.getGeneroQuerellante() != null ? q.getGeneroQuerellante() : "")
                .generoQuerellado(q.getGeneroQuerellado() != null ? q.getGeneroQuerellado() : "")
                .normasAplicables(q.getNormasAplicables() != null ? q.getNormasAplicables() : "")
                .barrio(q.getBarrio() != null ? q.getBarrio() : "")
                .comunaNombre(q.getComuna() != null ? q.getComuna().getNombre() : "")
                .estadoActual(estadoActual != null ? estadoActual : "")
                .materializacionMedida(q.getMaterializacionMedida())
                .observaciones(q.getObservaciones() != null ? q.getObservaciones() : "")
                .tieneFallo(q.getTieneFallo())
                .tieneApelacion(q.getTieneApelacion())
                .archivado(q.getArchivado())
                .build();
    }

    /**
     * Obtener estado actual de una querella
     */
    private String obtenerEstadoActual(Long querellaId) {
        String sql = """
            SELECT e.nombre
            FROM historial_estado he
            JOIN estado e ON e.id = he.estado_id
            WHERE he.modulo = 'QUERELLA'
              AND he.caso_id = :querellaId
            ORDER BY he.creado_en DESC
            LIMIT 1
            """;

        try {
            return (String) entityManager.createNativeQuery(sql)
                    .setParameter("querellaId", querellaId)
                    .getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Crear encabezados del Excel
     */
    private void crearEncabezados(Sheet sheet, CellStyle headerStyle) {
        Row headerRow = sheet.createRow(0);

        String[] headers = {
                "Fecha Radicado",
                "Inspector Asignado",
                "Zona",
                "Radicado Interno",
                "ID Local",
                "Tema",
                "Descripción",
                "Género Querellante",
                "Género Querellado",
                "Normas Aplicables",
                "Barrio",
                "Comuna",
                "Estado",
                "Materialización Medida",
                "Observaciones",
                "Tiene Fallo",
                "Tiene Apelación",
                "Archivado"
        };

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
    }

    /**
     * Llenar datos en el Excel
     */
    private void llenarDatos(Sheet sheet, List<QuerellaReporteExcelDTO> datos, CellStyle dateStyle, CellStyle normalStyle) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        int rowNum = 1;
        for (QuerellaReporteExcelDTO dato : datos) {
            Row row = sheet.createRow(rowNum++);

            // Fecha Radicado
            Cell cell0 = row.createCell(0);
            if (dato.getFechaRadicado() != null) {
                cell0.setCellValue(dato.getFechaRadicado().format(dateFormatter));
            }
            cell0.setCellStyle(normalStyle);

            // Inspector Asignado
            Cell cell1 = row.createCell(1);
            cell1.setCellValue(dato.getInspectorNombre());
            cell1.setCellStyle(normalStyle);

            // Zona
            Cell cell2 = row.createCell(2);
            cell2.setCellValue(dato.getInspectorZona());
            cell2.setCellStyle(normalStyle);

            // Radicado Interno
            Cell cell3 = row.createCell(3);
            cell3.setCellValue(dato.getRadicadoInterno());
            cell3.setCellStyle(normalStyle);

            // ID Local
            Cell cell4 = row.createCell(4);
            cell4.setCellValue(dato.getIdLocal());
            cell4.setCellStyle(normalStyle);

            // Tema
            Cell cell5 = row.createCell(5);
            cell5.setCellValue(dato.getTemaNombre());
            cell5.setCellStyle(normalStyle);

            // Descripción
            Cell cell6 = row.createCell(6);
            cell6.setCellValue(dato.getDescripcion());
            cell6.setCellStyle(normalStyle);

            // Género Querellante
            Cell cell7 = row.createCell(7);
            cell7.setCellValue(dato.getGeneroQuerellante());
            cell7.setCellStyle(normalStyle);

            // Género Querellado
            Cell cell8 = row.createCell(8);
            cell8.setCellValue(dato.getGeneroQuerellado());
            cell8.setCellStyle(normalStyle);

            // Normas Aplicables
            Cell cell9 = row.createCell(9);
            cell9.setCellValue(dato.getNormasAplicables());
            cell9.setCellStyle(normalStyle);

            // Barrio
            Cell cell10 = row.createCell(10);
            cell10.setCellValue(dato.getBarrio());
            cell10.setCellStyle(normalStyle);

            // Comuna
            Cell cell11 = row.createCell(11);
            cell11.setCellValue(dato.getComunaNombre());
            cell11.setCellStyle(normalStyle);

            // Estado
            Cell cell12 = row.createCell(12);
            cell12.setCellValue(dato.getEstadoActual());
            cell12.setCellStyle(normalStyle);

            // Materialización Medida
            Cell cell13 = row.createCell(13);
            cell13.setCellValue(booleanToString(dato.getMaterializacionMedida()));
            cell13.setCellStyle(normalStyle);

            // Observaciones
            Cell cell14 = row.createCell(14);
            cell14.setCellValue(dato.getObservaciones());
            cell14.setCellStyle(normalStyle);

            // Tiene Fallo
            Cell cell15 = row.createCell(15);
            cell15.setCellValue(booleanToString(dato.getTieneFallo()));
            cell15.setCellStyle(normalStyle);

            // Tiene Apelación
            Cell cell16 = row.createCell(16);
            cell16.setCellValue(booleanToString(dato.getTieneApelacion()));
            cell16.setCellStyle(normalStyle);

            // Archivado
            Cell cell17 = row.createCell(17);
            cell17.setCellValue(booleanToString(dato.getArchivado()));
            cell17.setCellStyle(normalStyle);
        }
    }

    /**
     * Convertir Boolean a String para Excel
     */
    private String booleanToString(Boolean value) {
        if (value == null) return "";
        return value ? "Sí" : "No";
    }

    /**
     * Ajustar ancho de columnas
     */
    private void ajustarAnchoColumnas(Sheet sheet) {
        for (int i = 0; i < 18; i++) {
            sheet.autoSizeColumn(i);
            // Agregar un poco más de espacio
            sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 1000);
        }
    }

    /**
     * Crear estilo para encabezados
     */
    private CellStyle crearEstiloEncabezado(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setWrapText(true);
        return style;
    }

    /**
     * Crear estilo para fechas
     */
    private CellStyle crearEstiloFecha(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat(workbook.getCreationHelper().createDataFormat().getFormat("dd/MM/yyyy HH:mm"));
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    /**
     * Crear estilo para celdas normales
     */
    private CellStyle crearEstiloNormal(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setVerticalAlignment(VerticalAlignment.TOP);
        style.setWrapText(true);
        return style;
    }
}
