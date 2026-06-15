package com.nightlynexus.alcohollabelverification

import kotlin.js.Promise
import org.w3c.files.File

// https://github.com/naptha/tesseract.js/blob/a1ca80d9e31c34512d0ded75ff8821ddcf3f2f91/docs/api.md

@JsModule("tesseract.js")
@JsNonModule
internal external object Tesseract {
  fun recognize(
    image: File,
    lang: String = definedExternally,
    options: dynamic = definedExternally
  ): Promise<OcrResult>
}

internal external interface OcrData {
  val text: String
}

internal external interface OcrResult {
  val data: OcrData
}
