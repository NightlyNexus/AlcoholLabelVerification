package com.nightlynexus.alcohollabelverification

import kotlin.js.Promise
import org.khronos.webgl.Uint8Array

// https://mozilla.github.io/pdf.js/api/

@JsModule("pdfjs-dist/build/pdf.worker.min.mjs")
@JsNonModule
external val pdfWorker: dynamic

@JsModule("pdfjs-dist")
@JsNonModule
internal external object PdfjsLib {
  /**
   * Global worker configuration. Must be set before any getDocument() call.
   * Use a CDN URL to avoid bundling the worker separately.
   */
  val GlobalWorkerOptions: GlobalWorkerOptions

  /**
   * Load a PDF document from a typed array, URL, or object with data/url.
   *
   * @param source  Uint8Array of raw PDF bytes, a URL string, or a DocumentInitParameters object.
   * @return A PDFDocumentLoadingTask whose promise resolves to a PDFDocumentProxy.
   */
  fun getDocument(source: dynamic): PDFDocumentLoadingTask
}

internal external interface DocumentInitParameters {
  val data: Uint8Array
}

internal fun DocumentInitParameters(uInt8Array: Uint8Array): DocumentInitParameters {
  val params = js("{}")
  params.data = uInt8Array
  return params.unsafeCast<DocumentInitParameters>()
}

internal external interface GlobalWorkerOptions {
  /** URL or blob URL of the PDF.js worker script. */
  var workerSrc: String
}

internal external interface PDFDocumentLoadingTask {
  /** Promise that resolves to the loaded PDFDocumentProxy. */
  val promise: Promise<PDFDocumentProxy>

  /** Cancel an in-progress load. */
  fun destroy(): Promise<Unit>
}

internal external interface PDFDocumentProxy {
  /** Total number of pages. */
  val numPages: Int

  /**
   * Get a page by 1-based index.
   */
  fun getPage(pageNumber: Int): Promise<PDFPageProxy>

  /**
   * Get the document's AcroForm field data.
   * Returns an array of field objects, or null if no form data.
   */
  fun getFieldObjects(): Promise<dynamic>

  /**
   * Get metadata including Info dictionary (title, author, etc.).
   */
  fun getMetadata(): Promise<PDFMetadata>

  /**
   * Get the document outline (bookmarks).
   */
  fun getOutline(): Promise<Array<dynamic>>

  /**
   * Release resources held by this document proxy.
   */
  fun cleanup(): Promise<Unit>
}

internal external interface PDFPageProxy {
  /** 1-based page number. */
  val pageNumber: Int

  /** Page dimensions in user units (pts). */
  val view: Array<Double>  // [x, y, width, height]

  /**
   * Extract the text content of this page.
   * @param params  Optional normalizeWhitespace / includeMarkedContent flags.
   */
  fun getTextContent(params: dynamic = definedExternally): Promise<PDFTextContent>

  /**
   * Get the viewport for rendering at a given scale.
   */
  fun getViewport(params: dynamic): PDFPageViewport

  /**
   * Render the page into a canvas context.
   * @param params  RenderParameters including canvasContext and viewport.
   * @return A RenderTask whose promise resolves when rendering is complete.
   */
  fun render(params: dynamic): PDFRenderTask

  /** Get annotations (includes AcroForm widget annotations). */
  fun getAnnotations(params: dynamic = definedExternally): Promise<Array<dynamic>>
}

internal external interface PDFTextContent {
  val items: Array<PDFTextItem>
  val styles: dynamic
}

internal external interface PDFTextItem {
  val str: String
  val dir: String
  val transform: Array<Double>
  val width: Double
  val height: Double
  val fontName: String
  val hasEOL: Boolean
}

internal external interface PDFPageViewport {
  val width: Double
  val height: Double
  val scale: Double
  val transform: Array<Double>
}

internal external interface PDFRenderTask {
  val promise: Promise<Unit>
  fun cancel(): Unit
}

internal external interface PDFMetadata {
  val info: dynamic
  val metadata: dynamic
}
