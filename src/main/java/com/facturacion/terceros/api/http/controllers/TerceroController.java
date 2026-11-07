package com.facturacion.terceros.api.http.controllers;

import com.facturacion.terceros.api.facade.dto.DatosTerceroDto;
import com.facturacion.terceros.api.facade.dto.NuevoTerceroDto;
import com.facturacion.terceros.api.facade.dto.TerceroDto;
import com.facturacion.terceros.api.http.dto.CrearTerceroRequest;
import com.facturacion.terceros.api.http.dto.ModificarTerceroRequest;
import com.facturacion.terceros.api.http.dto.TerceroResponse;
import com.facturacion.terceros.internal.application.CrearTercero;
import com.facturacion.terceros.internal.application.DesactivarTercero;
import com.facturacion.terceros.internal.application.ListarTerceros;
import com.facturacion.terceros.internal.application.ModificarTercero;
import com.facturacion.terceros.internal.application.ObtenerTercero;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.ResponseEntity;
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
@RequestMapping("/api/terceros")
public class TerceroController {

    private final CrearTercero crearTercero;
    private final ObtenerTercero obtenerTercero;
    private final ListarTerceros listarTerceros;
    private final ModificarTercero modificarTercero;
    private final DesactivarTercero desactivarTercero;

    public TerceroController(
            CrearTercero crearTercero,
            ObtenerTercero obtenerTercero,
            ListarTerceros listarTerceros,
            ModificarTercero modificarTercero,
            DesactivarTercero desactivarTercero) {
        this.crearTercero = crearTercero;
        this.obtenerTercero = obtenerTercero;
        this.listarTerceros = listarTerceros;
        this.modificarTercero = modificarTercero;
        this.desactivarTercero = desactivarTercero;
    }

    @PostMapping
    public ResponseEntity<TerceroResponse> crear(@Valid @RequestBody CrearTerceroRequest request) {
        TerceroDto creado = crearTercero.ejecutar(aNuevoTercero(request));
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(creado.id())
                .toUri();
        return ResponseEntity.created(ubicacion).body(aRespuesta(creado));
    }

    @GetMapping("/{id}")
    public TerceroResponse obtener(@PathVariable Long id) {
        return aRespuesta(obtenerTercero.ejecutar(id));
    }

    @GetMapping
    public List<TerceroResponse> listar(
            @RequestParam Long empresaId,
            @RequestParam(required = false) @Pattern(regexp = "CLIENTE|PROVEEDOR") String tipo) {
        return listarTerceros.ejecutar(empresaId, tipo).stream().map(TerceroController::aRespuesta).toList();
    }

    @PutMapping("/{id}")
    public TerceroResponse modificar(@PathVariable Long id, @Valid @RequestBody ModificarTerceroRequest request) {
        return aRespuesta(modificarTercero.ejecutar(id, aDatos(request)));
    }

    @PostMapping("/{id}/desactivar")
    public ResponseEntity<Void> desactivar(@PathVariable Long id) {
        desactivarTercero.ejecutar(id);
        return ResponseEntity.noContent().build();
    }

    private static NuevoTerceroDto aNuevoTercero(CrearTerceroRequest request) {
        return new NuevoTerceroDto(
                request.empresaId(),
                request.nif(),
                request.razonSocial(),
                request.direccion(),
                request.codigoPostal(),
                request.municipio(),
                request.provincia(),
                request.pais(),
                request.email(),
                request.telefono(),
                request.esCliente(),
                request.esProveedor());
    }

    private static DatosTerceroDto aDatos(ModificarTerceroRequest request) {
        return new DatosTerceroDto(
                request.nif(),
                request.razonSocial(),
                request.direccion(),
                request.codigoPostal(),
                request.municipio(),
                request.provincia(),
                request.pais(),
                request.email(),
                request.telefono(),
                request.esCliente(),
                request.esProveedor());
    }

    private static TerceroResponse aRespuesta(TerceroDto tercero) {
        return new TerceroResponse(
                tercero.id(),
                tercero.empresaId(),
                tercero.nif(),
                tercero.razonSocial(),
                tercero.direccion(),
                tercero.codigoPostal(),
                tercero.municipio(),
                tercero.provincia(),
                tercero.pais(),
                tercero.email(),
                tercero.telefono(),
                tercero.esCliente(),
                tercero.esProveedor(),
                tercero.activo());
    }
}
