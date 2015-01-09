/*
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
package ch.petikoch.libs.jtrag

import ch.petikoch.libs.jtwfg.DeadlockDetector
import ch.petikoch.libs.jtwfg.Graph
import com.google.common.collect.Multimap
import com.google.common.collect.Sets
import com.google.common.collect.TreeMultimap
import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.Sortable
import spock.lang.Specification

@SuppressWarnings("GroovyPointlessBoolean")
class ExecutableDocumentation extends Specification {

	def 'Use case 1: Check for deadlocks at a specific moment in time'() {

		given: 'Your own domain model with some kind of tasks and dependencies between them'

		Set<String> yourModelTasks = Sets.newTreeSet()
		yourModelTasks.add('t1')
		yourModelTasks.add('t2')

		Set<String> yourModelResources = Sets.newTreeSet()
		yourModelResources.add('r1')
		yourModelResources.add('r1')

		Multimap<String, String> yourModelTask2ResourceDependencies = TreeMultimap.create()
		yourModelTask2ResourceDependencies.put('t1', 'r1')
		yourModelTask2ResourceDependencies.put('t2', 'r2')

		Multimap<String, String> yourModelResource2TaskAssignment = TreeMultimap.create()
		yourModelResource2TaskAssignment.put('r1', 't2')
		yourModelResource2TaskAssignment.put('r2', 't1')

		when: 'you want to know if you have a deadlock in it, you transform your model into the jtrag model using the GraphBuilder'

		def jtragGraphBuilder = new GraphBuilder<String, String>()
		jtragGraphBuilder.addTasks(yourModelTasks)
		jtragGraphBuilder.addResources(yourModelResources)
		yourModelTask2ResourceDependencies.keySet().each { String taskId ->
			def resourceDependencies = yourModelTask2ResourceDependencies.get(taskId)
			resourceDependencies.each { String resourceId ->
				jtragGraphBuilder.addTask2ResourceDependency(taskId, resourceId)
			}
		}
		yourModelResource2TaskAssignment.keySet().each { String resourceId ->
			def taskAssignments = yourModelResource2TaskAssignment.get(resourceId)
			taskAssignments.each { String taskId ->
				jtragGraphBuilder.addResource2TaskAssignment(resourceId, taskId)
			}
		}

		and: 'build the graph'

		def jtwfgGraph = jtragGraphBuilder.build()

		and: 'run a deadlock analysis using the jtwfg deadlock detector'

		def analysisResult = new DeadlockDetector().analyze(jtwfgGraph)

		then: 'you check for deadlocks in the analysis report'

		analysisResult.hasDeadlock() == true
		analysisResult.deadlockCycles.size() == 1

		and: 'you see where the deadlock is'

		analysisResult.deadlockCycles.getAt(0).getCycleTasks() == ['r1', 't2', 'r2', 't1', 'r1']
	}

	def 'Custom types for task and resource IDs in the graph: You can use standard types like String, Integer, ... or you can use you own custom types from e.g. your domain model'() {
		given:
		def jtragGraphBuilder = new GraphBuilder<CustomTaskId, CustomResourceId>()
		def customTaskId1 = new CustomTaskId('t1')
		def customTaskId2 = new CustomTaskId('t2')
		def customResourceId1 = new CustomResourceId('r1')
		def customResourceId2 = new CustomResourceId('r2')

		when:
		jtragGraphBuilder.addTask(customTaskId1)
		jtragGraphBuilder.addTask(customTaskId2)
		jtragGraphBuilder.addResource(customResourceId1)
		jtragGraphBuilder.addResource(customResourceId2)
		Graph<Object> graph = jtragGraphBuilder.build()

		then:
		graph.getTasks().size() == 4
		graph.getTasks().collect { it.id }.toSet() ==
				[customTaskId1, customTaskId2, customResourceId1, customResourceId2] as Set
	}

	@CompileStatic
	// Groovy's Sortable = Java's Comparable: MAY
	@Sortable
	// MUST! Custom task id types must have a reasonable implementation of hashCode and equals
	@EqualsAndHashCode
	private static class CustomTaskId {

		final String internalId

		CustomTaskId(final String internalId) {
			this.internalId = internalId
		}
	}

	@CompileStatic
	// Groovy's Sortable = Java's Comparable: MAY
	@Sortable
	// MUST! Custom task id types must have a reasonable implementation of hashCode and equals
	@EqualsAndHashCode
	private static class CustomResourceId {

		final String internalId

		CustomResourceId(final String internalId) {
			this.internalId = internalId
		}
	}
}

