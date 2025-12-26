package com.neiva.querillas.domain.service;

import com.neiva.querillas.domain.entity.DespachoComisorio;
import com.neiva.querillas.domain.repo.DespachoComitorioRepository;
import com.neiva.querillas.web.dto.DespachoComitorioReporteDTO;
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
public class DespachoExcelService {

    private final DespachoComitorioRepository despachoRepo;

    /**
     * Generar reporte de despachos en Excel (Formato FOR-GGOJ-81)
     */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('DIRECTOR','AUXILIAR')")
    public byte[] generarReporteExcel(LocalDate desde, LocalDate hasta) throws IOException {
        log.info("Generando reporte Excel de despachos - desde: {}, hasta: {}", desde, hasta);

        OffsetDateTime desdeDateTime = desde.atStartOfDay().atOffset(ZoneOffset.UTC);
        OffsetDateTime hastaDateTime = hasta.plusDays(1).atStartOfDay().atOffset(ZoneOffset.UTC);

        // Obtener datos
        List<DespachoComisorio> despachos = despachoRepo.findByFechaRecibidoBetween(desdeDateTime, hastaDateTime);

        // Crear workbook
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Despachos Comisorios");

            // Estilos
            CellStyle headerStyle = crearEstiloEncabezado(workbook);
            CellStyle normalStyle = crearEstiloNormal(workbook);
            CellStyle dateStyle = crearEstiloFecha(workbook);

            // Crear encabezados
            crearEncabezados(sheet, headerStyle);

            // Llenar datos
            llenarDatos(sheet, despachos, normalStyle, dateStyle);

            // Ajustar anchos
            ajustarAnchoColumnas(sheet);

            // Escribir a byte array
            workbook.write(out);
            byte[] bytes = out.toByteArray();

            log.info("Reporte Excel de despachos generado - {} registros", despachos.size());
            return bytes;
        }
    }

    /**
     * Crear encabezados según formato FOR-GGOJ-81
     */
    private void crearEncabezados(Sheet sheet, CellStyle headerStyle) {
        Row headerRow = sheet.createRow(0);

        String[] headers = {
                "ITEM",
                "FECHA DE RECIBIDO",
                "RADICADO DEL PROCESO",
                "N° DESPACHO COMISORIO",
                "ENTIDAD PROCEDENTE",
                "ASUNTO",
                "DEMANDANTE Y/O APODERADO",
                "DEMANDADO Y/O APODERADO",
                "INSPECCIÓN DE POLICÍA O CORREGIMIENTO ASIGNADO",
                "FECHA DE DEVOLUCIÓN AL JUZGANDO COMITENTE"
        };

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
    }

    /**
     * Llenar datos
     */
    private void llenarDatos(Sheet sheet, List<DespachoComisorio> despachos, CellStyle normalStyle, CellStyle dateStyle) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        int rowNum = 1;
        int item = 1;

        for (DespachoComisorio despacho : despachos) {
            Row row = sheet.createRow(rowNum++);

            // ITEM
            Cell cell0 = row.createCell(0);
            cell0.setCellValue(item++);
            cell0.setCellStyle(normalStyle);

            // FECHA DE RECIBIDO
            Cell cell1 = row.createCell(1);
            if (despacho.getFechaRecibido() != null) {
                cell1.setCellValue(despacho.getFechaRecibido().format(dateFormatter));
            }
            cell1.setCellStyle(normalStyle);

            // RADICADO DEL PROCESO
            Cell cell2 = row.createCell(2);
            cell2.setCellValue(despacho.getRadicadoProceso() != null ? despacho.getRadicadoProceso() : "");
            cell2.setCellStyle(normalStyle);

            // N° DESPACHO COMISORIO
            Cell cell3 = row.createCell(3);
            cell3.setCellValue(despacho.getNumeroDespacho());
            cell3.setCellStyle(normalStyle);

            // ENTIDAD PROCEDENTE
            Cell cell4 = row.createCell(4);
            cell4.setCellValue(despacho.getEntidadProcedente());
            cell4.setCellStyle(normalStyle);

            // ASUNTO
            Cell cell5 = row.createCell(5);
            cell5.setCellValue(despacho.getAsunto());
            cell5.setCellStyle(normalStyle);

            // DEMANDANTE Y/O APODERADO
            Cell cell6 = row.createCell(6);
            cell6.setCellValue(despacho.getDemandanteApoderado() != null ? despacho.getDemandanteApoderado() : "");
            cell6.setCellStyle(normalStyle);

            // DEMANDADO Y/O APODERADO
            Cell cell7 = row.createCell(7);
            cell7.setCellValue(despacho.getDemandadoApoderado() != null ? despacho.getDemandadoApoderado() : "");
            cell7.setCellStyle(normalStyle);

            // INSPECCIÓN DE POLICÍA O CORREGIMIENTO ASIGNADO
            Cell cell8 = row.createCell(8);
            String inspectorInfo = "";
            if (despacho.getInspectorAsignado() != null) {
                inspectorInfo = despacho.getInspectorAsignado().getNombre();
                if (despacho.getInspectorAsignado().getZona() != null) {
                    inspectorInfo += " (" + despacho.getInspectorAsignado().getZona().name() + ")";
                }
            }
            cell8.setCellValue(inspectorInfo);
            cell8.setCellStyle(normalStyle);

            // FECHA DE DEVOLUCIÓN
            Cell cell9 = row.createCell(9);
            if (despacho.getFechaDevolucion() != null) {
                cell9.setCellValue(despacho.getFechaDevolucion().format(dateFormatter));
            }
            cell9.setCellStyle(normalStyle);
        }
    }

    /**
     * Ajustar ancho de columnas
     */
    private void ajustarAnchoColumnas(Sheet sheet) {
        for (int i = 0; i < 10; i++) {
            sheet.autoSizeColumn(i);
            // Agregar padding extra
            sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 1000);
        }
    }

    /**
     * Estilo para encabezados
     */
    private CellStyle crearEstiloEncabezado(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
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
     * Estilo para fechas
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
     * Estilo para celdas normales
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
