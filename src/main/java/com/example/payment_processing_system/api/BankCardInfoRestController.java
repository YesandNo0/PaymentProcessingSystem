package com.example.payment_processing_system.api;

import com.example.payment_processing_system.domain.BankCardInfoDTO;
import com.example.payment_processing_system.service.BankCardInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/bank-cards")
@RequiredArgsConstructor
@Tag(name = "Bank Cards", description = "APIs for managing bank card information")
public class BankCardInfoRestController {

    private final BankCardInfoService bankCardInfoService;

    @PostMapping("/add")
    @Operation(summary = "Register a new bank card", description = "Stores bank card information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Card successfully registered"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "409", description = "Card already exists")
    })
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public BankCardInfoDTO addBankCard(@RequestBody BankCardInfoDTO bankCardInfoDTO) {
        return bankCardInfoService.addBankCard(bankCardInfoDTO);
    }

    @GetMapping("/{cardNumber}")
    @Operation(summary = "Get bank card details", description = "Retrieves stored bank card information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Card found"),
            @ApiResponse(responseCode = "404", description = "Card not found")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public BankCardInfoDTO getBankCard(@PathVariable String cardNumber) {
        return bankCardInfoService.getBankCardByNumber(cardNumber);
    }

    @PutMapping("/{cardNumber}")
    @Operation(summary = "Update bank card details", description = "Modifies bank card information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Card updated"),
            @ApiResponse(responseCode = "401", description = "Unauthorized access"),
            @ApiResponse(responseCode = "404", description = "Card not found")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public BankCardInfoDTO updateBankCard(@PathVariable String cardNumber, @RequestBody BankCardInfoDTO bankCardInfoDTO) {
        return bankCardInfoService.updateBankCard(cardNumber, bankCardInfoDTO);
    }

    @DeleteMapping("/{cardNumber}")
    @Operation(summary = "Delete bank card", description = "Removes bank card information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Card deleted"),
            @ApiResponse(responseCode = "401", description = "Unauthorized access"),
            @ApiResponse(responseCode = "404", description = "Card not found")
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteBankCard(@PathVariable String cardNumber) {
        bankCardInfoService.deleteBankCard(cardNumber);
    }
}
