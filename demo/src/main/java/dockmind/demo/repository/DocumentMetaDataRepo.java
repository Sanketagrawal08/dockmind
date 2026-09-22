package dockmind.demo.repository;

import dockmind.demo.entity.DocumentMetadata;
import dockmind.demo.entity.DocumentStatus;
import dockmind.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DocumentMetaDataRepo extends JpaRepository<DocumentMetadata , UUID> {

    List<DocumentMetadata> findByStatus(DocumentStatus status);

    List<DocumentMetadata> findAllByOrderByCreatedAtDesc();


    List<DocumentMetadata> findByUserOrderByCreatedAtDesc(User user);

    Optional<DocumentMetadata> findByIdAndUser(UUID id, User user);
}
