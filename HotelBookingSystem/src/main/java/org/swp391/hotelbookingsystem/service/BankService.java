package org.swp391.hotelbookingsystem.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.swp391.hotelbookingsystem.model.Bank;
import org.swp391.hotelbookingsystem.repository.BankRepository;

@Service
public class BankService {
    @Autowired
    private BankRepository bankRepository;

    public List<Bank> getAllBank(){
        return bankRepository.getAllBank();
    }
}
