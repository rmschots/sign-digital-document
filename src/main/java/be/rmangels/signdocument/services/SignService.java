package be.rmangels.signdocument.services;

import be.e_contract.dssp.client.DigitalSignatureServiceClient;
import be.e_contract.dssp.client.DigitalSignatureServiceSession;
import be.e_contract.dssp.client.PendingRequestFactory;
import be.e_contract.dssp.client.SignResponseVerifier;
import be.e_contract.dssp.client.exception.*;
import be.rmangels.signdocument.domain.SignSessionData;
import be.rmangels.signdocument.dto.PendingRequestDto;
import be.rmangels.signdocument.dto.SignDocumentRequestDto;
import be.rmangels.signdocument.dto.SignResponseDto;
import be.rmangels.signdocument.dto.SigningCompleteDto;
import be.rmangels.signdocument.mappers.DigitalSignatureServiceSessionMapper;
import be.rmangels.signdocument.repository.SignSessionDataRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.xml.security.exceptions.Base64DecodingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import javax.transaction.Transactional;
import javax.xml.bind.JAXBException;
import javax.xml.crypto.MarshalException;
import javax.xml.crypto.dsig.XMLSignatureException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;

@Service
@Slf4j
public class SignService {

    @Value("${server.base-url}:${server.port}")
    private String serverBaseUrl;

    @Autowired
    private DigitalSignatureServiceClient digitalSignatureServiceClient;

    @Autowired
    private SignSessionDataRepository signSessionDataRepository;

    @Autowired
    private DigitalSignatureServiceSessionMapper sessionMapper;

    @Transactional
    public PendingRequestDto uploadDocument(SignDocumentRequestDto requestDto) {
        log.info("signing document");
        byte[] pdfData = getPdfData();
        DigitalSignatureServiceSession session = uploadDocument(pdfData);



        String destination = String.format("%s/sign-document-complete/%s", serverBaseUrl, session.getSecurityTokenId());
        String pendingRequestString = PendingRequestFactory.createPendingRequest(session, destination, "nl");

        SignSessionData signSessionData = SignSessionData.builder()
                .securityTokenId(session.getSecurityTokenId())
                .completionUrl(requestDto.getCompletionUrl())
                .digitalSignatureServiceSession(sessionMapper.mapToBytes(session))
                .build();
        signSessionDataRepository.save(signSessionData);
        return PendingRequestDto.builder().pendingRequest(pendingRequestString).build();
    }

    public SigningCompleteDto completeSigning(String securityTokenId, SignResponseDto signResponseDto) {
        log.info("completing signing document");
        SignSessionData signSessionData = signSessionDataRepository.findById(securityTokenId)
                .orElseThrow(() -> new IllegalArgumentException(String.format("cannot find signing request for id %s", securityTokenId)));
        DigitalSignatureServiceSession session = sessionMapper.mapToSession(signSessionData.getDigitalSignatureServiceSession());
        validateSignResponse(signResponseDto, session);
        byte[] signedDocument = downloadSignedDocument(session);
        writeDocumentToFile(signedDocument);
        return SigningCompleteDto.builder()
                .redirectUrl(signSessionData.getCompletionUrl())
                .build();
    }

    private void validateSignResponse(SignResponseDto signResponseDto, DigitalSignatureServiceSession session) {
        try {
            SignResponseVerifier.checkSignResponse(signResponseDto.getSignResponse(), session);
        } catch (JAXBException | ParserConfigurationException | IOException | SAXException | XMLSignatureException
                | MarshalException | UserCancelException | Base64DecodingException
                | SubjectNotAuthorizedException | ClientRuntimeException e) {
            throw new IllegalArgumentException("Could not check sign response");
        }
    }

    private void writeDocumentToFile(byte[] signedDocument) {
        try {
            FileUtils.writeByteArrayToFile(new File("signed-document.pdf"), signedDocument);
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not write signed document to file");
        }
    }

    private byte[] downloadSignedDocument(DigitalSignatureServiceSession session) {
        try {
            return digitalSignatureServiceClient.downloadSignedDocument(session);
        } catch (UnknownDocumentException e) {
            throw new IllegalArgumentException("Could not download signed document");
        }
    }

    private DigitalSignatureServiceSession uploadDocument(byte[] pdfData) {
        try {
            return digitalSignatureServiceClient.uploadDocument("application/pdf", pdfData);
        } catch (UnsupportedDocumentTypeException | UnsupportedSignatureTypeException | AuthenticationRequiredException | IncorrectSignatureTypeException | ApplicationDocumentAuthorizedException e) {
            throw new IllegalArgumentException("could not upload document");
        }
    }

    private byte[] getPdfData() {
        try {
            return IOUtils.toByteArray(getClass().getClassLoader().getResourceAsStream("document-to-sign.pdf"));
        } catch (IOException e) {
            throw new IllegalArgumentException("could not get pdf");
        }
    }
}
