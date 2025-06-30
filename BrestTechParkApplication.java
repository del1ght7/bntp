package com.bresttechpark;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@SpringBootApplication
class BrestTechParkApplication {
    public static void main(String[] args) {
        SpringApplication.run(BrestTechParkApplication.class, args);
    }

    @Bean
    public org.springframework.boot.CommandLineRunner initData(UserService userService, AnnouncementService announcementService) {
        return args -> {
            try {
                // Создаем админа
                userService.registerUser("admin", "admin123", "admin@bntp.by", "БНТП", UserRole.ADMIN);

                // Создаем тестового резидента
                User resident = userService.registerUser("resident1", "password123", "resident@example.com", "ИТ Компания", UserRole.RESIDENT);
                userService.approveUser(resident.getId());

                // Создаем тестовое объявление
                Announcement announcement = new Announcement();
                announcement.setTitle("Добро пожаловать в БНТП!");
                announcement.setContent("Брестский научно-технологический парк приветствует новых резидентов. Мы предлагаем современную инфраструктуру и поддержку для развития инновационного бизнеса.");
                announcement.setType(AnnouncementType.NEWS);
                announcement.setAuthor(resident);
                Announcement created = announcementService.createAnnouncement(announcement);
                announcementService.publishAnnouncement(created.getId());

                System.out.println("✅ Тестовые данные созданы:");
                System.out.println("👤 Админ: admin / admin123");
                System.out.println("👤 Резидент: resident1 / password123");
                System.out.println("🌐 Сайт: http://localhost:8080");
                System.out.println("🗄️ H2 Console: http://localhost:8080/h2-console");

            } catch (Exception e) {
                System.out.println("⚠️ Ошибка инициализации: " + e.getMessage());
            }
        };
    }
}

// Entities
@Entity
@Table(name = "users")
class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String username;

    private String password;
    private String email;
    private String company;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    private boolean approved = false;

    // Constructors, getters, setters
    public User() {}

    public User(String username, String password, String email, String company, UserRole role) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.company = company;
        this.role = role;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }
    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }
    public boolean isApproved() { return approved; }
    public void setApproved(boolean approved) { this.approved = approved; }
}

enum UserRole {
    ADMIN, USER, RESIDENT, TECHPARK
}

@Entity
@Table(name = "bookings")
class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String roomType; // CONFERENCE_HALL, OFFICE
    private String roomName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String purpose;
    private String contactInfo;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    private BookingStatus status = BookingStatus.PENDING;

    // Constructors, getters, setters
    public Booking() {}

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getRoomType() { return roomType; }
    public void setRoomType(String roomType) { this.roomType = roomType; }
    public String getRoomName() { return roomName; }
    public void setRoomName(String roomName) { this.roomName = roomName; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }
    public String getContactInfo() { return contactInfo; }
    public void setContactInfo(String contactInfo) { this.contactInfo = contactInfo; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public BookingStatus getStatus() { return status; }
    public void setStatus(BookingStatus status) { this.status = status; }
}

enum BookingStatus {
    PENDING, APPROVED, REJECTED
}

@Entity
@Table(name = "announcements")
class Announcement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    private AnnouncementType type;

    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "author_id")
    private User author;

    private boolean published = false;

    // Constructors, getters, setters
    public Announcement() {}

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public AnnouncementType getType() { return type; }
    public void setType(AnnouncementType type) { this.type = type; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public User getAuthor() { return author; }
    public void setAuthor(User author) { this.author = author; }
    public boolean isPublished() { return published; }
    public void setPublished(boolean published) { this.published = published; }
}

enum AnnouncementType {
    NEWS, VACANCY, EVENT
}

// Repositories
@Repository
interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    List<User> findByApprovedFalse();
}

@Repository
interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByStatus(BookingStatus status);
    List<Booking> findByUserId(Long userId);
}

@Repository
interface AnnouncementRepository extends JpaRepository<Announcement, Long> {
    List<Announcement> findByPublishedTrueOrderByCreatedAtDesc();
    List<Announcement> findByPublishedFalseOrderByCreatedAtDesc();
    List<Announcement> findByAuthorIdOrderByCreatedAtDesc(Long authorId);
}

// Services
@Service
class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User registerUser(String username, String password, String email, String company, UserRole role) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("Username already exists");
        }

        User user = new User(username, passwordEncoder.encode(password), email, company, role);
        if (role == UserRole.ADMIN) {
            user.setApproved(true);
        }
        return userRepository.save(user);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public List<User> getPendingUsers() {
        return userRepository.findByApprovedFalse();
    }

    public User approveUser(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        user.setApproved(true);
        return userRepository.save(user);
    }
}

@Service
class BookingService {
    private final BookingRepository bookingRepository;

    public BookingService(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    public Booking createBooking(Booking booking) {
        return bookingRepository.save(booking);
    }

    public List<Booking> getPendingBookings() {
        return bookingRepository.findByStatus(BookingStatus.PENDING);
    }

    public List<Booking> getUserBookings(Long userId) {
        return bookingRepository.findByUserId(userId);
    }

    public void approveBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow();
        booking.setStatus(BookingStatus.APPROVED);
        bookingRepository.save(booking);
    }

    public void rejectBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow();
        booking.setStatus(BookingStatus.REJECTED);
        bookingRepository.save(booking);
    }
}

@Service
class AnnouncementService {
    private final AnnouncementRepository announcementRepository;

    public AnnouncementService(AnnouncementRepository announcementRepository) {
        this.announcementRepository = announcementRepository;
    }

