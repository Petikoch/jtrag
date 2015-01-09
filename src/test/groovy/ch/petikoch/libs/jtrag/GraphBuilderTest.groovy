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

import spock.lang.Specification

/**
 * This is a "london style" unit test to test the interaction with
 * the underlying jtwfg {@link ch.petikoch.libs.jtwfg.GraphBuilder}.
 */
@SuppressWarnings("GroovyPointlessBoolean")
class GraphBuilderTest extends Specification {

	def jtwfgGraphBuilderMock = Mock(ch.petikoch.libs.jtwfg.GraphBuilder)
	GraphBuilder testee = new GraphBuilder(jtwfgGraphBuilderMock)

	def 'addTask: is delegated'() {
		given:
		def taskId = 't1'

		when:
		def result = testee.addTask(taskId)

		then:
		result.is(testee)
		1 * jtwfgGraphBuilderMock.addTask(taskId)
		0 * _._
	}

	def 'addTasks: is delegated'() {
		given:
		def taskIds = ['t1', 't2']

		when:
		def result = testee.addTasks(taskIds)

		then:
		result.is(testee)
		1 * jtwfgGraphBuilderMock.addTasks(taskIds)
		0 * _._
	}

	def 'hasTask: is delegated'() {
		given:
		def taskId = 't1'

		when:
		def result = testee.hasTask(taskId)

		then:
		result == false
		1 * jtwfgGraphBuilderMock.hasTask(taskId)
		0 * _._
	}

	def 'removeTask: is delegated'() {
		given:
		def taskId = 't1'

		when:
		def result = testee.removeTask(taskId)

		then:
		result.is(testee)
		1 * jtwfgGraphBuilderMock.removeTask(taskId)
		0 * _._
	}

	def 'removeTasks: is delegated'() {
		given:
		def taskIds = ['t1', 't2']

		when:
		def result = testee.removeTasks(taskIds)

		then:
		result.is(testee)
		1 * jtwfgGraphBuilderMock.removeTasks(taskIds)
		0 * _._
	}

	def 'addResource: is delegated'() {
		given:
		def resourceId = 'r1'

		when:
		def result = testee.addResource(resourceId)

		then:
		result.is(testee)
		1 * jtwfgGraphBuilderMock.addTask(resourceId)
		0 * _._
	}

	def 'addResources: is delegated'() {
		given:
		def resourceIds = ['r1', 'r2']

		when:
		def result = testee.addResources(resourceIds)

		then:
		result.is(testee)
		1 * jtwfgGraphBuilderMock.addTasks(resourceIds)
		0 * _._
	}

	def 'hasResource: is delegated'() {
		given:
		def resourceId = 'r1'

		when:
		def result = testee.hasResource(resourceId)

		then:
		result == false
		1 * jtwfgGraphBuilderMock.hasTask(resourceId)
		0 * _._
	}

	def 'removeResource: is delegated'() {
		given:
		def resourceId = 'r1'

		when:
		def result = testee.removeResource(resourceId)

		then:
		result.is(testee)
		1 * jtwfgGraphBuilderMock.removeTask(resourceId)
		0 * _._
	}

	def 'removeResources: is delegated'() {
		given:
		def resourceIds = ['r1', 'r2']

		when:
		def result = testee.removeResources(resourceIds)

		then:
		result.is(testee)
		1 * jtwfgGraphBuilderMock.removeTasks(resourceIds)
		0 * _._
	}

	def 'addTask2ResourceDependency: is delegated'() {
		given:
		def taskId = 't1'
		def resourceId = 'r1'

		when:
		def result = testee.addTask2ResourceDependency(taskId, resourceId)

		then:
		result.is(testee)
		1 * jtwfgGraphBuilderMock.addTaskWaitsFor(taskId, resourceId)
		0 * _._
	}

	def 'removeTask2ResourceDependency: is delegated'() {
		given:
		def taskId = 't1'
		def resourceId = 'r1'

		when:
		def result = testee.removeTask2ResourceDependency(taskId, resourceId)

		then:
		result.is(testee)
		1 * jtwfgGraphBuilderMock.removeTaskWaitForDependency(taskId, resourceId)
		0 * _._
	}

	def 'addResource2TaskAssignment: is delegated'() {
		given:
		def resourceId = 'r1'
		def taskId = 't1'

		when:
		def result = testee.addResource2TaskAssignment(resourceId, taskId)

		then:
		result.is(testee)
		1 * jtwfgGraphBuilderMock.addTaskWaitsFor(resourceId, taskId)
		0 * _._
	}

	def 'removeResource2TaskAssignment: is delegated'() {
		given:
		def resourceId = 'r1'
		def taskId = 't1'

		when:
		def result = testee.removeResource2TaskAssignment(resourceId, taskId)

		then:
		result.is(testee)
		1 * jtwfgGraphBuilderMock.removeTaskWaitForDependency(resourceId, taskId)
		0 * _._
	}

	def 'build: is delegated'() {
		when:
		testee.build()

		then:
		1 * jtwfgGraphBuilderMock.build()
		0 * _._
	}
}
