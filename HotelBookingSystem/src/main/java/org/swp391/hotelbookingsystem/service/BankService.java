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

    public int getIdByCode(String code){
        return bankRepository.getIdByCode(code);
    }

    public int countBank(int userId){
        return bankRepository.countBank(userId);
    }

    public int changeDefault(int userId, int bankId, String bankNumber){
        if(bankRepository.checkBank(userId, bankId, bankNumber)) return -1;
        return bankRepository.changeDefault(userId, bankId, bankNumber);
    }

    public int addBank(int userId, int bankId, String bankNumber, String userName){
        if(!bankRepository.checkBank(userId, bankId, bankNumber)) return -1;
        return bankRepository.addBank(userId, bankId, bankNumber, userName, bankRepository.noDefault(userId));
    }

    public int editBank(int userId, int bankId, String bankNumber, String userName, int oldId, String oldNumber){
        if(!bankRepository.checkBank(userId, bankId, bankNumber)) return -1;
        return bankRepository.editBank(userId, bankId, bankNumber, userName, oldId, oldNumber);
    }

    public int deleteBank(int userId, int bankId, String bankNumber){
        if(bankRepository.isDefault(userId, bankId, bankNumber)) return -1;
        return bankRepository.deleteBank(userId, bankId, bankNumber);
    }
}
