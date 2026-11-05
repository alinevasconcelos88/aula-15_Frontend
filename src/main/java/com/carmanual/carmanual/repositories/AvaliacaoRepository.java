package com.carmanual.carmanual.repositories;
import com.carmanual.carmanual.models.Avaliacao;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface AvaliacaoRepository extends JpaRepository<Avaliacao, Long> {
    List<Avaliacao> findByClienteId(Long clienteId);
    List<Avaliacao> findByMecanicoId(Long mecanicoId);
}
