package com.example.payment_processing_system.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.example.payment_processing_system.domain.PaymentRequestDTO;
import com.example.payment_processing_system.domain.PaymentResponseDTO;
import com.example.payment_processing_system.service.PaymentService;
import com.example.payment_processing_system.service.impl.InvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "APIs for processing payments and invoices")
public class PaymentRestController {

    private final PaymentService paymentService;
    private final InvoiceService invoiceService;

    @PostMapping("/process")
    @Operation(summary = "Process payment", description = "Handles a payment request and returns confirmation")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payment processed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid payment details")
    })
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public String processPayment(@Parameter(description = "Payment request details") @RequestBody PaymentRequestDTO paymentRequest) {
        return paymentService.processPayment(paymentRequest).toString();
    }

    @GetMapping("/invoice/{transactionId}")
    @Operation(summary = "Download invoice", description = "Generates an invoice for a completed transaction")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Invoice generated"),
            @ApiResponse(responseCode = "404", description = "Transaction not found")
    })
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<byte[]> downloadInvoice(@Parameter(description = "Transaction ID") @PathVariable String transactionId) {
        PaymentResponseDTO paymentResponse = paymentService.getPaymentByTransactionId(transactionId);
        byte[] pdf = invoiceService.generateInvoice(paymentResponse);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=invoice_" + transactionId + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
