package com.geocom.orders.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "sequence_counter")
public class SequenceCounter  {

    @Id
    private String id;
    private String name;
    private long sequence;

    public void increment() {
        this.sequence++;
    }
}
