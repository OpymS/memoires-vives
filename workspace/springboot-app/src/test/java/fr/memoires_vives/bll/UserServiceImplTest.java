package fr.memoires_vives.bll;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import fr.memoires_vives.bo.Role;
import fr.memoires_vives.bo.User;
import fr.memoires_vives.exception.EntityNotFoundException;
import fr.memoires_vives.exception.FileStorageException;
import fr.memoires_vives.exception.UnauthorizedActionException;
import fr.memoires_vives.exception.ValidationException;
import fr.memoires_vives.repositories.RoleRepository;
import fr.memoires_vives.repositories.UserRepository;
import fr.memoires_vives.security.CustomUserDetails;
import fr.memoires_vives.security.CustomUserDetailsService;
import jakarta.persistence.EntityManager;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private RoleRepository roleRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private FileService fileService;

	@Mock
	private CustomUserDetailsService customUserDetailsService;

	@Mock
	private EntityManager entityManager;

	@InjectMocks
	private UserServiceImpl userService;

	@BeforeEach
	void setUp() {
		SecurityContextHolder.clearContext();
	}

	/**
	 * Helper pour simuler un utilisateur connecté dans le SecurityContext
	 */
	private void mockAuthenticatedUser(User user) {
		CustomUserDetails cud = new CustomUserDetails(user);
		UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(cud, cud.getPassword(),
				cud.getAuthorities());

		SecurityContextHolder.getContext().setAuthentication(auth);
	}

// Tests getUserByPseudo

	@Test
	void getUserByPseudo_returnsUserWhenFound() {
		String pseudo = "john";
		User mockUser = new User();
		mockUser.setPseudo(pseudo);

		when(userRepository.findByPseudo(pseudo)).thenReturn(Optional.of(mockUser));

		User result = userService.getUserByPseudo(pseudo);

		assertNotNull(result);
		assertEquals(pseudo, result.getPseudo());
		verify(userRepository, times(1)).findByPseudo(pseudo);
	}

	@Test
	void getUserByPseudo_throwsExceptionWhenNotFound() {
		String pseudo = "unknown";
		when(userRepository.findByPseudo(pseudo)).thenReturn(Optional.empty());

		assertThrows(EntityNotFoundException.class, () -> userService.getUserByPseudo(pseudo));

		verify(userRepository, times(1)).findByPseudo(pseudo);
	}

// Tests getUserByEmail

	@Test
	void getUserByEmail_returnsUserWhenFound() {
		String email = "john@doo.fr";
		User mockUser = new User();
		mockUser.setEmail(email);

		when(userRepository.findByEmail(email)).thenReturn(Optional.of(mockUser));

		User result = userService.getUserByEmail(email);

		assertNotNull(result);
		assertEquals(email, result.getEmail());
		verify(userRepository, times(1)).findByEmail(email);
	}

	@Test
	void getUserByEmail_returnsNullWhenNotFound() {
		String email = "unknown@notfound.fr";
		when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

		User result = userService.getUserByEmail(email);

		assertNull(result);

		verify(userRepository, times(1)).findByEmail(email);
	}

// Tests getUserById

	@Test
	void getUserById_returnsUserWhenFound() {
		long userId = 42L;
		User mockUser = new User();
		mockUser.setUserId(userId);
		mockUser.setFriends(new ArrayList<>());
		mockUser.setMemories(new ArrayList<>());

		when(userRepository.findByUserId(userId)).thenReturn(Optional.of(mockUser));

		User result = userService.getUserById(userId);

		assertNotNull(result);
		assertEquals(userId, result.getUserId());
		verify(userRepository, times(1)).findByUserId(userId);
	}

	@Test
	void getUserById_throwsExceptionWhenNotFound() {
		long userId = 42L;
		when(userRepository.findByUserId(userId)).thenReturn(Optional.empty());

		assertThrows(EntityNotFoundException.class, () -> userService.getUserById(userId));

		verify(userRepository, times(1)).findByUserId(userId);
	}

