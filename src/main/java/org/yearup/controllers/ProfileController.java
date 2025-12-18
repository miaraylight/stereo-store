package org.yearup.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.yearup.data.ProfileDao;
import org.yearup.data.UserDao;
import org.yearup.models.Profile;

import java.security.Principal;

@RestController
@RequestMapping("profile")
@CrossOrigin
@PreAuthorize("hasRole('USER')")
public class ProfileController {
    private final ProfileDao profileDao;
    private final UserDao userDao;

    @Autowired
    public ProfileController(ProfileDao profileDao, UserDao userDao) {
        this.profileDao = profileDao;
        this.userDao = userDao;
    }

    @GetMapping
    public Profile getProfile(Principal principal) {
        try
        {
            int userId = userDao.getByUserName(principal.getName()).getId();

            return profileDao.getByUserId(userId);
        }
        catch(Exception e)
        {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad.");
        }
    }

    @PutMapping
    public Profile update(@RequestBody Profile profile, Principal principal) {
        try{
            int userId = userDao.getByUserName(principal.getName()).getId();

            return profileDao.update(userId, profile);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
