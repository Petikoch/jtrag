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
import com.google.common.collect.Multimaps
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

		analysisResult.deadlockCycles[0].getCycleTasks() == ['r1', 't2', 'r2', 't1', 'r1']
		analysisResult.deadlockCycles[0].toString() == 'DeadlockCycle: r1 -> t2 -> r2 -> t1 -> r1'
	}

	def 'Use case 2: As you update your domain model, you update the jtrag model and check for deadlocks'() {

		given: 'Your own domain model with some kind of tasks, resources and dependencies between them'

		Set<String> yourModelTasks = Sets.newTreeSet().asSynchronized()
		Set<String> yourModelResources = Sets.newTreeSet().asSynchronized()
		Multimap<String, String> yourModelTask2ResourceDependencies = Multimaps.synchronizedSortedSetMultimap(TreeMultimap.create())
		Multimap<String, String> yourModelResource2TaskAssignments = Multimaps.synchronizedSortedSetMultimap(TreeMultimap.create())

		and: 'the jtrag GraphBuilder with a DeadlockDetector'

		def jtragGraphBuilder = new GraphBuilder<String, String>()
		def jtwfgDeadlockDetector = new DeadlockDetector<Object>()

		when: 'you add a task into your model, you add it also into the jtwfg model (might be a separate thread)'

		yourModelTasks.add('t1')
		jtragGraphBuilder.addTask('t1')

		then: 'you immediately check for deadlocks'
		jtwfgDeadlockDetector.analyze(jtragGraphBuilder.build()).hasDeadlock() == false

		when: 'you add a resource into your model, you add it also into the jtwfg model (might be a separate thread)'

		yourModelResources.add('r1')
		jtragGraphBuilder.addResource('r1')

		then: 'you immediately check for deadlocks'
		jtwfgDeadlockDetector.analyze(jtragGraphBuilder.build()).hasDeadlock() == false

		when: 'you add more tasks and resources, you update your model and the jtwfg model (might be a separate thread)'

		yourModelTask2ResourceDependencies.put('t1', 'r1')
		jtragGraphBuilder.addTask2ResourceDependency('t1', 'r1')
		yourModelResource2TaskAssignments.put('r1', 't2')
		jtragGraphBuilder.addResource2TaskAssignment('r1', 't2')
		yourModelTask2ResourceDependencies.put('t2', 'r2')
		jtragGraphBuilder.addTask2ResourceDependency('t2', 'r2')
		yourModelResource2TaskAssignments.put('r2', 't1')
		jtragGraphBuilder.addResource2TaskAssignment('r2', 't1')

		and: 'you immediately check for deadlocks again'

		def analysisReport = jtwfgDeadlockDetector.analyze(jtragGraphBuilder.build())

		then: 'you see if you have a deadlock'

		analysisReport.hasDeadlock() == true

		and: 'you see where the deadlock is'

		analysisReport.deadlockCycles.size() == 1
		analysisReport.deadlockCycles[0].getCycleTasks() == ['r1', 't2', 'r2', 't1', 'r1']
		analysisReport.deadlockCycles[0].toString() == 'DeadlockCycle: r1 -> t2 -> r2 -> t1 -> r1'

		and: 'you can also ask if a certain task is deadlocked'
		analysisReport.isDeadlocked('t1')
		analysisReport.isDeadlocked('t2')
		analysisReport.isDeadlocked('r1')
		analysisReport.isDeadlocked('r2')
	}

	def 'Custom types for task and resource IDs in the graph: You can use standard types like String, Integer, ... or you can use you own custom types from e.g. your domain model'() {
		given:
		def jtragGraphBuilder = new GraphBuilder<CustomTaskId, CustomResourceId>()
		def customTaskId1 = new CustomTaskId('t1')
		def customTaskId2 = new CustomTaskId('t2')
		def customTaskId3 = new CustomTaskId('t3')
		def customResourceId1 = new CustomResourceId('r1')
		def customResourceId2 = new CustomResourceId('r2')
		def customResourceId3 = new CustomResourceId('r3')

		when:
		jtragGraphBuilder.addTask2ResourceDependency(customTaskId1, customResourceId1)
		jtragGraphBuilder.addResource2TaskAssignment(customResourceId1, customTaskId2)
		jtragGraphBuilder.addTask2ResourceDependency(customTaskId2, customResourceId2)
		jtragGraphBuilder.addResource2TaskAssignment(customResourceId2, customTaskId3)
		jtragGraphBuilder.addTask2ResourceDependency(customTaskId3, customResourceId3)
		jtragGraphBuilder.addResource2TaskAssignment(customResourceId3, customTaskId1)
		Graph<Object> graph = jtragGraphBuilder.build()

		then:
		graph.getTasks().size() == 6
		graph.getTasks().collect { it.id }.toSet() ==
				[customTaskId1, customTaskId2, customTaskId3, customResourceId1, customResourceId2, customResourceId3] as Set

		when:
		def jtwfgDeadlockDetector = new DeadlockDetector<Object>()
		def analysisResult = jtwfgDeadlockDetector.analyze(graph)

		then:
		analysisResult.hasDeadlock() == true
		analysisResult.deadlockCycles.size() == 1
		analysisResult.deadlockCycles[0].toString() == 'DeadlockCycle: CustomTaskId-t1 -> CustomResourceId-r1 -> CustomTaskId-t2 -> CustomResourceId-r2 -> CustomTaskId-t3 -> CustomResourceId-r3 -> CustomTaskId-t1'
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

		String toString() {
			"${CustomTaskId.class.simpleName}-${internalId}"
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

		String toString() {
			"${CustomResourceId.class.simpleName}-${internalId}"
		}
	}
}

