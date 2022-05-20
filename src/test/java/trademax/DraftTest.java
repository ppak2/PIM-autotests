package trademax;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.response.ResponseBody;
import io.restassured.specification.RequestSpecification;
import lombok.SneakyThrows;
import org.testng.annotations.*;

import java.io.Writer;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;


public class DraftTest extends BaseTest {

    private String token;
    private String productID;
    private static final String PRODUCT_BY_ID = BASE_URI+"/products/"; //+productID
    private static final String PRODUCTS_LIST = PRODUCT_BY_ID+"list";


    @Test
    @SneakyThrows(Exception.class)
    @SuppressWarnings("unchecked")
    public void getProductByID(){

       getPackageIDsByPageImport.apply(2,20).forEach(System.out::println);


       final List<String> list = new ArrayList<>();
       list.addAll(getChannelsCodesAndIDs.get().values());

        final List<Map<String, Object>> products = getProductsPerPage.apply(1,40);
        productID = (String) products.get(0).get("id");

        System.out.println(composeChannelsJson.apply(list, true));
//        given().header("authorization",token)
//                .pathParam("channelID", productID)
//                .contentType("application/json")
//                .body(composeChannelsJson.apply(list, true))
//                .post(PUBLISH_PRODUCT).then().statusCode(200);

    }


    @BeforeClass
    public void signIn() {

        final RequestSpecification request = new RequestSpecBuilder().addFormParam("email", LOGIN)
                .addFormParam("password", PASSWORD).build();

        final Response response =

                given().spec(request).when().post(AUTH_PATH).then()
                        .assertThat().statusCode(200)
                        .and().contentType("application/json")
                        .and().body("size()", is(1))
                        .and().body("data.token", isA(String.class))
                        .and().body("data.refreshToken", isA(String.class))
                        .and().body("data.expireAt", isA(Integer.class))
                        .extract().response();

        token = response.body().jsonPath().get("data.token");
        objectMapper = new ObjectMapper();

        System.out.println("##### Product tests started #####");

    }

//    private Function<Integer, List<Map<String, Object>>> getProductsPerPage = pageNumber->{
//
//        RequestSpecification request = new RequestSpecBuilder()
//                .addHeader("authorization", token)
//                .setContentType("application/json")
//                .setBody("{\"pagination\":{\"current_page\":"+pageNumber+",\"per_page\":100}}")
//                .build();
//
//        return given().spec(request)
//                .when().post(PRODUCTS_LIST)
//                .jsonPath().getObject("data", new TypeRef<List<Map<String, Object>>>() {});
//    };

//    Function<String, Map<String, String>> getChannelIDs = responseJSON->{
//
//        Map<String, String> result = new LinkedHashMap<>();
//
//        List<Map<String, String>> mapList = given().header("authorization",token)
//                .when().post(PRODUCTS_LIST).jsonPath().getList("schemas.channels");
//
//        mapList.forEach(map->{
//
//            result.put(map.get("code"), map.get("id"));
//        });
//
//        result.entrySet().forEach(System.out::println);
//
//        return result;
//    };

    BiFunction<List<String>, Boolean, String> composeChannelsJson = (channelIDs, marginPriceForce)->{

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

}
