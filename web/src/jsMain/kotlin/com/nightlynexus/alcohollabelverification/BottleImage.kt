package com.nightlynexus.alcohollabelverification

import kotlinx.coroutines.await
import org.w3c.files.File

internal suspend fun readBottleImageText(file: File): String {
  return Tesseract.recognize(file, "eng").await().data.text
}

internal fun String.containsBrandName(brandName: String): Boolean {
  return contains(brandName, ignoreCase = true)
}

internal fun String.searchForDesignation(): LabelInformation.Designation? {
  if (containsDesignationText("wine")) {
    return LabelInformation.Designation.Wine
  }

  if (containsDesignationText("whisky")) {
    // TODO: Is whisky a distilled spirit or a malt beverage?
    return LabelInformation.Designation.DistilledSpirits
  }
  if (containsDesignationText("vodka")) {
    return LabelInformation.Designation.DistilledSpirits
  }
  if (containsDesignationText("rum")) {
    return LabelInformation.Designation.DistilledSpirits
  }
  if (containsDesignationText("tequila")) {
    return LabelInformation.Designation.DistilledSpirits
  }
  if (containsDesignationText("gin")) {
    return LabelInformation.Designation.DistilledSpirits
  }
  if (containsDesignationText("brandy")) {
    return LabelInformation.Designation.DistilledSpirits
  }
  if (containsDesignationText("liqueur")) {
    return LabelInformation.Designation.DistilledSpirits
  }

  if (containsDesignationText("ale")) {
    return LabelInformation.Designation.MaltBeverages
  }
  if (containsDesignationText("beer")) {
    return LabelInformation.Designation.MaltBeverages
  }
  if (containsDesignationText("malt")) {
    return LabelInformation.Designation.MaltBeverages
  }
  if (containsDesignationText("seltzer")) {
    return LabelInformation.Designation.MaltBeverages
  }

  return null
}

// "wine" is not in "Barleywine." "ale" is not in "Alen."
private fun String.containsDesignationText(designationText: String): Boolean {
  return containsWord(designationText, ignoreCase = true)
}

internal fun String.searchForAlcoholContent(): String? {
  var index = 0
  while (true) {
    val percentIndex = indexOf('%', index)
    if (percentIndex == -1) {
      return null
    }
    var digitStartIndex = percentIndex
    while (true) {
      if (digitStartIndex == 0) {
        break
      }
      val nextDigitStartIndex = digitStartIndex - 1
      if (!this[nextDigitStartIndex].isDigit()) {
        break
      }
      digitStartIndex = nextDigitStartIndex
    }
    if (digitStartIndex != percentIndex) {
      // We found a number.
      return substring(digitStartIndex, percentIndex + 1)
    }
    index = percentIndex + 1
  }
}

internal fun String.containsNetContents(netContents: String): Boolean {
  // TODO: Leniency for abbreviations like pt/pint, qt/quart, gal/gallon, etc.?
  return containsLenient(netContents)
}

internal fun String.containsNameAndAddressOfBottler(nameAndAddressOfBottler: String): Boolean {
  return containsLenient(nameAndAddressOfBottler)
}

internal fun String.containsCountryOfOrigin(countryOfOrigin: String): Boolean {
  return containsLenient(countryOfOrigin)
}

internal fun String.containsGovernmentHealthWarningStatement(
  governmentHealthWarningStatement: String
): Boolean {
  return containsLenient(governmentHealthWarningStatement)
}

private fun String.containsLenient(other: String): Boolean =
  filter {
    it.isLetterOrDigit() || it == '(' || it == ')'
  }
    .contains(
      other.filter {
        it.isLetterOrDigit() || it == '(' || it == ')'
      },
      ignoreCase = true
    )

private fun String.containsWord(word: String, ignoreCase: Boolean): Boolean {
  var startIndex = 0
  while (true) {
    val start = indexOf(word, startIndex, ignoreCase)
    if (start == -1) {
      return false
    }
    val end = start + word.length
    val startsWithWhitespace = start == 0 || !get(start - 1).isLetterOrDigit()
    if (startsWithWhitespace) {
      val endsWithWhitespace = end == length || !get(end).isLetterOrDigit()
      if (endsWithWhitespace) {
        return true
      }
    }
    startIndex = end + 1
  }
}
