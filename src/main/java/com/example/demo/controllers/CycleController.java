package com.example.demo.controllers;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;


import com.example.demo.config.AppConstants;
import com.example.demo.payloads.ApiResponse;
import com.example.demo.payloads.CreateCycleRequest;
import com.example.demo.payloads.CycleDto;
import com.example.demo.payloads.CycleResponse;
import com.example.demo.services.CycleService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequestMapping("/api/v1/cycles")
public class CycleController {

    @Autowired
    private CycleService cycleService;


    @PostMapping("/create")
    public ResponseEntity<CycleDto> createCycle(@RequestBody CreateCycleRequest createCycleRequest) {
        CycleDto createCycle = this.cycleService.addCycle(createCycleRequest);
        return new ResponseEntity<CycleDto>(createCycle, HttpStatus.CREATED);
    }

    // get by user

    @GetMapping("/user/{userId}/cycle")
    public ResponseEntity<List<CycleDto>> getCycleByUser(@PathVariable Integer userId) {

        List<CycleDto> cycles = this.cycleService.getCyclesByUser(userId);
        return new ResponseEntity<List<CycleDto>>(cycles, HttpStatus.OK);

    }



    // get all posts

    @GetMapping("/")
    public ResponseEntity<CycleResponse> getAllCycle(
            @RequestParam(value = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(value = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(value = "sortBy", defaultValue = AppConstants.SORT_BY, required = false) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = AppConstants.SORT_DIR, required = false) String sortDir) {

        CycleResponse cycleResponse = this.cycleService.getAllCycle(pageNumber, pageSize, sortBy, sortDir);
        return new ResponseEntity<CycleResponse>(cycleResponse, HttpStatus.OK);
    }

    // get post details by id

    @GetMapping("/cycle/{cycleId}")
    public ResponseEntity<CycleDto> getCycleById(@PathVariable Integer cycleId) {

        CycleDto cycleDto = this.cycleService.getCycleById(cycleId);
        return new ResponseEntity<CycleDto>(cycleDto, HttpStatus.OK);

    }


    // delete post
    @DeleteMapping("/cycle/{cycleId}")
    public ApiResponse deletePost(@PathVariable Integer cycleId) {
        this.cycleService.deleteCycle(cycleId);
        return new ApiResponse("Post is successfully deleted !!", true);
    }



    // search
    @GetMapping("/cycle/search/{keywords}")
    public ResponseEntity<List<CycleDto>> searchCycleBycycleNumber(@PathVariable("keywords") String keywords) {
        List<CycleDto> result = this.cycleService.searchCycle(keywords);
        return new ResponseEntity<List<CycleDto>>(result, HttpStatus.OK);
    }
//    @PostMapping("/post/image/upload/{postId}")
//    public ResponseEntity<PostDto> uploadPostImage(@RequestParam("image") MultipartFile image,
//                                                   @PathVariable Integer postId) throws IOException {
//
//        PostDto postDto = this.postService.getPostById(postId);
//
//        String fileName = this.fileService.uploadFile( image);
//        postDto.setImageName(fileName);
//        PostDto updatePost = this.postService.updatePost(postDto, postId);
//        return new ResponseEntity<PostDto>(updatePost, HttpStatus.OK);
//
//    }
//    @GetMapping("/post/image/{fileName}")
//    public ResponseEntity<ByteArrayResource> downloadFile(@PathVariable String fileName) {
//        byte[] data = this.fileService.downloadFile(fileName);
//        ByteArrayResource resource = new ByteArrayResource(data);
//        return ResponseEntity
//                .ok()
//                .contentLength(data.length)
//                .header("Content-type", "application/octet-stream")
//                .header("Content-disposition", "attachment; filename=\"" + fileName + "\"")
//                .body(resource);
//    }

}

