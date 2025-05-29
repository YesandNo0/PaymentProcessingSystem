package com.example.payment_processing_system.controller;

import com.example.payment_processing_system.domain.AccountDTO;
import com.example.payment_processing_system.domain.auth.RegisterRequest;
import com.example.payment_processing_system.service.AccountService;
import com.example.payment_processing_system.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class RegisterController {

    private final AuthService authService;

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("request", new RegisterRequest());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute RegisterRequest request) {
        authService.registerAccount(request);
        return "redirect:/login";
    }
}