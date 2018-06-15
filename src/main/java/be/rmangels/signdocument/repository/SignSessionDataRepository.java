package be.rmangels.signdocument.repository;

import be.rmangels.signdocument.domain.SignSessionData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SignSessionDataRepository extends JpaRepository<SignSessionData, String> {
}
