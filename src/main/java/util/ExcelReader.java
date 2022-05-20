package util;

import lombok.SneakyThrows;
import org.apache.poi.ss.usermodel.*;
import java.io.File;
import java.io.IOException;


public final class ExcelReader {


    private Workbook workbook;
    private Sheet firstPage;

    @SneakyThrows(IOException.class)
    public ExcelReader(String filePath){

        workbook = WorkbookFactory.create(new File(filePath));
        firstPage = workbook.getSheetAt(0);
    }

    public boolean importFileIsEmpty(){

        return firstPage.getLastRowNum() == 0;
    }

    public boolean sheetContainsShiftedColumns(){

        int lastTitleColumnIndex = firstPage.getRow(0).getLastCellNum();

        if (lastTitleColumnIndex > firstPage.getRow(0).getPhysicalNumberOfCells()){

            throw new AssertionError("Sheet contains blank Title columns");
//            throw new RuntimeException("Sheet contains blank Title columns");
        }

        else {

            firstPage.rowIterator().forEachRemaining(row->{

                if(row.getRowNum() == 0) return;
                if(row.getLastCellNum() > lastTitleColumnIndex){

                    throw new AssertionError("Sheet contains shifted column values "+"Row #"+row.getRowNum());
//                    throw new RuntimeException("Sheet contains shifted column values "+"Row #"+row.getRowNum());
                }
            });
        }
        return false;
    }
}
