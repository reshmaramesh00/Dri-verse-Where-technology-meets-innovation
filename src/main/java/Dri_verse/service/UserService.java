package Dri_verse.service;

import org.springframework.stereotype.Service;

import Dri_verse.model.User;
import Dri_verse.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public int registerUser(User user) {
        return userRepository.saveUser(user);
    }
    public User loginUser(String email, String password) {
    return userRepository.findUserByEmailAndPassword(email, password);
}
}
