package com.facturacion.usuarios.api.http.controllers;

import com.facturacion.usuarios.api.facade.dto.AccesoEmpresaDto;
import com.facturacion.usuarios.api.facade.dto.DatosUsuarioDto;
import com.facturacion.usuarios.api.facade.dto.NuevoUsuarioDto;
import com.facturacion.usuarios.api.facade.dto.UsuarioDto;
import com.facturacion.usuarios.api.http.dto.AccesoEmpresaRequest;
import com.facturacion.usuarios.api.http.dto.AccesoEmpresaResponse;
import com.facturacion.usuarios.api.http.dto.AsignarEmpresaUsuarioRequest;
import com.facturacion.usuarios.api.http.dto.CrearUsuarioRequest;
import com.facturacion.usuarios.api.http.dto.ModificarUsuarioRequest;
import com.facturacion.usuarios.api.http.dto.UsuarioResponse;
import com.facturacion.usuarios.internal.application.AsignarEmpresaUsuario;
import com.facturacion.usuarios.internal.application.CrearUsuario;
import com.facturacion.usuarios.internal.application.DesactivarUsuario;
import com.facturacion.usuarios.internal.application.ListarUsuarios;
import com.facturacion.usuarios.internal.application.ModificarUsuario;
import com.facturacion.usuarios.internal.application.ObtenerUsuario;
import com.facturacion.usuarios.internal.application.RetirarEmpresaUsuario;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final CrearUsuario crearUsuario;
    private final ObtenerUsuario obtenerUsuario;
    private final ListarUsuarios listarUsuarios;
    private final ModificarUsuario modificarUsuario;
    private final DesactivarUsuario desactivarUsuario;
    private final AsignarEmpresaUsuario asignarEmpresaUsuario;
    private final RetirarEmpresaUsuario retirarEmpresaUsuario;

    public UsuarioController(
            CrearUsuario crearUsuario,
            ObtenerUsuario obtenerUsuario,
            ListarUsuarios listarUsuarios,
            ModificarUsuario modificarUsuario,
            DesactivarUsuario desactivarUsuario,
            AsignarEmpresaUsuario asignarEmpresaUsuario,
            RetirarEmpresaUsuario retirarEmpresaUsuario) {
        this.crearUsuario = crearUsuario;
        this.obtenerUsuario = obtenerUsuario;
        this.listarUsuarios = listarUsuarios;
        this.modificarUsuario = modificarUsuario;
        this.desactivarUsuario = desactivarUsuario;
        this.asignarEmpresaUsuario = asignarEmpresaUsuario;
        this.retirarEmpresaUsuario = retirarEmpresaUsuario;
    }

    @PostMapping
    public ResponseEntity<UsuarioResponse> crear(@Valid @RequestBody CrearUsuarioRequest request) {
        UsuarioDto creado = crearUsuario.ejecutar(aNuevoUsuario(request));
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(creado.id())
                .toUri();
        return ResponseEntity.created(ubicacion).body(aRespuesta(creado));
    }

    @GetMapping("/{id}")
    public UsuarioResponse obtener(@PathVariable Long id) {
        return aRespuesta(obtenerUsuario.ejecutar(id));
    }

    @GetMapping
    public List<UsuarioResponse> listar(@RequestParam(required = false) Long empresaId) {
        return listarUsuarios.ejecutar(empresaId).stream().map(UsuarioController::aRespuesta).toList();
    }

    @PutMapping("/{id}")
    public UsuarioResponse modificar(@PathVariable Long id, @Valid @RequestBody ModificarUsuarioRequest request) {
        return aRespuesta(modificarUsuario.ejecutar(id, new DatosUsuarioDto(request.nombre(), request.email())));
    }

    @PostMapping("/{id}/desactivar")
    public ResponseEntity<Void> desactivar(@PathVariable Long id) {
        desactivarUsuario.ejecutar(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/empresas/{empresaId}")
    public UsuarioResponse asignarEmpresa(
            @PathVariable Long id,
            @PathVariable Long empresaId,
            @Valid @RequestBody AsignarEmpresaUsuarioRequest request) {
        return aRespuesta(asignarEmpresaUsuario.ejecutar(id, empresaId, request.rol()));
    }

    @DeleteMapping("/{id}/empresas/{empresaId}")
    public ResponseEntity<Void> retirarEmpresa(@PathVariable Long id, @PathVariable Long empresaId) {
        retirarEmpresaUsuario.ejecutar(id, empresaId);
        return ResponseEntity.noContent().build();
    }

    private static NuevoUsuarioDto aNuevoUsuario(CrearUsuarioRequest request) {
        List<AccesoEmpresaDto> accesos = request.accesos() == null
                ? null
                : request.accesos().stream().map(UsuarioController::aAcceso).toList();
        return new NuevoUsuarioDto(request.email(), request.nombre(), request.password(), accesos);
    }

    private static AccesoEmpresaDto aAcceso(AccesoEmpresaRequest acceso) {
        return new AccesoEmpresaDto(acceso.empresaId(), acceso.rol());
    }

    private static UsuarioResponse aRespuesta(UsuarioDto usuario) {
        return new UsuarioResponse(
                usuario.id(),
                usuario.email(),
                usuario.nombre(),
                usuario.activo(),
                usuario.accesos().stream()
                        .map(acceso -> new AccesoEmpresaResponse(acceso.empresaId(), acceso.rol()))
                        .toList());
    }
}
