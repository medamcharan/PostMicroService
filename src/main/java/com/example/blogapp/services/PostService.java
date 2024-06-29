package com.example.blogapp.services;

import com.example.blogapp.models.Post;
import com.example.blogapp.repositories.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class PostService {
    private static final Logger logger = LoggerFactory.getLogger(PostService.class);
    
    @Autowired
    private PostRepository postRepository;

    @Autowired
    private RestTemplate restTemplate;

    private static final String USER_SERVICE_URL = "http://user-service:8080/api/users/";

    public Post createPost(Post post) {
        logger.info("Creating new post: {}", post);
        Post savedPost = postRepository.save(post);
        fetchAndSetUserDetails(savedPost);
        return savedPost;
    }

    public void approvePost(int postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + postId));
        post.setApproved(true);
        postRepository.save(post);
    }

    public List<Post> getAllPosts() {
        List<Post> posts = postRepository.findAll();
        posts.forEach(this::fetchAndSetUserDetails);
        return posts;
    }

    public Post getPostById(int postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + postId));
        fetchAndSetUserDetails(post);
        return post;
    }

    public Post updatePost(int postId, Post postDetails) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + postId));
        post.setTitle(postDetails.getTitle());
        post.setContent(postDetails.getContent());
        return postRepository.save(post);
    }

    public void deletePost(int postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + postId));
        postRepository.delete(post);
    }

    public List<Post> getPostsByUserId(int userId) {
        List<Post> allPosts = postRepository.findAll();
        return allPosts.stream()
                .filter(post -> post.getUserId() == userId)
                .collect(Collectors.toList());
    }

    private void fetchAndSetUserDetails(Post post) {
        try {
            Map<String, Object> userDetails = restTemplate.getForObject(USER_SERVICE_URL + post.getUserId(), Map.class);
            if (userDetails != null) {
                post.setUserName((String) userDetails.get("username"));
                post.setUserEmail((String) userDetails.get("email"));
            }
        } catch (Exception e) {
            logger.error("Error fetching user details for user id: " + post.getUserId(), e);
        }
    }
}