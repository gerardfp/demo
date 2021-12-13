package com.example.demo.controller;

import com.example.demo.domain.model.Favorite;
import com.example.demo.domain.dto.RequestUserRegister;
import com.example.demo.domain.dto.ResponseList;
import com.example.demo.domain.dto.ResponseMessage;
import com.example.demo.domain.model.User;
import com.example.demo.domain.model.projection.ProjectionUserDetail;
import com.example.demo.repository.FavoriteRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired private UserRepository userRepository;
    @Autowired private FavoriteRepository favoriteRepository;
    @Autowired private BCryptPasswordEncoder passwordEncoder;

    @PostMapping(path = "/register" )
    public ResponseEntity<?> register(@RequestBody RequestUserRegister requestUserRegister) {

        if (userRepository.findByUsername(requestUserRegister.username) == null) {
            User user = new User();
            user.username = requestUserRegister.username;
            user.password = passwordEncoder.encode(requestUserRegister.password);
            user.enabled = true;
            return ResponseEntity.ok().body(userRepository.save(user).userid.toString());
        }
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ResponseMessage.message("Nom d'usuari no disponible"));
    }

    @GetMapping("/")
    public ResponseEntity<?> getALl(){
        return ResponseEntity.ok().body(ResponseList.list(userRepository.findBy()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUser(@PathVariable UUID id){
        return ResponseEntity.ok().body(ResponseList.list(userRepository.findByUserid(id, ProjectionUserDetail.class)));
    }

    @PostMapping("/{id}/favorites")
    public ResponseEntity<?> addFavorite(@RequestBody Favorite favorite, Authentication authentication) {
        if (userRepository.findByUsername(authentication.getName()).userid.equals(favorite.userid)) {
            favoriteRepository.save(favorite);
            return ResponseEntity.ok().build();
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ResponseMessage.message("Not authorized"));
    }




    // WEB REGISTER FORM (for testing)
    @GetMapping("/register/web")
    public String hack(){
        return "<div style='display:flex;flex-direction:column;width:20em;gap:0.5em'>" +
                "<input name='username' id='username' placeholder='Username'>" +
                "<input id='password' type='password' placeholder='Password'>" +
                "<input type='button' value='Register' onclick='fetch(\"/users/register/\",{method:\"POST\",headers:{\"Content-Type\":\"application/json\"},body:`{\"username\":\"${username.value}\",\"password\":\"${password.value}\"}`})'></div>";
    }
}