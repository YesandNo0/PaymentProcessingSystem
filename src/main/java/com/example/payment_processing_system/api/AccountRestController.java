package com.example.payment_processing_system.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import com.example.payment_processing_system.domain.AccountDTO;
import com.example.payment_processing_system.service.AccountService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
@Tag(name = "Accounts", description = "APIs for managing user accounts")
public class AccountRestController {

    private final AccountService accountService;

    @PostMapping("/create")
    @Operation(summary = "Register new account", description = "Creates a new user account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Account successfully created"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "409", description = "Account already exists")
    })
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public AccountDTO createAccount(@RequestBody AccountDTO accountDTO) {
        return accountService.createAccount(accountDTO);
    }

    @GetMapping("/{email}")
    @Operation(summary = "Get account details", description = "Retrieves user account information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Account found"),
            @ApiResponse(responseCode = "404", description = "Account not found")
    })
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public AccountDTO getAccount(@Parameter(description = "User email") @PathVariable String email) {
        return accountService.getAccountByEmail(email);
    }

    @PutMapping("/{email}")
    @Operation(summary = "Update account", description = "Modifies user account information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Account updated"),
            @ApiResponse(responseCode = "401", description = "Unauthorized access"),
            @ApiResponse(responseCode = "404", description = "Account not found")
    })
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public AccountDTO updateAccount(@PathVariable String email, @RequestBody AccountDTO accountDTO) {
        return accountService.updateAccount(email, accountDTO);
    }

    @DeleteMapping("/{email}")
    @Operation(summary = "Delete account", description = "Removes a user account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Account deleted"),
            @ApiResponse(responseCode = "401", description = "Unauthorized access"),
            @ApiResponse(responseCode = "404", description = "Account not found")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAccount(@Parameter(description = "User email") @PathVariable String email) {
        accountService.deleteAccount(email);
    }
}
