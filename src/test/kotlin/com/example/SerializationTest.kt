package com.example

import com.example.model.FakeTaskRepository
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*

class SerializationTest : BehaviorSpec({
    given("a TaskRepository with some tasks") {
        val repository = FakeTaskRepository()

        `when`("requesting all tasks") {
            then("it should return all tasks") {
                testApplication {
                    application { module() }
                    client.get("/tasks").apply {
                        status shouldBe HttpStatusCode.OK
                        bodyAsText() shouldBe """[{"name":"cleaning","description":"Clean the house","priority":"Low"},{"name":"gardening","description":"Mow the lawn","priority":"Medium"},{"name":"shopping","description":"Buy the groceries","priority":"High"},{"name":"painting","description":"Paint the fence","priority":"Medium"}]"""
                    }
                }
            }
        }

        `when`("requesting a task by name") {
            then("it should return the task if it exists") {
                testApplication {
                    application { module() }
                    client.get("/tasks/byName/cleaning").apply {
                        status shouldBe HttpStatusCode.OK
                        bodyAsText() shouldBe """{"name":"cleaning","description":"Clean the house","priority":"Low"}"""
                    }
                }
            }

            then("it should return 404 if the task does not exist") {
                testApplication {
                    application { module() }
                    client.get("/tasks/byName/nonexistent").apply {
                        status shouldBe HttpStatusCode.NotFound
                    }
                }
            }
        }

        `when`("requesting tasks by priority") {
            then("it should return tasks with the specified priority") {
                testApplication {
                    application { module() }
                    client.get("/tasks/byPriority/Medium").apply {
                        status shouldBe HttpStatusCode.OK
                        bodyAsText() shouldBe """[{"name":"gardening","description":"Mow the lawn","priority":"Medium"},{"name":"painting","description":"Paint the fence","priority":"Medium"}]"""
                    }
                }
            }

            then("it should return 404 if no tasks have the specified priority") {
                testApplication {
                    application { module() }
                    client.get("/tasks/byPriority/Low").apply {
                        status shouldBe HttpStatusCode.OK
                        bodyAsText() shouldBe """[{"name":"cleaning","description":"Clean the house","priority":"Low"}]"""
                    }
                }
            }
        }

        `when`("adding a new task") {
            then("it should add the task successfully") {
                testApplication {
                    application { module() }
                    client.post("/tasks") {
                        contentType(ContentType.Application.Json)
                        setBody("""{"name":"newtask","description":"New task description","priority":"High"}""")
                    }.apply {
                        status shouldBe HttpStatusCode.NoContent
                    }
                }
            }

            then("it should return 400 if the task name already exists") {
                testApplication {
                    application { module() }
                    client.post("/tasks") {
                        contentType(ContentType.Application.Json)
                        setBody("""{"name":"cleaning","description":"Duplicate task","priority":"Low"}""")
                    }.apply {
                        status shouldBe HttpStatusCode.BadRequest
                    }
                }
            }
        }

        `when`("removing a task by name") {
            then("it should remove the task if it exists") {
                testApplication {
                    application { module() }
                    client.delete("/tasks/cleaning").apply {
                        status shouldBe HttpStatusCode.NoContent
                    }
                }
            }

            then("it should return 404 if the task does not exist") {
                testApplication {
                    application { module() }
                    client.delete("/tasks/nonexistent").apply {
                        status shouldBe HttpStatusCode.NotFound
                    }
                }
            }
        }
    }
})