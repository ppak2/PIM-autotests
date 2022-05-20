package data.dataproviders;

import org.testng.annotations.DataProvider;

public class ExportsDataProvider {

    public ExportsDataProvider(){}

    @DataProvider(name = "listProvider", parallel = true)
    public Object[][] packageListProvider() {

        return new Object[][]{
                {
                        "1", "10"
                },
                {
                        "5", "20"
                },
                {
                        "10", "30"
                },
                {
                        "20", "30"  /////<--------50//40
                }
        };
    }
}
