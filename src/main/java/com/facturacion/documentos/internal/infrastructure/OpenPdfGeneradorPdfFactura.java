package com.facturacion.documentos.internal.infrastructure;

import com.facturacion.documentos.internal.domain.model.FacturaImprimible;
import com.facturacion.documentos.internal.domain.model.Interviniente;
import com.facturacion.documentos.internal.domain.model.LineaImprimible;
import com.facturacion.documentos.internal.domain.service.GeneradorPdfFactura;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;

@Component
class OpenPdfGeneradorPdfFactura implements GeneradorPdfFactura {

    private static final Font TITULO = new Font(Font.HELVETICA, 16, Font.BOLD);
    private static final Font SECCION = new Font(Font.HELVETICA, 11, Font.BOLD);
    private static final Font NORMAL = new Font(Font.HELVETICA, 10, Font.NORMAL);
    private static final String SIN_VALOR = "-";

    @Override
    public byte[] generar(FacturaImprimible factura) {
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        Document documento = new Document(PageSize.A4);
        PdfWriter.getInstance(documento, salida);
        documento.open();
        documento.add(new Paragraph("Factura " + texto(factura.numero()), TITULO));
        documento.add(new Paragraph("Fecha: " + texto(factura.fecha()), NORMAL));
        documento.add(new Paragraph("Vencimiento: " + texto(factura.fechaVencimiento()), NORMAL));
        agregarInterviniente(documento, "Emisor", factura.emisor());
        agregarInterviniente(documento, "Receptor", factura.receptor());
        documento.add(lineas(factura));
        documento.add(new Paragraph("Base imponible: " + texto(factura.base()), NORMAL));
        documento.add(new Paragraph(
                "IVA (" + texto(factura.porcentajeIva()) + " %): " + texto(factura.cuotaIva()), NORMAL));
        documento.add(new Paragraph("Total: " + texto(factura.total()), SECCION));
        documento.close();
        return salida.toByteArray();
    }

    private static void agregarInterviniente(Document documento, String rol, Interviniente interviniente) {
        Paragraph titulo = new Paragraph(rol, SECCION);
        titulo.setSpacingBefore(10);
        documento.add(titulo);
        documento.add(new Paragraph(texto(interviniente.nombre()), NORMAL));
        documento.add(new Paragraph("NIF: " + texto(interviniente.nif()), NORMAL));
        documento.add(new Paragraph("Dirección: " + texto(interviniente.direccion()), NORMAL));
    }

    private static PdfPTable lineas(FacturaImprimible factura) {
        PdfPTable tabla = new PdfPTable(new float[] {5, 1.5f, 2, 2});
        tabla.setWidthPercentage(100);
        tabla.setSpacingBefore(15);
        tabla.setSpacingAfter(15);
        tabla.setHeaderRows(1);
        for (String cabecera : new String[] {"Concepto", "Cantidad", "Precio unitario", "Importe"}) {
            tabla.addCell(celda(cabecera, SECCION, Element.ALIGN_LEFT));
        }
        for (LineaImprimible linea : factura.lineas()) {
            tabla.addCell(celda(texto(linea.concepto()), NORMAL, Element.ALIGN_LEFT));
            tabla.addCell(celda(texto(linea.cantidad()), NORMAL, Element.ALIGN_RIGHT));
            tabla.addCell(celda(texto(linea.precioUnitario()), NORMAL, Element.ALIGN_RIGHT));
            tabla.addCell(celda(texto(linea.importe()), NORMAL, Element.ALIGN_RIGHT));
        }
        return tabla;
    }

    private static PdfPCell celda(String contenido, Font fuente, int alineacion) {
        PdfPCell celda = new PdfPCell(new Phrase(contenido, fuente));
        celda.setHorizontalAlignment(alineacion);
        return celda;
    }

    private static String texto(Object valor) {
        if (valor == null) {
            return SIN_VALOR;
        }
        if (valor instanceof BigDecimal importe) {
            return importe.toPlainString();
        }
        return valor.toString();
    }
}
