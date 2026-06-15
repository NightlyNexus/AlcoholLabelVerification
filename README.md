TTB Label Verification
=====================

This is a proof-of-concept webpage for comparing text extracted from an image to text filled into the [TTB F 5100.31 PDF form](https://www.ttb.gov/system/files/images/pdfs/forms/f510031.pdf). See the [example image](examples/example.png) and the [example completed form](examples/example-f510031.pdf).

Use the web app
--------

<https://nightlynexus.github.io/AlcoholLabelVerification/>

Build from source
--------

- Ensure you have `java` on your path.
- Execute `./gradlew :web:jsBrowserDevelopmentRun` within this project’s directory.
- The first build may take a couple minutes.
- Open [localhost:8080](http://localhost:8080/).

Limitations
--------

- This webpage only uses local JavaScript in the browser on the device. The text extraction uses [Tesseract.js](https://github.com/naptha/tesseract.js/) due to reported restrictions on cloud APIs. A future version could use external services or an internal machine on our network with a good GPU running better models for much beter text extraction results.

- This webpage has four requirements of the completed PDF form:
  - Field "6" must have the brand name.
  - Field "8" must have the name and address of the bottler and, if imported, must end with a comma followed by the country of origin.
  - Field "9" must have the percentage of alcohol (e.g. "10%") or be empty if exempt.
  - Field "15" must have the net contents (e.g. "1 PINT").

- Batch uploading is not yet implemented, but a future version could accept multiple images and multiple PDFs with corresponding file names and compare them with minimal changes.

- In a production version, we are likely retrieving these images and form fields from an internal service/database, rather than from a user’s local machine.

- A production version should have a loading state while processing the image and PDF. This is not a high priority now because Tesseract is fast, but a better text extraction model will take a couple seconds.

- With a more accurate text extraction model, we can provide more granular text comparison failure explanations with Google’s [diff-match-patch](https://github.com/google/diff-match-patch).

Tech
--------

- [Tesseract.js](https://github.com/naptha/tesseract.js/)
- [Mozilla’s pdfjs](https://mozilla.github.io/pdf.js/api/)
- Kotlin multiplatform and Kotlin coroutines. The Kotlin source compiles to JavaScript.
- Thanks to Claude for generating much of the HTML, CSS, and UI-manipulating JavaScript source in index.html.

Reading the source
--------

- [Main.kt](web/src/jsMain/kotlin/com/nightlynexus/alcohollabelverification/Main.kt) is the entry point to the main logic script.
- [LabelInformation.kt](web/src/jsMain/kotlin/com/nightlynexus/alcohollabelverification/LabelInformation.kt) holds the data to compare.
- [PdfjsExternals.kt](web/src/jsMain/kotlin/com/nightlynexus/alcohollabelverification/PdfjsExternals.kt) adapts pdfjs APIs to Kotlin APIs.
- [TesseractExternals.kt](web/src/jsMain/kotlin/com/nightlynexus/alcohollabelverification/TesseractExternals.kt) adapts Tesseract.js APIs to Kotlin APIs.
- [PdfService.kt](web/src/jsMain/kotlin/com/nightlynexus/alcohollabelverification/PdfService.kt) reads fields from a PDF file (using pdfjs).
- [TtbForm.kt](web/src/jsMain/kotlin/com/nightlynexus/alcohollabelverification/TtbForm.kt) reads the TTB form data from a PDF file (using PdfService).
- [BottleImage.kt](web/src/jsMain/kotlin/com/nightlynexus/alcohollabelverification/BottleImage.kt) reads text from an image (using Tesseract.js).
- [index.html](web/src/jsMain/resources/index.html) contains the most of the UI, including HTML, CSS, and the UI-manipulating JavaScript source.
