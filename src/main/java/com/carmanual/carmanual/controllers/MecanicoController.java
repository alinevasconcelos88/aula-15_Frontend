package com.carmanual.carmanual.controllers;
import com.carmanual.carmanual.models.Mecanico;
import com.carmanual.carmanual.services.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController @RequestMapping("/mecanico")
public class MecanicoController {
    private final MecanicoService service; private final AvaliacaoService avaliacaoService;
    public MecanicoController(MecanicoService s,AvaliacaoService a){service=s;avaliacaoService=a;}
    @GetMapping public List<Mecanico> listar(){return service.listar();}
    @GetMapping("/{id}") public Mecanico buscar(@PathVariable Long id){return service.buscar(id);}
    @PostMapping public Mecanico salvar(@RequestBody Mecanico m,@RequestParam(required=false) Long idPlan){return service.salvar(m,idPlan);}
    @PutMapping("/{id}") public Mecanico atualizar(@PathVariable Long id,@RequestBody Mecanico m){return service.atualizar(id,m);}
    @DeleteMapping("/{id}") public Object excluir(@PathVariable Long id,@RequestParam(defaultValue="false") boolean dryRun){if(dryRun)return service.buscar(id); service.excluir(id); return null;}
    @GetMapping("/{id}/avaliacoes") public Object avaliacoes(@PathVariable Long id){return avaliacaoService.porMecanico(id);}
}
