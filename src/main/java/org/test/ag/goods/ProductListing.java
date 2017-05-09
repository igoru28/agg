package org.test.ag.goods;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by igor on 14.03.17.
 */
public class ProductListing {
    private static final Logger logger = LogManager.getLogger(ProductListing.class);
    private final XSSFWorkbook workbook;
    private final List<Product> products;
    private final int defaultLatency;
    private final int defaultQuantity;
    private final String defaultNote;
    private final ProductReport productReport;


    public ProductListing(String fileName, int defaultLatency, int defaultQuantity,
                          String defaultNote, ProductReport productReport) throws Exception {
        workbook = new XSSFWorkbook(fileName);
        this.defaultLatency = defaultLatency;
        this.defaultQuantity = defaultQuantity;
        this.defaultNote = defaultNote;
        this.productReport = productReport;
        XSSFSheet sheet = workbook.getSheetAt(0);
        Iterator<Row> iterator = sheet.iterator();
        List<Product> productList = new LinkedList<Product>();
        while (iterator.hasNext()) {
            Row row = iterator.next();
            Iterator<Cell> cellIterator = row.iterator();
            if (!cellIterator.hasNext()) {
                logger.warn("skipping empty line!!!");
                continue;
            }
            String id = cellIterator.next().getStringCellValue();
            if (!cellIterator.hasNext()) {
                productReport.writeFailureReport(id, "", -1.0, "no price for product", "");
                continue;
//                throw new IllegalStateException(String.format("No price given for product id=%s !!!", id));
            }
            Cell priceCell = cellIterator.next();
            double price;
            String dataFormatString = priceCell.getCellStyle().getDataFormatString();
            try {
                price = priceCell.getNumericCellValue();
            } catch (Exception ex) {
                logger.error("Cannot read price from the cell with format {}!!!", dataFormatString);
                productReport.writeFailureReport(id, "", -2.0, "invalid format for price cell", ex.toString());
                continue;
//                throw ex;
            }
            int latency;
            if (cellIterator.hasNext()) {
                latency = (int) cellIterator.next().getNumericCellValue();
            } else {
                latency = defaultLatency;
            }
            int quantity;
            if (cellIterator.hasNext()) {
                quantity = (int) cellIterator.next().getNumericCellValue();
            } else {
                quantity = defaultQuantity;
            }
            String note;
            if (cellIterator.hasNext()) {
                note = cellIterator.next().getStringCellValue();
            } else {
                note = defaultNote;
            }
            productList.add(new Product(null, id, price, latency, quantity, note));
        }
        products = Collections.unmodifiableList(productList);
        workbook.close();
    }

    public List<Product> getProducts() {
        return products;
    }


}
