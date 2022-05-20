package util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import model.ProductData;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

import static io.vavr.API.*;


//ProductData
@Slf4j
public class ProductDataDeserializer extends StdDeserializer<ProductData> {

    public ProductDataDeserializer() {
        this(null);
    }

    public ProductDataDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    @SneakyThrows({IOException.class})
    public ProductData deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) {

        JsonNode mainNode = jsonParser.getCodec().readTree(jsonParser); //OR READ MAIN NODE JsonNode node = objectMapper.readValue(carJson, JsonNode.class);

        JsonNode dataNode = mainNode.get("data");

        ProductData product = new ProductData();
        product.setId(dataNode.get("id").textValue());
        product.setCreatedAt(dataNode.get("createdAt").longValue());
        product.setUpdatedAt(dataNode.get("updatedAt").longValue());

        Class clazz = ProductData.class;  // class var for setting field values

        JsonNode node = dataNode.get("data");

        final LinkedList<String> productDataFieldNames = product.getFieldNames();

        node.fields().forEachRemaining(nodeEntry -> {

            String entryKey = nodeEntry.getKey();

            if (productDataFieldNames.contains(entryKey)) {
                try {
                    Field field = clazz.getField(entryKey);
                    field.set(product, getNodeValue(nodeEntry));
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            } else {

                Match(entryKey).of(Case($(entryK -> entryK.startsWith("title:")), () -> product.getTitle().add(new AbstractMap.SimpleEntry<>(nodeEntry.getKey(), nodeEntry.getValue().get("value").textValue()))),
                        Case($(entryK -> entryK.startsWith("type:")), () -> product.getType().add(new AbstractMap.SimpleEntry<>(nodeEntry.getKey(), nodeEntry.getValue().get("value").textValue()))),
                        Case($(entryK -> entryK.startsWith("description:")), () -> product.getDescription().add(new AbstractMap.SimpleEntry<>(nodeEntry.getKey(), nodeEntry.getValue().get("value").textValue()))),
                        Case($(entryK -> entryK.startsWith("description_short:")), () -> product.getDescription_short().add(new AbstractMap.SimpleEntry<>(nodeEntry.getKey(), nodeEntry.getValue().get("value").textValue()))),
                        Case($(entryK -> entryK.startsWith("forbidden:")), () -> product.getForbidden().add(new AbstractMap.SimpleEntry<>(nodeEntry.getKey(), nodeEntry.getValue().get("value").booleanValue()))),
                        Case($(entryK -> entryK.startsWith("price_campaign:")), () -> product.getPrice_campaign().add(getKeyValuesObjects(nodeEntry))),
                        Case($(entryK -> entryK.startsWith("price_regular:")), () -> product.getPrice_regular().add(getKeyValuesObjects(nodeEntry))),
                        Case($(entryK -> entryK.startsWith("price_recommended:")), () -> product.getPrice_recommended().add(getKeyValuesObjects(nodeEntry))),
                        Case($(entryK -> entryK.startsWith("calculated_purchase_price_max_rate:")), () -> product.getCalculated_purchase_price_max_rate().add(getKeyValuesObjects(nodeEntry))),
                        Case($(entryK -> entryK.startsWith("price_current:")), () -> product.getPrice_current().add(getKeyValuesObjects(nodeEntry))),
                        Case($(), new Object())
                );
            }
        });

        return product;
    }

    private Object getNodeValue(Map.Entry<String, JsonNode> nodeEntry) {

        int nodeSize = nodeEntry.getValue().size();

        if (nodeSize == 1) {

            return getSingleValueByFieldName.apply(nodeEntry,"value");

        } else {

            return getArrayObjectValues(nodeEntry);
        }
    }

    private List<Map.Entry<String, Object>> getArrayObjectValues(Map.Entry<String, JsonNode> nodeEntry) {

        final List<Map.Entry<String, Object>> objectResult = new ArrayList<>();

        nodeEntry.getValue().fields().forEachRemaining(node -> {

            String nodeKey = node.getKey();
            Object nodeValue = getSingleValue.apply(node.getValue());

            objectResult.add(new AbstractMap.SimpleEntry<>(nodeKey, nodeValue));
        });

        return objectResult;
    }

    private Map<String, Map<String, Object>> getKeyValuesObjects(Map.Entry<String, JsonNode> nodeEntry) {

        final Map<String, Map<String, Object>> result = new HashMap<>();
        final Map<String, Object> nestedValues = new HashMap<>();


        nodeEntry.getValue().fields().forEachRemaining(node -> {

            String nodeKey = node.getKey();
            Object nodeValue = getSingleValue.apply(node.getValue());

            nestedValues.put(nodeKey, nodeValue);
        });

        result.put(nodeEntry.getKey(), nestedValues);

        return result;
    }

    private BiFunction<Map.Entry<String, JsonNode>, String, Object> getSingleValueByFieldName = (nodeEntry, fieldName)->{

        JsonNode value = nodeEntry.getValue().get(fieldName);

        return getSingleValue.apply(value);
    };

    private static Function<JsonNode, Object> getSingleValue = jsonNode->{

        if (jsonNode.isTextual()) return jsonNode.textValue();
        else if (jsonNode.isDouble()) return jsonNode.doubleValue();
        else if (jsonNode.isInt()) return jsonNode.intValue();
        else if (jsonNode.isBoolean()) return jsonNode.booleanValue();
        else return null;
    };
}
