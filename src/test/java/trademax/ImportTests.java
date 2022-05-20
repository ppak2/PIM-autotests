package trademax;

import data.dataproviders.ImportsDataProvider;
import io.qameta.allure.Description;
import lombok.SneakyThrows;
import lombok.extern.java.Log;
import org.apache.commons.io.FileUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import util.ExcelReader;
import java.io.*;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Log
public class ImportTests extends BaseTest {


    @Test(dataProvider = "provider",dataProviderClass = ImportsDataProvider.class)
    @Description("Multiple tests that check correct response on import pagination")
    public void packagePaginatedList(String pageNumber, String perPage){

        given().header("authorization",token)
                .contentType("application/json")
                .body("{\"pagination\":{\"current_page\":"+pageNumber+",\"per_page\":"+perPage+"}}")
                .when().post(PAGINATED_LIST)
                .then()
                .assertThat().statusCode(200)
                .body("meta.pagination.current_page",is(Integer.valueOf(pageNumber)))
                .body("meta.pagination.per_page",is(Integer.valueOf(perPage)));
    }

    @Test(priority = 1)
    @Description("Test that checks whether excel file can be imported")
    public void importProductsByFile(){

        given().header("authorization",token)
                .multiPart(new File("src/test/resources/import/fullTemplate.xlsx"))
                .formParam("fileType","xlsx")
                .when().post(FILES_UPLOAD)
                .then().statusCode(200);
    }

    @Test(priority = 2)
    @Description("Multiple tests that check whether package data can be received with package id")
    public void getPackageDataByID(){

        final List<String> packageIDs = getPackageIDsByPageImport.apply(1,20);

        packageIDs.forEach(s->{

            given().header("authorization",token)
                    .when().post(BASE_URI + "/import/packages/"+ s +"/data")
                    .then()
                    .assertThat().statusCode(200)
                    .and().body("data.size()",greaterThan(0));
        });
    }

    @Test(priority = 3)
    @SneakyThrows(IOException.class)
    @Description("Test that checks if imported excel file can be downloaded and if it is not empty")
    public void downloadImportedFileByPackageID(){

        final List<String> packageIDs = getPackageIDsByPageImport.apply(1,20);
        String packageID = packageIDs.get(0);
        log.info("[Import tests]: PackageID "+packageID);
        final InputStream inputStream = given().header("authorization", token)
                .when().get(BASE_URI+"/import/packages/"+ packageID +"/file/download").asInputStream();

        FileUtils.copyInputStreamToFile(inputStream, new File(IMPORT_DOWNLOAD_PATH));

        ExcelReader excelReader = new ExcelReader(IMPORT_DOWNLOAD_PATH);

        assertThat(excelReader.importFileIsEmpty(), is(false));
        assertThat(excelReader.sheetContainsShiftedColumns(), is(false));
    }

    @BeforeClass
    public void signIn() {

        log.info("#### Import tests started #####");
    }

}
