package trademax;

import data.dataproviders.ProductsDataProvider;
import io.qameta.allure.Description;
import lombok.SneakyThrows;
import lombok.extern.java.Log;
import model.ProductData;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import java.util.*;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Log
public class ProductsTests extends BaseTest {

    private String productID;
    private ProductData product;


    @Test(dataProvider = "provider", dataProviderClass = ProductsDataProvider.class)
    @Description("Multiple tests that check Products page pagination")
    public void productPaginatedList(String pageNumber, String perPage){

        given().header("authorization",token)
                .contentType("application/json")
                .body("{\"pagination\":{\"current_page\":"+pageNumber+",\"per_page\":"+perPage+"}}")
                .when().post(PRODUCTS_LIST)
                .then()
                .assertThat().statusCode(200)
                .body("data.size()",is(Integer.valueOf(perPage)));
    }

    @Test(priority = 1)
    @SneakyThrows(Exception.class)
    @SuppressWarnings("unchecked")
    @Description("Test that checks whether particular product can be fetched by ID")
    public void getProductByID(){

        final List<Map<String, Object>> products = getProductsPerPage.apply(1,40);
        int value = new Random().nextInt(40);
        log.info("[Products tests]: Random value for Product from response list "+value);

        productID = (String) products.get(value).get("id");
        log.info("[Products tests]: productID "+productID);
        assertThat(productID, not(isEmptyOrNullString()));

        product = (ProductData) deserializeProductByID.apply(productID);
        log.info("[Products tests]: product.getId() "+product.getId());
        log.info("[Products tests]: product artno "+product.getArtno());
        assertThat(productID, equalTo(product.getId()));
    }

    @Test(priority = 2)
    @SuppressWarnings("unchecked")
    @Description("Test that checks whether particular product can be updated by ID")
    public void updateProduct(){

        Number currentPrice = (Number) product.getPrice_current().get(0).entrySet().iterator().next().getValue().get("value");
        int cPrice = currentPrice.intValue();
        if(cPrice < 1) cPrice = (cPrice+1)*2000;
        int newCampaignPrice = (int) Math.round(cPrice-(cPrice*0.1));

        log.info("[Products tests]: Current price "+cPrice);
        log.info("[Products tests]: New campaign price "+newCampaignPrice);

        product.setIntrastat_code("111222333");
        List list = new ArrayList<>();
        Map<String, Map<String,Object>> mapMap = new HashMap<>();
        Map<String,Object> map = new HashMap<>();
        map.put("stock_max",null);
        map.put("currency", "SEK");
        map.put("value",newCampaignPrice);
        map.put("date_start",1564617600);
        map.put("stock_min",null);
        map.put("is_active",false);
        map.put("stock_vendor",false);
        map.put("date_end",1565395200);
        mapMap.put("price_campaign:tm-pricelist-se",map);

        list.add(mapMap);

        product.getPrice_campaign().clear();
        product.setPrice_campaign(list);

        String json = serializeProduct.apply(product);

        given().header("authorization",token)
                .contentType(JSON)
                .body(json)
                .when().put(PRODUCT_BY_ID+productID)
                .then().statusCode(200);

        ProductData productNew = (ProductData) deserializeProductByID.apply(productID);
        Number responseCampaignPrice = (Number) productNew.getPrice_campaign().get(0).entrySet().iterator().next().getValue().get("value");
        log.info("[Products tests]: Response campaign price "+responseCampaignPrice);

        assertThat(responseCampaignPrice.intValue(), equalTo(newCampaignPrice));
        assertThat(productNew.getIntrastat_code(), is("111222333"));

    }

    @BeforeClass
    public void signIn() {

        log.info("#### Products tests started #####");
    }
}
