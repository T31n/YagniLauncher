# Yagni Launcher Architecture

This document describes how Yagni Launcher's Gradle modules are organized and how they map onto Clean Architecture. It focuses on stable module responsibilities and dependency rules rather than an inventory of classes, which changes far more often than the architecture itself.

This module structure follows [The Clean Architecture](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)

![Clean Architecture](yagni-clean-architecture-onion.svg)

---

## Table of Contents

- [Clean Architecture Layers](#clean-architecture-layers)
- [Dependency Rule](#dependency-rule)
- [Module Groups](#module-groups)
    - [Domain](#domain)
    - [Data](#data)
    - [Framework](#framework)
    - [Presentation](#presentation)
    - [Android Entry Points](#android-entry-points)
    - [Shared Infrastructure](#shared-infrastructure)
- [design-system, ui, and feature:* Boundaries](#design-system-ui-and-feature-boundaries)
- [Further Reading](#further-reading)

---

## Clean Architecture Layers

The codebase is split into five layers:

1. **Domain** — Pure Kotlin entities, repository/framework interfaces, use cases, and grid algorithms. No Android SDK imports.
2. **Data** — Persistence implementations: Room database, Proto DataStore preferences, and the repositories that combine them.
3. **Framework** — Modules that own integrations with Android system APIs (`PackageManager`, `LauncherApps`, `WallpaperManager`, `AppWidgetManager`, etc.). They implement Domain abstractions where the Domain needs those capabilities.
4. **Presentation** — Compose UI and ViewModels: the `feature:*` screens plus the `ui` and `design-system` component libraries.
5. **Android Entry Points** — The outermost layer: `app` and `service`, the two modules Android itself talks to (the `Application`/Activity graph and background `Service` components). They wire every other module together and are the only modules allowed to depend on the whole graph at once.

## Dependency Rule

Dependencies point **inward**: Android Entry Points compose the graph; Presentation depends on Domain and the framework capabilities it uses; Data and Framework implement contracts from Domain. Domain does not depend on Android, Data, Framework, Presentation, or the entry points.

## Module Groups

### Domain

Pure Kotlin modules with no Android dependency, so their logic is fully unit-testable in isolation:

| Module              | Responsibility                                                                                           |
|---------------------|----------------------------------------------------------------------------------------------------------|
| `domain:model`      | Entity and value-object definitions shared by every other layer.                                         |
| `domain:repository` | Repository interfaces describing the data operations the domain needs; implemented by `data:repository`. |
| `domain:framework`  | Interfaces abstracting Android system services; implemented by the `framework:*` modules.                |
| `domain:use-case`   | Application business logic, composed from repositories and framework interfaces.                         |
| `domain:grid`       | Grid layout and collision-resolution algorithms used when moving or resizing items.                      |
| `domain:common`     | Cross-cutting abstractions such as coroutine dispatcher qualifiers.                                      |

### Data

Concrete persistence implementations behind the `domain:repository` interfaces:

| Module                 | Responsibility                                                                          |
|------------------------|-----------------------------------------------------------------------------------------|
| `data:repository`      | Repository implementations that combine `data:room` and `data:datastore` sources.       |
| `data:room`            | The local SQLite database (grid items, installed apps, widgets, shortcuts, icon packs). |
| `data:datastore`       | User settings persistence via Proto DataStore.                                          |
| `data:datastore-proto` | The `.proto` schema definitions consumed by `data:datastore`.                           |

### Framework

Each `framework:*` module owns its Android integration. Current modules are `framework:accessibility-manager`, `framework:content-resolver`, `framework:icon-pack-manager`, `framework:jaro-winkler-similarity`, `framework:launcher-apps`, `framework:locale`, `framework:notification-manager`, `framework:package-manager`, `framework:resources`, `framework:settings`, `framework:statusbar-manager`, `framework:user-manager`, `framework:wallpaper-manager`, and `framework:widget-manager`.

When the Domain needs a capability, its abstraction is declared in `domain:framework` and the owning framework module binds its implementation with Hilt. Framework modules generally call Android APIs directly; `AndroidUserManagerWrapper` is declared in `framework:user-manager`, which also provides its Hilt implementation. `ui` exposes that module through an API dependency, while `feature:home` and `service` depend on it directly.

### Presentation

| Module                                                                                         | Responsibility                                            |
|------------------------------------------------------------------------------------------------|-----------------------------------------------------------|
| `feature:*` (`home`, `action`, `pin`, `edit-application-info`, `edit-grid-item`, `settings:*`) | Feature-specific screens, ViewModels, and UI state.       |
| `design-system`                                                                                | Generic, model-free Compose primitives.                   |
| `ui`                                                                                           | Shared, application-aware UI reused by multiple features. |

### Android Entry Points

The outermost layer. `app` and `service` are the only modules the Android OS talks to directly, so they're the two places allowed to reach across the whole module graph to wire things together:

| Module    | Responsibility                                                                                        |
|-----------|-------------------------------------------------------------------------------------------------------|
| `app`     | Wires every module together: Hilt setup, `Application` class, and the root Activity/navigation graph. |
| `service` | Background Android services (accessibility, notification listener, icon pack updates).                |

### Shared Infrastructure

| Module        | Responsibility                                                                                                                                      |
|---------------|-----------------------------------------------------------------------------------------------------------------------------------------------------|
| `common`      | Shared Android infrastructure: image serialization, file and icon-key utilities, and dispatcher bindings. |
| `build-logic` | Gradle convention plugins that standardize module build configuration.                                                                              |

---

## design-system, ui, and feature:* Boundaries

These three module groups all sit in the Presentation layer, but each has a distinct responsibility:

- **`design-system`** contains generic, reusable visual primitives — theming, icons, animations, dialogs, and other Compose building blocks that carry no knowledge of the launcher's domain models or features. It is model-free: it has no dependency on `domain:model` or any other domain module.
- **`ui`** contains shared, *application-aware* UI: composites built from `design-system` primitives that are reused across multiple features (dialogs like `SelectApplicationDialog`, settings composables, etc.). It depends on `design-system`, `domain:model`, and the framework modules needed by its shared UI, which it exposes as API dependencies.
- **`feature:*`** modules contain screens, ViewModels, and UI state specific to one feature. They depend on both `design-system` and `ui` (added automatically by the `feature` Gradle convention plugin), plus any `domain:*` or framework modules they use directly.

---

## Further Reading

Yagni Launcher uses its own Clean Architecture and module dependency rules, as described throughout this document. Now in Android only influenced selected tooling and Gradle module terminology — for example `build-logic`, `design-system`, and `ui` — and the link below does not mean that Yagni Launcher's architecture is based on that project.

- [The Clean Architecture](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
- [Now in Android modularization guide](https://github.com/android/nowinandroid/blob/main/docs/ModularizationLearningJourney.md)