package com.geocom.orders;

import com.geocom.orders.model.SequenceCounter;
import com.geocom.orders.repository.SequenceCounterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@SpringBootApplication
@EnableCaching
public class OrdersApplication implements ApplicationRunner {

	@Autowired
	private SequenceCounterRepository sequenceCounterRepository;

	@Bean
	public MessageSource messageSource() {
		ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
		messageSource.setBasename("classpath:messages");
		messageSource.setDefaultEncoding("UTF-8");
		return messageSource;
	}

	@Bean
	public LocalValidatorFactoryBean validator() {
		LocalValidatorFactoryBean bean = new LocalValidatorFactoryBean();
		bean.setValidationMessageSource(messageSource());
		return bean;
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
		String sequenceName = "order_id";
		if(sequenceCounterRepository.findByName(sequenceName) == null) {
			SequenceCounter sequenceCounter = new SequenceCounter();
			sequenceCounter.setName(sequenceName);
			sequenceCounter.setSequence(0L);
			sequenceCounterRepository.save(sequenceCounter);
		}
	}

	public static void main(String[] args) {
		SpringApplication.run(OrdersApplication.class, args);
	}
}
