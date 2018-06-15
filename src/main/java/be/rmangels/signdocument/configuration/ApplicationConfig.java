package be.rmangels.signdocument.configuration;

import be.e_contract.dssp.client.DigitalSignatureServiceClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfig {

    @Bean
    public DigitalSignatureServiceClient digitalSignatureServiceClient() {
        return new DigitalSignatureServiceClient("https://www.e-contract.be/dss-ws/dss");
    }
}
