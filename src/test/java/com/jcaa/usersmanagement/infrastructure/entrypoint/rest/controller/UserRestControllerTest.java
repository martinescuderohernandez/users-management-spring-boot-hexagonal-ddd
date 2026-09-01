package com.jcaa.usersmanagement.infrastructure.entrypoint.rest.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jcaa.usersmanagement.application.port.in.CreateUserUseCase;
import com.jcaa.usersmanagement.application.port.in.DeleteUserUseCase;
import com.jcaa.usersmanagement.application.port.in.GetAllUsersUseCase;
import com.jcaa.usersmanagement.application.port.in.GetUserByIdUseCase;
import com.jcaa.usersmanagement.application.port.in.UpdateUserUseCase;
import com.jcaa.usersmanagement.application.service.dto.query.GetUserByIdQuery;
import com.jcaa.usersmanagement.domain.enums.UserRole;
import com.jcaa.usersmanagement.domain.enums.UserStatus;
import com.jcaa.usersmanagement.domain.model.UserModel;
import com.jcaa.usersmanagement.domain.valueobject.UserEmail;
import com.jcaa.usersmanagement.domain.valueobject.UserId;
import com.jcaa.usersmanagement.domain.valueobject.UserName;
import com.jcaa.usersmanagement.domain.valueobject.UserPassword;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
    controllers = UserRestController.class,
    properties = "spring.main.web-application-type=servlet")
@DisplayName("UserRestController")
class UserRestControllerTest {

  private static final String ID = "8f684dc4-0c67-4813-8d58-7b5e7f7937b8";

  @Autowired private MockMvc mockMvc;

  @MockBean private CreateUserUseCase createUserUseCase;
  @MockBean private UpdateUserUseCase updateUserUseCase;
  @MockBean private DeleteUserUseCase deleteUserUseCase;
  @MockBean private GetUserByIdUseCase getUserByIdUseCase;
  @MockBean private GetAllUsersUseCase getAllUsersUseCase;

  @Test
  @DisplayName("GET /api/users/{id} debe responder usando el caso de uso inyectado por Spring")
  void shouldGetUserByIdThroughSpringMvc() throws Exception {
    // Arrange
    final UserModel user =
        new UserModel(
            new UserId(ID),
            new UserName("Ada Lovelace"),
            new UserEmail("ada@example.com"),
            UserPassword.fromHash("$2a$12$abcdefghijklmnopqrstuuuuuuuuuuuuuuuuuuuuuuuuuuuuu"),
            UserRole.ADMIN,
            UserStatus.ACTIVE);
    when(getUserByIdUseCase.execute(new GetUserByIdQuery(ID))).thenReturn(user);

    // Act & Assert
    mockMvc
        .perform(get("/api/users/{id}", ID))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(ID))
        .andExpect(jsonPath("$.name").value("Ada Lovelace"))
        .andExpect(jsonPath("$.email").value("ada@example.com"))
        .andExpect(jsonPath("$.role").value("ADMIN"))
        .andExpect(jsonPath("$.status").value("ACTIVE"));
  }

  @Test
  @DisplayName("POST /api/users debe rechazar una solicitud inválida antes del caso de uso")
  void shouldRejectInvalidCreateRequest() throws Exception {
    // Arrange
    final String invalidRequest =
        """
        {
          "id": "",
          "name": "A",
          "email": "invalid",
          "password": "short",
          "role": ""
        }
        """;

    // Act & Assert
    mockMvc
        .perform(post("/api/users").contentType(MediaType.APPLICATION_JSON).content(invalidRequest))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400));
  }
}