// Test getCurrentUser

	@Test
	void getCurrentUser_returnsNullWhenNoAuthentication() {
		SecurityContextHolder.clearContext();

		User result = userService.getCurrentUser();

		assertNull(result);
	}

	@Test
	void getCurrentUser_returnsNullWhenPrincipalNotCustomUserDetails() {
		UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken("notACustomUserDetails",
				null, new ArrayList<>());
		SecurityContextHolder.getContext().setAuthentication(auth);

		User result = userService.getCurrentUser();

		assertNull(result);
	}

	@Test
	void getCurrentUser_returnsNullWhenUserNotFound() {
		User mockUser = new User();
		mockUser.setUserId(42L);
		mockAuthenticatedUser(mockUser);

		when(userRepository.findByUserId(mockUser.getUserId())).thenReturn(Optional.empty());

		User result = userService.getCurrentUser();

		assertNull(result);
		verify(userRepository, times(1)).findByUserId(mockUser.getUserId());
	}

//	Test getUserOrCurrent

	@Test
	void getUserOrCurrent_returnsCurrentUser_whenUserIdIsNull() {
		User mockCurrentUser = new User();
		mockCurrentUser.setUserId(99L);

		UserServiceImpl spyUserService = Mockito.spy(userService);
		doReturn(mockCurrentUser).when(spyUserService).getCurrentUser();

		User result = spyUserService.getUserOrCurrent(null);

		assertNotNull(result);
		assertEquals(99L, result.getUserId());
		verify(spyUserService, times(1)).getCurrentUser();
		verify(spyUserService, never()).getUserById(anyLong());
	}

	@Test
	void getUserOrCurrent_returnsCurrentUser_whenUserIdIsZero() {
		User mockCurrentUser = new User();
		mockCurrentUser.setUserId(99L);

		UserServiceImpl spyUserService = Mockito.spy(userService);
		doReturn(mockCurrentUser).when(spyUserService).getCurrentUser();

		User result = spyUserService.getUserOrCurrent(0L);

		assertNotNull(result);
		assertEquals(99L, result.getUserId());
		verify(spyUserService, times(1)).getCurrentUser();
		verify(spyUserService, never()).getUserById(anyLong());
	}

	@Test
	void getUserOrCurrent_returnsUserById_whenUserIdProvided() {
		User mockUser = new User();
		mockUser.setUserId(42L);

		UserServiceImpl spyUserService = Mockito.spy(userService);
		doReturn(mockUser).when(spyUserService).getUserById(42L);

		User result = spyUserService.getUserOrCurrent(42L);

		assertNotNull(result);
		assertEquals(42L, result.getUserId());
		verify(spyUserService, never()).getCurrentUser();
		verify(spyUserService, times(1)).getUserById(42L);
	}

//  Test des méthodes privées de createAccount et updateProfile
//	Test de checkPassword

	@Test
	void createAccount_throwsValidationException_whenPasswordIsBlank() {
		String pseudo = "testuser";
		String email = "test@test.com";
		String password = "";
		String passwordConfirm = "";
		MultipartFile image = null;

		when(userRepository.findByPseudo(pseudo)).thenReturn(Optional.empty());
		when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

		ValidationException ex = assertThrows(ValidationException.class,
				() -> userService.createAccount(pseudo, email, password, passwordConfirm, image));

		assertTrue(ex.getFieldErrors().stream().anyMatch(
				err -> err.getField().equals("password") && err.getMessage().contains("ne peut pas être vide")));

		verify(userRepository, never()).save(any());
	}

	@Test
	void createAccount_throwsValidationException_whenPasswordsDoNotMatch() {
		String pseudo = "testuser";
		String email = "test@test.com";
		String password = "abc123";
		String passwordConfirm = "different";
		MultipartFile image = null;

		when(userRepository.findByPseudo(pseudo)).thenReturn(Optional.empty());
		when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

		ValidationException ex = assertThrows(ValidationException.class,
				() -> userService.createAccount(pseudo, email, password, passwordConfirm, image));

		assertTrue(ex.getFieldErrors().stream()
				.anyMatch(err -> err.getField().equals("password") && err.getMessage().contains("identiques")));

		verify(userRepository, never()).save(any());
	}

