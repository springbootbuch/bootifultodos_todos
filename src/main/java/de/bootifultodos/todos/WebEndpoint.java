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
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

/**
 * @author Michael J. Simons, 2017-04-05
 */
@RequiredArgsConstructor
@Controller
public class WebEndpoint {

	private final TodoRepository todoRepository;

	private final TodoValidator todoValidator;

	@InitBinder
	void initBinder(final WebDataBinder binder) {
		binder.addValidators(todoValidator);
	}

	@ModelAttribute(name = "statii")
	Status[] statii() {
		return Todo.Status.values();
	}

	@GetMapping(path = {"/todos/new", "/todos/{id}"})
	public String form(@PathVariable final Optional<Long> id, final Model model) {
		final Optional<Todo> todo = id.flatMap(todoRepository::findOne);
		model
			.addAttribute("id", todo.map(Todo::getId).orElse(null))
			.addAttribute("todo", todo.orElseGet(Todo::new))
			.addAttribute("method", todo.isPresent() ? "PUT" : "POST");
		return "form";
	}

	@PostMapping(path = "/todos")
	public String create(@Valid final Todo newTodo, final BindingResult bindingResult) {
		String rv = "form";
		if (!bindingResult.hasErrors()) {
			rv = "redirect:/todos/" + this.todoRepository.save(newTodo).getId();
		}
		return rv;
	}

	@PutMapping(path = "/todos/{id}")
	public String update(
		@PathVariable final Long id,
		@Valid final Todo updatedTodo, final BindingResult bindingResult
	) throws NoSuchMethodException {
		final Todo todo = this.todoRepository
			.findOne(id)
			.orElseThrow(TodoNotFoundException::new);

		String rv = "form";
		if (!bindingResult.hasErrors()) {
			todo.setAufgabe(updatedTodo.getAufgabe());
			todo.setStatus(updatedTodo.getStatus());
			rv = "redirect:/todos/" + this.todoRepository.save(todo).getId();
		}

		return rv;
	}
}
