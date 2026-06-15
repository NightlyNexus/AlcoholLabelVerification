afterEvaluate {
  configurations.configureEach {
    resolutionStrategy.eachDependency {
      val requested = requested
      if (requested.version?.endsWith("+") == true) {
        throw GradleException(
          "Wildcard dependency forbidden: ${requested.group}:${requested.name}:${requested.version}"
        )
      }
    }
  }
}
