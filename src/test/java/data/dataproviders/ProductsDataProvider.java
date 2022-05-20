package data.dataproviders;

import org.testng.annotations.DataProvider;

public class ProductsDataProvider {

    public ProductsDataProvider(){}

    @DataProvider(name="provider",parallel = true)
    public Object[][] listPaginationDataProvider(){
        return new Object[][]{
                {
                        "1","25"
                },
                {
                        "2","50"
                },
                {
                        "10","100"
                }
        };
    }
}
