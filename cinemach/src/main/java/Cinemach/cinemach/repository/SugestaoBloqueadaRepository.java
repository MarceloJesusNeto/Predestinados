package Cinemach.cinemach.repository;

import Cinemach.cinemach.model.SugestaoBloqueada;
import Cinemach.cinemach.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface SugestaoBloqueadaRepository extends JpaRepository<SugestaoBloqueada, Long> {
    List<SugestaoBloqueada> findByUsuario(Usuario usuario);
    boolean existsByUsuarioAndBloqueado(Usuario usuario, Usuario bloqueado);

    Optional<SugestaoBloqueada> findByUsuarioAndBloqueado(Usuario usuario, Usuario bloqueado);
}
