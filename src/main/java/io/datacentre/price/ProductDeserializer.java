package io.datacentre.price;

import io.quarkus.kafka.client.serialization.JsonbDeserializer;

public class ProductDeserializer extends JsonbDeserializer {

    public ProductDeserializer() {
        super(Product.class);
    }

    
    
}
