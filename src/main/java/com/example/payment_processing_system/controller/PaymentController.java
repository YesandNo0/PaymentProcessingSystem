package com.example.payment_processing_system.controller;

import com.example.payment_processing_system.domain.PaymentRequestDTO;
import com.example.payment_processing_system.domain.PaymentResponseDTO;
import com.example.payment_processing_system.service.PaymentService;
import com.example.payment_processing_system.service.impl.InvoiceService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.io.IOException;

@Controller
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final InvoiceService invoiceService;

    @GetMapping("/payments")
    public String getPaymentForm(Model model) {
        model.addAttribute("paymentRequest", PaymentRequestDTO.builder().build());
        return "payment_form";
    }


    @PostMapping("/process")
    public String processPayment(@ModelAttribute PaymentRequestDTO paymentRequest, Model model) {
        PaymentResponseDTO response = paymentService.processPayment(paymentRequest);
        model.addAttribute("response", response);
        return "payment_result";
    }

    @GetMapping("/invoice/{transactionId}")
    public void downloadInvoice(@PathVariable String transactionId, HttpServletResponse response) throws IOException {
        PaymentResponseDTO paymentResponse = paymentService.getPaymentByTransactionId(transactionId);
        byte[] pdf = invoiceService.generateInvoice(paymentResponse);

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=invoice_" + transactionId + ".pdf");
        response.getOutputStream().write(pdf);
        response.getOutputStream().flush();
    }
}
