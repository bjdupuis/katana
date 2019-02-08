package org.rewedigital.katana

import org.amshove.kluent.shouldNotThrow
import org.amshove.kluent.shouldThrow
import org.rewedigital.katana.dsl.classic.bind
import org.rewedigital.katana.dsl.classic.factory
import org.rewedigital.katana.dsl.get
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

object OverrideTests : Spek(
    {
        describe("Modules with overrides") {

            it("should throw exception when dependencies are overridden in same createModule") {
                val fn = {
                    createModule {

                        bind<MyComponent> { factory { MyComponentA() } }

                        bind<MyComponent> { factory { MyComponentA() } }
                    }
                }

                fn shouldThrow OverrideException::class
            }

            it("should throw exception when dependencies are overridden in same createModule with identical names") {
                val fn = {
                    createModule {

                        bind<MyComponentA>("createComponent") { factory { MyComponentA() } }

                        bind<MyComponentB<String>>("createComponent") {
                            factory {
                                MyComponentB("Hello world")
                            }
                        }
                    }
                }

                fn shouldThrow OverrideException::class
            }

            it("should throw exception when dependencies are overridden in multiple modules") {
                val module1 = createModule {

                    bind<MyComponent> { factory { MyComponentA() } }
                }

                val module2 = createModule {

                    bind<MyComponent> { factory { MyComponentA() } }
                }

                val fn = { createComponent(module1, module2) }

                fn shouldThrow OverrideException::class
            }

            it("should throw exception for internal module binding overrides") {
                val fn = {
                    createModule {

                        bind<String>("internal", internal = true) { factory { "Hello world" } }

                        bind<String>("internal", internal = true) { factory { "Hello world" } }
                    }
                }

                fn shouldThrow OverrideException::class
            }

            it("should not throw exception for internal module binding overrides across module boundaries") {
                val module1 = createModule {

                    bind<String>("internal", internal = true) { factory { "Hello world" } }

                    bind<MyComponentA> { factory { MyComponentA() } }
                }

                val module2 = createModule {

                    bind<String>("internal", internal = true) { factory { "Hello world 2" } }

                    bind<MyComponentB<String>> { factory { MyComponentB(get("internal")) } }
                }

                val fn = {
                    createComponent(module1, module2)
                }

                fn shouldNotThrow OverrideException::class
            }
        }
    })
