package trademax;

import io.qameta.allure.Description;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import lombok.extern.java.Log;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.expect;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@Log
public class AuthorizationTests extends BaseTest {

    private String refreshToken;
    private RequestSpecification request;

    @BeforeClass
    public void initializeUser() {

        log.info("#### Auth tests started #####");
        request = new RequestSpecBuilder().addFormParam("email", LOGIN)
                .addFormParam("password", PASSWORD).build();

    }

    @Test
    @Description("Negative test that checks error response on invalid user")
    public void invalidUserOnSignIn() {

        String invalidUser = "bambi@pim.com";

        given().formParam("email", invalidUser).formParam("password", PASSWORD)

                .when()
                .post(AUTH_PATH)
                .then()
                .statusCode(is(both(greaterThan(399)).and(lessThan(500))))
                .and()
                .body(containsString("message\":\"User is not found"));
    }

    @Test(priority = 1)
    @Description("Negative test that checks error response on invalid password")
    public void invalidPasswordOnSignIn() {

        String invalidPassword = "invalid_password";

        given().formParam("email", LOGIN).formParam("password", invalidPassword)

                .when()
                .post(AUTH_PATH)
                .then()
                .statusCode(is(both(greaterThan(399)).and(lessThan(500))))
                .and()
                .body(containsString("message\":\"Incorrect password"));
    }


    @Test(priority = 2)
    @Description("Negative test that checks error response on forbidden sign in HTTP methods")
    public void invalidHTTPMethodsOnSignIn() {

        given().spec(request).when().get(AUTH_PATH).then().statusCode(405);
        given().spec(request).when().put(AUTH_PATH).then().statusCode(405);
        given().spec(request).when().delete(AUTH_PATH).then().statusCode(405);
        given().spec(request).when().patch(AUTH_PATH).then().statusCode(405);
        given().spec(request).when().head(AUTH_PATH).then().statusCode(405);
        given().spec(request).when().options(AUTH_PATH).then().statusCode(200);

    }

    @Test(priority = 3)
    @Description("Test that retrieves refresh token from successful sign in")
    public void signInSuccess() {

        Response response =

                given().spec(request).when().post(AUTH_PATH).then()
                        .assertThat().statusCode(200)
                        .and().contentType("application/json")
                        .and().body("size()", is(1))
                        .and().body("data.token", isA(String.class))
                        .and().body("data.refreshToken", isA(String.class))
                        .and().body("data.expireAt", isA(Integer.class))
                        .extract().response();

        refreshToken = response.body().jsonPath().get("data.refreshToken");
    }

    @Test(priority = 4)
    @Description("Test that validates successful sign out")
    public void signOutSuccess(){

        expect().statusCode(200)
                .when().post(SIGN_OUT_PATH);
    }

    @Test(priority = 5)
    @Description("Negative test that checks error response on invalid refresh token")
    public void invalidTokenOnRefreshTokenSignIn(){

        given().param("refreshToken","This is an invalid token")
                .when().post(REFRESH_TOKEN_PATH)
                .then().statusCode(400)
                .and().body(containsString("message\":\"User is not found"));
    }

    @Test(priority = 6)
    @Description("Negative test that checks error response on forbidden refresh token HTTP methods")
    public void invalidHTTPMethodsOnRefreshTokenSignIn(){

        given().param("refreshToken", refreshToken).when().get(REFRESH_TOKEN_PATH).then().statusCode(405);
        given().param("refreshToken", refreshToken).when().put(REFRESH_TOKEN_PATH).then().statusCode(405);
        given().param("refreshToken", refreshToken).when().delete(REFRESH_TOKEN_PATH).then().statusCode(405);
        given().param("refreshToken", refreshToken).when().patch(REFRESH_TOKEN_PATH).then().statusCode(405);
        given().param("refreshToken", refreshToken).when().head(REFRESH_TOKEN_PATH).then().statusCode(405);
        given().param("refreshToken", refreshToken).when().options(REFRESH_TOKEN_PATH).then().statusCode(200);
    }


    @Test(priority = 7)
    @Description("Test that checks refresh tokens successful sign in")
    public void refreshTokenSignInSuccess(){

        String newRefreshToken =

                given().param("refreshToken", refreshToken)
                        .when().post(REFRESH_TOKEN_PATH)
                        .then().statusCode(200)
                        .extract().response().getBody().jsonPath().get("data.refreshToken");

        given().param("refreshToken", newRefreshToken)
                .when().post(REFRESH_TOKEN_PATH)
                .then().statusCode(200);
    }
}
