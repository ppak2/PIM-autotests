package data.dataproviders;

import org.testng.annotations.DataProvider;

public class ImportsDataProvider {

    public ImportsDataProvider(){}

    @DataProvider(name="provider",parallel = true)
    private Object[][] listPaginationDataProvider(){
        return new Object[][]{
                {
                        "1","10"
                },
                {
                        "2","20"
                },
                {
                        "10","30"
                },
                {
                        "50","40"
                }
        };
    }
}
