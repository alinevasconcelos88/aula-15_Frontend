package com.carmanual.carmanual.controllers;
import com.carmanual.carmanual.models.Plano;
import com.carmanual.carmanual.services.PlanoService;
import com.carmanual.carmanual.services.ClienteService;
import com.carmanual.carmanual.services.MecanicoService;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController @RequestMapping("/plano")
public class PlanoController {
    private final PlanoService service; private final ClienteService clienteService; private final MecanicoService mecanicoService;
    public PlanoController(PlanoService s,ClienteService c,MecanicoService m){service=s;clienteService=c;mecanicoService=m;}
    @GetMapping public List<Plano> listar(){return service.listar();}
    @GetMapping("/{id}") public Plano buscar(@PathVariable Long id){return service.buscar(id);}
    @PostMapping public Plano salvar(@RequestBody Plano p){return service.salvar(p);}
    @PutMapping("/{id}") public Plano atualizar(@PathVariable Long id,@RequestBody Plano p){return service.atualizar(id,p);}
    @DeleteMapping("/{id}") public Object excluir(@PathVariable Long id,@RequestParam(defaultValue="false") boolean dryRun){if(dryRun)return service.buscar(id); service.excluir(id); return null;}
    @GetMapping("/{id}/clientes") public Object clientes(@PathVariable Long id){return clienteService.porPlano(id);}
    @GetMapping("/{id}/mecanicos") public Object mecanicos(@PathVariable Long id){return mecanicoService.porPlano(id);}
}
