package trademax;

import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;

//NOT IMPLEMENTED
public class CategoriesTests extends BaseTest {


    @Test
    public void getTreeByID(){

        given().header("authorization", token)
                .contentType("application/json")
                .pathParam("treeID", 3)
                .when().get(TREE_BY_ID).body().prettyPrint();

    }

    @Test(priority = 1)
    public void createTree(){

        given().header("authorization", token)
                .contentType("application/json")
                .body("{\"title\":\"Smereka\"}").when().post(CREATE_TREE).body().prettyPrint();
    }

    @Test(priority = 2)
    public void updateTree(){

        given().header("authorization", token)
                .contentType("application/json")
                .pathParam("treeID", 3)
                .body("{\"title\":\"Yalynka\"}")
                .when().put(TREE_BY_ID).body().prettyPrint();
    }

    @Test(priority = 3)
    public void deleteTree(){

        given().header("authorization", token)
                .contentType("application/json")
                .pathParam("treeID", 3)
                .when().delete(TREE_BY_ID).body().prettyPrint();
    }

    @Test(priority = 4)
    public void getCategoryByID(){

        given().header("authorization", token)
                .contentType("application/json")
                .pathParam("categoryID", 1)
                .when().get(GET_CATEGORY_BY_ID).body().prettyPrint();
    }
}
