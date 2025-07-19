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

    public List<Bank> getUserBanks(int userId){
        return bankRepository.getUserBanks(userId);
    }

    public int addBank(int userId, int bankId, int bankNumber, int userName){
        return bankRepository.addBank(userId, bankId, bankNumber, userName, true);
    }

    public int editBank(int userId, int bankId, int bankNumber, int userName){
        return bankRepository.editBank(userId, bankId, bankNumber, userName);
    }

    public int deleteBank(int userId, int bankId){
        return bankRepository.deleteBank(userId, bankId);
    }
}
