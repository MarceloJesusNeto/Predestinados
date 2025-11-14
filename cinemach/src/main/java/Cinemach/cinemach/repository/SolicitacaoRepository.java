package Cinemach.cinemach.repository;

import Cinemach.cinemach.model.Solicitacao;
import Cinemach.cinemach.model.SugestaoBloqueada;
import Cinemach.cinemach.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SolicitacaoRepository extends JpaRepository<Solicitacao, Long> {

    @Query("SELECT s FROM Solicitacao s WHERE (s.remetente = :usuario OR s.destinatario = :usuario) AND s.status = 'ACEITA'")
    List<Solicitacao> findChatsAtivos(@Param("usuario") Usuario usuario);

    List<Solicitacao> findByDestinatarioAndStatus(Usuario destinatario, String status);

    @Query("SELECT s FROM Solicitacao s WHERE ((s.remetente = :usuario1 AND s.destinatario = :usuario2) OR " +
            "(s.remetente = :usuario2 AND s.destinatario = :usuario1)) AND s.status = 'ACEITA'")
    Optional<Solicitacao> findChatAtivoEntreUsuarios(@Param("usuario1") Usuario usuario1, @Param("usuario2") Usuario usuario2);

    @Query("SELECT s FROM Solicitacao s WHERE " +
            "(s.remetente = :usuario1 AND s.destinatario = :usuario2) OR " +
            "(s.remetente = :usuario2 AND s.destinatario = :usuario1)")
    List<Solicitacao> findByRemetenteOrDestinatario(@Param("usuario1") Usuario usuario1,
                                                    @Param("usuario2") Usuario usuario2);
}
