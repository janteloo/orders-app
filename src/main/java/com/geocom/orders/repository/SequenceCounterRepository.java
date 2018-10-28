package com.geocom.orders.repository;

import com.geocom.orders.model.SequenceCounter;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SequenceCounterRepository extends MongoRepository<SequenceCounter, String> {

    SequenceCounter findByName(String name);
}
