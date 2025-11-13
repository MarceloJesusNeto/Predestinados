package Cinemach.cinemach.repository;

import Cinemach.cinemach.model.Solicitacao;
import Cinemach.cinemach.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SolicitacaoRepository extends JpaRepository<Solicitacao, Long> {

    @Query("SELECT s FROM Solicitacao s WHERE (s.remetente = :usuario OR s.destinatario = :usuario) AND s.status = 'ACEITA'")
    List<Solicitacao> findChatsAtivos(@Param("usuario") Usuario usuario);

    List<Solicitacao> findByDestinatarioAndStatus(Usuario destinatario, String status);

    List<Solicitacao> findByRemetenteOrDestinatario(Usuario remetente, Usuario destinatario);

}
