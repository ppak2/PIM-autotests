package model;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import model.interfaces.FieldNameKey;
import model.interfaces.IRequestObject;
import model.interfaces.IgnoreField;
import util.ProductDataSerializer;
import util.ProductDataDeserializer;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@NoArgsConstructor
@ToString
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonDeserialize(using = ProductDataDeserializer.class)
@JsonSerialize(using = ProductDataSerializer.class)
public class ProductData implements IRequestObject {

    @Getter
    @Setter
    @IgnoreField
    public String id;

    @Getter
    @Setter
    @IgnoreField
    public Long createdAt;

    @Getter
    @Setter
    @IgnoreField
    public Long updatedAt;

    @Getter
    @Setter
    public String artno;

    @Getter
    @Setter
    public List<Map.Entry<String, String>> title = new ArrayList<>();

    @Getter
    @Setter
    public List<Map.Entry<String, String>> type = new ArrayList<>();

    @Getter
    @Setter
    public List<Map.Entry<String, String>> description = new ArrayList<>();

    @Getter
    @Setter
    public List<Map.Entry<String, String>> description_short = new ArrayList<>();

    @Getter
    @Setter
    public Integer pid;

    @Getter
    @Setter
    public Boolean virtual;

    @Getter
    @Setter
    public Integer base_product_pid;

    @Getter
    @Setter
    public String artno_manufacturers;

    @Getter
    @Setter
    public String artno_manufacturers2;

    @Getter
    @Setter
    public String ean_13;

    @Getter
    @Setter
    public String intrastat_code;

    @Getter
    @Setter
    public String purchase_planning_code;

    @Getter
    @Setter
    public String discontinued_code;

    @Getter
    @Setter
    public String manufacturer_id;

    @Getter
    @Setter
    public String manufacturer_name;

    @Getter
    @Setter
    public String navision_product_group_code;

    @Getter
    @Setter
    public String freight_package_type;

    @Getter
    @Setter
    public List<Map.Entry<String, Boolean>> forbidden = new ArrayList<>();

    @Getter
    @Setter
    public Integer stock;

    @Getter
    @Setter
    public Integer stock_vendor;

    @Getter
    @Setter
    @FieldNameKey
    public List<Map.Entry<String, Object>> calculated_purchase_price_max = new ArrayList<>();

    @Getter
    @Setter
    public List<Map<String, Map<String, Object>>> calculated_purchase_price_max_rate = new ArrayList<>();

    @Getter
    @Setter
    public List<Map<String, Map<String, Object>>> price_current = new ArrayList<>();

    @Getter
    @Setter
    public List<Map<String, Map<String, Object>>> price_campaign = new ArrayList<>();

    @Getter
    @Setter
    public List<Map<String, Map<String, Object>>> price_regular = new ArrayList<>();

    @Getter
    @Setter
    public List<Map<String, Map<String, Object>>> price_recommended = new ArrayList<>();

    @Getter
    @Setter
    @FieldNameKey
    public List<Map.Entry<String, Object>> price_manufacturers;

    @Getter
    @Setter
    public String description_manufacturers;

    @Getter
    @Setter
    public Integer quantity_min;

    @Getter
    @Setter
    public Integer quantity_step;

    @Getter
    @Setter
    public Integer quantity_min_manufacturers_stock;

    @Getter
    @Setter
    public Integer leadtime_days_ondemand;

    @Getter
    @Setter
    public Integer leadtime_days_forecast;

    @Getter
    @Setter
    public Integer leadtime_days_internal;

    @Getter
    @Setter
    public Integer leadtime_days_production;

    @Getter
    @Setter
    public Integer leadtime_days_transportation;

    @Getter
    @Setter
    public Integer leadtime_days_discrepancy;

    @Getter
    @Setter
    public String country_of_origin;

    @Getter
    @Setter
    public String default_warehouse_code;

    @Getter
    @Setter
    public String factory_manufacturers;

    @Getter
    @Setter
    public Boolean is_monitoring_allowed;

    @Getter
    @Setter
    public Boolean only_as_component;

    @Getter
    @Setter
    public Object[] images;

    @Getter
    @Setter
    public Object[] videos;


    @Override
    public LinkedList<String> getFieldNames() {

        return Stream.of(this.getClass().getDeclaredFields()).map(Field::getName)
                .collect(Collectors.toCollection(LinkedList::new));
    }

    @Override
    public LinkedList<Field> getNotEmptyFields() {

        return Stream.of(this.getClass().getDeclaredFields()).filter(field -> {
            try {

                Object o = field.get(this);
                if(o instanceof List){
                    List l = (List)o;
                    return l.size() != 0;
                }
                else if(o instanceof String){
                    String s = (String) o;
                    return !s.isEmpty();
                }
                else if(o instanceof Array){
                    Object[] arr = (Object[])o;
                    return arr.length != 0;
                }
                else return o != null;

            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toCollection(LinkedList::new));
    }
}
