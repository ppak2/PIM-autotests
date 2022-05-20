package trademax;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.common.mapper.TypeRef;
import io.restassured.filter.log.LogDetail;
import io.restassured.response.Response;
import io.restassured.response.ResponseBody;
import io.restassured.specification.RequestSpecification;
import io.vavr.Tuple3;
import lombok.SneakyThrows;
import lombok.extern.java.Log;
import model.ProductData;
import model.interfaces.IRequestObject;
import org.testng.annotations.BeforeClass;

import java.io.IOException;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;

@Log
public abstract class BaseTest {

    String token;
    ObjectMapper objectMapper;
    static final String BASE_URI = "http://pim.1415.trademax-test.com";
    static final String LOGIN = "admin@pim.com";
    static final String AUTH_PATH = BASE_URI+"/auth/sign-in/credentials";
    static final String PASSWORD = "uS4PSeWZ";
    static final String IMPORT_DOWNLOAD_PATH = "target/importDownLoad.xlsx";
    static final String EXPORT_DOWNLOAD_PATH = "target/exportDownLoad.xlsx";
    //Auth tests
    static final String SIGN_OUT_PATH = BASE_URI+"/auth/sign-out";
    static final String REFRESH_TOKEN_PATH = BASE_URI+"/auth/sign-in/refresh-token";
    //Import tests
    static final String PAGINATED_LIST = BASE_URI+"/import/packages/list";
    static final String FILES_UPLOAD = BASE_URI+"/import/files/upload";
    //Export tests
    static final String EXPORT_PRODUCTS = BASE_URI + "/export/products";
    static final String EXPORT_PACKAGE_LIST = BASE_URI + "/export/list";
    static final String JSON_FILES = "src/test/resources/export/";
    static final String DOWNLOAD_FILE = "/export/download/";
    //Products tests
    static final String PRODUCT_BY_ID = BASE_URI+"/products/"; //+productID
    static final String PRODUCTS_LIST = PRODUCT_BY_ID+"list";
    //Publish tests
    static final String PUBLISH_PRODUCT = BASE_URI + "/products/{productID}/publish";
    static final String CLIENT_EVENT_LOG = BASE_URI + "/clients/log";
    static final String SYNC_EVENTS_CLIENT = BASE_URI + "/clients/sync";
    static final String PUBLISHED_PRODUCTS = BASE_URI + "/publish/packages/list";
    static final String UPDATE_PRODUCT_BY_CLIENT = BASE_URI + "/clients/product"; //+body data GET,PATCH
    static final String PUBLISH_PACKAGE_DATA = BASE_URI + "/publish/packages/{packageID}/data";
    //Categories tests
    static final String TREE_BY_ID = BASE_URI + "/menu/{treeID}"; //+POST UPDATE TREE //+DELETE DELETE TREE
    static final String CREATE_TREE = BASE_URI + "/menu";
    static final String GET_CATEGORY_BY_ID = BASE_URI + "/categories/{categoryID}";



    @BeforeClass
    public void setUpConnection(){

        RestAssured.baseURI = BASE_URI;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails(LogDetail.ALL);

        objectMapper = new ObjectMapper();

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
    }


    /**
    * ImportTests
    * downloadImportedFileByPackageID test*/
    BiFunction<Integer, Integer, List<String>> getPackageIDsByPageImport = (pageNumber,perPage)->{

        RequestSpecification request = new RequestSpecBuilder()
                .addHeader("authorization", token)
                .setContentType("application/json")
                .setBody("{\"pagination\":{\"current_page\":"+ pageNumber +",\"per_page\":"+ perPage +"}}")
                .build();

        ArrayList<Map<String, String>> jsonDataToCollection = given().spec(request)
                .when().post(PAGINATED_LIST)
                .body().path("data");

        return jsonDataToCollection.stream().filter(m->m.get("status")
                .matches("import.success"))
                .map(m->m.get("id")).collect(Collectors.toList());
    };

    /**
    * ExportTests
    * beforeClass*/
    Supplier<List<String>> getFilePathOfExportedProduct = () ->

            given().header("authorization", token)
                    .when().post(EXPORT_PACKAGE_LIST)
                    .body().jsonPath().getList("data.filePath.findAll{filePath->filePath!=null}");


    /**
    * ProductsTests
    * getProductByID test*/
    BiFunction<Integer,Integer, List<Map<String, Object>>> getProductsPerPage = (pageNumber,perPage)->{

        RequestSpecification request = new RequestSpecBuilder()
                .addHeader("authorization", token)
                .setContentType("application/json")
                .setBody("{\"pagination\":{\"current_page\":"+pageNumber+",\"per_page\":"+ perPage +"}}")
                .build();

        return given().spec(request)
                .when().post(PRODUCTS_LIST)
                .jsonPath().getObject("data", new TypeRef<List<Map<String, Object>>>() {});
    };

    /**
     * Products and Publish tests*/
    Function<String, IRequestObject> deserializeProductByID = productID->{

        String responseJSON =
                given().header("authorization",token)
                        .when().get(PRODUCT_BY_ID+productID)
                        .asString();

        ProductData product = null;

        try {
            product = objectMapper.readValue(responseJSON, ProductData.class);
        }
        catch (IOException e){e.printStackTrace();}

        return product;
    };

    /**
     * Products and Publish tests*/
    Function<IRequestObject, String> serializeProduct = product->{

        try {
            return objectMapper.writeValueAsString(product);
        }
        catch (JsonProcessingException e){
            throw new RuntimeException(e);
        }
    };

    /**
     * Products and Publish tests*/
    Function<ResponseBody, List<Tuple3<String, String, String>>> extractPublishPackageDataIDs = responseData->{

        List<Tuple3<String, String, String>> result = new ArrayList<>();
        int dataSize = responseData.jsonPath().get("data.size()");

        for (int i=0; i<dataSize; i++){

            String packageID = responseData.jsonPath().get("data"+"["+i+"]"+"."+"id");
            String userID = responseData.jsonPath().get("data"+"["+i+"]"+"."+"user.id");
            Object o = responseData.jsonPath().get("data"+"["+i+"]"+"."+"properties.import_package_id");
            String importPackageID;

            if(o instanceof String) importPackageID = (String) o;
            else importPackageID = "0";

            result.add(new Tuple3<>(packageID,userID,importPackageID));
        }

        return result;
    };

    /**
     * Products and Publish tests*/
    Supplier<Map<String, String>> getChannelsCodesAndIDs = ()->{

        Map<String, String> result = new LinkedHashMap<>();

        List<Map<String, String>> mapList = given().header("authorization",token)
                .when().post(PRODUCTS_LIST).jsonPath().getList("schemas.channels");

        mapList.forEach(map->{

            result.put(map.get("code"), map.get("id"));
        });

        return  result;
    };


}