//  Test de checkPseudo

	@Test
	void createAccount_throwsValidationException_whenPseudoAlreadyUsed() {
		String pseudo = "testuser";
		String email = "test@test.com";
		String password = "abc123";
		String passwordConfirm = "abc123";
		MultipartFile image = null;

		User existingUser = new User();
		existingUser.setPseudo(pseudo);
		when(userRepository.findByPseudo(pseudo)).thenReturn(Optional.of(existingUser));

		when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

		ValidationException ex = assertThrows(ValidationException.class,
				() -> userService.createAccount(pseudo, email, password, passwordConfirm, image));

		assertTrue(ex.getFieldErrors().stream().anyMatch(err -> err.getField().equals("pseudo")));

		verify(userRepository, never()).save(any());
	}

//  Test de checkEmail	

	@Test
	void createAccount_throwsValidationException_whenEmailAlreadyUsed() {
		String pseudo = "testuser";
		String email = "test@test.com";
		String password = "abc123";
		String passwordConfirm = "abc123";
		MultipartFile image = null;

		User existingUser = new User();
		existingUser.setEmail(email);
		when(userRepository.findByEmail(email)).thenReturn(Optional.of(existingUser));

		when(userRepository.findByPseudo(pseudo)).thenReturn(Optional.empty());

		ValidationException ex = assertThrows(ValidationException.class,
				() -> userService.createAccount(pseudo, email, password, passwordConfirm, image));

		assertTrue(ex.getFieldErrors().stream().anyMatch(err -> err.getField().equals("email")));

		verify(userRepository, never()).save(any());
	}

//	Test de saveProfilImage

	@Test
	void createAccount_setsMediaUUID_whenImageIsSavedSuccessfully() throws Exception {
		String pseudo = "testuser";
		String email = "test@test.com";
		String password = "abc123";
		String passwordConfirm = "abc123";

		MultipartFile image = mock(MultipartFile.class);
		when(image.isEmpty()).thenReturn(false);

		when(userRepository.findByPseudo(pseudo)).thenReturn(Optional.empty());
		when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
		when(passwordEncoder.encode(password)).thenReturn("encodedPassword");
		when(fileService.saveUserFile(image, pseudo)).thenReturn("uuid-123");
		when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

		User created = userService.createAccount(pseudo, email, password, passwordConfirm, image);

		assertEquals("uuid-123", created.getMediaUUID());
	}

	@Test
	void createAccount_setsMediaUUID_null_whenImageIsNullOrEmpty() throws Exception {
		String pseudo = "testuser";
		String email = "test@test.com";
		String password = "abc123";
		String passwordConfirm = "abc123";

		MultipartFile image = null;

		when(userRepository.findByPseudo(pseudo)).thenReturn(Optional.empty());
		when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
		when(passwordEncoder.encode(password)).thenReturn("encodedPassword");
		when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

		User created = userService.createAccount(pseudo, email, password, passwordConfirm, image);

		assertNull(created.getMediaUUID());
	}

	@Test
	void createAccount_setsMediaUUID_null_whenFileServiceThrowsException() throws Exception {
		String pseudo = "testuser";
		String email = "test@test.com";
		String password = "abc123";
		String passwordConfirm = "abc123";

		MultipartFile image = mock(MultipartFile.class);
		when(image.isEmpty()).thenReturn(false);

		when(userRepository.findByPseudo(pseudo)).thenReturn(Optional.empty());
		when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
		when(passwordEncoder.encode(password)).thenReturn("encodedPassword");
		when(fileService.saveUserFile(image, pseudo)).thenThrow(new FileStorageException("fail"));
		when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

		User created = userService.createAccount(pseudo, email, password, passwordConfirm, image);

		assertNull(created.getMediaUUID());
	}

