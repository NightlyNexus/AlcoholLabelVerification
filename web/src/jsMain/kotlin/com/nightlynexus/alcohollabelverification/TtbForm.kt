package com.nightlynexus.alcohollabelverification

import org.w3c.files.File

// https://www.ttb.gov/system/files/images/pdfs/forms/f510031.pdf

private const val brandNameFieldName = "6. BRAND NAME (Required)"
private const val designationFieldName = "Check Box22"
private const val designationValueWine = "Wine"
private const val designationValueDistilledSpirits = "Spirits"
private const val designationValueMaltBeverages = "Malt"
private const val alcoholContentFieldName = "9.  FORMULA"
private const val netContentsFieldName =
  "15.  SHOW ANY INFORMATION THAT IS BLOWN, BRANDED, OR EMBOSSED ON THE CONTAINER (e.g., net contents) ONLY IF IT DOES NOT APPEAR ON THE LABELS"
private const val nameAndAddressOfBottlerFieldName =
  "8. NAME AND ADDRESS OF APPLICANT AS SHOWN ON PLANT REGISTRY, BASIC"
private const val sourceOfProductFieldName = "Check Box34"
private const val sourceOfProductValueDomestic = "Domes"
private const val sourceOfProductValueImported = "Import"

// https://www.ttb.gov/regulated-commodities/beverage-alcohol/distilled-spirits/ds-labeling-home/ds-health-warning
private const val governmentHealthWarningStatement =
  "GOVERNMENT WARNING: (1) According to the Surgeon General, women should not drink alcoholic beverages during pregnancy because of the risk of birth defects. (2) Consumption of alcoholic beverages impairs your ability to drive a car or operate machinery, and may cause health problems."

internal suspend fun readTtbFormLabelInformation(file: File): LabelInformation? {
  val fields = PdfService.extractFromFile(file)
  var brandName: String? = null
  var designation: LabelInformation.Designation? = null
  var alcoholContent: String? = null
  var netContents: String? = null
  var nameAndAddressOfBottler: String? = null
  var sourceOfProduct = -1 // 0 for domestic and 1 for imported.
  for (i in fields.indices) {
    val field = fields[i]

    when (field.name) {
      brandNameFieldName -> {
        brandName = field.value
      }

      designationFieldName -> {
        val possibleDesignation = field.value?.toDesignationOrNull()
        if (possibleDesignation != null) {
          designation = possibleDesignation
        }
      }

      alcoholContentFieldName -> {
        val possibleAlcoholContent = field.value
        if (!possibleAlcoholContent.isNullOrEmpty()) {
          alcoholContent = field.value
        }
      }

      netContentsFieldName -> {
        netContents = field.value
      }

      nameAndAddressOfBottlerFieldName -> {
        nameAndAddressOfBottler = field.value
      }

      sourceOfProductFieldName -> {
        val possibleSourceOfProduct = field.value
        when (possibleSourceOfProduct) {
          sourceOfProductValueDomestic -> sourceOfProduct = 0
          sourceOfProductValueImported -> sourceOfProduct = 1
        }
      }
    }
  }
  if (brandName == null
    || designation == null
    || netContents == null
    || nameAndAddressOfBottler == null
    || sourceOfProduct == -1
  ) {
    return null
  }
  val countryOfOrigin = when (sourceOfProduct) {
    // Domestic.
    0 -> null
    // Imported.
    1 -> {
      nameAndAddressOfBottler.parseCountryOfOrigin() ?: return null
    }
    else -> throw AssertionError()
  }
  return LabelInformation(
    brandName,
    designation,
    alcoholContent,
    netContents,
    nameAndAddressOfBottler,
    countryOfOrigin,
    governmentHealthWarningStatement
  )
}

private fun String.toDesignationOrNull(): LabelInformation.Designation? {
  return when (this) {
    designationValueWine -> LabelInformation.Designation.Wine
    designationValueDistilledSpirits -> LabelInformation.Designation.DistilledSpirits
    designationValueMaltBeverages -> LabelInformation.Designation.MaltBeverages
    else -> null
  }
}

// TODO: Where should we get the country from the form?
private fun String.parseCountryOfOrigin(): String? {
  val lastCommaIndex = lastIndexOf(',')
  if (lastCommaIndex == -1) {
    return null
  }
  return substring(lastCommaIndex + 1).trim()
}
