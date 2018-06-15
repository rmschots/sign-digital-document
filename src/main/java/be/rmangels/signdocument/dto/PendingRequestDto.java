package be.rmangels.signdocument.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PendingRequestDto {
    private String pendingRequest;
}
