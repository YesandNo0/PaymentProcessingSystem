package com.example.payment_processing_system.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;


@Data
@Entity
@Table(name = "bank_card_info")
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class BankCardInfoEntity {
    @Id
    @Column(name = "card_number")
    private String cardNumber;

    private BigDecimal balance;

    @Column(name = "card_expiry_date")
    private String cardExpiryDate;

    private int cvv;

    @ManyToOne
    @JoinColumn(name = "account_id")
    private AccountEntity account;
}
