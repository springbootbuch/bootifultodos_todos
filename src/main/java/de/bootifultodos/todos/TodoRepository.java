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

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * Beinhaltet alle Todos.
 *
 * @author Michael J. Simons, 2017-03-13
 */
@RestResource(path = "todos", rel = "todos")
public interface TodoRepository extends Repository<Todo, Long> {
	@PreAuthorize("(#entity.userId ?: authentication.name) == authentication.name")
	Todo save(Todo entity);

	@PostAuthorize("(returnObject.orElse(null)?.userId ?: authentication.name) == authentication.name")
	Optional<Todo> findOne(Long id);

	@Query("Select e from #{#entityName} e where e.userId = ?#{authentication.name}")
	List<Todo> findAll();
}
