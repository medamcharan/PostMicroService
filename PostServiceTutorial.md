1. Post Model Change:

The Post model didn't change significantly. The main change was ensuring that the User relationship was properly defined.

Before:

```java
@ManyToOne(fetch = FetchType.EAGER)
@JoinColumn(name = "user_id")
private User user;
```

After:

```java
@ManyToOne(fetch = FetchType.EAGER)
@JoinColumn(name = "user_id")
private User user;
```

Why: This relationship didn't change because we still need to maintain the association between Posts and Users, even in separate microservices.

2. User Model in Post Service:

Before (in the original monolithic app):

```java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int userId;
    private String username;
    private String email;
    // getters and setters
}
```

After (in the Post microservice):

```java
public class User {
    private int userId;
    private String username;
    private String email;
    // getters and setters
}
```

Why: In the Post microservice, we don't need the User class to be an entity because user data is managed by the User microservice. We only need a simple POJO to represent user data within posts.

3. PostService Change:

Before:

```java
@Autowired
private UserRepository userRepository;

public Post createPost(Post post) {
    if (post.getUser() != null && post.getUser().getUserId() != 0) {
        User user = userRepository.findById(post.getUser().getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        post.setUser(user);
    }
    return postRepository.save(post);
}
```

After:

```java
@Autowired
private RestTemplate restTemplate;

private static final String USER_SERVICE_URL = "http://user-service:8080/api/users/";

public Post createPost(Post post) {
    if (post.getUser() != null && post.getUser().getUserId() != 0) {
        User user = restTemplate.getForObject(USER_SERVICE_URL + post.getUser().getUserId(), User.class);
        post.setUser(user);
    }
    return postRepository.save(post);
}
```

Why: In the microservice architecture, we can't directly access the UserRepository from the Post service. Instead, we need to make an HTTP request to the User service to get user information. This is done using RestTemplate.

4. Addition to PostServiceApplication:

Before: There was no RestTemplate bean.

After:

```java
@SpringBootApplication
public class PostServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(PostServiceApplication.class, args);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
```

Why: We added a RestTemplate bean to enable making HTTP requests to other services (in this case, the User service).

These changes reflect the transition from a monolithic architecture to a microservices architecture. The main goals were:

1. Decoupling the Post service from direct database access to User data.
2. Enabling communication between microservices via HTTP requests.
3. Simplifying the User model in the Post service to only what's necessary for Post operations.

These changes allow the Post service to operate independently while still maintaining the necessary associations with User data.
