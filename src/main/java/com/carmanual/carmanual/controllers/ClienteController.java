package com.carmanual.carmanual.controllers;
import com.carmanual.carmanual.models.Cliente;
import com.carmanual.carmanual.services.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController @RequestMapping("/cliente")
public class ClienteController {
    private final ClienteService service; private final DiagnosticoService diagnosticoService; private final AvaliacaoService avaliacaoService;
    public ClienteController(ClienteService s,DiagnosticoService d,AvaliacaoService a){service=s;diagnosticoService=d;avaliacaoService=a;}
    @GetMapping public List<Cliente> listar(){return service.listar();}
    @GetMapping("/{id}") public Cliente buscar(@PathVariable Long id){return service.buscar(id);}
    @PostMapping public Cliente salvar(@RequestBody Cliente c,@RequestParam(required=false) Long idPlan){return service.salvar(c,idPlan);}
    @PutMapping("/{id}") public Cliente atualizar(@PathVariable Long id,@RequestBody Cliente c){return service.atualizar(id,c);}
    @DeleteMapping("/{id}") public Object excluir(@PathVariable Long id,@RequestParam(defaultValue="false") boolean dryRun){if(dryRun)return service.buscar(id); service.excluir(id); return null;}
    @GetMapping("/{id}/diagnosticos") public Object diagnosticos(@PathVariable Long id){return diagnosticoService.porCliente(id);}
    @GetMapping("/{id}/avaliacoes") public Object avaliacoes(@PathVariable Long id){return avaliacaoService.porCliente(id);}
}
