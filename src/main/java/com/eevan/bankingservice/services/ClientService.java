package com.eevan.bankingservice.services;

import com.eevan.bankingservice.entities.Client;
import com.eevan.bankingservice.repositories.ClientsRepository;
import com.eevan.bankingservice.utils.ClientNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class ClientService {

    private final ClientsRepository clientsRepository;


    @Autowired
    public ClientService(ClientsRepository clientsRepository) {
        this.clientsRepository = clientsRepository;
    }

    @Transactional
    public void save(Client client) {
        if (clientsRepository.existsByLogin(client.getLogin())) {
            throw new RuntimeException("User with the same login is existing");
        }

        if (clientsRepository.existsByEmailMainOrEmailAdditional(client.getEmailMain(),
                client.getEmailMain())) {
            throw new RuntimeException("User with the same email is existing");
        }

        if (clientsRepository.existsByPhoneNumberMainOrPhoneNumberAdditional(client.getPhoneNumberMain(),
                client.getPhoneNumberMain())) {
            throw new RuntimeException("User with the same phone number is existing");
        }

        clientsRepository.save(client);
    }

    @Transactional
    public void changeMainPhone(int id, String phoneNumber) {
        if (clientsRepository.existsByPhoneNumberMainOrPhoneNumberAdditional(phoneNumber, phoneNumber)) {
            throw new RuntimeException("User with the same phone number is existing");
        }
        Client updatedClient = findClientById(id);
        updatedClient.setPhoneNumberMain(phoneNumber);
        clientsRepository.save(updatedClient);
    }

    @Transactional
    public void changeMainEmail(int id, String email) {
        if (clientsRepository.existsByEmailMainOrEmailAdditional(email, email)) {
            throw new RuntimeException("User with the same email is existing");
        }
        Client updatedClient = findClientById(id);
        updatedClient.setEmailMain(email);
        clientsRepository.save(updatedClient);
    }

    @Transactional
    public void addAdditionalPhone(int id, String phoneNumber) {
        if (clientsRepository.existsByPhoneNumberMainOrPhoneNumberAdditional(phoneNumber, phoneNumber)) {
            throw new RuntimeException("User with the same phone number is existing");
        }
        Client updatedClient = findClientById(id);
        updatedClient.setPhoneNumberAdditional(phoneNumber);
        clientsRepository.save(updatedClient);
    }

    @Transactional
    public void addAdditionalEmail(int id, String email) {
        if (clientsRepository.existsByEmailMainOrEmailAdditional(email, email)) {
            throw new RuntimeException("User with the same email is existing");
        }
        Client updatedClient = findClientById(id);
        updatedClient.setEmailAdditional(email);
        clientsRepository.save(updatedClient);
    }

    @Transactional
    public void deleteAdditionalPhone(int id) {
        Client updatedClient = findClientById(id);
        updatedClient.setPhoneNumberAdditional(null);
        clientsRepository.save(updatedClient);
    }

    @Transactional
    public void deleteAdditionalEmail(int id) {
        Client updatedClient = findClientById(id);
        updatedClient.setEmailAdditional(null);
        clientsRepository.save(updatedClient);
    }

    @Transactional(readOnly = true)
    public List<Client> findClientByBirthdate(LocalDate birthdate, int pageNo, int recordCount) {
        Pageable pageable = PageRequest.of(pageNo, recordCount);
        Optional<List<Client>> foundClients = clientsRepository.findByDateOfBirthAfter(birthdate, pageable);
        return foundClients.orElseThrow(ClientNotFoundException::new);
    }

    @Transactional(readOnly = true)
    public Client findClientByPhone(String phoneNumber) {
        Optional<Client> foundClient = clientsRepository.findByPhoneNumberMainOrPhoneNumberAdditional(phoneNumber,
                phoneNumber);
        return foundClient.orElseThrow(ClientNotFoundException::new);
    }

    @Transactional(readOnly = true)
    public Client findClientByEmail(String email) {
        Optional<Client> foundClient = clientsRepository.findByEmailMain(email);
        return foundClient.orElseThrow(ClientNotFoundException::new);
    }

    @Transactional(readOnly = true)
    public List<Client> findClientByFIO(String surname, String name, String patronymic, int pageNo, int recordCount) {
        Pageable pageable = PageRequest.of(pageNo, recordCount);
        Optional<List<Client>> foundClients = clientsRepository.
                findByNameLikeAndSurnameLikeAndPatronymicLikeAllIgnoreCase(surname,
                        name,
                        patronymic,
                        pageable);
        return foundClients.orElseThrow(ClientNotFoundException::new);
    }

    @Transactional(readOnly = true)
    public Client findClientById(int id) {
        Optional<Client> foundClient = clientsRepository.findById(id);
        return foundClient.orElseThrow(ClientNotFoundException::new);
    }

    @Transactional(readOnly = true)
    public Client findByLogin(String login) {
        Optional<Client> foundClient = clientsRepository.findByLogin(login);
        return foundClient.orElseThrow(ClientNotFoundException::new);
    }

    public UserDetailsService userDetailsService() {
        return this::findByLogin;
    }

    @Transactional(readOnly = true)
    public Client getCurrentClient() {
        var login = SecurityContextHolder.getContext().getAuthentication().getName();
        return findByLogin(login);
    }

    @Transactional
    public void updateBalance() {
        List<Client> clients = clientsRepository.findAll();
        for (Client client : clients) {
            double maxBalance = client.getInitialBalance() * 2.07;
            if (client.getCurrentBalance() <= maxBalance) {
                client.setCurrentBalance(Math.min(client.getCurrentBalance() * 1.05, maxBalance));
                clientsRepository.save(client);
            }
        }
    }

    @Transactional
    public synchronized void transferMoney(Long fromClientId, Long toClientId, double amount) {
        if (fromClientId.equals(toClientId)) {
            throw new IllegalArgumentException("Cannot transfer money to the same client");
        }

        Client fromClient = clientsRepository.findById(fromClientId)
                .orElseThrow(() -> new IllegalArgumentException("Client not found: " + fromClientId));
        Client toClient = clientsRepository.findById(toClientId)
                .orElseThrow(() -> new IllegalArgumentException("Client not found: " + toClientId));

        if (fromClient.getCurrentBalance() < amount) {
            throw new IllegalArgumentException("Insufficient balance");
        }

        fromClient.setCurrentBalance(fromClient.getCurrentBalance() - amount);
        toClient.setCurrentBalance(toClient.getCurrentBalance() + amount);

        clientsRepository.save(fromClient);
        clientsRepository.save(toClient);
    }
}