//	Test cas nominal createAccount

	@Test
	void createAccount_createsUserSuccessfully_whenAllInputsAreValid() throws Exception {
		String pseudo = "newuser";
		String email = "newuser@test.com";
		String password = "abc123";
		String passwordConfirm = "abc123";

		MultipartFile image = mock(MultipartFile.class);
		when(image.isEmpty()).thenReturn(false);

		Role userRole = new Role();
		userRole.setName("ROLE_USER");

		when(userRepository.findByPseudo(pseudo)).thenReturn(Optional.empty());
		when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
		when(passwordEncoder.encode(password)).thenReturn("encodedPassword");
		when(fileService.saveUserFile(image, pseudo)).thenReturn("uuid-123");
		when(roleRepository.findByName("ROLE_USER")).thenReturn(userRole);
		when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

		User created = userService.createAccount(pseudo, email, password, passwordConfirm, image);

		assertNotNull(created);
		assertEquals(pseudo, created.getPseudo());
		assertEquals(email, created.getEmail());
		assertEquals("encodedPassword", created.getPassword());
		assertFalse(created.isAdmin());
		assertFalse(created.isActivated());
		assertEquals(1, created.getRoles().size());
		assertTrue(created.getRoles().contains(userRole));
		assertEquals("uuid-123", created.getMediaUUID());

		verify(userRepository, times(1)).save(any());
		verify(fileService, times(1)).saveUserFile(image, pseudo);
	}

//  Tests de assignUserRole

	@Test
	void createAccount_assignsExistingRoleToUser() {
		String pseudo = "john";
		String email = "john@doe.fr";
		String password = "password123";
		String passwordConfirm = "password123";
		MultipartFile image = null;

		Role existingRole = new Role();
		existingRole.setName("ROLE_USER");
		when(roleRepository.findByName("ROLE_USER")).thenReturn(existingRole);
		when(passwordEncoder.encode(passwordConfirm)).thenReturn("encodedPassword");
		when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

		User createdUser = userService.createAccount(pseudo, email, password, passwordConfirm, image);

		assertNotNull(createdUser);
		assertEquals(1, createdUser.getRoles().size());
		assertTrue(createdUser.getRoles().contains(existingRole));
		verify(roleRepository, times(1)).findByName("ROLE_USER");
		verify(roleRepository, never()).save(any());
	}

// Test de createAccount

	@SuppressWarnings("serial")
	@Test
	void createAccount_throwsValidationException_whenUserRepositorySaveFails() {
		String pseudo = "john";
		String email = "john@doe.fr";
		String password = "password123";
		String passwordConfirm = "password123";
		MultipartFile image = null;

		when(passwordEncoder.encode(passwordConfirm)).thenReturn("encodedPassword");
		when(userRepository.save(any())).thenThrow(new DataAccessException("DB error") {
		});

		ValidationException exception = assertThrows(ValidationException.class,
				() -> userService.createAccount(pseudo, email, password, passwordConfirm, image));

		assertTrue(exception.hasError());
		assertEquals(1, exception.getGlobalErrors().size());
		assertEquals("Un problème est survenu lors de l'accès à la base de données.",
				exception.getGlobalErrors().get(0));
	}

