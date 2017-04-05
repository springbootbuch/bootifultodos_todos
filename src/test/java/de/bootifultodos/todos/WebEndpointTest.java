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

import de.bootifultodos.todos.Todo.Status;
import java.util.Optional;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import org.mockito.ArgumentCaptor;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan.Filter;
import static org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * @author Michael J. Simons, 2017-04-05
 */
@RunWith(SpringRunner.class)
@WebMvcTest(
	includeFilters
	= @Filter(type = ASSIGNABLE_TYPE, classes = TodoValidator.class)
)
public class WebEndpointTest {

	@MockBean
	private TodoRepository todoRepository;

	@Autowired
	private MockMvc mvc;

	@Test
	public void emptyFormShouldWork() throws Exception {
		this.mvc
			.perform(get("/todos/new"))
			.andExpect(status().isOk())
			.andExpect(view().name("form"))
			.andExpect(model().attributeDoesNotExist("id"))
			.andExpect(model().attributeExists("todo"))
			.andExpect(model().attribute("method", "POST"))
			.andExpect(model().attribute("statii", Todo.Status.values()));
	}

	@Test
	public void filledFormShouldWork() throws Exception {
		final Todo todo = new Todo();
		todo.setAufgabe("test");
		todo.setStatus(Todo.Status.OFFEN);
		when(todoRepository.findOne("23")).thenReturn(Optional.of(todo));

		this.mvc
			.perform(get("/todos/23"))
			.andExpect(status().isOk())
			.andExpect(view().name("form"))
			.andExpect(model().attribute("id", "23"))
			.andExpect(model().attribute("todo", todo))
			.andExpect(model().attribute("method", "PUT"))
			.andExpect(model().attribute("statii", Todo.Status.values()));
	}

	@Test
	public void createShouldWorkWithValidData() throws Exception {
		final Todo todo = new Todo();
		ReflectionTestUtils.setField(todo, "id", "23");
		when(todoRepository.save(any(Todo.class))).thenReturn(todo);

		this.mvc
			.perform(
				post("/todos")
					.param("aufgabe", "test")
					.param("status", "OFFEN"))
			.andExpect(status().isFound())
			.andExpect(view().name("redirect:/todos/23"));
	}

	@Test
	public void createShouldWorkWithInValidData() throws Exception {
		final Todo todo = new Todo();
		ReflectionTestUtils.setField(todo, "id", "23");
		when(todoRepository.save(any(Todo.class))).thenReturn(todo);
		
		this.mvc
			.perform(post("/todos"))
			.andExpect(status().isOk())
			.andExpect(model().attributeHasFieldErrors("todo", "aufgabe", "status"))
			.andExpect(view().name("form"));
	}

	@Test
	public void updateShouldWorkWithInvalidTodo() throws Exception {
		when(todoRepository.findOne("23")).thenReturn(Optional.empty());
		
		this.mvc
			.perform(put("/todos/23")
				.param("aufgabe", "test")
				.param("status", "ERLEDIGT"))
			.andExpect(status().isNotFound());
	}

	@Test
	public void updateShouldWorkWithValidData() throws Exception {
		final Todo todo = new Todo();
		ReflectionTestUtils.setField(todo, "id", "23");
		when(todoRepository.findOne("23")).thenReturn(Optional.of(todo));
		when(todoRepository.save(any(Todo.class))).then(returnsFirstArg());

		this.mvc
			.perform(put("/todos/23")
				.param("aufgabe", "test")
				.param("status", "ERLEDIGT"))
			.andExpect(status().isFound())
			.andExpect(view().name("redirect:/todos/23"));

		final ArgumentCaptor<Todo> locationArg = ArgumentCaptor.forClass(Todo.class);
		verify(todoRepository).save(locationArg.capture());
		final Todo updatedTodo = locationArg.getValue();
		assertThat(updatedTodo.getAufgabe(), is("test"));
		assertThat(updatedTodo.getStatus(), is(Status.ERLEDIGT));
	}
	
	@Test
	public void updateShouldWorkWithInValidData() throws Exception {
		final Todo todo = new Todo();
		ReflectionTestUtils.setField(todo, "id", "23");
		when(todoRepository.findOne("23")).thenReturn(Optional.of(todo));
		
		this.mvc
			.perform(put("/todos/23"))
			.andExpect(status().isOk())
			.andExpect(model().attributeHasFieldErrors("todo", "aufgabe", "status"))
			.andExpect(view().name("form"));
	}
}
