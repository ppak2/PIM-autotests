package trademax;

import data.dataproviders.ExportsDataProvider;
import io.qameta.allure.Description;
import io.qameta.allure.Issue;
import lombok.SneakyThrows;
import lombok.extern.java.Log;
import org.apache.commons.io.FileUtils;
import org.testng.annotations.*;
import util.ExcelReader;

import java.io.File;
import java.io.InputStream;
import java.util.*;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@Log
public class ExportTests extends BaseTest {

    private List<String> filePaths = new LinkedList<>();
    private LinkedList<File> jsonFiles;

    @Test(dataProvider = "jsonProvider")
    @Description("Multiple tests that check whether JSON files can be uploaded via export job")
    public void createProductExportJob(String name) {

        log.info("[Export tests]: JSON export file "+name);
        given().header("authorization", token)
                .contentType(JSON)
                .body(new File(JSON_FILES, name))
                .when().post(EXPORT_PRODUCTS).then().statusCode(200);

    }

    @Test(priority = 1, dataProvider = "listProvider", dataProviderClass = ExportsDataProvider.class)
    @Description("Multiple tests that check correct response on export package list pagination")
    ///Issue 50+ pagination
    public void getExportPackageList(String pageNumber, String perPage) {

        given().header("authorization", token)
                .contentType("application/json")
                .body("{\"pagination\":{\"current_page\":" + pageNumber + ",\"per_page\":" + perPage + "}}")
                .when().post(EXPORT_PACKAGE_LIST)
                .then().assertThat().body("data.size()", is(Integer.valueOf(perPage)));

    }

    @Test(priority = 2)
    @SneakyThrows(Exception.class)
    @Description("Test that checks downloaded file format correctness")
    @Issue("PIM-188")
    public void downLoadExportedFile() {

        Thread.sleep(5000);
        filePaths.addAll(getFilePathOfExportedProduct.get());

        final InputStream inputStream = given().header("authorization", token)
                .when().get(DOWNLOAD_FILE + filePaths.get(0)).asInputStream();

        FileUtils.copyInputStreamToFile(inputStream, new File(EXPORT_DOWNLOAD_PATH));

        ExcelReader excelReader = new ExcelReader(EXPORT_DOWNLOAD_PATH);

        assertThat(excelReader.importFileIsEmpty(), is(false));
        assertThat(excelReader.sheetContainsShiftedColumns(), is(false));

    }

    @BeforeClass
    public void signIn() {

        log.info("#### Export tests started #####");
        jsonFiles = new LinkedList<>(FileUtils.listFiles(new File(JSON_FILES), null, false));
    }

    @DataProvider
    @SneakyThrows(NullPointerException.class)
    @Description("Data provider for JSON files retrieving from Resources folder")
    private Object[][] jsonProvider() {

        Object[][] array = new Object[jsonFiles.size()][1];

        for (int i = 0; i < array.length; i++) {
            array[i][0] = jsonFiles.poll().getName();
        }
        return array;
    }
}
