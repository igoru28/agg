import org.junit.Assert;
import org.junit.Test;
import org.test.ag.goods.ProductListing;
import org.test.ag.goods.ProductReport;

/**
 * Created by igor on 14.03.17.
 */
public class ProductListingTest {
    final String name = ClassLoader.getSystemClassLoader().getResource("input.xlsx").getFile();

    @Test
    public void testGoodsListing() throws Exception {
        ProductListing listing = new ProductListing(name, 77, 777, "",
                new ProductReport("target/input_out.xlsx"));
        Assert.assertFalse(listing.getProducts().isEmpty());
        System.out.println(listing.getProducts());
        Assert.assertEquals(3, listing.getProducts().get(0).getLatency());
        Assert.assertEquals(4, listing.getProducts().get(1).getLatency());
        Assert.assertEquals(5, listing.getProducts().get(2).getLatency());

        Assert.assertEquals(20, listing.getProducts().get(0).getQuantity());
        Assert.assertEquals(10, listing.getProducts().get(1).getQuantity());
        Assert.assertEquals(20, listing.getProducts().get(2).getQuantity());

        Assert.assertEquals("note1", listing.getProducts().get(0).getConditionNote());
        Assert.assertEquals("note2", listing.getProducts().get(1).getConditionNote());
        Assert.assertEquals("note3", listing.getProducts().get(2).getConditionNote());
    }
}
