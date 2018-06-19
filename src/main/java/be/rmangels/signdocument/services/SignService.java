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

    private String xmlData =
            "<document xmlns=\"urn:be:dav:birth\">\n" +
                    "    <certificateOrigin>NEW BELGIAN CERTIFICATE</certificateOrigin>\n" +
                    "    <establishingDate>2017-11-09+01:00</establishingDate>\n" +
                    "    <redactionPlace>\n" +
                    "        <placeCode>01237</placeCode>\n" +
                    "        <countryCode>109</countryCode>\n" +
                    "    </redactionPlace>\n" +
                    "    <origin>DABS</origin>\n" +
                    "    <persons>\n" +
                    "        <person>\n" +
                    "            <personNumber/>\n" +
                    "            <nobility/>\n" +
                    "            <nobilityFree/>\n" +
                    "            <birthPlace>\n" +
                    "                <municipalityCode/>\n" +
                    "                <municipalityFreeText>Barcelona</municipalityFreeText>\n" +
                    "                <countryCode>109</countryCode>\n" +
                    "                <countryFreeText/>\n" +
                    "            </birthPlace>\n" +
                    "            <birthDate>2017-11-01</birthDate>\n" +
                    "            <nationalityCode>150</nationalityCode>\n" +
                    "            <nationalityFree/>\n" +
                    "            <firstname>Jan</firstname>\n" +
                    "            <lastname>Test</lastname>\n" +
                    "            <roles>\n" +
                    "                <role>CHILD</role>\n" +
                    "            </roles>\n" +
                    "        </person>\n" +
                    "        <person>\n" +
                    "            <personNumber>93091731824</personNumber>\n" +
                    "            <origin>RR</origin>\n" +
                    "            <nobility/>\n" +
                    "            <nobilityFree/>\n" +
                    "            <birthPlace>\n" +
                    "                <municipalityCode>12007</municipalityCode>\n" +
                    "                <municipalityFreeText/>\n" +
                    "                <countryCode>150</countryCode>\n" +
                    "                <countryFreeText/>\n" +
                    "            </birthPlace>\n" +
                    "            <birthDate>1993-09-17</birthDate>\n" +
                    "            <nationalityFree/>\n" +
                    "            <firstname>Tine Ria Henrik</firstname>\n" +
                    "            <lastname>Lybeert</lastname>\n" +
                    "            <roles>\n" +
                    "                <role>ABS</role>\n" +
                    "            </roles>\n" +
                    "        </person>\n" +
                    "        <person>\n" +
                    "            <personNumber>82060401577</personNumber>\n" +
                    "            <origin>RR</origin>\n" +
                    "            <nobility/>\n" +
                    "            <nobilityFree/>\n" +
                    "            <birthPlace>\n" +
                    "                <municipalityCode>41018</municipalityCode>\n" +
                    "                <municipalityFreeText/>\n" +
                    "                <countryCode>150</countryCode>\n" +
                    "                <countryFreeText/>\n" +
                    "            </birthPlace>\n" +
                    "            <birthDate>1982-06-04</birthDate>\n" +
                    "            <nationalityFree/>\n" +
                    "            <firstname>Filip Roger Annie</firstname>\n" +
                    "            <lastname>L'Ecluse</lastname>\n" +
                    "            <roles>\n" +
                    "                <role>FATHER</role>\n" +
                    "            </roles>\n" +
                    "        </person>\n" +
                    "        <person>\n" +
                    "            <personNumber>83110727475</personNumber>\n" +
                    "            <origin>RR</origin>\n" +
                    "            <nobility/>\n" +
                    "            <nobilityFree/>\n" +
                    "            <birthPlace>\n" +
                    "                <municipalityCode>24062</municipalityCode>\n" +
                    "                <municipalityFreeText/>\n" +
                    "                <countryCode>150</countryCode>\n" +
                    "                <countryFreeText/>\n" +
                    "            </birthPlace>\n" +
                    "            <birthDate>1983-11-07</birthDate>\n" +
                    "            <nationalityFree/>\n" +
                    "            <firstname>Anneleen Hilde Marie Edgard</firstname>\n" +
                    "            <lastname>Van Steen</lastname>\n" +
                    "            <roles>\n" +
                    "                <role>MOTHER</role>\n" +
                    "            </roles>\n" +
                    "        </person>\n" +
                    "    </persons>\n" +
                    "    <oldDocumentReference/>\n" +
                    "    <docState>FINAL</docState>\n" +
                    "    <signature/>\n" +
                    "    <language>nl</language>\n" +
                    "    <birthTime>10:10:00</birthTime>\n" +
                    "    <nameDeclaration>\n" +
                    "        <name>Test</name>\n" +
                    "        <name1/>\n" +
                    "        <name2/>\n" +
                    "    </nameDeclaration>\n" +
                    "    <nameDeclarationMadeByTheParents>false</nameDeclarationMadeByTheParents>\n" +
                    "    <gender>MALE</gender>\n" +
                    "</document>";

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
//        byte[] pdfData = getPdfData();
        byte[] xmlData = getXmlData();
//        DigitalSignatureServiceSession session = uploadDocument(pdfData, "application/pdf");
        DigitalSignatureServiceSession session = uploadDocument(xmlData, "text/xml");


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

    private DigitalSignatureServiceSession uploadDocument(byte[] pdfData, String mimeType) {
        try {
            return digitalSignatureServiceClient.uploadDocument(mimeType, pdfData);
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

    private byte[] getXmlData() {
//        try {
//            return IOUtils.toByteArray(getClass().getClassLoader().getResourceAsStream("xml-to-sign.pdf"));
        return xmlData.getBytes();
//        } catch (IOException e) {
//            throw new IllegalArgumentException("could not get pdf");
//        }
    }
}
