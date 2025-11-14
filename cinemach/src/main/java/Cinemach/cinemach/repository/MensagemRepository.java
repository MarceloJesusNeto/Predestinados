package Cinemach.cinemach.repository;

import Cinemach.cinemach.model.Mensagem;
import Cinemach.cinemach.model.Solicitacao;
import Cinemach.cinemach.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MensagemRepository extends JpaRepository<Mensagem, Long> {

    List<Mensagem> findBySolicitacaoOrderByDataEnvioAsc(Solicitacao solicitacao);

    @Query("SELECT m FROM Mensagem m WHERE m.solicitacao = :solicitacao ORDER BY m.dataEnvio ASC")
    List<Mensagem> findMensagensPorSolicitacao(@Param("solicitacao") Solicitacao solicitacao);

    List<Mensagem> findByDestinatarioAndLidaFalse(Usuario destinatario);

    // Buscar última mensagem de cada chat
    @Query("SELECT m FROM Mensagem m WHERE m.id IN (" +
            "SELECT MAX(m2.id) FROM Mensagem m2 WHERE m2.solicitacao IN " +
            "(SELECT s FROM Solicitacao s WHERE (s.remetente = :usuario OR s.destinatario = :usuario) " +
            "AND s.status = 'ACEITA') GROUP BY m2.solicitacao)")
    List<Mensagem> findUltimasMensagensPorUsuario(@Param("usuario") Usuario usuario);
}
