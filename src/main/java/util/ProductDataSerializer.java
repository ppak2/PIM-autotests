package util;


import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import model.ProductData;
import model.interfaces.FieldNameKey;
import model.interfaces.IgnoreField;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ProductDataSerializer extends JsonSerializer<ProductData> {

    private JsonSerializer<Object> defaultSerializer;

    public ProductDataSerializer() {}

    public ProductDataSerializer(JsonSerializer<Object> serializer) {

        defaultSerializer = serializer;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void serialize(ProductData productData, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {

        jsonGenerator.configure(JsonGenerator.Feature.IGNORE_UNKNOWN, true);
        jsonGenerator.writeStartObject();
        jsonGenerator.writeObjectFieldStart("data");

        productData.getNotEmptyFields().forEach(field -> {

            Class fieldType = field.getType();

            if (!fieldType.isAssignableFrom(List.class) && !field.isAnnotationPresent(IgnoreField.class)) {

                try {

                    Object o;

                    jsonGenerator.writeFieldName(field.getName());
                    o = field.get(productData);
                    jsonGenerator.writeStartObject();
                    jsonGenerator.writeObjectField("value", o);
                    jsonGenerator.writeEndObject();

                } catch (IOException | IllegalAccessException e) {
                    e.printStackTrace();
                }

            } else if (fieldType.isAssignableFrom(List.class)) {

                try {

                    List<?> o = (List) field.get(productData);

                    if(field.isAnnotationPresent(FieldNameKey.class)){

                        try {

                            jsonGenerator.writeObjectFieldStart(field.getName());

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        o.forEach(element->{

                            Map.Entry<String, ?> mapEntry = (Map.Entry) element;
                            String stringEntryKey = mapEntry.getKey();
                            Object stringEntryValue = mapEntry.getValue();

                            try {
                                jsonGenerator.writeObjectField(stringEntryKey, stringEntryValue);
                            } catch (IOException e) {

                                e.printStackTrace();
                            }
                        });

                        try {
                            jsonGenerator.writeEndObject();
                        }
                        catch (IOException e){e.printStackTrace();}
                    }
                    else {

                        o.forEach(element -> {

                            if (element instanceof Map.Entry) {

                                Map.Entry<String, ?> mapEntry = (Map.Entry) element;
                                String stringEntryKey = mapEntry.getKey();
                                Object stringEntryValue = mapEntry.getValue();

                                try {

                                    jsonGenerator.writeFieldName(stringEntryKey);
                                    jsonGenerator.writeStartObject();
                                    jsonGenerator.writeObjectField("value", stringEntryValue);
                                    jsonGenerator.writeEndObject();

                                } catch (IOException e) {

                                    e.printStackTrace();
                                }
                            }
                            else {

                                Map<String, Map<String, Object>> stringMapMap = (Map<String, Map<String, Object>>) element;
                                String stringMapMapKeyValue = stringMapMap.entrySet().iterator().next().getKey();

                                try {

                                    jsonGenerator.writeObjectFieldStart(stringMapMapKeyValue);

                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                stringMapMap.get(stringMapMapKeyValue).forEach((key,value)->{

                                    try {
                                        jsonGenerator.writeObjectField(key, value);

                                    } catch (IOException e) {

                                        e.printStackTrace();
                                    }
                                });

                                try {
                                    jsonGenerator.writeEndObject();
                                }
                                catch (IOException e){e.printStackTrace();}

                            }
                        });
                    }
            }
                catch(IllegalAccessException e){
                e.printStackTrace();
            }
        }
    });

        jsonGenerator.writeEndObject();
        jsonGenerator.writeEndObject();
    }
}
