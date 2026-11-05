package com.carmanual.carmanual.services;
import jakarta.persistence.EntityNotFoundException;
import com.carmanual.carmanual.models.*;
import com.carmanual.carmanual.repositories.*;
import org.springframework.stereotype.Service;
import java.util.List;
@Service
public class AvaliacaoService {
    private final AvaliacaoRepository repo; private final ClienteRepository clienteRepo; private final MecanicoRepository mecanicoRepo;
    public AvaliacaoService(AvaliacaoRepository repo,ClienteRepository clienteRepo,MecanicoRepository mecanicoRepo){this.repo=repo;this.clienteRepo=clienteRepo;this.mecanicoRepo=mecanicoRepo;}
    public List<Avaliacao> listar(){return repo.findAll();}
    public Avaliacao buscar(Long id){return repo.findById(id).orElseThrow(() -> naoEncontrado("Avaliação", id));}
    public Avaliacao salvar(Avaliacao a,Long clienteId,Long mecanicoId){
        resolverCliente(a, clienteId);
        resolverMecanico(a, mecanicoId);
        validar(a);
        if(a.getCliente()==null) throw new IllegalArgumentException("Cliente obrigatório para a avaliação.");
        if(a.getMecanico()==null) throw new IllegalArgumentException("Mecânico obrigatório para a avaliação.");
        Avaliacao salvo=repo.save(a); recalcular(salvo.getMecanico()); return salvo;
    }
    public Avaliacao atualizar(Long id,Avaliacao a,Long clienteId,Long mecanicoId){buscar(id); a.setId(id); resolverCliente(a, clienteId); resolverMecanico(a, mecanicoId); validar(a); if(a.getCliente()==null) throw new IllegalArgumentException("Cliente obrigatório para a avaliação."); if(a.getMecanico()==null) throw new IllegalArgumentException("Mecânico obrigatório para a avaliação."); Avaliacao salvo=repo.save(a); recalcular(salvo.getMecanico()); return salvo;}
    public void excluir(Long id){Avaliacao a=buscar(id); Mecanico m=a.getMecanico(); repo.deleteById(id); recalcular(m);}
    public List<Avaliacao> porCliente(Long id){return repo.findByClienteId(id);}
    public List<Avaliacao> porMecanico(Long id){return repo.findByMecanicoId(id);}
    private void validar(Avaliacao a){if(a==null)throw new IllegalArgumentException("A avaliação não pode ser nula."); if(a.getNota()==null||a.getNota()<1||a.getNota()>5)throw new IllegalArgumentException("A nota deve estar entre 1 e 5.");}
    private void resolverCliente(Avaliacao a,Long clienteId){
        if(a.getCliente()!=null && a.getCliente().getId()!=null){
            Long id = a.getCliente().getId();
            a.setCliente(clienteRepo.findById(id).orElseThrow(() -> naoEncontrado("Cliente", id)));
            return;
        }
        if(clienteId!=null){
            a.setCliente(clienteRepo.findById(clienteId).orElseThrow(() -> naoEncontrado("Cliente", clienteId)));
        }
    }
    private void resolverMecanico(Avaliacao a,Long mecanicoId){
        if(a.getMecanico()!=null && a.getMecanico().getId()!=null){
            Long id = a.getMecanico().getId();
            a.setMecanico(mecanicoRepo.findById(id).orElseThrow(() -> naoEncontrado("Mecânico", id)));
            return;
        }
        if(mecanicoId!=null){
            a.setMecanico(mecanicoRepo.findById(mecanicoId).orElseThrow(() -> naoEncontrado("Mecânico", mecanicoId)));
        }
    }
    private void recalcular(Mecanico m){
        if(m==null)return;
        List<Avaliacao> lista=repo.findByMecanicoId(m.getId());
        double media=lista.stream().mapToInt(Avaliacao::getNota).average().orElse(0.0);
        m.setAvaliacaoMedia(media); mecanicoRepo.save(m);
    }
    private EntityNotFoundException naoEncontrado(String entidade, Long id){return new EntityNotFoundException(entidade + " não encontrado com ID: " + id);}
}
