package com.nightlynexus.alcohollabelverification

import kotlinx.browser.document
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.w3c.dom.Document
import org.w3c.dom.HTMLInputElement
import org.w3c.files.File

fun main() {
  WebApp(document)
}

@JsName("removePdfFile")
external fun removePdfFileFromUi()

private class WebApp(
  document: Document
) {
  private val inputImage = document.getElementById("input-image") as HTMLInputElement
  private val inputPdf = document.getElementById("input-pdf") as HTMLInputElement
  private val removeImage = document.getElementById("remove-image")!!
  private val removePdf = document.getElementById("remove-pdf")!!
  private val resultMessageHeader = document.getElementById("result-message-header")!!
  private val resultMessage = document.getElementById("result-message")!!
  private val readScope = CoroutineScope(Dispatchers.Default)

  private var bottleText: String? = null
  private var ttbForm: LabelInformation? = null
  private var readImageJob: Job? = null
  private var readPdfJob: Job? = null

  init {
    inputImage.onchange = {
      val file = inputImage.files?.item(0)
      if (file != null) {
        processBottleImageFile(file)
      }
      null
    }
    inputPdf.onchange = {
      val file = inputPdf.files?.item(0)
      if (file != null) {
        processTtbFormPdfFile(file)
      }
      null
    }
    removeImage.addEventListener("click") {
      resetImage()
    }
    removePdf.addEventListener("click") {
      resetPdf()
    }
  }

  private fun processBottleImageFile(imageFile: File) {
    resetImage()
    readImageJob = readScope.launch {
      val bottleText = readBottleImageText(imageFile)
      withContext(Dispatchers.Main) {
        this@WebApp.bottleText = bottleText
        val ttbForm = this@WebApp.ttbForm
        if (ttbForm != null) {
          compareAndPostResultMessage(
            bottleText,
            ttbForm
          )
        }
      }
    }
  }

  private fun processTtbFormPdfFile(pdfFile: File) {
    resetPdf()
    readPdfJob = readScope.launch {
      val ttbForm = readTtbFormLabelInformation(pdfFile)
      withContext(Dispatchers.Main) {
        if (ttbForm == null) {
          removePdfFileFromUi()
          postInvalidFormResultMessage()
        } else {
          this@WebApp.ttbForm = ttbForm
          val bottleText = this@WebApp.bottleText
          if (bottleText != null) {
            compareAndPostResultMessage(
              bottleText,
              ttbForm
            )
          }
        }
      }
    }
  }

  private fun resetImage() {
    readImageJob?.cancel()
    bottleText = null
    clearResultMessage()
  }

  private fun resetPdf() {
    readPdfJob?.cancel()
    ttbForm = null
    clearResultMessage()
  }

  private fun clearResultMessage() {
    resultMessageHeader.innerHTML = ""
    resultMessage.innerHTML = ""
  }

  private fun postInvalidFormResultMessage() {
    resultMessageHeader.innerHTML = "Invalid PDF form. Check that you have a completed 5100.31."
    resultMessage.innerHTML = ""
  }

  private fun compareAndPostResultMessage(
    bottleText: String,
    ttbForm: LabelInformation
  ) {
    val containsBrandName = bottleText.containsBrandName(
      ttbForm.brandName
    )
    val bottleTextDesignation = bottleText.searchForDesignation()
    val bottleTextAlcoholContent = bottleText.searchForAlcoholContent()
    val containsNetContents = bottleText.containsNetContents(
      ttbForm.netContents
    )
    val containsNameAndAddressOfBottler = bottleText.containsNameAndAddressOfBottler(
      ttbForm.nameAndAddressOfBottler
    )
    val containsCountryOfOrigin = if (ttbForm.countryOfOrigin == null) {
      // Domestic.
      true
    } else {
      bottleText.containsCountryOfOrigin(
        ttbForm.countryOfOrigin
      )
    }
    val containsGovernmentHealthWarningStatement =
      bottleText.containsGovernmentHealthWarningStatement(
        ttbForm.governmentHealthWarningStatement
      )

    val pass = containsBrandName
      && bottleTextDesignation === ttbForm.designation
      && bottleTextAlcoholContent == ttbForm.alcoholContent
      && containsNetContents
      && containsNameAndAddressOfBottler
      && containsCountryOfOrigin
      && containsGovernmentHealthWarningStatement

    if (pass) {
      resultMessageHeader.innerHTML =
        """<img src="pass.png" alt="PASS" style="height: 1.5em; width: auto; vertical-align: middle;"> PASS<br><br>"""
    } else {
      resultMessageHeader.innerHTML =
        """<img src="fail.png" alt="FAIL" style="height: 1.2em; width: auto; vertical-align: middle;"> FAIL<br><br>"""
    }

    val message = StringBuilder()
    if (containsBrandName) {
      message.append(
        "✅ Found brand name: ${ttbForm.brandName}"
      )
    } else {
      message.append(
        "❌ Could not find brand name: ${ttbForm.brandName}"
      )
    }
    if (bottleTextDesignation == null) {
      message.appendNextMessage(
        "❌ Could not find designation"
      )
    } else if (bottleTextDesignation === ttbForm.designation) {
      message.appendNextMessage(
        "✅ Found designation: ${bottleTextDesignation.htmlText()}"
      )
    } else {
      message.appendNextMessage(
        "❌ Designation did not match. Found ${bottleTextDesignation.htmlText()} but expected " +
          ttbForm.designation.htmlText()
      )
    }
    if (bottleTextAlcoholContent == ttbForm.alcoholContent) {
      // Do not show any alcohol content message when both are null (both are exempt).
      if (bottleTextAlcoholContent != null) {
        message.appendNextMessage(
          "✅ Found alcohol content: $bottleTextAlcoholContent"
        )
      }
    } else if (bottleTextAlcoholContent == null) {
      message.appendNextMessage(
        "❌ Could not find alcohol content. Expected ${ttbForm.alcoholContent}"
      )
    } else if (ttbForm.alcoholContent == null) {
      message.appendNextMessage(
        "❌ Alcohol content did not match. Found $bottleTextAlcoholContent but expected no " +
          "percentage because the formula field on the form was empty, indicating exemption."
      )
    } else {
      message.appendNextMessage(
        "❌ Alcohol content did not match. Found $bottleTextAlcoholContent but expected " +
          ttbForm.alcoholContent
      )
    }
    if (containsNetContents) {
      message.appendNextMessage(
        "✅ Found net contents: ${ttbForm.netContents}"
      )
    } else {
      message.appendNextMessage(
        "❌ Could not find net contents: ${ttbForm.netContents}"
      )
    }
    if (containsNameAndAddressOfBottler) {
      message.appendNextMessage(
        "✅ Found name and address of bottler: ${ttbForm.nameAndAddressOfBottler}"
      )
    } else {
      message.appendNextMessage(
        "❌ Could not find name and address of bottler: ${ttbForm.nameAndAddressOfBottler}"
      )
    }
    // Do not show any country of origin message if the form stated that the product is domestic.
    if (ttbForm.countryOfOrigin != null) {
      // Imported.
      if (containsCountryOfOrigin) {
        message.appendNextMessage(
          "✅ Found country of origin: ${ttbForm.countryOfOrigin}"
        )
      } else {
        message.appendNextMessage(
          "❌ Could not find country of origin: ${ttbForm.countryOfOrigin}"
        )
      }
    }
    if (containsGovernmentHealthWarningStatement) {
      message.appendNextMessage(
        "✅ Found government health warning statement: " +
          ttbForm.governmentHealthWarningStatement
      )
    } else {
      message.appendNextMessage(
        "❌ Could not find government health warning statement: " +
          ttbForm.governmentHealthWarningStatement
      )
    }
    message.appendNextMessage(
      "📜 Here is what AI read from the label image:<br>$bottleText"
    )
    resultMessage.innerHTML = message.toString()
  }

  private fun LabelInformation.Designation.htmlText(): String {
    return when (this) {
      LabelInformation.Designation.Wine -> "Wine"
      LabelInformation.Designation.DistilledSpirits -> "Distilled Spirits"
      LabelInformation.Designation.MaltBeverages -> "Malt Beverages"
    }
  }

  private fun StringBuilder.appendNextMessage(message: String) {
    append("<br><br>")
    append(message)
  }
}
