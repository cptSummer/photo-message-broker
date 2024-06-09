package org.profitsoft.photomessagebroker;

import org.springframework.boot.SpringApplication;

public class TestPhotoMessageBrokerApplication {

    public static void main(String[] args) {
        SpringApplication.from(Application::main).with(TestcontainersConfiguration.class).run(args);
    }

}
