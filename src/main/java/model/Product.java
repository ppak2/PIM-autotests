package model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.Map;

@NoArgsConstructor @ToString
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class Product {

    @Getter@Setter
    private String id;

    @Getter@Setter
    private String art_no;

    @Getter@Setter@JsonProperty
    private Double priceRegular;

}
