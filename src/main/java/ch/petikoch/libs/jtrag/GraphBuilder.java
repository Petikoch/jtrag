/**
 * Copyright 2015 Peti Koch
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ch.petikoch.libs.jtrag;

import ch.petikoch.libs.jtwfg.Graph;

/**
 * A builder class for graph instance creation. Can be used concurrently by different threads which create together the
 * graph.
 * <p/>
 * Thread-safe.
 *
 * @param <T>
 * 		The type of the ID of the tasks. Something with a meaningful {@link Object#equals(Object)} and {@link
 * 		Object#hashCode()} implementation like {@link String}, {@link Long} or a class of your domain model which is fine
 * 		to use as a key e.g. in a {@link java.util.HashMap}. If T implements Comparable, then you get sorted collections.
 * @param <R>
 * 		The type of the ID of the resources. Something with a meaningful {@link Object#equals(Object)} and {@link
 * 		Object#hashCode()} implementation like {@link String}, {@link Long} or a class of your domain model which is fine
 * 		to use as a key e.g. in a {@link java.util.HashMap}. If T implements Comparable, then you get sorted collections.
 */
public class GraphBuilder<T, R> {

	private final ch.petikoch.libs.jtwfg.GraphBuilder<Object> jtwfgGraphBuilderDelegate;

	public GraphBuilder() {
		this(new ch.petikoch.libs.jtwfg.GraphBuilder<>());
	}

	private GraphBuilder(final ch.petikoch.libs.jtwfg.GraphBuilder<Object> jtwfgGraphBuilderDelegate) {
		this.jtwfgGraphBuilderDelegate = jtwfgGraphBuilderDelegate;
	}

	public GraphBuilder<T, R> addTask(T taskId) {
		jtwfgGraphBuilderDelegate.addTask(taskId);
		return this;
	}

	public GraphBuilder<T, R> addTasks(Iterable<T> taskIds) {
		//noinspection unchecked
		jtwfgGraphBuilderDelegate.addTasks((Iterable<Object>) taskIds);
		return this;
	}

	public boolean hasTask(T taskId) {
		return jtwfgGraphBuilderDelegate.hasTask(taskId);
	}

	public GraphBuilder<T, R> removeTask(T taskId) {
		jtwfgGraphBuilderDelegate.removeTask(taskId);
		return this;
	}

	public GraphBuilder<T, R> removeTasks(Iterable<T> taskIds) {
		//noinspection unchecked
		jtwfgGraphBuilderDelegate.removeTasks((Iterable<Object>) taskIds);
		return this;
	}

	public GraphBuilder<T, R> addResource(R resourceId) {
		jtwfgGraphBuilderDelegate.addTask(resourceId);
		return this;
	}

	public GraphBuilder<T, R> addResources(Iterable<R> resourceIds) {
		//noinspection unchecked
		jtwfgGraphBuilderDelegate.addTasks((Iterable<Object>) resourceIds);
		return this;
	}

	public boolean hasResource(R resourceId) {
		return jtwfgGraphBuilderDelegate.hasTask(resourceId);
	}

	public GraphBuilder<T, R> removeResource(R resourceId) {
		jtwfgGraphBuilderDelegate.removeTask(resourceId);
		return this;
	}

	public GraphBuilder<T, R> removeResources(Iterable<R> resourceIds) {
		//noinspection unchecked
		jtwfgGraphBuilderDelegate.removeTasks((Iterable<Object>) resourceIds);
		return this;
	}

	public GraphBuilder<T, R> addTask2ResourceDependency(T taskId, R resourceId) {
		jtwfgGraphBuilderDelegate.addTaskWaitsFor(taskId, resourceId);
		return this;
	}

	public GraphBuilder<T, R> removeTask2ResourceDependency(T taskId, R resourceId) {
		jtwfgGraphBuilderDelegate.removeTaskWaitForDependency(taskId, resourceId);
		return this;
	}

	public GraphBuilder<T, R> addResource2TaskAssignment(R resourceId, T taskId) {
		jtwfgGraphBuilderDelegate.addTaskWaitsFor(resourceId, taskId);
		return this;
	}

	public GraphBuilder<T, R> removeResource2TaskAssignment(R resourceId, T taskId) {
		jtwfgGraphBuilderDelegate.removeTaskWaitForDependency(resourceId, taskId);
		return this;
	}

	public Graph<Object> build() {
		return jtwfgGraphBuilderDelegate.build();
	}

}
