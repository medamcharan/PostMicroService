1. Separation of Services:
   The main change is the separation of User and Post functionalities into two distinct services.

2. UserService Changes:
   The UserService remains largely the same, but it's now a separate microservice. The key components are:

```java
@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User createUser(User user) {
        return userRepository.save(user);
    }

    // Other methods for user management
}
```

This service is now responsible only for user-related operations.

3. PostService Changes:
   The PostService has undergone significant changes:

```java
@Service
public class PostService {
    @Autowired
    private PostRepository postRepository;

    @Autowired
    private RestTemplate restTemplate;

    private static final String USER_SERVICE_URL = "http://user-service:8080/api/users/";

    public Post createPost(Post post) {
        // Implementation
    }

    public List<Post> getAllPosts() {
        List<Post> posts = postRepository.findAll();
        posts.forEach(this::fetchAndSetUserDetails);
        return posts;
    }

    private void fetchAndSetUserDetails(Post post) {
        try {
            Map<String, Object> userDetails = restTemplate.getForObject(USER_SERVICE_URL + post.getUserId(), Map.class);
            if (userDetails != null) {
                post.setUserName((String) userDetails.get("username"));
                post.setUserEmail((String) userDetails.get("email"));
            }
        } catch (Exception e) {
            // Error handling
        }
    }

    // Other methods
}
```

Key changes in PostService:

- Removal of direct UserRepository dependency.
- Addition of RestTemplate for inter-service communication.
- Implementation of fetchAndSetUserDetails method to get user information from the UserService.

4. Post Model Changes:
   The Post model has been updated to remove the direct User relationship:

```java
@Entity
@Table(name = "posts")
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int postId;
    private String title;
    private String content;
    private boolean approved;
    private int userId;

    @Transient
    private String userName;
    @Transient
    private String userEmail;

    // Getters and setters
}
```

The User object is replaced with userId, and transient fields for userName and userEmail are added.

5. Inter-Service Communication:
   The PostService now uses RestTemplate to communicate with the UserService:

```java
private static final String USER_SERVICE_URL = "http://user-service:8080/api/users/";

// In fetchAndSetUserDetails method
Map<String, Object> userDetails = restTemplate.getForObject(USER_SERVICE_URL + post.getUserId(), Map.class);
```

6. Docker Compose Configuration:
   A Docker Compose file is added to manage the two services:

```yaml
version: "3"
services:
  user-service:
    image: blogappuserservice
    ports:
      - "8080:8080"
    environment:
      - SPRING_DATASOURCE_URL=jdbc:h2:mem:userdb
      - SPRING_DATASOURCE_USERNAME=sa
      - SPRING_DATASOURCE_PASSWORD=password

  post-service:
    image: blogapppostservice
    ports:
      - "8081:8080"
    environment:
      - SPRING_DATASOURCE_URL=jdbc:h2:mem:postdb
      - SPRING_DATASOURCE_USERNAME=sa
      - SPRING_DATASOURCE_PASSWORD=password
      - USER_SERVICE_URL=http://user-service:8080
    depends_on:
      - user-service
```

This configuration allows the two services to run independently and communicate with each other.

These changes transform the application from a monolithic structure to a microservices architecture, with separate services for User and Post functionalities, and inter-service communication for data that spans both services.
