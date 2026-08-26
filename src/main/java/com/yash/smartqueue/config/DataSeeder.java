package com.yash.smartqueue.config;

import com.yash.smartqueue.model.*;
import com.yash.smartqueue.repository.CounterRepository;
import com.yash.smartqueue.repository.ServiceTypeRepository;
import com.yash.smartqueue.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {

    private final ServiceTypeRepository serviceTypeRepository;
    private final CounterRepository counterRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(ServiceTypeRepository serviceTypeRepository,
                      CounterRepository counterRepository,
                      UserRepository userRepository,
                      PasswordEncoder passwordEncoder) {
        this.serviceTypeRepository = serviceTypeRepository;
        this.counterRepository = counterRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (serviceTypeRepository.count() == 0) {
            ServiceType billing = new ServiceType();
            billing.setName("Billing");
            billing.setPrefix("B");
            billing.setAvgServiceMinutes(5.0);

            ServiceType consultation = new ServiceType();
            consultation.setName("Consultation");
            consultation.setPrefix("C");
            consultation.setAvgServiceMinutes(15.0);

            ServiceType pharmacy = new ServiceType();
            pharmacy.setName("Pharmacy");
            pharmacy.setPrefix("P");
            pharmacy.setAvgServiceMinutes(7.0);

            serviceTypeRepository.saveAll(List.of(billing, consultation, pharmacy));
            System.out.println("✅ Seeded ServiceTypes: Billing, Consultation, Pharmacy");
        }

        if (counterRepository.count() == 0) {
            ServiceType billing = serviceTypeRepository.findAll()
                    .stream()
                    .filter(s -> s.getName().equals("Billing"))
                    .findFirst()
                    .orElseThrow();

            Counter counter1 = new Counter();
            counter1.setName("Counter 1");
            counter1.setStatus(CounterStatus.ACTIVE);
            counter1.setCurrentServiceType(billing);
            counter1.getCapableServiceTypes().add(billing);

            counterRepository.save(counter1);
            System.out.println("✅ Seeded Counter 1 assigned to Billing");
        }

        if (userRepository.count() == 0) {
            User admin = new User();
            admin.setName("Yash Admin");
            admin.setPhone("9999999999");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(Role.ADMIN);
            userRepository.save(admin);
            System.out.println("✅ Seeded default admin user");
        }
    }
}