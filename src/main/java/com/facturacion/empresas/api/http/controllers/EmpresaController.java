package com.facturacion.empresas.api.http.controllers;

import com.facturacion.empresas.api.facade.dto.ContactoEmpresaDto;
import com.facturacion.empresas.api.facade.dto.DatosFiscalesEmpresaDto;
import com.facturacion.empresas.api.facade.dto.EmpresaDto;
import com.facturacion.empresas.api.facade.dto.NuevaEmpresaDto;
import com.facturacion.empresas.api.http.dto.CrearEmpresaRequest;
import com.facturacion.empresas.api.http.dto.EmpresaResponse;
import com.facturacion.empresas.api.http.dto.ModificarContactoEmpresaRequest;
import com.facturacion.empresas.api.http.dto.ModificarDatosFiscalesEmpresaRequest;
import com.facturacion.empresas.internal.application.CrearEmpresa;
import com.facturacion.empresas.internal.application.ListarEmpresas;
import com.facturacion.empresas.internal.application.ModificarContactoEmpresa;
import com.facturacion.empresas.internal.application.ModificarDatosFiscalesEmpresa;
import com.facturacion.empresas.internal.application.ObtenerEmpresa;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/empresas")
public class EmpresaController {

    private final CrearEmpresa crearEmpresa;
    private final ObtenerEmpresa obtenerEmpresa;
    private final ListarEmpresas listarEmpresas;
    private final ModificarDatosFiscalesEmpresa modificarDatosFiscalesEmpresa;
    private final ModificarContactoEmpresa modificarContactoEmpresa;

    public EmpresaController(
            CrearEmpresa crearEmpresa,
            ObtenerEmpresa obtenerEmpresa,
            ListarEmpresas listarEmpresas,
            ModificarDatosFiscalesEmpresa modificarDatosFiscalesEmpresa,
            ModificarContactoEmpresa modificarContactoEmpresa) {
        this.crearEmpresa = crearEmpresa;
        this.obtenerEmpresa = obtenerEmpresa;
        this.listarEmpresas = listarEmpresas;
        this.modificarDatosFiscalesEmpresa = modificarDatosFiscalesEmpresa;
        this.modificarContactoEmpresa = modificarContactoEmpresa;
    }

    @PostMapping
    public ResponseEntity<EmpresaResponse> crear(@Valid @RequestBody CrearEmpresaRequest request) {
        EmpresaDto creada = crearEmpresa.ejecutar(aNuevaEmpresa(request));
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(creada.id())
                .toUri();
        return ResponseEntity.created(ubicacion).body(aRespuesta(creada));
    }

    @GetMapping("/{id}")
    public EmpresaResponse obtener(@PathVariable Long id) {
        return aRespuesta(obtenerEmpresa.ejecutar(id));
    }

    @GetMapping
    public List<EmpresaResponse> listar() {
        return listarEmpresas.ejecutar().stream().map(EmpresaController::aRespuesta).toList();
    }

    @PutMapping("/{id}/datos-fiscales")
    public EmpresaResponse modificarDatosFiscales(
            @PathVariable Long id, @Valid @RequestBody ModificarDatosFiscalesEmpresaRequest request) {
        return aRespuesta(modificarDatosFiscalesEmpresa.ejecutar(id, aDatosFiscales(request)));
    }

    @PutMapping("/{id}/contacto")
    public EmpresaResponse modificarContacto(
            @PathVariable Long id, @Valid @RequestBody ModificarContactoEmpresaRequest request) {
        return aRespuesta(modificarContactoEmpresa.ejecutar(
                id, new ContactoEmpresaDto(request.email(), request.telefono())));
    }

    private static NuevaEmpresaDto aNuevaEmpresa(CrearEmpresaRequest request) {
        return new NuevaEmpresaDto(
                request.razonSocial(),
                request.nif(),
                request.direccion(),
                request.codigoPostal(),
                request.municipio(),
                request.provincia(),
                request.pais(),
                request.email(),
                request.telefono());
    }

    private static DatosFiscalesEmpresaDto aDatosFiscales(ModificarDatosFiscalesEmpresaRequest request) {
        return new DatosFiscalesEmpresaDto(
                request.razonSocial(),
                request.nif(),
                request.direccion(),
                request.codigoPostal(),
                request.municipio(),
                request.provincia(),
                request.pais());
    }

    private static EmpresaResponse aRespuesta(EmpresaDto empresa) {
        return new EmpresaResponse(
                empresa.id(),
                empresa.razonSocial(),
                empresa.nif(),
                empresa.direccion(),
                empresa.codigoPostal(),
                empresa.municipio(),
                empresa.provincia(),
                empresa.pais(),
                empresa.email(),
                empresa.telefono(),
                empresa.activa());
    }
}
