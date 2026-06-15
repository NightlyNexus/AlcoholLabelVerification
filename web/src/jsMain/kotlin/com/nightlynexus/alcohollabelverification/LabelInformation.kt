package com.nightlynexus.alcohollabelverification

internal data class LabelInformation(
  val brandName: String,
  val designation: Designation,
  val alcoholContent: String?, // Some beer and wine can omit the alcohol content percentage.
  val netContents: String,
  val nameAndAddressOfBottler: String,
  val countryOfOrigin: String?, // null if and only if domestic.
  val governmentHealthWarningStatement: String
) {
  enum class Designation {
    Wine,
    DistilledSpirits,
    MaltBeverages
  }
}
