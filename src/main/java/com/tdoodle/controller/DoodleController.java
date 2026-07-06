package com.tdoodle.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/tdoodle")
public class DoodleController {

    @GetMapping(value = "/users/{userId}/time-slots", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<String>> getTimeSlots(@PathVariable("userId") Integer userId) {
        //  Return an empty canned response for now.
        return ResponseEntity.ok(List.of());
    }
}