/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.hateoas;

import static org.assertj.core.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

/**
 * @author Oliver Drotbohm
 */
public class FundamentalsTest {

	@Test
	public void links() {

		// tag::links[]
		Link link = new Link("/something");
		assertThat(link.getHref()).isEqualTo("/something");
		assertThat(link.getRel()).isEqualTo(IanaLinkRelations.SELF);

		link = new Link("/something", "my-rel");
		assertThat(link.getHref()).isEqualTo("/something");
		assertThat(link.getRel()).isEqualTo(LinkRelation.of("my-rel"));
		// end::links[]
	}

	@Test
	public void templatedLinks() {

		// tag::templatedLinks[]
		Link link = new Link("/{segment}/something{?parameter}");
		assertThat(link.isTemplated()).isTrue(); // <1>
		assertThat(link.getVariableNames()).contains("segment", "parameter"); // <2>

		Map<String, Object> values = new HashMap<>();
		values.put("segment", "path");
		values.put("parameter", 42);

		assertThat(link.expand(values).getHref()) // <3>
				.isEqualTo("/path/something?parameter=42");
		// end::templatedLinks[]
	}
}
