package trademax;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.qameta.allure.Description;
import io.restassured.response.ResponseBody;
import io.vavr.Tuple3;
import lombok.extern.java.Log;
import model.ProductData;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

@Log
public class PublishTests extends BaseTest {


    private static final String CLIENT_TOKEN = "10cc95cd4dd7a861d7cd4923becf945c043290364e6abb274250296345b9a8de";
    private List<Tuple3<String, String, String>> listIDs;
    private List<String> channelIDs;
    private List<String> productIDs;
    private List<String> notificationsIDs;
    private List<Map<String, Object>> products;

    @Test
    @Description("Multiple tests that check product publish via product ids ")
    public void publishProduct(){

        String bodyJSON = composeChannelsJson.apply(channelIDs, false);

        productIDs.forEach(productID->{

            given().header("authorization",token)
                    .pathParam("productID", productID)
                    .contentType("application/json")
                    .body(bodyJSON)
                    .when().post(PUBLISH_PRODUCT)
                    .then().statusCode(200);
        });
    }

    @Test(priority = 1)
    @Description("Test that checks whether client event logs can be retrieved")
    public void getClientEventLog(){

        notificationsIDs = given().header("authorization",CLIENT_TOKEN)
                .when().post(CLIENT_EVENT_LOG)
                .body().jsonPath().getList("data.id");

       assertThat(notificationsIDs.size(), is(greaterThan(0)));
    }

    @Test(priority = 2)
    @Description("Test that checks whether events can by synced with client")
    public void syncEventsWithClient(){

        String bodyJSON = composeNotificationsJson.apply(notificationsIDs);

        given().header("authorization",CLIENT_TOKEN)
                .contentType("application/json")
                .body(bodyJSON)
                .when().post(SYNC_EVENTS_CLIENT).then().statusCode(202);

    }

    @Test(priority = 3)
    @Description("Test that checks whether client publish data can be retrieved")
    public void getClientPublishData(){

        String bodyJSON = composeProductIDs.apply(productIDs);
        given().header("authorization", token)
                .contentType("application/json")
                .body(bodyJSON)
                .when().post(PUBLISHED_PRODUCTS).then().statusCode(200);
    }

    @Test(priority = 4)
    @SuppressWarnings("unchecked")
    @Description("Test that checks whether particular product can be updated by client")
    public void updateProductsByClient(){

        ProductData productData = (ProductData) deserializeProductByID.apply(productIDs.get(0));
        String pID = productData.getId();

        given().header("authorization", CLIENT_TOKEN)
                .contentType("application/json")
                .body("{\"products\":[{\"productId\":\""+productData.getId()+"\",\"pid\":"+productData.getPid()+",\"calculated_purchase_price_max\":1010}]}")
                .when().patch(UPDATE_PRODUCT_BY_CLIENT).body().prettyPrint();

        productData = (ProductData)deserializeProductByID.apply(pID);
        Integer newPurchasePriceMax = (Integer) productData.getCalculated_purchase_price_max().get(0).getValue();

        assertThat(newPurchasePriceMax, equalTo(1010));
    }

    @Test(priority = 5)
    @Description("Test that checks whether publish package data can be retrieved")
    public void getPublishPackageData(){

        given().header("authorization", token).pathParam("packageID", listIDs.get(0)._1)
                .when().post(PUBLISH_PACKAGE_DATA).then().statusCode(200);
    }

    @BeforeClass
    @Description("Before class method that runs Get publish packages test")
    public void getPublishPackages(){

        log.info("#### Publish tests started #####");

        channelIDs = new ArrayList<>();
        productIDs = new ArrayList<>();

        ResponseBody responseData = given().header("authorization", token)
                .when().post(PUBLISHED_PRODUCTS)
                .body();

        listIDs = extractPublishPackageDataIDs.apply(responseData);
        products = getProductsPerPage.apply(1,20);
        channelIDs.addAll(getChannelsCodesAndIDs.get().values());

        products.forEach(map->{

            productIDs.add((String) map.get("id"));

        });

        assertThat(listIDs.size(), is(greaterThan(0)));
    }

    private BiFunction<List<String>, Boolean, String> composeChannelsJson = (channelIDs, marginPriceForce)->{

        ObjectNode mainNode = objectMapper.createObjectNode();
        ArrayNode channels = objectMapper.createArrayNode();

        channelIDs.forEach(id->{

            ObjectNode node = objectMapper.createObjectNode();
            node.put("id", id);
            node.put("published", true);
            node.put("margin_price_force", marginPriceForce);

            channels.add(node);
        });

        mainNode.set("channels", channels);
        mainNode.put("force", marginPriceForce);

        return mainNode.toString();
    };

    private Function<List<String>, String> composeNotificationsJson = ids->{

        ObjectNode mainNode = objectMapper.createObjectNode();
        ArrayNode notifications = objectMapper.createArrayNode();

        ids.forEach(val->{

            ObjectNode node = objectMapper.createObjectNode();
            node.put("id", val);
            node.put("status", "success");
            node.put("message","TEST MESSAGE");

            notifications.add(node);
            mainNode.set("notifications", notifications);
        });

        return mainNode.toString();
    };

    private Function<List<String>, String> composeProductIDs = ids->{

        ObjectNode mainNode = objectMapper.createObjectNode();
        ArrayNode productIDs = objectMapper.createArrayNode();

        ids.forEach(val->{

            productIDs.add(val);
            mainNode.set("productIds", productIDs);
        });

        return mainNode.toString();
    };

}
