import org.apache.logging.log4j.Level;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.test.ag.Main;
import org.test.ag.goods.Product;
import org.test.ag.site.AmazonClient;

/**
 * Created by igor on 16.03.17.
 */
public class AmazonClientTest {
    private AmazonClient amazonClient = new AmazonClient();
    @Test
    @Ignore
    public void integration() throws Exception {
        Main.setLogLevel(Level.TRACE);
        Assert.assertTrue(amazonClient.needAuth());
        amazonClient.auth("one@two", "pwd1234");
        Assert.assertFalse(amazonClient.needAuth());
        Product product = new Product(null, "B01MTT88MM", 68, 3, 20, "test");
        amazonClient.addProduct(product);
    }

}
