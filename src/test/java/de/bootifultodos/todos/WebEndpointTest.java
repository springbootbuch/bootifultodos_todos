/*
 * Copyright 2017 michael-simons.eu.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.bootifultodos.todos;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.AdditionalAnswers.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.context.annotation.FilterType.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import de.bootifultodos.todos.Todo.Status;

import java.util.Locale;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.FixedLocaleResolver;

/**
 * @author Michael J. Simons, 2017-04-05
 */
@ExtendWith(SpringExtension.class)
@WebMvcTest(
	includeFilters
	= @Filter(type = ASSIGNABLE_TYPE, classes = TodoValidator.class)
)
public class WebEndpointTest {

	/**
	 * Makes the tests locale aware.
	 */
	@TestConfiguration
	static class Config {
		
		@Bean
		public LocaleResolver localeResolver() {
			return new FixedLocaleResolver(Locale.GERMANY);
		}
	}
	
	@MockBean
	private TodoRepository todoRepository;
	
	@Autowired
	private MockMvc mvc;
	
	@Test
	void emptyFormShouldWork() throws Exception {
		this.mvc
			.perform(get("/todos/new").with(user("test")))
			.andExpect(status().isOk())
			.andExpect(view().name("form"))
			.andExpect(model().attributeDoesNotExist("id"))
			.andExpect(model().attributeExists("todo"))
			.andExpect(model().attribute("method", "POST"))
			.andExpect(model().attribute("statii", Todo.Status.values()));
	}
	
	@Test
	void filledFormShouldWork() throws Exception {
		final Todo todo = new Todo();
		ReflectionTestUtils.setField(todo, "id", 23L);
		todo.setAufgabe("test");
		todo.setStatus(Todo.Status.OFFEN);
		when(todoRepository.findOne(23L)).thenReturn(Optional.of(todo));
		
		this.mvc
			.perform(get("/todos/23").with(user("test")))
			.andExpect(status().isOk())
			.andExpect(view().name("form"))
			.andExpect(model().attribute("id", 23L))
			.andExpect(model().attribute("todo", todo))
			.andExpect(model().attribute("method", "PUT"))
			.andExpect(model().attribute("statii", Todo.Status.values()));
	}
	
	@Test
	@WithMockUser
	void createShouldWorkWithValidData() throws Exception {
		final Todo todo = new Todo();
		ReflectionTestUtils.setField(todo, "id", 23L);
		when(todoRepository.save(any(Todo.class))).thenReturn(todo);
		
		this.mvc
			.perform(
				post("/todos")
					.with(user("test")).with(csrf())
					.param("aufgabe", "test")
					.param("status", "OFFEN"))
			.andExpect(status().isFound())
			.andExpect(view().name("redirect:/todos/23"));
	}
	
	@Test
	void createShouldWorkWithInvalidData() throws Exception {
		final Todo todo = new Todo();
		ReflectionTestUtils.setField(todo, "id", 23L);
		when(todoRepository.save(any(Todo.class))).thenReturn(todo);
		
		this.mvc
			.perform(post("/todos").with(user("test")).with(csrf()))
			.andExpect(status().isOk())
			.andExpect(model().attributeHasFieldErrors("todo", "aufgabe"))
			.andExpect(view().name("form"));
	}
	
	@Test
	void updateShouldWorkWithInvalidTodo() throws Exception {
		when(todoRepository.findOne(23L)).thenReturn(Optional.empty());
		
		this.mvc
			.perform(put("/todos/23")
				.with(user("test")).with(csrf())
				.param("aufgabe", "test")
				.param("status", "ERLEDIGT"))
			.andExpect(status().isNotFound());
	}
	
	@Test
	void updateShouldWorkWithValidData() throws Exception {
		final Todo todo = new Todo();
		ReflectionTestUtils.setField(todo, "id", 23L);
		when(todoRepository.findOne(23L)).thenReturn(Optional.of(todo));
		when(todoRepository.save(any(Todo.class))).then(returnsFirstArg());
		
		this.mvc
			.perform(put("/todos/23")
				.with(user("test")).with(csrf())
				.param("aufgabe", "test")
				.param("status", "ERLEDIGT"))
			.andExpect(status().isFound())
			.andExpect(view().name("redirect:/todos/23"));
		
		final ArgumentCaptor<Todo> locationArg = ArgumentCaptor.forClass(Todo.class);
		verify(todoRepository).save(locationArg.capture());
		final Todo updatedTodo = locationArg.getValue();
		assertThat(updatedTodo.getAufgabe()).isEqualTo("test");
		assertThat(updatedTodo.getStatus()).isEqualTo(Status.ERLEDIGT);
	}
	
	@Test
	void updateShouldWorkWithInvalidData() throws Exception {
		final Todo todo = new Todo();
		ReflectionTestUtils.setField(todo, "id", 23L);
		when(todoRepository.findOne(23L)).thenReturn(Optional.of(todo));
		
		this.mvc
			.perform(put("/todos/23").with(user("test")).with(csrf()))
			.andExpect(status().isOk())
			.andExpect(model().attributeHasFieldErrors("todo", "aufgabe"))
			.andExpect(view().name("form"));
	}
}
