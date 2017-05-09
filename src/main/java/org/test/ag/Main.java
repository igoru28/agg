package org.test.ag;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.test.ag.goods.Product;
import org.test.ag.goods.ProductListing;
import org.test.ag.goods.ProductReport;
import org.test.ag.site.AmazonClient;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

/**
 * Created by igor on 14.03.17.
 */
public class Main {
    private static final Logger logger = LogManager.getLogger(Main.class);

    public static void main(String[] args) throws Exception {
        File f = new File(".");
        String[] xslxs = f.list(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".xlsx");
            }
        });
        if (xslxs.length == 0) {
            logger.error("Cannot find any input excel file. Program will quit");
            System.exit(1);
        }
        try {

            Properties properties = new Properties();
            properties.load(new FileInputStream("client.conf"));
            Level level = Level.getLevel(properties.getProperty("logLevel", "info").toUpperCase());
            setLogLevel(level);

            System.setProperty("webdriver.gecko.driver", properties.getProperty("gecko", "./bin/geckodriver"));
            ProductReport productReport = new ProductReport(fileNameWithoutExtension(xslxs[0]) + "_out.xlsx");
            ProductListing productListing = new ProductListing(xslxs[0],
                    Integer.parseInt(properties.getProperty("defaultLatency", "3")),
                    Integer.parseInt(properties.getProperty("defaultQuantity", "20")),
                    properties.getProperty("defaultNote", ""), productReport);
            long startTime = System.currentTimeMillis();
            AmazonClient amazonClient = new AmazonClient();
            List<Product> successfullProducts = new ArrayList<Product>(productListing.getProducts().size());
            List<Product> failedProducts = new LinkedList<Product>();
            logger.info("starting addition of {} products", productListing.getProducts().size());
            if (amazonClient.needAuth()) {
                amazonClient.auth(properties.getProperty("email"), properties.getProperty("password"));
            }
            try {
                for (Product product : productListing.getProducts()) {
                    try {
                        if (amazonClient.needAuth()) {
                            amazonClient.auth(properties.getProperty("email"), properties.getProperty("password"));
                        }
                        String productDetails = amazonClient.addProduct(product);
                        successfullProducts.add(product);
                        productReport.writeSuccessReport(product.getId(), productDetails, product.getPrice());
                    } catch (Exception ex) {
                        failedProducts.add(product);
                        StringWriter wr = new StringWriter();
                        ex.printStackTrace(new PrintWriter(wr));
                        productReport.writeFailureReport(product.getId(), "", product.getPrice(),
                                "product wasn't added", wr.toString());
                        logger.error("product {} wasn't added", product);
                        logger.warn("Re-initializing AmazonClient");
                        amazonClient.quit();
                        amazonClient = new AmazonClient();
                    }
                }
            } finally {
                long finishTime = System.currentTimeMillis();
                StringBuilder report = new StringBuilder();
                report.append("Finished in ").append((finishTime - startTime) / 1000).append(" seconds").append("\n");
                report.append("Successfully added ").append(successfullProducts.size()).append(" products:").append("\n");
                for (Product product : successfullProducts) {
                    report.append(product).append("\n");
                }
                logger.info(report.toString());
                if (!failedProducts.isEmpty()) {
                    report = new StringBuilder();
                    report.append(failedProducts.size()).append(" product(s) failed:").append("\n");
                    for (Product product : failedProducts) {
                        report.append(product).append("\n");
                    }
                    logger.warn(report.toString());
                }
                amazonClient.quit();
            }
        } catch (Throwable ex) {
            logger.error("error", ex);
        }
        System.out.println("Press any key to exit");
        System.in.read();
    }

    private static String fileNameWithoutExtension(String fileName) {
        return fileName.substring(0, fileName.lastIndexOf("."));
    }

    public static void setLogLevel(Level level) {
        logger.warn("setting log level to " + level);
        final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        final Configuration config = ctx.getConfiguration();
        for (LoggerConfig loggerConfig : config.getLoggers().values()) {
            loggerConfig.setLevel(level);
        }
        ctx.updateLoggers();
    }
}
