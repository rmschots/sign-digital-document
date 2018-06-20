package be.rmangels.signdocument.configuration;

import be.e_contract.dssp.client.DigitalSignatureServiceClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfig {

    @Bean
    public DigitalSignatureServiceClient digitalSignatureServiceClient() {
        DigitalSignatureServiceClient client = new DigitalSignatureServiceClient("https://doccle.e-contract.be/dss-ws/dss");
        client.resetCredentials();
        client.setCredentials("dav","60c4e856");
        return client;
    }
}
