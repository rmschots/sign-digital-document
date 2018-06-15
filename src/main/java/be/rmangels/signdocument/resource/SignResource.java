package be.rmangels.signdocument.resource;

import be.rmangels.signdocument.dto.PendingRequestDto;
import be.rmangels.signdocument.dto.SignDocumentRequestDto;
import be.rmangels.signdocument.dto.SignResponseDto;
import be.rmangels.signdocument.dto.SigningCompleteDto;
import be.rmangels.signdocument.services.SignService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.net.URI;

@RestController
public class SignResource {

    @Autowired
    private SignService signService;

    @PostMapping("/sign-document")
    public PendingRequestDto signDocument(@RequestBody @Valid SignDocumentRequestDto signDocumentRequestDto) {
        return signService.uploadDocument(signDocumentRequestDto);
    }

    @PostMapping("/sign-document-complete/{securityTokenId}")
    public ResponseEntity signDocumentComplete(@PathVariable String securityTokenId, @Valid SignResponseDto signResponseDto) {
        SigningCompleteDto signingCompleteDto = signService.completeSigning(securityTokenId, signResponseDto);
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create(signingCompleteDto.getRedirectUrl()));
        return new ResponseEntity<>(headers, HttpStatus.MOVED_PERMANENTLY);
    }
}
