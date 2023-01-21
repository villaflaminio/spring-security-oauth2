package com.flaminiovilla.security.service;

import com.flaminiovilla.security.model.Role;
import com.flaminiovilla.security.model.User;
import com.flaminiovilla.security.model.dto.AlterUserDto;
import com.flaminiovilla.security.repository.RoleRepository;
import com.flaminiovilla.security.repository.UserRepository;
import com.flaminiovilla.security.util.Util;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    /**
     * @param probe         Dto contenente i dati della palestra da filtrare.
     * @param page          Pagina da visualizzare
     * @param size          Numero di elementi per pagina
     * @param sortField     Campo per ordinamento
     * @param sortDirection Direzione di ordinamento
     * @return La pagina di risultati della ricerca.
     */
    public ResponseEntity<Page<User>> filter(User probe, Integer page, Integer size, String sortField, String sortDirection) {
        Pageable pageable;

        // Controllo se la palestra per filtrare è nulla
        if (probe == null) {
            probe = new User();
        }
        probe.setAuthorities(null);
        // Controllo se il campo per ordinare è nullo
        if (StringUtils.isEmpty(sortField)) {
            pageable = PageRequest.of(page, size); // Se è nullo, ordino per id
        } else {
            // Se non è nullo, ordino per il campo specificato
            Sort.Direction dir = StringUtils.isEmpty(sortDirection) ? Sort.Direction.ASC : Sort.Direction.valueOf(sortDirection.trim().toUpperCase());
            pageable = PageRequest.of(page, size, dir, sortField);
        }

        // Filtro le palestre
        ExampleMatcher matcher = ExampleMatcher.matchingAll().withIgnoreCase().withIgnoreNullValues().withStringMatcher(ExampleMatcher.StringMatcher.STARTING);
        Example<User> example = Example.of(probe, matcher);

        return ResponseEntity.ok(userRepository.findAll(example, pageable));
    }

    public ResponseEntity updateUser(long id, AlterUserDto user) {
        try {
            User userOld = userRepository.findById(id).orElseThrow(() -> new Exception("Utente non trovato"));
            if (!user.getRoles().isEmpty()) {
                List<Role> roles = new ArrayList<>();
                for (String role : user.getRoles()) {
                    roles.add(roleRepository.findByName(role).orElseThrow(() -> new Exception("Ruolo non trovato")));
                }
                userOld.setRoles(roles);

            }
            if (user.getImageUrl() != null) userOld.setImageUrl(user.getImageUrl());
            if (user.getName()!= null) userOld.setName(user.getName());
            if (user.getEmail()!= null)userOld.setEmail(user.getEmail());


            return ResponseEntity.ok(userRepository.save(userOld));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        }
    }


}
