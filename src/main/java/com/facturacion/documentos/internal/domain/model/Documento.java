package com.facturacion.documentos.internal.domain.model;

import com.facturacion.documentos.internal.domain.exception.DocumentoInvalidoException;

import java.time.Instant;

public record Documento(
        Long id,
        EntidadTipo entidadTipo,
        Long entidadId,
        String nombre,
        TipoContenido tipoContenido,
        long tamano,
        OrigenDocumento origen,
        Instant creadoEn) {

    public static final long TAMANO_MAXIMO = 10L * 1024 * 1024;
    private static final int LONGITUD_MAXIMA_NOMBRE = 255;

    public Documento {
        if (entidadTipo == null) {
            throw new DocumentoInvalidoException("El tipo de entidad del documento es obligatorio");
        }
        if (entidadId == null) {
            throw new DocumentoInvalidoException("La entidad del documento es obligatoria");
        }
        validarNombre(nombre);
        if (tipoContenido == null) {
            throw new DocumentoInvalidoException("El tipo de contenido del documento es obligatorio");
        }
        if (tamano <= 0) {
            throw new DocumentoInvalidoException("El documento no puede estar vacío");
        }
        if (tamano > TAMANO_MAXIMO) {
            throw new DocumentoInvalidoException("El documento no puede superar 10 MB");
        }
        if (origen == null) {
            throw new DocumentoInvalidoException("El origen del documento es obligatorio");
        }
        if (creadoEn == null) {
            throw new DocumentoInvalidoException("La fecha de creación del documento es obligatoria");
        }
    }

    public static Documento nuevo(
            EntidadTipo entidadTipo,
            Long entidadId,
            String nombre,
            TipoContenido tipoContenido,
            long tamano,
            OrigenDocumento origen,
            Instant creadoEn) {
        return new Documento(null, entidadTipo, entidadId, nombre, tipoContenido, tamano, origen, creadoEn);
    }

    public static String nombreDeArchivoAdjunto(String nombreOriginal) {
        if (nombreOriginal == null) {
            return null;
        }
        int ultimoSeparador = Math.max(nombreOriginal.lastIndexOf('/'), nombreOriginal.lastIndexOf('\\'));
        return sinCaracteresNoPermitidos(nombreOriginal.substring(ultimoSeparador + 1));
    }

    public static String nombreDePdfDeFactura(String numeroFactura) {
        return sinCaracteresNoPermitidos("factura-" + numeroFactura + ".pdf");
    }

    private static String sinCaracteresNoPermitidos(String nombre) {
        StringBuilder saneado = new StringBuilder();
        nombre.codePoints()
                .filter(caracter -> !esNoPermitido(caracter))
                .forEach(saneado::appendCodePoint);
        return saneado.toString().strip();
    }

    private static void validarNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new DocumentoInvalidoException("El nombre del documento es obligatorio");
        }
        if (nombre.length() > LONGITUD_MAXIMA_NOMBRE) {
            throw new DocumentoInvalidoException(
                    "El nombre del documento no puede superar " + LONGITUD_MAXIMA_NOMBRE + " caracteres");
        }
        if (nombre.codePoints().anyMatch(Documento::esNoPermitido)) {
            throw new DocumentoInvalidoException(
                    "El nombre del documento no puede contener caracteres de control ni comillas");
        }
    }

    private static boolean esNoPermitido(int caracter) {
        return Character.isISOControl(caracter) || caracter == '"';
    }
}