    public Announcement createAnnouncement(Announcement announcement) {
        return announcementRepository.save(announcement);
    }

    public List<Announcement> getPublishedAnnouncements() {
        return announcementRepository.findByPublishedTrueOrderByCreatedAtDesc();
    }

    public List<Announcement> getPendingAnnouncements() {
        return announcementRepository.findByPublishedFalseOrderByCreatedAtDesc();
    }

    public List<Announcement> getUserAnnouncements(Long userId) {
        return announcementRepository.findByAuthorIdOrderByCreatedAtDesc(userId);
    }

    public void publishAnnouncement(Long announcementId) {
        Announcement announcement = announcementRepository.findById(announcementId).orElseThrow();
        announcement.setPublished(true);
        announcementRepository.save(announcement);
    }
}

// Controllers
@Controller
class WebController {

    private final UserService userService;
    private final BookingService bookingService;
    private final AnnouncementService announcementService;

    public WebController(UserService userService, BookingService bookingService, AnnouncementService announcementService) {
        this.userService = userService;
        this.bookingService = bookingService;
        this.announcementService = announcementService;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("announcements", announcementService.getPublishedAnnouncements());
        return "index";
    }

    @GetMapping("/login")
    public String login(HttpServletRequest request, Model model) {
        // Добавить ошибку аутентификации если есть
        String error = request.getParameter("error");
        if (error != null) {
            model.addAttribute("error", "Неверный логин или пароль");
        }
        return "login";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        String username = authentication.getName();
        Optional<User> userOpt = userService.findByUsername(username);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            model.addAttribute("user", user);
            model.addAttribute("role", user.getRole().name());

            // Добавить данные в зависимости от роли
            switch (user.getRole()) {
                case ADMIN:
                    model.addAttribute("pendingUsers", userService.getPendingUsers());
                    model.addAttribute("pendingBookings", bookingService.getPendingBookings());
                    model.addAttribute("pendingAnnouncements", announcementService.getPendingAnnouncements());
                    break;
                case RESIDENT:
                case USER:
                    model.addAttribute("userBookings", bookingService.getUserBookings(user.getId()));
                    model.addAttribute("userAnnouncements", announcementService.getUserAnnouncements(user.getId()));
                    break;
            }
        }

        return "dashboard";
    }

    @GetMapping("/booking")
    public String booking(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        return "booking";
    }
}

@RestController
@RequestMapping("/api")
class ApiController {

    private final UserService userService;
    private final BookingService bookingService;
    private final AnnouncementService announcementService;

    public ApiController(UserService userService, BookingService bookingService, AnnouncementService announcementService) {
        this.userService = userService;
        this.bookingService = bookingService;
        this.announcementService = announcementService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserRegistrationRequest request) {
        try {
            UserRole role = UserRole.valueOf(request.getRole().toUpperCase());
            User user = userService.registerUser(request.getUsername(), request.getPassword(),
                    request.getEmail(), request.getCompany(), role);
            return ResponseEntity.ok().body("Registration successful. Awaiting approval.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Registration failed: " + e.getMessage());
        }
    }

    @PostMapping("/booking")
    public ResponseEntity<?> createBooking(@RequestBody Booking booking) {
        try {
            // In real app, get user from security context
            Booking created = bookingService.createBooking(booking);
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Booking failed: " + e.getMessage());
        }
    }

    @PostMapping("/announcement")
    public ResponseEntity<?> createAnnouncement(@RequestBody Announcement announcement) {
        try {
            // In real app, get user from security context
            Announcement created = announcementService.createAnnouncement(announcement);
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Announcement creation failed: " + e.getMessage());
        }
    }

    @GetMapping("/announcements")
    public ResponseEntity<List<Announcement>> getAnnouncements() {
        return ResponseEntity.ok(announcementService.getPublishedAnnouncements());
    }

    @PostMapping("/admin/approve-user/{userId}")
    public ResponseEntity<?> approveUser(@PathVariable Long userId) {
        try {
            userService.approveUser(userId);
            return ResponseEntity.ok("User approved");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Approval failed: " + e.getMessage());
        }
    }

    @PostMapping("/admin/approve-booking/{bookingId}")
    public ResponseEntity<?> approveBooking(@PathVariable Long bookingId) {
        try {
            bookingService.approveBooking(bookingId);
            return ResponseEntity.ok("Booking approved");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Approval failed: " + e.getMessage());
        }
    }

    @PostMapping("/admin/publish-announcement/{announcementId}")
    public ResponseEntity<?> publishAnnouncement(@PathVariable Long announcementId) {
        try {
            announcementService.publishAnnouncement(announcementId);
            return ResponseEntity.ok("Announcement published");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Publishing failed: " + e.getMessage());
        }
    }
}

// DTOs
class UserRegistrationRequest {
    private String username;
    private String password;
    private String email;
    private String company;
    private String role;

    // Getters and setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}

@Service
class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        if (!user.isApproved()) {
            throw new DisabledException("User account is not approved");
        }

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities("ROLE_" + user.getRole().name())
                .build();
    }
}

// Security Configuration
@EnableWebSecurity
@Configuration
class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/", "/api/register", "/api/announcements", "/register", "/css/**", "/js/**", "/h2-console/**").permitAll()
                        .requestMatchers("/admin/**", "/api/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/dashboard", true)
                        .failureUrl("/login?error=true")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                        .permitAll()
                )
                .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.disable())); // Для H2 консоли
        return http.build();
    }
}

// Template Controller for serving HTML
@Controller
class TemplateController {

    @GetMapping("/templates/{template}")
    public String getTemplate(@PathVariable String template) {
        return template;
    }
}