package Cinemach.cinemach.repository;

import Cinemach.cinemach.model.Solicitacao;
import Cinemach.cinemach.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SolicitacaoRepository extends JpaRepository<Solicitacao, Long> {
    List<Solicitacao> findByDestinatarioAndStatus(Usuario destinatario, String status);
    List<Solicitacao> findByRemetenteOrDestinatario(Usuario remetente, Usuario destinatario);
}
