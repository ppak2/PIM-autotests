package model.interfaces;

import java.lang.reflect.Field;
import java.util.LinkedList;

public interface IRequestObject {

    LinkedList<String> getFieldNames();

    LinkedList<Field> getNotEmptyFields();

}
