package com.example.payment_processing_system.service.impl;

import com.example.payment_processing_system.domain.PaymentResponseDTO;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
@RequiredArgsConstructor
public class InvoiceService {

    public byte[] generateInvoice(PaymentResponseDTO paymentResponse) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, outputStream);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Font textFont = FontFactory.getFont(FontFactory.HELVETICA, 12);

            Paragraph title = new Paragraph("Payment Invoice", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            document.add(new Paragraph("Transaction ID: " + paymentResponse.getTransactionId(), textFont));
            document.add(new Paragraph("Status: " + paymentResponse.getStatus(), textFont));
            document.add(new Paragraph("Amount: " + paymentResponse.getAmount(), textFont));
            document.add(new Paragraph("Thank you for your payment!", textFont));

            document.close();
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate invoice PDF", e);
        }
    }
}
