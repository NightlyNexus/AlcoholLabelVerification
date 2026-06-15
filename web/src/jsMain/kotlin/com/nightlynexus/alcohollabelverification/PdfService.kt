package com.nightlynexus.alcohollabelverification

import kotlin.js.Promise
import kotlinx.coroutines.await
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Uint8Array
import org.w3c.files.File

internal data class AcroFormField(
  val name: String,
  val value: String?,
  val type: String?
)

internal object PdfService {
  init {
    /*PdfjsLib.GlobalWorkerOptions.workerSrc =
      "https://cdn.jsdelivr.net/npm/pdfjs-dist@6.0.227/build/pdf.worker.min.mjs"*/

    // Update this if we ever update pdfjs-dist.
    PdfjsLib.GlobalWorkerOptions.workerSrc = "pdf.worker.min.mjs"
  }

  suspend fun extractFromFile(file: File): List<AcroFormField> {
    val uint8Array = file.readUint8Array()
    val documentInitParameters = DocumentInitParameters(uint8Array)

    val loadingTask = PdfjsLib.getDocument(documentInitParameters)
    val pdfDoc = loadingTask.promise.await()

    val acroFields = extractAcroFormFields(pdfDoc)

    pdfDoc.cleanup().await()

    return acroFields
  }

  private suspend fun extractAcroFormFields(
    pdfDoc: PDFDocumentProxy
  ): List<AcroFormField> {
    val fields = mutableListOf<AcroFormField>()
    val seenIds = mutableSetOf<String>()

    for (pageNumber in 1..pdfDoc.numPages) {
      val page = pdfDoc.getPage(pageNumber).await()
      val annotations: Array<dynamic> = try {
        page.getAnnotations().await()
      } catch (e: Throwable) {
        continue
      }

      for (annotation in annotations) {
        // Only process widget annotations (form fields).
        val subtype = annotation["subtype"] as? String ?: continue
        if (subtype != "Widget") continue

        // Deduplicate by field id because multipage fields share the same id.
        val id = annotation["id"] as? String ?: continue
        if (!seenIds.add(id)) continue

        // TODO: When is fieldName not a String?
        val name: String = (annotation["fieldName"] as? String) ?: continue
        // TODO: When is fieldType null?
        val type: String? = annotation["fieldType"] as? String
        // TODO: When is fieldValue not a String? Maybe checkboxes and multi-select can be Arrays?
        val value = when (val rawValue = annotation["fieldValue"]) {
          is String -> rawValue
          is Boolean -> rawValue.toString()
          is Array<*> -> (rawValue as Array<*>).joinToString(", ")
          null, undefined -> null
          else -> null
        }

        fields += AcroFormField(name, value, type)
      }
    }

    return fields
  }

  private suspend fun File.readUint8Array(): Uint8Array {
    val buffer = asDynamic().arrayBuffer().unsafeCast<Promise<ArrayBuffer>>().await()
    return Uint8Array(buffer)
  }
}
