package org.test.ag.goods;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.xssf.usermodel.*;

import java.awt.*;
import java.io.Closeable;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by igor on 27.04.17.
 */
public class ProductReport implements Closeable {
    private static final Logger logger = LogManager.getLogger(ProductListing.class);
    private final XSSFWorkbook workbook;
    private final XSSFSheet sheet;
    private final String[] header = {
            "ID",
            "NAME",
            "PRICE",
            "STATUS",
            "REASON",
            "DETAILS"
    };
    private int currentRow = -1;
    public final String fileName;

    public ProductReport(String fileName) throws IOException {
        this.fileName = fileName;
        workbook = new XSSFWorkbook();
        sheet = workbook.createSheet("product report");
        writeHeader();
    }

    private void writeHeader() {
        XSSFRow row = sheet.createRow(currentRow = 0);
        for (int i = 0; i < header.length; i++) {
            XSSFCell cell = row.createCell(i);
            cell.setCellValue(header[i]);
        }
    }

    public void writeSuccessReport(String id, String name, double price) {
        currentRow = currentRow + 1;
        XSSFRow row = sheet.createRow(currentRow);
        XSSFCellStyle cellStyle = new XSSFCellStyle(workbook.getStylesSource());
        cellStyle.setFillForegroundColor(new XSSFColor(Color.GREEN));
        cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND );
        row.setRowStyle(cellStyle);
        row.createCell(0).setCellValue(id);
        row.createCell(1).setCellValue(name);
        row.createCell(2).setCellValue(price);
        row.createCell(3).setCellValue("SUCCESS");
        row.createCell(4).setCellValue("ADDED");
        row.createCell(5).setCellValue("");
    }

    public void writeFailureReport(String id, String name, Double price, String reason, String details) {
        currentRow = currentRow + 1;
        XSSFRow row = sheet.createRow(currentRow);
        XSSFCellStyle cellStyle = new XSSFCellStyle(workbook.getStylesSource());
        cellStyle.setFillForegroundColor(new XSSFColor(Color.RED));
        cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND );
        row.setRowStyle(cellStyle);
        row.createCell(0).setCellValue(opt(id));
        row.createCell(1).setCellValue(opt(name));
        row.createCell(2).setCellValue(opt(price));
        row.createCell(3).setCellValue("FAILURE");
        row.createCell(4).setCellValue(opt(reason));
        row.createCell(5).setCellValue(opt(details));
    }

    private String opt(String value) {
        return value != null ? value : "";
    }
    private Double opt(Double value) {
        return value != null ? value : 0.0;
    }

    public void close() throws IOException {
        workbook.write(new FileOutputStream(fileName));
        workbook.close();
    }

}
