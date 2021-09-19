---
layout: default
title: append
permalink: append
parent: Scenario DSL
---

It's often useful to decompose a scenario on several small sub-scenarios or predefine some common scenario logic to share across different scenarios. 

This can be done by using the `append` function and its overloads. The `append` function allows you to append a scenario to any part of other scenario. It also provides configuration parameters to define which strategy should we use to **merge context** or to **merge hooks**.

## Merge context

Another scenario can be appended to the state with or without providing custom context.

### Append with context
Append with context will create a new child context in the current state and place all top-level states of appended scenario in that context. You can think about this context as an additional state, meaning that all sub-scenario states' names will be resolved against the given context name and all sub-scenario top-level states can only be directly activated from the context root but not from the state they are merged to.

Also, it's possible to declare a sub-scenario with context as modal, it will work the same as for the states.

```kotlin
val AskForNameScenario = Scenario {
  // AskForNameScenario implementation
}

val MainScenario = Scenario {
  state("Start") {
    append(context = "AskForName", AskForNameScenario, modal = true)
    
    activators {
      intent("Hello")
    }

    action {
      val name = activator.getName() ?: context.result
      if (name == null) {
        reactions.go("/AskForName", callbackState = "/Start")
      } else {
        reactions.say("Hello, $name!")
      }
    }
  }
}
```

### Append without context

Append without context will simply append all top-level states of an appended scenario to the current state. In other words, all top-level states of an appended scenario will become children of the current state, meaning they are all will be directly accessible from the current state and their names will be resolved against the name of the current state.

```kotlin
val CartScenario = Scenario {
  // ...
}
val OrderScenario = Scenario {
  // ...
}

val ShopScenario = Scenario {
  state("Start") {
    append(CartScenario)
    append(OrderScenario)

    // ...
  }
}
```

## Hooks merge strategies

### Hooks propagation

When you append another scenario with a custom context, you have an option either to propagate hooks defined in the current scenario to the appended one or not.

When the parameter `propagateHooks` is set to `true`, then all hooks defined in the current scenario will be invoked in sub-scenario states as well. Otherwise, hooks defined in the current scenario will not be invoked in any sub-scenario states. The default value is `true`

```kotlin
val MainScenario = Scenario {
  handle<BeforeActionHook> {
    // Will be invoked anywhere except the states inside the /Helper context
  }

  append(context = "Helper", HelperScenario, propagateHooks = false)
}
```

> NOTE: Append without context will always propagate hooks.

### Hooks exposure

When you append some sub-scenario on a top-level of a scenario, you have an option either to expose hooks from the sub-scenario to the current scenario or not.

When the parameter `exposeHooks` is set to `true`, then all hooks from the sub-scenario will be added to the current scenario.

When the parameter `exposeHooks` is set to `false`, then the behavior depends on whether the sub-scenario is appended with a context or without. In the case of append with context, all sub-scenario hooks will be available only inside the sub-scenario context and nowhere else. In the case of append without context, all sub-scenario hooks will be fully ignored and will not be invoked neither in the sub-scenario states nor in other states in this scenario.

The default value is `true`.

```kotlin
val HelperScenario = Scenario {
  handle<BeforeActionHook> {
    // ...
  }
}

val MainScenario = Scenario {
  append(context = "/Helper", HelperScenario) // Helper's hooks will be exposed to the MainScenario
  append(context = "/Helper", HelperScenario, exposeHooks = false) // Helper's hooks will only be accessible inside the /Helper context
  append(HelperScenario) // Helper's hooks will be exposed to the MainScenario
  append(HelperScenario, exposeHooks = false) // Helper's hooks will be fully ignored

}
```

> NOTE: Append to the inner state will never expose hooks.