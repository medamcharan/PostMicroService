Planning Microservices

Architecture

1. User Creation:

The user creation process is handled within the same application as the post service. Here's how it works:

```
Client
  |
  | POST /api/users {username: "john", email: "john@example.com"}
  v
+-------------------+
|   Blog App        |
| +---------------+ |
| | UserController| |
| +---------------+ |
|         |         |
|         v         |
| +---------------+ |
| |  UserService  | |
| |  +---------+  | |
| |  |  User   |  | |
| |  | userId:0|  | | <-- Initially 0, will be set by DB
| |  | username|  | |
| |  | email   |  | |
| |  +---------+  | |
| +---------------+ |
|         |         |
|         v         |
| +---------------+ |
| |UserRepository | |
| +---------------+ |
|         |         |
|         v         |
| +---------------+ |
| |   User DB     | |
| | +---------+   | |
| | |  User   |   | |
| | | userId:1|   | | <-- Assigned by DB
| | | username|   | |
| | | email   |   | |
| | +---------+   | |
| +---------------+ |
+-------------------+
          |
          | Return created User
          v
Client
```

2. Post Creation:

The post creation process involves creating a post and associating it with an existing user:

```
Client
  |
  | POST /api/posts {title: "New Post", content: "Content", user: {userId: 1}}
  v
+-------------------+
|   Blog App        |
| +---------------+ |
| | PostController| |
| +---------------+ |
|         |         |
|         v         |
| +---------------+ |
| |  PostService  | |
| |  +---------+  | |
| |  |  Post   |  | |
| |  | postId:0|  | | <-- Will be set by DB
| |  | title   |  | |
| |  | content |  | |
| |  | user    |  | | <-- Contains userId: 1
| |  +---------+  | |
| +---------------+ |
|         |         |
|         | Fetch associated user
|         v         |
| +---------------+ |
| |UserRepository | |
| +---------------+ |
|         |         |
|         | User found
|         v         |
| +---------------+ |
| |  PostService  | |
| |  +---------+  | |
| |  |  Post   |  | |
| |  | postId:0|  | |
| |  | title   |  | |
| |  | content |  | |
| |  | user    |  | | <-- Now contains full User object
| |  +---------+  | |
| +---------------+ |
|         |         |
|         v         |
| +---------------+ |
| |PostRepository | |
| +---------------+ |
|         |         |
|         v         |
| +---------------+ |
| |   Post DB     | |
| | +---------+   | |
| | |  Post   |   | |
| | | postId:1|   | | <-- Assigned by DB
| | | title   |   | |
| | | content |   | |
| | | user_id |   | | <-- Foreign key to User
| | +---------+   | |
| +---------------+ |
+-------------------+
          |
          | Return created Post
          v
Client
```

Key points:

1. Both User and Post entities are managed within the same application (BlogApp).
2. The User entity is created and stored independently.
3. When creating a Post, the client provides the userId of the associated User.
4. The PostService fetches the full User object from the UserRepository before saving the Post.
5. The Post is then saved with a reference to the full User object.
6. The database handles the relationship between Post and User using a foreign key.

This architecture allows for efficient management of both users and posts within a single application, while maintaining the relationship between them. The @ManyToOne annotation in the Post entity ensures that each post is associated with a user, and JPA handles the database relationships automatically.

This illustrates the separation of concerns between services and how they interact to maintain data consistency across the system.

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

1. `@Autowired private RestTemplate restTemplate;`

   - RestTemplate is a Spring class used for making HTTP requests.
   - The `@Autowired` annotation tells Spring to inject a RestTemplate bean into this field.

2. `private static final String USER_SERVICE_URL = "http://user-service:8080/api/users/";`

   - This is the base URL for the User Service microservice.
   - "user-service" is likely a Docker service name or Kubernetes service name, allowing for service discovery in a containerized environment.

3. `public Post createPost(Post post) { ... }`

   - This method is responsible for creating a new blog post.

4. `if (post.getUser() != null && post.getUser().getUserId() != 0) { ... }`

   - This checks if the post has an associated user and if that user has a valid ID.

5. `User user = restTemplate.getForObject(USER_SERVICE_URL + post.getUser().getUserId(), User.class);`

   - If there's a valid user ID, this line makes an HTTP GET request to the User Service.
   - It concatenates the base URL with the user ID to form the full URL.
   - The `getForObject` method sends the request and expects a User object in response.
   - This allows the Post Service to fetch up-to-date user information from the User Service.

6. `post.setUser(user);`

   - The retrieved user information is then set on the post object.

7. `return postRepository.save(post);`
   - Finally, the post (now with updated user information) is saved to the database.

This approach demonstrates a key principle of microservices architecture: each service is responsible for its own domain (in this case, posts), but can communicate with other services (like the user service) to get necessary information. This allows for decoupling of services while still maintaining data consistency across the system.
