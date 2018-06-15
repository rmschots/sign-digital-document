package be.rmangels.signdocument.mappers;

import be.e_contract.dssp.client.DigitalSignatureServiceSession;
import org.springframework.stereotype.Component;
import org.springframework.util.SerializationUtils;

@Component
public class DigitalSignatureServiceSessionMapper {
    public byte[] mapToBytes(DigitalSignatureServiceSession session) {
        return SerializationUtils.serialize(session);
    }

    public DigitalSignatureServiceSession mapToSession(byte[] data) {
        return (DigitalSignatureServiceSession) SerializationUtils.deserialize(data);
    }
}
