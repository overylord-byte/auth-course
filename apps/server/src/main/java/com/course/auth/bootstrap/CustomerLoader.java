package com.course.auth.bootstrap;

import com.course.auth.domain.Customer;
import com.course.auth.repositories.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class CustomerLoader implements CommandLineRunner {
    private final CustomerRepository customerRepository;

    @Override
    public void run(String... args) throws Exception {
        loadCustomers();
    }

    private synchronized void loadCustomers() {
        log.info("Loading initial data (customers). Current count: " + customerRepository.count());

        if (customerRepository.count() == 0) {

            customerRepository.save(
                    Customer.builder()
                            .name("John")
                            .build()
            );

            customerRepository.save(
                    Customer.builder()
                            .name("Karen")
                            .build()
            );

            customerRepository.save(
                    Customer.builder()
                            .name("Donald")
                            .build()
            );

            customerRepository.save(
                    Customer.builder()
                            .name("Anna")
                            .build()
            );

            customerRepository.save(
                    Customer.builder()
                            .name("Martha")
                            .build()
            );

            customerRepository.save(
                    Customer.builder()
                            .name("Michael")
                            .build()
            );

            customerRepository.save(
                    Customer.builder()
                            .name("Sarah")
                            .build()
            );

            customerRepository.save(
                    Customer.builder()
                            .name("David")
                            .build()
            );

            customerRepository.save(
                    Customer.builder()
                            .name("Emily")
                            .build()
            );

            customerRepository.save(
                    Customer.builder()
                            .name("Daniel")
                            .build()
            );

            customerRepository.save(
                    Customer.builder()
                            .name("Jessica")
                            .build()
            );

            customerRepository.save(
                    Customer.builder()
                            .name("Matthew")
                            .build()
            );

            customerRepository.save(
                    Customer.builder()
                            .name("Ashley")
                            .build()
            );

            customerRepository.save(
                    Customer.builder()
                            .name("Christopher")
                            .build()
            );

            customerRepository.save(
                    Customer.builder()
                            .name("Amanda")
                            .build()
            );

            customerRepository.save(
                    Customer.builder()
                            .name("Joshua")
                            .build()
            );

            customerRepository.save(
                    Customer.builder()
                            .name("Melissa")
                            .build()
            );

            customerRepository.save(
                    Customer.builder()
                            .name("Andrew")
                            .build()
            );

            customerRepository.save(
                    Customer.builder()
                            .name("Stephanie")
                            .build()
            );

            customerRepository.save(
                    Customer.builder()
                            .name("Joseph")
                            .build()
            );

            customerRepository.save(
                    Customer.builder()
                            .name("Nicole")
                            .build()
            );

            customerRepository.save(
                    Customer.builder()
                            .name("Ryan")
                            .build()
            );

            customerRepository.save(
                    Customer.builder()
                            .name("Samantha")
                            .build()
            );

            customerRepository.save(
                    Customer.builder()
                            .name("Brandon")
                            .build()
            );

            customerRepository.save(
                    Customer.builder()
                            .name("Olivia")
                            .build()
            );

            log.info("Customer list has been loaded. Total count: " + customerRepository.count());
        }
    }
}
