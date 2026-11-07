package com.facturacion.usuarios.internal.domain.model;

import com.facturacion.usuarios.internal.domain.exception.AccesoEmpresaNoEncontradoException;
import com.facturacion.usuarios.internal.domain.exception.UsuarioInvalidoException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

public record Usuario(
        Long id,
        String email,
        String nombre,
        String passwordHash,
        boolean activo,
        List<AccesoEmpresa> accesos) {

    public Usuario {
        exigir(email, "El email del usuario es obligatorio");
        exigir(nombre, "El nombre del usuario es obligatorio");
        exigir(passwordHash, "La contraseña cifrada del usuario es obligatoria");
        email = normalizarEmail(email);
        accesos = copiarAccesos(accesos);
    }

    public static Usuario nuevo(String email, String nombre, String passwordHash, List<AccesoEmpresa> accesos) {
        return new Usuario(null, email, nombre, passwordHash, true, accesos);
    }

    public static String normalizarEmail(String email) {
        return email == null ? null : email.toLowerCase(Locale.ROOT);
    }

    public Usuario conDatos(String nuevoNombre, String nuevoEmail) {
        return new Usuario(id, nuevoEmail, nuevoNombre, passwordHash, activo, accesos);
    }

    public Usuario conPasswordHash(String nuevoPasswordHash) {
        return new Usuario(id, email, nombre, nuevoPasswordHash, activo, accesos);
    }

    public Usuario desactivado() {
        return new Usuario(id, email, nombre, passwordHash, false, accesos);
    }

    public Usuario conAcceso(AccesoEmpresa acceso) {
        if (acceso == null) {
            throw new UsuarioInvalidoException("El acceso a la empresa es obligatorio");
        }
        List<AccesoEmpresa> nuevos = new ArrayList<>(accesos);
        int posicion = posicionDelAccesoA(acceso.empresaId());
        if (posicion < 0) {
            nuevos.add(acceso);
        } else {
            nuevos.set(posicion, acceso);
        }
        return new Usuario(id, email, nombre, passwordHash, activo, nuevos);
    }

    public Usuario sinAccesoA(Long empresaId) {
        int posicion = posicionDelAccesoA(empresaId);
        if (posicion < 0) {
            throw new AccesoEmpresaNoEncontradoException(id, empresaId);
        }
        List<AccesoEmpresa> restantes = new ArrayList<>(accesos);
        restantes.remove(posicion);
        return new Usuario(id, email, nombre, passwordHash, activo, restantes);
    }

    @Override
    public String toString() {
        return "Usuario[id=" + id + ", email=" + email + ", nombre=" + nombre
                + ", activo=" + activo + ", accesos=" + accesos + "]";
    }

    private int posicionDelAccesoA(Long empresaId) {
        for (int posicion = 0; posicion < accesos.size(); posicion++) {
            if (accesos.get(posicion).empresaId().equals(empresaId)) {
                return posicion;
            }
        }
        return -1;
    }

    private static List<AccesoEmpresa> copiarAccesos(List<AccesoEmpresa> accesos) {
        if (accesos == null) {
            throw new UsuarioInvalidoException("Los accesos del usuario son obligatorios");
        }
        if (accesos.stream().anyMatch(Objects::isNull)) {
            throw new UsuarioInvalidoException("Los accesos del usuario no pueden ser nulos");
        }
        Set<Long> empresas = new HashSet<>();
        if (!accesos.stream().allMatch(acceso -> empresas.add(acceso.empresaId()))) {
            throw new UsuarioInvalidoException("El usuario solo puede tener un acceso por empresa");
        }
        return List.copyOf(accesos);
    }

    private static void exigir(String valor, String mensaje) {
        if (valor == null || valor.isBlank()) {
            throw new UsuarioInvalidoException(mensaje);
        }
    }
}
