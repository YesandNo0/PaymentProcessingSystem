package com.example.payment_processing_system.service.impl;

import com.example.payment_processing_system.domain.BankCardInfoDTO;
import com.example.payment_processing_system.entity.BankCardInfoEntity;
import com.example.payment_processing_system.exception.BankCardAlreadyExistsException;
import com.example.payment_processing_system.exception.BankCardNotFoundException;
import com.example.payment_processing_system.repository.BankCardInfoRepository;
import com.example.payment_processing_system.service.BankCardInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class BankCardInfoServiceImpl implements BankCardInfoService {

    private final BankCardInfoRepository bankCardInfoRepository;

    @Override
    public BankCardInfoDTO addBankCard(BankCardInfoDTO bankCardInfoDTO) {
        if (bankCardInfoRepository.findByCardNumber(bankCardInfoDTO.getCardNumber()).isPresent()) {
            throw new BankCardAlreadyExistsException("Card number already registered: " + bankCardInfoDTO.getCardNumber());
        }

        BankCardInfoEntity entity = BankCardInfoEntity.builder()
                .cardNumber(bankCardInfoDTO.getCardNumber())
                .balance(bankCardInfoDTO.getBalance())
                .cardExpiryDate(bankCardInfoDTO.getCardExpiryDate())
                .cvv(bankCardInfoDTO.getCvv())
                .build();

        BankCardInfoEntity savedEntity = bankCardInfoRepository.save(entity);
        return mapToDTO(savedEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public BankCardInfoDTO getBankCardByNumber(String cardNumber) {
        BankCardInfoEntity entity = bankCardInfoRepository.findByCardNumber(cardNumber)
                .orElseThrow(() -> new BankCardNotFoundException("Card not found: " + cardNumber));

        return mapToDTO(entity);
    }

    @Override
    public BankCardInfoDTO updateBankCard(String cardNumber, BankCardInfoDTO bankCardInfoDTO) {
        BankCardInfoEntity entity = bankCardInfoRepository.findByCardNumber(cardNumber)
                .orElseThrow(() -> new BankCardNotFoundException("Card not found: " + cardNumber));

        entity.setBalance(bankCardInfoDTO.getBalance());
        entity.setCardExpiryDate(bankCardInfoDTO.getCardExpiryDate());
        entity.setCvv(bankCardInfoDTO.getCvv());

        BankCardInfoEntity updatedEntity = bankCardInfoRepository.save(entity);
        return mapToDTO(updatedEntity);
    }

    @Override
    public void deleteBankCard(String cardNumber) {
        BankCardInfoEntity entity = bankCardInfoRepository.findByCardNumber(cardNumber)
                .orElseThrow(() -> new BankCardNotFoundException("Card not found: " + cardNumber));

        bankCardInfoRepository.delete(entity);
    }

    private BankCardInfoDTO mapToDTO(BankCardInfoEntity entity) {
        return BankCardInfoDTO.builder()
                .cardNumber(entity.getCardNumber())
                .balance(entity.getBalance())
                .cardExpiryDate(entity.getCardExpiryDate())
                .cvv(entity.getCvv())
                .build();
    }
}
