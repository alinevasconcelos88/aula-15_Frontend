package com.carmanual.carmanual.services;
import jakarta.persistence.EntityNotFoundException;
import com.carmanual.carmanual.models.Mecanico;
import com.carmanual.carmanual.repositories.MecanicoRepository;
import com.carmanual.carmanual.repositories.PlanoRepository;
import org.springframework.stereotype.Service;
import java.util.List;
@Service
public class MecanicoService {
    private final MecanicoRepository repo; private final PlanoRepository planoRepo;
    public MecanicoService(MecanicoRepository repo, PlanoRepository planoRepo){this.repo=repo;this.planoRepo=planoRepo;}
    public List<Mecanico> listar(){return repo.findAll();}
    public Mecanico buscar(Long id){return repo.findById(id).orElseThrow(() -> naoEncontrado("Mecânico", id));}
    public Mecanico salvar(Mecanico m, Long idPlan){
        if(idPlan != null) m.setPlano(planoRepo.findById(idPlan).orElseThrow(() -> naoEncontrado("Plano", idPlan)));
        return repo.save(m);
    }
    public Mecanico atualizar(Long id,Mecanico m){buscar(id);m.setId(id);return repo.save(m);}
    public void excluir(Long id){buscar(id);repo.deleteById(id);}
    public List<Mecanico> porPlano(Long id){return repo.findByPlanoId(id);}
    private EntityNotFoundException naoEncontrado(String entidade, Long id){return new EntityNotFoundException(entidade + " não encontrado com ID: " + id);}
}
