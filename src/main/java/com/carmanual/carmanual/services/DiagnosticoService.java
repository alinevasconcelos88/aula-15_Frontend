package com.carmanual.carmanual.services;
import jakarta.persistence.EntityNotFoundException;
import com.carmanual.carmanual.models.*;
import com.carmanual.carmanual.repositories.*;
import org.springframework.stereotype.Service;
import java.util.List;
@Service
public class DiagnosticoService {
    private final DiagnosticoRepository repo; private final ClienteRepository clienteRepo; private final ManualRepository manualRepo;
    public DiagnosticoService(DiagnosticoRepository repo,ClienteRepository clienteRepo,ManualRepository manualRepo){this.repo=repo;this.clienteRepo=clienteRepo;this.manualRepo=manualRepo;}
    public List<Diagnostico> listar(){return repo.findAll();}
    public Diagnostico buscar(Long id){return repo.findById(id).orElseThrow(() -> naoEncontrado("Diagnóstico", id));}
    public Diagnostico salvar(Diagnostico d,Long idCliente,Long idManual){
        if(idCliente!=null)d.setCliente(clienteRepo.findById(idCliente).orElseThrow(() -> naoEncontrado("Cliente", idCliente)));
        if(idManual!=null)d.setManual(manualRepo.findById(idManual).orElseThrow(() -> naoEncontrado("Manual", idManual)));
        return repo.save(d);
    }
    public Diagnostico atualizar(Long id,Diagnostico d){buscar(id);d.setId(id);return repo.save(d);}
    public void excluir(Long id){buscar(id);repo.deleteById(id);}
    public List<Diagnostico> porCliente(Long id){return repo.findByClienteId(id);}
    public Manual manual(Long id){return buscar(id).getManual();}
    private EntityNotFoundException naoEncontrado(String entidade, Long id){return new EntityNotFoundException(entidade + " não encontrado com ID: " + id);}
}
