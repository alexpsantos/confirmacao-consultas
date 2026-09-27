package br.com.confirmacao.whatsapp.infrastructure;
import br.com.confirmacao.whatsapp.domain.WhatsAppMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface WhatsAppMessageRepository extends JpaRepository<WhatsAppMessage, UUID> {
    Optional<WhatsAppMessage> findByIdempotencyKey(String key);
    Optional<WhatsAppMessage> findByMetaMessageId(String metaMessageId);
}