//	Test de updateProfile

	@Test
	void updateProfile_throwsUnauthorizedActionException_whenUserIsNotAdminAndDifferentUser() {
		User currentUser = new User();
		currentUser.setUserId(1L);

		User targetUser = new User();
		targetUser.setUserId(2L); // différent du courant

		User updatedData = new User();
		updatedData.setUserId(targetUser.getUserId());

		UserServiceImpl spyUserService = Mockito.spy(userService);
		doReturn(currentUser).when(spyUserService).getCurrentUser();
		doReturn(targetUser).when(spyUserService).getUserById(targetUser.getUserId());
		doReturn(false).when(spyUserService).isAdmin();

		assertThrows(UnauthorizedActionException.class,
				() -> spyUserService.updateProfile(updatedData, "somePassword", null));
	}

	@Test
	void updateProfile_throwsValidationException_whenCurrentPasswordIsInvalid() {
		User currentUser = new User();
		currentUser.setUserId(1L);
		currentUser.setPseudo("currentPseudo");
		currentUser.setEmail("newuser@test.com");
		currentUser.setPassword("encodedCurrentPassword");

		User updatedData = new User();
		updatedData.setUserId(1L);
		updatedData.setPseudo("currentPseudo");
		updatedData.setEmail("newuser@test.com");
		updatedData.setPassword("");
		updatedData.setPasswordConfirm("");

		UserServiceImpl spyUserService = Mockito.spy(userService);
		doReturn(currentUser).when(spyUserService).getCurrentUser();
		doReturn(currentUser).when(spyUserService).getUserById(currentUser.getUserId());
		doReturn(false).when(spyUserService).isAdmin();
		when(passwordEncoder.matches("wrongPassword", currentUser.getPassword())).thenReturn(false);

		ValidationException ex = assertThrows(ValidationException.class,
				() -> spyUserService.updateProfile(updatedData, "wrongPassword", null));

		assertTrue(ex.getFieldErrors().stream().anyMatch(err -> err.getField().equals("currentPassword")));
	}

	@Test
	void updateProfile_throwsEntityNotFound_whenUserDoesNotExist() {
		User updatedData = new User();
		updatedData.setUserId(1L);

		when(userRepository.findByUserId(1L)).thenReturn(Optional.empty());

		assertThrows(EntityNotFoundException.class,
				() -> userService.updateProfile(updatedData, "currentPassword", null));
	}

//  Tests de getAllUsers

	@Test
	void getAllUsers_returnsListOfUsers() {
		List<User> mockUsers = new ArrayList<>();
		mockUsers.add(new User());
		mockUsers.add(new User());

		when(userRepository.findAll()).thenReturn(mockUsers);

		List<User> result = userService.getAllUsers();

		assertNotNull(result);
		assertEquals(2, result.size());
		verify(userRepository, times(1)).findAll();
	}

	@Test
    void getAllUsers_returnsEmptyList_whenNoUsers() {
        when(userRepository.findAll()).thenReturn(new ArrayList<>());

        List<User> result = userService.getAllUsers();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(userRepository, times(1)).findAll();
    }

//  Tests de updatePassword

    @Test
    void updatePassword_throwsValidationException_whenPasswordIsBlank() {
        User user = new User();
        user.setUserId(1L);

        ValidationException ve = assertThrows(ValidationException.class, () -> {
            userService.updatePassword(user, "");
        });

        assertTrue(ve.getFieldErrors().stream()
                .anyMatch(fe -> fe.getField().equals("currentPassword")));
    }

    @Test
    void updatePassword_throwsEntityNotFoundException_whenUserNotFound() {
        User user = new User();
        user.setUserId(1L);

        when(userRepository.findByUserId(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            userService.updatePassword(user, "newPassword");
        });
    }

    @Test
    void updatePassword_setsHashedPassword_whenValid() {
        User user = new User();
        user.setUserId(1L);
        User managedUser = new User();
        managedUser.setUserId(1L);

        when(userRepository.findByUserId(1L)).thenReturn(Optional.of(managedUser));
        when(passwordEncoder.encode("newPassword")).thenReturn("hashedPassword");

        userService.updatePassword(user, "newPassword");

        assertEquals("hashedPassword", managedUser.getPassword());
    }

}
