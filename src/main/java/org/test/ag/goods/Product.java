package org.test.ag.goods;

/**
 * Created by igor on 14.03.17.
 */
public class Product {
    private final String name;
    private final String id;
    private final double price;
    private final int latency;
    private final int quantity;
    private final String conditionNote;

    public Product(String name, String id, double price, int latency, int quantity, String conditionNote) {
        this.name = name;
        this.id = id;
        this.price = price;
        this.latency = latency;
        this.quantity = quantity;
        this.conditionNote = conditionNote;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public double getPrice() {
        return price;
    }

    public int getLatency() {
        return latency;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getConditionNote() {
        return conditionNote;
    }

    @Override
    public String toString() {
        return "Product{" +
                "name='" + name + '\'' +
                ", id='" + id + '\'' +
                ", price=" + price +
                ", latency=" + latency +
                ", quantity=" + quantity +
                ", conditionNote='" + conditionNote + '\'' +
                '}';
    }
}
