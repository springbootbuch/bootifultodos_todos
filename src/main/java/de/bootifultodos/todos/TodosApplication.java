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

import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.rest.core.event.ValidatingRepositoryEventListener;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurerAdapter;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.data.repository.query.SecurityEvaluationContextExtension;

/**
 * @author Michael J. Simons, 2017-03-10
 */
@SuppressWarnings({"checkstyle:designforextension"})
@RequiredArgsConstructor
@SpringBootApplication
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class TodosApplication extends RepositoryRestConfigurerAdapter {

	public static void main(final String... args) {
		SpringApplication.run(TodosApplication.class, args);
	}

	private final TodoValidator todoValidator;

	@Override
	public void configureValidatingRepositoryEventListener(final ValidatingRepositoryEventListener el) {
		el.addValidator("beforeCreate", todoValidator);
		el.addValidator("beforeSave", todoValidator);
	}

	@Bean
	public SecurityEvaluationContextExtension
		securityEvaluationContextExtension() {
		return new SecurityEvaluationContextExtension();
	}
}
