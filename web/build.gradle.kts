plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.kotlinx.jsPlainObjects)
}

group = "com.nightlynexus.alcohollabelverification"
version = "1.0-SNAPSHOT"

kotlin {
  js {
    browser()
    useEsModules()
    binaries.executable()

    compilerOptions {
      optIn.add("kotlin.js.ExperimentalJsExport")
    }
  }

  sourceSets {
    commonTest.dependencies {
      implementation(libs.kotlin.test)
    }
    jsMain.dependencies {
      implementation(libs.kotlinx.io)
      implementation(libs.kotlinx.coroutines)
      implementation(libs.kotlinx.browser)
      implementation(npm("pdfjs-dist", "6.0.227"))
      implementation(npm("tesseract.js", "5.1.1"))
    }
  }
}
