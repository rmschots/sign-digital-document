package be.rmangels.signdocument.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "sign_session_data")
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignSessionData {

    @Id
    private String securityTokenId;

    @NotNull
    @Column(name = "completion_url")
    private String completionUrl;

    @Lob
    @NotNull
    @Column(name = "digital_signature_service_session")
    private byte[] digitalSignatureServiceSession;
}
