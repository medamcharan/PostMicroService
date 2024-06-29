package com.example.blogapp.controllers;

import com.example.blogapp.models.Post;
import com.example.blogapp.services.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import javax.annotation.PostConstruct;

@RestController
@RequestMapping("/api/posts")
public class PostController {
    private static final Logger logger = LoggerFactory.getLogger(PostController.class);
    
    @Autowired
    private PostService postService;

    @PostConstruct
    public void init() {
        logger.info("PostController initialized");
    }

    @PostMapping
    public ResponseEntity<?> createPost(@RequestBody Post post) {
        logger.info("Received request to create post: {}", post);
        try {
            Post createdPost = postService.createPost(post);
            logger.info("Created post: {}", createdPost);
            return new ResponseEntity<>(createdPost, HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error("Error creating post", e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{postId}/approve")
    public ResponseEntity<?> approvePost(@PathVariable int postId) {
        logger.info("Received request to approve post with id: {}", postId);
        try {
            postService.approvePost(postId);
            logger.info("Post approved successfully: {}", postId);
            return new ResponseEntity<>("Post approved successfully", HttpStatus.OK);
        } catch (RuntimeException e) {
            logger.error("Error approving post with id: {}", postId, e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping
    public ResponseEntity<List<Post>> getAllPosts() {
        logger.info("Received request to get all posts");
        try {
            List<Post> posts = postService.getAllPosts();
            logger.info("Retrieved {} posts", posts.size());
            return new ResponseEntity<>(posts, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error retrieving all posts", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{postId}")
    public ResponseEntity<?> getPostById(@PathVariable int postId) {
        logger.info("Received request to get post with id: {}", postId);
        try {
            Post post = postService.getPostById(postId);
            logger.info("Retrieved post: {}", post);
            return new ResponseEntity<>(post, HttpStatus.OK);
        } catch (RuntimeException e) {
            logger.error("Error retrieving post with id: {}", postId, e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/{postId}")
    public ResponseEntity<?> updatePost(@PathVariable int postId, @RequestBody Post post) {
        logger.info("Received request to update post with id: {}", postId);
        try {
            Post updatedPost = postService.updatePost(postId, post);
            logger.info("Updated post: {}", updatedPost);
            return new ResponseEntity<>(updatedPost, HttpStatus.OK);
        } catch (RuntimeException e) {
            logger.error("Error updating post with id: {}", postId, e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<?> deletePost(@PathVariable int postId) {
        logger.info("Received request to delete post with id: {}", postId);
        try {
            postService.deletePost(postId);
            logger.info("Deleted post with id: {}", postId);
            return new ResponseEntity<>("Post deleted successfully", HttpStatus.OK);
        } catch (RuntimeException e) {
            logger.error("Error deleting post with id: {}", postId, e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Post>> getPostsByUserId(@PathVariable int userId) {
        logger.info("Received request to get posts for user with id: {}", userId);
        try {
            List<Post> posts = postService.getPostsByUserId(userId);
            logger.info("Retrieved {} posts for user with id: {}", posts.size(), userId);
            return new ResponseEntity<>(posts, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error retrieving posts for user with id: {}", userId, e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}