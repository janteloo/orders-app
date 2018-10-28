package com.geocom.orders.service.impl;

import com.geocom.orders.model.SequenceCounter;
import com.geocom.orders.repository.SequenceCounterRepository;
import com.geocom.orders.service.SequenceCounterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SequenceCounterImpl implements SequenceCounterService {

    public static final String ORDER_ID_SEQUENCE_NAME = "order_id";

    private SequenceCounterRepository sequenceCounterRepository;

    @Autowired
    public SequenceCounterImpl(SequenceCounterRepository sequenceCounterRepository) {
        this.sequenceCounterRepository = sequenceCounterRepository;
    }

    @Override
    @Transactional
    public Long getNextOrderIdSequence() {
        return increaseCounter(ORDER_ID_SEQUENCE_NAME);
    }

    private Long increaseCounter(String counterName){
        SequenceCounter sequenceCounter = sequenceCounterRepository.findByName(counterName);
        sequenceCounter.increment();
        sequenceCounterRepository.save(sequenceCounter);
        return sequenceCounter.getSequence();
    }

}
